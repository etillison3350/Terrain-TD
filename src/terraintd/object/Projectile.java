package terraintd.object;

import java.util.ArrayList;

import terraintd.GameLogic;
import terraintd.types.DeliveryType;
import terraintd.types.ProjectileType;

public class Projectile {

	public final ProjectileType type;
	public final Entity shootingEntity;
	private final double startX, startY;
	private double x, y;
	private double rotation;
	private double radius;
	private double deathTime;

	private final Enemy target;
	private final double targetX, targetY;
	private ArrayList<Enemy> hitTargets;

	public <E extends Entity & Weapon> Projectile(ProjectileType type, E shootingEntity) {
		this.type = type;
		this.x = this.startX = shootingEntity.getX();
		this.y = this.startY = shootingEntity.getY();
		this.rotation = type.rotation + ((type.absRotation && shootingEntity instanceof Tower) ? ((Tower) shootingEntity).getRotation() : 0);
		this.shootingEntity = shootingEntity;

		this.deathTime = -1;

		this.target = shootingEntity.getGun().getTarget();
		targetX = type.delivery == DeliveryType.SINGLE_TARGET && !type.follow ? this.target.getX() : 0;
		targetY = type.delivery == DeliveryType.SINGLE_TARGET && !type.follow ? this.target.getX() : 0;
	}

	public boolean move() {
		if (this.type.delivery == DeliveryType.SINGLE_TARGET) {
			double tx, ty;
			double dx, dy;
			if (this.type.follow) {
				tx = target.getX();
				ty = target.getY();
				dx = tx - this.x;
				dy = ty - this.y;

				this.rotation = Math.atan2(dy, dx);
			} else {
				tx = targetX;
				ty = targetY;
				dx = tx - this.x;
				dy = ty - this.y;
			}

			if (dx * dx + dy * dy < 2) {
				this.x = tx;
				this.y = ty;
				return false;
			}

			this.x += Math.cos(this.rotation) * this.type.speed * GameLogic.FRAME_TIME;
			this.y += Math.sin(this.rotation) * this.type.speed * GameLogic.FRAME_TIME;

			this.radius = Math.hypot(this.x - startX, this.y - startY);
		} else {
			this.radius += this.type.speed * GameLogic.FRAME_TIME;
		}

		if (this.type.maxDist - this.radius < 0.01) {
			this.radius = this.type.maxDist;
			if (!this.type.follow) {
				this.x = this.startX + this.radius * Math.cos(this.rotation);
				this.y = this.startY + this.radius * Math.sin(this.rotation);
			}
			return false;
		}

		return true;
	}

	public boolean fade() {
		if (this.deathTime < 0) {
			this.deathTime = 0;
		} else {
			this.deathTime += GameLogic.FRAME_TIME;
		}

		return this.deathTime >= this.type.dyingFadeTime;
	}

	/**
	 * <ul>
	 * <li><b><i>damageForEntity</i></b><br>
	 * <br>
	 * {@code public double damageForEntity(Entity e)}<br>
	 * <br>
	 * @param e The entity to calcualte damage for
	 * @return The amount of damage that this projectile should do to this entity (based on distance)
	 *         </ul>
	 */
	public double damageForEntity(Entity e) {
		return this.type.damage - this.type.falloff * Math.hypot(e.getX() - startX, e.getY() - startY) / this.type.maxDist;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getRotation() {
		return rotation;
	}

	public double getRadius() {
		return radius;
	}

	public Enemy getTarget() {
		return target;
	}

//	public double damageEntity(Entity e) {
//		if (this.type.delivery == DeliveryType.SINGLE_TARGET) {
//			double dx = this.x - e.getX();
//			double dy = this.y - e.getY();
//			this.deathTime = 0;
//			return dx * dx + dy * dy < 1 ? this.type.damage : 0;
//		} else {
//
//		}
//
//	}
//
//	private double getDamageForLocation(double x, double y) {
//
//	}

	public ArrayList<Enemy> getHitTargets() {
		return hitTargets;
	}

	public void hitTarget(Enemy target) {
		this.hitTargets.add(target);
	}

	/**
	 * <ul>
	 * <li><b><i>getDeathTime</i></b><br>
	 * <br>
	 * {@code public double getDeathTime()}<br>
	 * <br>
	 * @return this Projectile's death time (in seconds)
	 *         </ul>
	 */
	public double getDeathTime() {
		return deathTime;
	}

}
