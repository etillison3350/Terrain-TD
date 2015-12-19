package terraintd.types;

import java.util.HashMap;

public class EnemyType {

	public final HashMap<Terrain, Double> speed;
	public final double upSpeed;
	public final double downSpeed;
	public final double health;
	public final double damage;
	public final int reward;
	public final ImageType image;
	public final ImageType death;
	public final ProjectileType[] projectiles;

	EnemyType(HashMap<Terrain, Double> speed, double upSpeed, double downSpeed, double health, double damage, int reward, ImageType image, ImageType death, ProjectileType[] projectiles) {
		this.speed = speed;
		this.upSpeed = upSpeed;
		this.downSpeed = downSpeed;
		this.health = health;
		this.damage = damage;
		this.reward = reward;
		this.image = image;
		this.death = death;
		this.projectiles = projectiles;
	}
	
	public static EnemyType[] values() {
		return TypeGenerator.enemies();
	}
}
