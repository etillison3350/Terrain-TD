package terraintd.types;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public enum StatusEffectType {
	FIRE(Color.ORANGE, "fireEffect.png"),
	PARALYSIS(Color.LIGHT_GRAY, null),
	SLOWNESS(new Color(0.25F, 0.125F, 0.5F), null),
	WEAKNESS(new Color(0.5F, 0.0F, 1.0F), null),
	BLUNTNESS(Color.BLACK, null),
	FROST(new Color(0.0F, 1.0F, 1.0F), null),
	POISON(Color.GREEN, null),
	// REVERSE(Color.YELLOW, null), // This effect is illogical because there is no method of reversing on open world maps
	BLEED(Color.RED, null);

	public final Color color;
	private BufferedImage image;

	private StatusEffectType(Color color, String imgSrc) {
		this.color = color;
		try {
			// TODO
			this.image = ImageIO.read(new File("resources" + System.getProperty("file.separator") + imgSrc));
		} catch (IOException e) {}
	}

	public BufferedImage getImage() {
		return image;
	}
	
	public String toString() {
		return Language.get("effect-" + this.name());
	}
}
