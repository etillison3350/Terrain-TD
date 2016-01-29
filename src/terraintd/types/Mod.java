package terraintd.types;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Mod extends IdType {

	public final Path path;
	public final String version;
	public final String[] authors;
	public final String contact;
	public final String homepage;
	public final String description;
	public final Icon icon;
	public final Icon gray;

	public Mod(String id, Path path, String version, String[] authors, String contact, String homepage, String description, String icon) {
		super(id);

		this.path = path;
		this.version = version;
		this.authors = authors;
		this.contact = contact;
		this.homepage = homepage;
		this.description = description;
		
		Image image;
		try {
			image = ImageIO.read(path.resolve(icon).toFile()).getScaledInstance(32, 32, 0);
		} catch (Exception e) {
			image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		}
		
		this.icon = new ImageIcon(image);
		
		BufferedImage img = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D ig = img.createGraphics();
		ig.drawImage(image, 0, 0, null);
		ig.dispose();
		
		this.gray = new ImageIcon(new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(img, null));
	}

	public static Mod[] values() {
		return TypeGenerator.mods();
	}

}
