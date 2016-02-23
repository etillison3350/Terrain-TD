package terraintd.types;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import terraintd.Language;

public enum StatusEffectType {
	FIRE(true, Color.ORANGE, "fireEffect.png"),
	PARALYSIS(false, new Color(0.8F, 0.8F, 0.9F), null),
	SLOWNESS(true, new Color(0.25F, 0.125F, 0.5F), null),
	WEAKNESS(true, new Color(0.5F, 0.0F, 1.0F), null),
	BLUNTNESS(true, Color.DARK_GRAY, null),
	FROST(true, new Color(0.0F, 1.0F, 1.0F), null),
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
