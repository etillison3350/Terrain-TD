package terraintd.types;

import java.awt.Color;
import java.util.HashMap;

import terraintd.Language;

public class InstantType extends ModdedType implements Purchasable {

	public final int cost;
	public final Target target;
	public final boolean unique;
	public final double range;
	public final double spread;
	public final boolean individual;
	public final double delay;
	public final boolean sync;
	public final int count;
	public final double repeatDelay;
	public final ImageType icon;
	public final ProjectileType[] projectiles;

	protected InstantType(Mod mod, String id, int cost, Target target, boolean unique, double range, double spread, boolean individual, double delay, boolean sync, int count, double repeatDelay, ImageType icon, ProjectileType[] projectiles) {
		super(mod, id);

		typeIds.put(id, this);

		this.cost = cost;
		this.target = target;
		this.unique = unique;
		this.range = range;
		this.spread = spread;
		this.individual = individual;
		this.delay = delay;
		this.sync = sync;
		this.count = count;
		this.repeatDelay = repeatDelay;
		this.icon = icon;
		this.projectiles = projectiles;
	}

	public static enum Target {
		ENEMY,
		LOCATION,
		ROTATION,
		SPREAD;
		
		public String toString() {
			return Language.get("inst-target-" + this.name().toLowerCase());
		}
	}

	static final HashMap<String, InstantType> typeIds = new HashMap<>();

	public static InstantType valueOf(String id) {
		return typeIds.get(id);
	}

	public static InstantType[] values() {
		return TypeGenerator.instants();
	}

	@Override
	public int getCost() {
		return cost;
	}

	@Override
	public ImageType getIcon() {
		return icon;
	}
	
	@Override
	public Color getBackgroundColor() {
		return new Color(0.375F, 0.75F, 0.5625F);
	}

}
