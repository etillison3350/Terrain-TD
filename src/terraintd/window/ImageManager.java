package terraintd.window;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

import terraintd.object.Enemy;
import terraintd.object.StatusEffect;
import terraintd.types.EnemyType;
import terraintd.types.ImageType;
import terraintd.types.StatusEffectType;

public class ImageManager {

	private static final HashMap<Resource, BufferedImage> images = new HashMap<>();

	private ImageManager() {}

	public static BufferedImage get(Resource resource) {
		BufferedImage ret = images.get(resource);
		if (ret != null) return ret;

		ImageType img = resource.dead ? resource.type.death : resource.type.image;

		ret = new BufferedImage((int) (GamePanel.getTile() * img.width), (int) (GamePanel.getTile() * img.height), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = ret.createGraphics();
		g.drawImage(img.image, 0, 0, ret.getWidth(), ret.getHeight(), null);
		if (!resource.dead && resource.statusEffects.length > 0) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5F));
			g.setColor(averageColors(resource.statusEffects));
			g.fillRect(0, 0, ret.getWidth(), ret.getHeight());
		}

		images.put(resource, ret);
		return ret;
	}

	protected static Color averageColors(StatusEffectType[] effects) {
		double r = 0, g = 0, b = 0;

		for (StatusEffectType type : effects) {
			r += type.color.getRed();
			g += type.color.getGreen();
			b += type.color.getBlue();
		}

		return new Color((int) (r / effects.length), (int) (g / effects.length), (int) (b / effects.length));
	}

	public static class Resource {

		public final EnemyType type;
		public final StatusEffectType[] statusEffects;
		public final boolean dead;
		public final Color color;

		public Resource(EnemyType type, StatusEffectType[] statusEffects, boolean dead) {
			this.type = type;
			this.statusEffects = statusEffects;
			this.dead = dead;
			this.color = averageColors(statusEffects);
		}

		public Resource(EnemyType type, StatusEffect[] statusEffects, boolean dead) {
			this.type = type;
			StatusEffectType[] statusEffectTypes = new StatusEffectType[statusEffects.length];
			for (int i = 0; i < statusEffects.length; i++) {
				statusEffectTypes[i] = statusEffects[i].type;
			}
			this.statusEffects = statusEffectTypes;
			this.dead = dead;
			this.color = averageColors(this.statusEffects);
		}

		public Resource(Enemy enemy) {
			this(enemy.type, enemy.getStatusEffects(), enemy.isDead());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (dead ? 1231 : 1237);
			result = prime * result + (dead ? 0 : Arrays.hashCode(statusEffects));
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;

			if (getClass() != obj.getClass()) return false;
			Resource other = (Resource) obj;
			if (dead != other.dead) return false;
			if (dead && !Arrays.equals(statusEffects, other.statusEffects)) return false;
			if (type == null) {
				if (other.type != null) return false;
			} else if (!type.equals(other.type)) {
				return false;
			}
			return true;
		}

	}

}
