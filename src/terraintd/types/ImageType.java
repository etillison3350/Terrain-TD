package terraintd.types;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class ImageType {

	public static final ImageType BLANK = new ImageType(null, 1, 1, 0, 0);
	
	public final BufferedImage image;
	public final Path src;
	public final double width;
	public final double height;
	public final double x;
	public final double y;
//	public final boolean clip;

	protected ImageType(Path src, double width, double height, double x, double y) {
		this.src = src;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;

		BufferedImage image;
		try {
			image = ImageIO.read(src.toFile());
		} catch (Exception e) {
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
		this.image = image;
	}
}
