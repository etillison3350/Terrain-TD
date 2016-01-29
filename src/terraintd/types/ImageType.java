package terraintd.types;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class ImageType {

	public final BufferedImage image;
	public final Path src;
	public final double width;
	public final double height;
	public final double x;
	public final double y;
//	public final boolean clip;

	ImageType(Path src, double width, double height, double x, double y) {
		this.src = src;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;

		BufferedImage image;
		try {
			image = ImageIO.read(src.toFile());
		} catch (IOException e) {
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
		this.image = image;
	}
}
