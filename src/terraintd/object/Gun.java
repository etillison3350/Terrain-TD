package terraintd.object;

import java.util.ArrayList;

import terraintd.GameLogic;
import terraintd.types.ProjectileType;

public class Gun {

	final ProjectileType[] projectiles;
	final double range;
	final double x, y;

	private Entity target;

	private double[] time;

	public Gun(ProjectileType[] projectiles, double range, double x, double y) {
		this.projectiles = projectiles;
		if (this.projectiles == null || this.projectiles.length == 0) throw new IllegalArgumentException("projectiles must not be null or empty.");
		this.range = range;
		this.time = new double[projectiles.length];
		this.x = x;
		this.y = y;
	}

	/**
	 * <ul>
	 * <li><b><i>Gun</i></b><br>
	 * <br>
	 * {@code Gun()}<br>
	 * <br>
	 * Creates a new gun using the position and projectiles of the given {@link Tower} or {@link Enemy}<br>
	 * @param e A tower or enemy whose type has projectiles
	 * @throws IllegalArgumentException if e is not a tower or enemy, or if its list of projectiles is <code>null</code> or empty.
	 *         </ul>
	 */
	public Gun(Entity e) {
		double range;

		if (e instanceof Tower) {
			this.projectiles = ((Tower) e).type.projectiles;
			range = ((Tower) e).type.range;
		} else if (e instanceof Enemy) {
			this.projectiles = ((Enemy) e).type.projectiles;
			range = ((Enemy) e).type.range;
		} else {
			throw new IllegalArgumentException("e must be a Tower or Enemy with projectiles");
		}

		this.range = range;

		if (this.projectiles == null || this.projectiles.length == 0) throw new IllegalArgumentException("e must be a Tower or Enemy with projectiles");

		this.time = new double[projectiles.length];

		this.x = e.getX();
		this.y = e.getY();
	}

	public Entity target(Entity e) {
		return this.target = e;
	}

	public Entity getTarget() {
		return target;
	}

	/**
	 * <ul>
	 * <li><b><i>getRotation</i></b><br>
	 * <br>
	 * {@code public double getRotation()}<br>
	 * <br>
	 * @return The angle at which this Gun's target is relative to the Gun's location, in radians.
	 *         </ul>
	 */
	public double getRotation() {
		return target == null ? 0 : Math.atan2(this.y - target.getY(), this.x - target.getX());
	}

	private double getDistanceSq() {
		return target == null ? Float.MAX_VALUE : this.y - target.getY() * this.y - target.getY() + this.x - target.getX() * this.x - target.getX();
	}

	public TempProjectile[] fire() {
		ArrayList<TempProjectile> firing = new ArrayList<>();

		if (getDistanceSq() < range * range) {
			for (int n = 0; n < time.length; n++) {
				time[n] += GameLogic.FRAME_TIME;

				while (time[n] > 0) {
					time[n] -= 1.0 / projectiles[n].rate;
					firing.add(new TempProjectile(projectiles[n], this.x, this.y));
				}
			}
		}

		return firing.toArray(new TempProjectile[firing.size()]);
	}

	public static class TempProjectile {

		public final ProjectileType type;
		public final double x, y;

		public TempProjectile(ProjectileType type, double x, double y) {
			this.type = type;
			this.x = x;
			this.y = y;
		}

	}

}
