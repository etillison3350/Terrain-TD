package terraintd.types;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import terraintd.Language;

public enum StatusEffectType {
	FIRE(true, Color.ORANGE, "fireEffect.png"),
	PARALYSIS(false, new Color(205, 205, 230), null),
	SLOWNESS(true, new Color(64, 32, 128), null),
	WEAKNESS(true, new Color(128, 0, 255), null),
	BLUNTNESS(true, Color.DARK_GRAY, null),
	FROST(true, new Color(0, 255, 255), null),
	POISON(true, Color.GREEN, null),
	BLEED(true, Color.RED, null);

	public final boolean amplifiable;
	public final Color color;
	private BufferedImage image;

	private StatusEffectType(boolean amplifiable, Color color, String imgSrc) {
		this.amplifiable = amplifiable;
		this.color = color;
		try {
			this.image = ImageIO.read(Paths.get("terraintd/mods/base/images/" + imgSrc).toFile());
		} catch (IOException e) {}
	}

	public BufferedImage getImage() {
		return image;
	}
	
	public String toString() {
		return Language.get("effect-" + this.name());
	}
}
