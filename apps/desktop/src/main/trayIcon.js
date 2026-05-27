const { nativeImage } = require("electron");

function createTrayImage(state) {
  const image = nativeImage.createEmpty();

  [18, 36].forEach((size) => {
    const representation = createRepresentation(state, size);
    image.addRepresentation({
      scaleFactor: size === 36 ? 2 : 1,
      width: size,
      height: size,
      buffer: representation.toPNG()
    });
  });

  image.setTemplateImage(false);
  return image;
}

function createRepresentation(state, size) {
  const palette = resolveTrayPalette(state);
  const svg = buildSvg(state, size, palette);
  const dataUrl = `data:image/svg+xml;base64,${Buffer.from(svg).toString("base64")}`;
  return nativeImage.createFromDataURL(dataUrl);
}

function buildSvg(state, size, palette) {
  const strokeWidth = size <= 18 ? 1.6 : 2.4;
  const shellY = size * 0.2;
  const shellHeight = size * 0.52;
  const shellRadius = size * 0.16;
  const shellWidth = size * 0.78;
  const shellX = (size - shellWidth) / 2;
  const center = size / 2;
  const bottomY = shellY + shellHeight + size * 0.05;

  const accent =
    state === "pending"
      ? `<circle cx="${size * 0.78}" cy="${size * 0.24}" r="${size * 0.11}" fill="${palette.accent}" />`
      : state === "off"
        ? `<line x1="${size * 0.28}" y1="${size * 0.3}" x2="${size * 0.72}" y2="${size * 0.74}" stroke="${palette.accent}" stroke-width="${strokeWidth}" stroke-linecap="round" />`
        : state === "on"
          ? `<path d="M ${size * 0.35} ${size * 0.52} L ${size * 0.47} ${size * 0.64} L ${size * 0.69} ${size * 0.38}" fill="none" stroke="${palette.accent}" stroke-width="${strokeWidth}" stroke-linecap="round" stroke-linejoin="round" />`
          : `<circle cx="${center}" cy="${size * 0.48}" r="${size * 0.1}" fill="${palette.accent}" />`;

  return `
    <svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
      <rect x="${shellX}" y="${shellY}" width="${shellWidth}" height="${shellHeight}" rx="${shellRadius}" fill="${palette.background}" />
      <circle cx="${center}" cy="${shellY + shellHeight / 2}" r="${size * 0.13}" fill="${palette.foreground}" />
      <rect x="${size * 0.39}" y="${bottomY}" width="${size * 0.22}" height="${size * 0.1}" rx="${size * 0.05}" fill="${palette.foreground}" />
      ${accent}
    </svg>
  `.trim();
}

function resolveTrayPalette(state) {
  if (state === "on") {
    return {
      background: "#0f766e",
      foreground: "#ecfeff",
      accent: "#99f6e4"
    };
  }

  if (state === "pending") {
    return {
      background: "#d97706",
      foreground: "#fff7ed",
      accent: "#fef3c7"
    };
  }

  if (state === "off") {
    return {
      background: "#475569",
      foreground: "#f8fafc",
      accent: "#fecaca"
    };
  }

  return {
    background: "#94a3b8",
    foreground: "#f8fafc",
    accent: "#e2e8f0"
  };
}

module.exports = {
  createTrayImage
};
