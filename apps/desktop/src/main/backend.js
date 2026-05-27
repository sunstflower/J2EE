const { randomUUID } = require("node:crypto");
const { spawn } = require("node:child_process");
const fs = require("node:fs");
const path = require("node:path");

const REPOSITORY_ROOT = path.resolve(__dirname, "../../../../");
const LOCAL_API_DIR = path.resolve(__dirname, "../../../../services/local-api");
const DEFAULT_RUNTIME_ROOT = path.join(REPOSITORY_ROOT, ".runtime");
const DEFAULT_CLASH_META_PATH = path.join(
  REPOSITORY_ROOT,
  "runtime-assets",
  "clash-meta",
  "bin",
  "clash-meta"
);
const READY_PATTERN = /LOCAL_API_READY port=(\d+)/i;
const SHELL_PATH = fs.existsSync("/bin/zsh") ? "/bin/zsh" : "zsh";

function resolveMavenCommandForShell() {
  const explicitBinary = process.env.MAVEN_BIN;

  if (explicitBinary) {
    return explicitBinary;
  }

  if (process.platform === "win32") {
    return "mvn.cmd";
  }

  const candidates = [
    "/opt/homebrew/bin/mvn",
    "/usr/local/bin/mvn",
    "/usr/bin/mvn"
  ];

  const resolved = candidates.find((candidate) => fs.existsSync(candidate));
  return resolved || "mvn";
}

function resolveRuntimeRoot() {
  const configuredRoot = process.env.APP_RUNTIME_ROOT;
  return configuredRoot && configuredRoot.trim()
    ? path.resolve(configuredRoot)
    : DEFAULT_RUNTIME_ROOT;
}

function resolveClashMetaPath() {
  const configuredPath = process.env.APP_CORE_CLASH_META_PATH;

  if (configuredPath && configuredPath.trim()) {
    return path.resolve(configuredPath);
  }

  if (fs.existsSync(DEFAULT_CLASH_META_PATH)) {
    return DEFAULT_CLASH_META_PATH;
  }

  return "";
}

function startBackend() {
  const sessionToken = randomUUID();
  const mavenCommand = resolveMavenCommandForShell();
  const shellCommand = `${mavenCommand} spring-boot:run`;
  const runtimeRoot = resolveRuntimeRoot();
  const clashMetaPath = resolveClashMetaPath();

  return new Promise((resolve, reject) => {
    let settled = false;

    const child = spawn(SHELL_PATH, ["-lc", shellCommand], {
      cwd: LOCAL_API_DIR,
      env: {
        ...process.env,
        APP_SESSION_TOKEN: sessionToken,
        APP_RUNTIME_ROOT: runtimeRoot,
        APP_CORE_CLASH_META_PATH: clashMetaPath
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
          sessionToken,
          runtimeRoot,
          clashMetaPath
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
