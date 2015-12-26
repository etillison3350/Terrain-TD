package terraintd.types;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageType {

	public BufferedImage image;
	public final String src;
	public final double width;
	public final double height;
	public final double x;
	public final double y;
//	public final boolean clip;

	ImageType(String src, double width, double height, double x, double y) {
		this.src = src;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		
		try {
			this.image = ImageIO.read(new File(src));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
