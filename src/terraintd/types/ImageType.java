package terraintd.types;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class ImageType {

	public BufferedImage image;
	public final String src;
	public final double width;
	public final double height;
	public final double x;
	public final double y;

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

	@Deprecated
	ImageType(String s) {
		Matcher srcM = Pattern.compile("src=([0-9a-z_\\-/\"\\.]+)").matcher(s);
		this.src = srcM.find() ? srcM.group(1) : "";

		Matcher collM = Pattern.compile("collision=([\\[\\{][0-9a-z\\,\\.][\\]\\}])").matcher(s);

		String collsion = collM.find() ? collM.group(1) : "[1,1]";
		double width = 1, height = 1;
		if (collsion.matches("[hw]")) {
			Matcher whM = Pattern.compile("(width|height)=\\+?([0-9\\.]+),").matcher(collsion);
			while (whM.find()) {
				if (whM.group(1).equals("width")) {
					width = Integer.parseUnsignedInt(whM.group(2));
				} else {
					height = Integer.parseUnsignedInt(whM.group(2));
				}
			}
		} else {
			Matcher whM = Pattern.compile("\\[\\+?([0-9\\.]+),\\+?([0-9\\.]+)\\]").matcher(collsion);
			if (whM.find()) {
				width = Integer.parseUnsignedInt(whM.group(1));
				height = Integer.parseUnsignedInt(whM.group(2));
			}
		}

		this.width = width;
		this.height = height;

		Matcher oriM = Pattern.compile("origin=([\\[\\{][0-9a-z\\,\\.][\\]\\}])").matcher(s);

		String origin = oriM.find() ? oriM.group(1) : "[0,0]";
		int x = 0, y = 0;
		if (collsion.matches("[xy]")) {
			Matcher xyM = Pattern.compile("([xy])=\\+?([0-9\\.]+),").matcher(origin);
			while (xyM.find()) {
				if (xyM.group(1).equals("x")) {
					x = Integer.parseUnsignedInt(xyM.group(2));
				} else {
					y = Integer.parseUnsignedInt(xyM.group(2));
				}
			}
		} else {
			Matcher xyM = Pattern.compile("\\[\\+?([0-9\\.]+),\\+?([0-9\\.]+)\\]").matcher(origin);
			if (xyM.find()) {
				x = Integer.parseUnsignedInt(xyM.group(1));
				y = Integer.parseUnsignedInt(xyM.group(2));
			}
		}

		this.x = x;
		this.y = y;
	}
}
