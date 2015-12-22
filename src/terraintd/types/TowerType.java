package terraintd.types;

import java.util.HashMap;

public class TowerType extends CollidableType {

	public final HashMap<Terrain, Boolean> terrain;
	public final boolean onHill;
	public final boolean rotate;
	public final ProjectileType[] projectiles;

	TowerType(String id, int cost, int width, int height, HashMap<Terrain, Boolean> terrain, boolean onHill, boolean rotate, ImageType image, ImageType icon, ProjectileType[] projectiles) {
		super(id, width, height, cost, image, icon);
		
		this.terrain = terrain;
		this.onHill = onHill;
		this.rotate = rotate;
		this.projectiles = projectiles;
	}
	
	public static TowerType[] values() {
		return TypeGenerator.towers();
	}
}
