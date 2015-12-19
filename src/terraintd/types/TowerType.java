package terraintd.types;

import java.util.HashMap;

public class TowerType {

	public final String id;
	public final int cost;
	public final int width;
	public final int height;
	public final HashMap<Terrain, Boolean> terrain;
	public final boolean onHill;
	public final boolean rotate;
	public final ImageType image;
	public final ImageType icon;
	public final ProjectileType[] projectiles;

	TowerType(String id, int cost, int width, int height, HashMap<Terrain, Boolean> terrain, boolean onHill, boolean rotate, ImageType image, ImageType icon, ProjectileType[] projectiles) {
		this.id = id;
		this.cost = cost;
		this.width = width;
		this.height = height;
		this.terrain = terrain;
		this.onHill = onHill;
		this.rotate = rotate;
		this.image = image;
		this.icon = icon;
		this.projectiles = projectiles;
	}
	
	public static TowerType[] values() {
		return TypeGenerator.towers();
	}
}
