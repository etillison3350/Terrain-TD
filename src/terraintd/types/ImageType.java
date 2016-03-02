package terraintd.types;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class ImageType extends ModdedType {

	public static final ImageType BLANK = new ImageType("blankimage", null, null, 1, 1, 0, 0);
	
	public final BufferedImage image;
	public final Path src;
	public final double width;
	public final double height;
	public final double x;
	public final double y;
//	public final boolean clip;

	protected ImageType(String id, Mod mod, Path src, double width, double height, double x, double y) {
		super(mod, id);
		
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
