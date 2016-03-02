package terraintd.types;

import java.awt.Color;
import java.util.HashMap;

public class TowerType extends CollidableType {

	public final HashMap<Terrain, Boolean> terrain;
	public final boolean onHill;
	public final double range;
	public final boolean rotate;
	public final ProjectileType[] projectiles;
	public final TowerUpgrade[] upgrades;

	protected TowerType(Mod mod, String id, int cost, int sellCost, int width, int height, HashMap<Terrain, Boolean> terrain, boolean onHill, double range, boolean rotate, ImageType image, ImageType icon, ProjectileType[] projectiles, TowerUpgrade[] upgrades) {
		super(mod, id, width, height, cost, sellCost, image, icon);

		typeIds.put(id, this);

		this.terrain = terrain;
		this.onHill = onHill;
		this.range = range;
		this.rotate = rotate;
		this.projectiles = projectiles;
		this.upgrades = upgrades;
	}

	static final HashMap<String, TowerType> typeIds = new HashMap<>();

	public static TowerType valueOf(String id) {
		return typeIds.get(id);
	}

	public static TowerType[] values() {
		return TypeGenerator.towers();
	}
	
	@Override
	public Color getBackgroundColor() {
		return new Color(0.375F, 0.375F, 0.75F);
	}
}
