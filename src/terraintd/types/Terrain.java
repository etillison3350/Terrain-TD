package terraintd.types;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public enum Terrain {
	GRASS(new Color(0.4F, 0.75F, 0.25F)),
	STONE(Color.GRAY),
	SAND(new Color(0.9F, 0.75F, 0.5F)),
	MARSHLANDS(new Color(0.3F, 0.5F, 0.25F)),
	SNOW(new Color(0.9F, 0.9F, 0.9F)),
	FROZEN_WATER(new Color(0.75F, 0.75F, 1.0F)),
	SHALLOW_WATER(new Color(0.0F, 0.5F, 1.0F)),
	DEEP_WATER(new Color(0.0F, 0.0F, 0.75F));
	
	private BufferedImage image;
	
	private Terrain(String imgSrc) {
		try {
			// TODO
			image = ImageIO.read(new File("resources" + System.getProperty("file.separator") + imgSrc));
		} catch (IOException e) {}
	}
	
	private Terrain(Color color) {
		image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = image.createGraphics();
		
		g.setColor(color);
		g.fillRect(0, 0, 8, 8);
		g.dispose();
	}
	
	public BufferedImage getImage() {
		return image;
	}
}
