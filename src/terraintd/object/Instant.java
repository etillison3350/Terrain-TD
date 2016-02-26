package terraintd.object;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import terraintd.GameLogic;
import terraintd.types.InstantType;

public class Instant extends Entity {

	public final InstantType type;

	public final double x, y;

	private List<Repetition> repetitions;

	public Instant(InstantType type, double x, double y) {
		this.type = type;
		this.x = x;
		this.y = y;

		this.repetitions = new ArrayList<>();
		for (int i = 0; i < type.count; i++) {
			this.repetitions.add(new Repetition(type.delay + type.repeatDelay * i));
		}
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public InstantType getType() {
		return type;
	}

	public Projectile[] fire() {
		List<Projectile> projectiles = new ArrayList<>();

		for (Repetition rep : repetitions) {
			if (!rep.done) projectiles.addAll(rep.fire());
		}

		return projectiles.toArray(new Projectile[projectiles.size()]);
	}

	public boolean isDone() {
		return !repetitions.stream().anyMatch(r -> !r.done);
	}

	public class Repetition {

		private boolean done;

		private double[] times;
		private List<Entity> targeted;

		private Repetition(double time) {
			this.times = new double[type.projectiles.length];
			if (type.sync) {
				for (int i = 0; i < times.length; i++)
					times[i] = -1.0 / type.projectiles[i].rate - time;
			} else {
				double t = -time;
				for (int i = 0; i < times.length; i++) {
					times[i] = t;
					t -= 1.0 / type.projectiles[i].rate;
				}
			}
			this.targeted = new ArrayList<>();
		}

		private List<Projectile> fire() {
			List<Projectile> projectiles = new ArrayList<>();

			double rem = 0;

			for (int i = 0; i < times.length; i++) {
				if (times[i] <= Integer.MIN_VALUE) continue;

				Point2D sp = randomPoint(type.spread);
				final double sx = x + sp.getX();
				final double sy = y + sp.getY();

				Entity[] targets;
				switch (type.target) {
					case ENEMY:
						targets = Arrays.stream(GameLogic.getEntities()).filter(e -> e instanceof Enemy && GameLogic.distanceSq(e.getX(), x, e.getY(), y) < type.range * type.range && ((Enemy) e).getFutureHealth() >= 0 && (!type.unique || !targeted.contains(e))).sorted(new Comparator<Entity>() {

							@Override
							public int compare(Entity o1, Entity o2) {
								return Double.compare(((Enemy) o1).getNextNode().getCost(), ((Enemy) o2).getNextNode().getCost());
							}
						}).toArray(size -> new Entity[size]);
						break;
					case LOCATION:
						targets = new Entity[] {new Position(randomPoint(type.range))};
						break;
					case ROTATION:
						double t = 2 * Math.PI * GameLogic.rand.nextDouble();
						targets = new Entity[] {new Position(type.range * Math.cos(t), type.range * Math.sin(t))};
						break;
					case SPREAD:
						double z = Math.atan2(sp.getY(), sp.getX());
						targets = new Entity[] {new Position(type.range * Math.cos(z), type.range * Math.sin(z))};
						break;
					default:
						targets = new Entity[0];
						break;
				}
				if (targets.length <= 0) {
					if (times[i] > 0) times[i] = Integer.MIN_VALUE;
					break;
				}

				rem++;
				
				times[i] += GameLogic.FRAME_TIME;
				if (times[i] > 0) {
					times[i] = Integer.MIN_VALUE;
					projectiles.add(new Projectile(type.projectiles[i], Instant.this, targets[0], sx, sy));
					targeted.add(targets[0]);
				}
			}

			this.done = rem <= 0;

			return projectiles;
		}
	}

	/**
	 * <ul>
	 * <li><b><i>randomPoint</i></b><br>
	 * <br>
	 * {@code Point2D randomPoint()}<br>
	 * <br>
	 * @param radius - The radius to generate points within.
	 * @return A pseudo-random point within a circle centered at the origin with the given radius.
	 *         </ul>
	 */
	public static Point2D randomPoint(double radius) {
		double t = GameLogic.rand.nextDouble() * Math.PI * 2;
		double r = (GameLogic.rand.nextDouble() + GameLogic.rand.nextDouble()) * radius;
		if (r > radius) r = radius * 2 - r;

		return new Point2D.Double(r * Math.cos(t), r * Math.sin(t));
	}

}
