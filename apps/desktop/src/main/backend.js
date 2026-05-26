const { randomUUID } = require("node:crypto");
const { spawn } = require("node:child_process");
const path = require("node:path");

const LOCAL_API_DIR = path.resolve(__dirname, "../../../services/local-api");
const READY_PATTERN = /LOCAL_API_READY port=(\d+)/i;

function resolveMavenCommand() {
  return process.platform === "win32" ? "mvn.cmd" : "mvn";
}

function startBackend() {
  const sessionToken = randomUUID();
  const mavenCommand = resolveMavenCommand();

  return new Promise((resolve, reject) => {
    let settled = false;

    const child = spawn(mavenCommand, ["spring-boot:run"], {
      cwd: LOCAL_API_DIR,
      env: {
        ...process.env,
        APP_SESSION_TOKEN: sessionToken
      },
      stdio: ["ignore", "pipe", "pipe"]
    });

    const settleResolve = (value) => {
      if (settled) {
        return;
      }

      settled = true;
      resolve(value);
    };

    const settleReject = (error) => {
      if (settled) {
        return;
      }

      settled = true;
      child.kill();
      reject(error);
    };

    child.once("error", (error) => {
      settleReject(new Error(`Failed to start local API process: ${error.message}`));
    });

    child.once("exit", (code) => {
      if (!settled) {
        settleReject(new Error(`Local API exited before startup completed with code ${code ?? "unknown"}`));
      }
    });

    child.stdout.on("data", (chunk) => {
      const text = chunk.toString();
      const match = text.match(READY_PATTERN);

      if (match) {
        settleResolve({
          child,
          port: match[1],
          sessionToken
        });
      }
    });

    child.stderr.on("data", (chunk) => {
      const text = chunk.toString();

      if (/APPLICATION FAILED TO START/i.test(text)) {
        settleReject(new Error("Spring Boot reported application startup failure"));
      }
    });
  });
}

module.exports = {
  startBackend
};
