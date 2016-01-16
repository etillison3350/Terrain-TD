package terraintd.object;

import java.util.ArrayList;

import terraintd.GameLogic;
import terraintd.types.ProjectileType;

public class Gun {

	public final ProjectileType[] projectiles;
	public final double range;
	public final Entity shooter;

	private Enemy target;

	private double[] time;

	public Gun(ProjectileType[] projectiles, double range, double x, double y) {
		this.projectiles = projectiles;
		if (this.projectiles == null || this.projectiles.length == 0) throw new IllegalArgumentException("projectiles must not be null or empty.");
		this.range = range;
		this.time = new double[projectiles.length];
		this.shooter = new Position(x, y);
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

		this.shooter = e;
	}

	public Enemy target(Enemy e) {
		return this.target = e;
	}

	public Enemy getTarget() {
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
		return target == null ? 0 : Math.atan2(shooter.getY() - target.getY(), shooter.getX() - target.getX());
	}

	private double getDistanceSq() {
		return target == null ? Float.MAX_VALUE : shooter.getY() - target.getY() * shooter.getY() - target.getY() + shooter.getX() - target.getX() * shooter.getX() - target.getX();
	}

	public ProjectileType[] fire() {
		ArrayList<ProjectileType> firing = new ArrayList<>();

		if (getDistanceSq() < range * range) {
			for (int n = 0; n < time.length; n++) {
				time[n] += GameLogic.FRAME_TIME;

				while (time[n] > 0) {
					time[n] -= 1.0 / projectiles[n].rate;
					firing.add(projectiles[n]);
				}
			}
		}

		return firing.toArray(new ProjectileType[firing.size()]);
	}

}
