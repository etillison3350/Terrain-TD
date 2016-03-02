package terraintd.types;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public enum Terrain {
	GRASS(new Color(102, 192, 64)),
	STONE(Color.GRAY),
	SAND(new Color(230, 192, 128)),
	MARSHLANDS(new Color(77, 128, 64)),
	SNOW(new Color(230, 230, 230)),
	FROZEN_WATER(new Color(150, 150, 255)),
	SHALLOW_WATER(new Color(0, 64, 255)),
	DEEP_WATER(new Color(0, 0, 192));

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
