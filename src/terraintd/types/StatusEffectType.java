package terraintd.types;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import terraintd.Language;

public enum StatusEffectType {
	FIRE(Color.ORANGE, "fireEffect.png"),
	PARALYSIS(Color.LIGHT_GRAY, null),
	SLOWNESS(new Color(0.25F, 0.125F, 0.5F), null),
	WEAKNESS(new Color(0.5F, 0.0F, 1.0F), null),
	BLUNTNESS(Color.DARK_GRAY, null),
	FROST(new Color(0.0F, 1.0F, 1.0F), null),
	POISON(Color.GREEN, null),
	BLEED(Color.RED, null);

	public final Color color;
	private BufferedImage image;

	private StatusEffectType(Color color, String imgSrc) {
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
