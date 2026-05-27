const fs = require("node:fs");
const path = require("node:path");
const { nativeImage } = require("electron");
const { createTrayImage } = require("./trayIcon");

const DEFAULT_STATE_FILE_CANDIDATES = {
  idle: ["tray-idle.png", "tray-idle@2x.png"],
  on: ["tray-on.png", "tray-on@2x.png"],
  off: ["tray-off.png", "tray-off@2x.png"],
  pending: ["tray-pending.png", "tray-pending@2x.png"]
};

const MACOS_TEMPLATE_STATE_FILE_CANDIDATES = {
  idle: ["macos-template-tray-idle.png", "macos-template-tray-idle@2x.png"],
  on: ["macos-template-tray-on.png", "macos-template-tray-on@2x.png"],
  off: ["macos-template-tray-off.png", "macos-template-tray-off@2x.png"],
  pending: ["macos-template-tray-pending.png", "macos-template-tray-pending@2x.png"]
};

function resolveTrayImage(state) {
  const assetImage = loadTrayAssetImage(state);
  if (assetImage && !assetImage.isEmpty()) {
    return assetImage;
  }

  return createTrayImage(state);
}

function loadTrayAssetImage(state) {
  const candidates = resolveStateFileCandidates(state);
  const trayAssetDir = path.resolve(__dirname, "../../assets/tray");
  const filenames = candidates[state] ?? candidates.idle;
  const image = nativeImage.createEmpty();
  let loaded = false;

  filenames.forEach((filename) => {
    const absolutePath = path.join(trayAssetDir, filename);
    if (!fs.existsSync(absolutePath)) {
      return;
    }

    const scaleFactor = filename.includes("@2x") ? 2 : 1;
    const representation = nativeImage.createFromPath(absolutePath);
    if (representation.isEmpty()) {
      return;
    }

    const size = representation.getSize();
    image.addRepresentation({
      scaleFactor,
      width: Math.round(size.width),
      height: Math.round(size.height),
      buffer: representation.toPNG()
    });
    loaded = true;
  });

  if (!loaded) {
    return null;
  }

  image.setTemplateImage(process.platform === "darwin" && candidates === MACOS_TEMPLATE_STATE_FILE_CANDIDATES);
  return image;
}

function resolveStateFileCandidates(state) {
  if (process.platform === "darwin") {
    const hasTemplateFiles = hasAnyAssetFiles(MACOS_TEMPLATE_STATE_FILE_CANDIDATES[state] ?? MACOS_TEMPLATE_STATE_FILE_CANDIDATES.idle);
    if (hasTemplateFiles) {
      return MACOS_TEMPLATE_STATE_FILE_CANDIDATES;
    }
  }

  return DEFAULT_STATE_FILE_CANDIDATES;
}

function hasAnyAssetFiles(filenames) {
  const trayAssetDir = path.resolve(__dirname, "../../assets/tray");
  return filenames.some((filename) => fs.existsSync(path.join(trayAssetDir, filename)));
}

module.exports = {
  resolveTrayImage
};
