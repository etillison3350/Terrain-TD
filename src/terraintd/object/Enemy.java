package terraintd.object;

import terraintd.object.Gun.TempProjectile;
import terraintd.pathfinder.Node;
import terraintd.types.EnemyType;

public class Enemy extends Entity implements Weapon {

	public final EnemyType type;
	private final Gun gun;

	private double x, y;
	private double health;
	
	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}

	public Enemy(EnemyType type, double x, double y) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.gun = type.projectiles != null && type.projectiles.length > 0 ? new Gun(this) : null;
	}

	@Override
	public Gun getGun() {
		return this.gun;
	}

	@Override
	public Projectile[] convertFromTempProjectiles(TempProjectile[] temps) {
		Projectile[] ps = new Projectile[temps.length];

		for (int n = 0; n < temps.length; n++) {
			ps[n] = new Projectile(temps[n].type, this);
		}

		return ps;
	}
	
	private int nextX, nextY;
	private boolean nextTop;
	
	public boolean move() {
		// TODO
		return true;
	}
	
	public int damage(double damage) {
		this.health -= damage;
		
		if (this.health <= 0.00001) {
			return this.type.reward;
		}
		
		return 0;
	}
	
	public double getHealth() {
		return this.health;
	}

}
