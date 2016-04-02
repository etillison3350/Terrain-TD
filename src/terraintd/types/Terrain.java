package terraintd.types;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public enum Terrain {
	GRASS("grass.png"),
	STONE("stone-tex.png"),
	SAND("sand.png"),
	MARSHLANDS("marsh.png"),
	SNOW("snow.png"),
	FROZEN_WATER("ice.png"),
	SHALLOW_WATER("water.png"),
	DEEP_WATER("deep-water.png");

	private BufferedImage image;

	private Terrain(String imgSrc) {
		try {
			image = ImageIO.read(Paths.get("terraintd/mods/base/images/" + imgSrc).toFile());
		} catch (IOException e) {}
	}

	private Terrain(Color color) {
		image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();

		g.setColor(color);
		g.fillRect(0, 0, 1, 1);
		g.dispose();
	}

	public BufferedImage getImage() {
		return image;
	}
}
