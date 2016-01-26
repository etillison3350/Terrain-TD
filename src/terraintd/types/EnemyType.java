package terraintd.types;

import java.util.HashMap;

public class EnemyType extends ModdedType {

	public final HashMap<Terrain, Double> speed;
	public final double upSpeed;
	public final double downSpeed;
	public final double health;
	public final double damage;
	public final int reward;
	public final double range;
	public final double hbWidth;
	public final double hbY;
	public final ImageType image;
	public final ImageType death;
	public final ProjectileType[] projectiles;

	EnemyType(Mod mod, String id, HashMap<Terrain, Double> speed, double upSpeed, double downSpeed, double health, double damage, int reward, double range, double hbWidth, double hbY, ImageType image, ImageType death, ProjectileType[] projectiles) {
		super(mod, id);
		
		typeIds.put(id, this);
		
		this.speed = speed;
		this.upSpeed = upSpeed;
		this.downSpeed = downSpeed;
		this.health = health;
		this.damage = damage;
		this.reward = reward;
		this.range = range;
		this.hbWidth = hbWidth;
		this.hbY = hbY;
		this.image = image;
		this.death = death;
		this.projectiles = projectiles;
	}
	
	static final HashMap<String, EnemyType> typeIds = new HashMap<>();
	
	public static EnemyType valueOf(String id) {
		return typeIds.get(id);
	}
	
	public static EnemyType[] values() {
		return TypeGenerator.enemies();
	}
}
