package terraintd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import terraintd.object.Enemy;
import terraintd.object.Entity;
import terraintd.object.Gun;
import terraintd.object.Projectile;
import terraintd.object.Weapon;
import terraintd.pathfinder.PathFinder;
import terraintd.types.DeliveryType;
import terraintd.types.Level;
import terraintd.types.World;
import terraintd.window.GamePanel;

public class GameLogic implements ActionListener {

	/**
	 * <ul>
	 * <li><b><i>FRAME_RATE</i></b><br>
	 * <br>
	 * {@code public static final int FRAME_RATE}<br>
	 * <br>
	 * The frame rate of GameLogic objects (the number of calculations per second)<br>
	 * </ul>
	 */
	public static final int FRAME_RATE = 40;

	/**
	 * <ul>
	 * <li><b><i>FRAME_TIME</i></b><br>
	 * <br>
	 * {@code public static final double FRAME_TIME}<br>
	 * <br>
	 * The length of a frame for GameLogic objects (the number of seconds between calculations)<br>
	 * </ul>
	 */
	public static final double FRAME_TIME = 1.0 / FRAME_RATE;

	private final GamePanel panel;

	private Entity[] permanentEntities;

	private ArrayList<Projectile> projectiles;

	private Level currentLevel;
	private World currentWorld;
	protected final PathFinder pathFinder;

	private int money;

	private final Timer timer = new Timer(1000 / FRAME_RATE, this);

	public GameLogic(GamePanel p) {
		this.panel = p;

		this.pathFinder = new PathFinder(this);
		
		this.reset();
	}

	public void start() {
		this.timer.start();
	}

	public void stop() {
		this.timer.stop();
	}

	public void reset() {
		this.timer.stop();
		this.money = 1000;

		this.currentWorld = World.values()[0];
		this.currentLevel = new Level();

		// TODO init enemies
		this.permanentEntities = new Entity[] {};
		
		this.pathFinder.calculatePaths();
		this.currentWorld.setNode(this.pathFinder.getNodes()[4][4][0]);
	}

	/**
	 * Runs a single frame
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		t0 = System.nanoTime();

		processPermanents();

		t1 = System.nanoTime();

		processProjectiles();

		t2 = System.nanoTime();

		GameLogic.this.panel.repaint();
	}

	private long t0, t1, t2;

	/**
	 * <ul>
	 * <li><b><i>getExecutionTimes</i></b><br>
	 * <br>
	 * {@code public long[] getExecutionTimes()}<br>
	 * <br>
	 * @return an array of three <code>long</code>s, representing:
	 *         <ul>
	 *         <li>0: the total time</li>
	 *         <li>1: the time to process "permanent" objects, i.e. towers, enemies, and obstacles (although there is no logic
	 *         associated with them)</li>
	 *         <li>2: the time to process projectiles</li>
	 *         </ul>
	 *         of the last frame/cycle of this logic, in nanoseconds.</li>
	 *         </ul>
	 */
	public long[] getExecutionTimes() {
		return new long[] {t2 - t0, t1 - t0, t2 - t1};
	}

	private void processPermanents() {
		for (Entity e : permanentEntities) {
			if (e instanceof Weapon) {
				Gun g = ((Weapon) e).getGun();
				if (g != null) {
					for (Projectile p : ((Weapon) e).convertFromTempProjectiles(g.fire())) {
						this.projectiles.add(p);
					}
				}
			}

			if (e instanceof Enemy) {
				Enemy enemy = (Enemy) e;

				if (!enemy.move()) {
					// TODO Damage
				}
			}
		}
	}

	private void processProjectiles() {
		Projectile[] projectiles = this.projectiles.toArray(new Projectile[this.projectiles.size()]);
		for (Projectile p : projectiles) {
			if (p.getDeathTime() >= 0) {
				if (!p.fade()) this.projectiles.remove(p);
				continue;
			}

			if (!p.move()) { // The projectile is "dead"
				p.fade();

				if (p.type.delivery == DeliveryType.SINGLE_TARGET) {
					if (p.type.follow && p.getTarget() instanceof Enemy) {
						this.money += ((Enemy) p.getTarget()).damage(p.type.damage);
					}

					if (p.type.explodeRadius > 0.00001) {
						for (Entity e : permanentEntities) {
							if (!(e instanceof Enemy)) continue;

							Enemy enemy = (Enemy) e;

							double dx = e.getX() - p.getX();
							double dy = e.getY() - p.getX();

							if (dy * dy + dx * dx <= p.type.explodeRadius * p.type.explodeRadius) {
								this.money += enemy.damage(p.damageForEntity(enemy));
							}
						}
					}
				} else {
					for (Entity e : permanentEntities) {
						if (!(e instanceof Enemy)) continue;

						double dx = e.getX() - p.getX();
						double dy = e.getY() - p.getX();

						if (dy * dy + dx * dx > p.type.maxDist * p.type.maxDist) continue;

						boolean damage = false;

						if (p.getHitTargets().contains(e)) continue;

						switch (p.type.delivery) {
							case AREA:
								damage = true;
								break;
							case LINE:
								damage = lineCollides(e, p, 1.25);
								break;
							case SECTOR:
								damage = sectorCollides(e, p);
								break;
							default:
								break;
						}

						if (damage) {
							this.money += ((Enemy) e).damage(p.damageForEntity(e));
							p.hitTarget(e);
						}
					}
				}
			}
		}
	}

//	public HashMap<Enemy, double[]> getEnemiesInRange(double x, double y, double range) {
//		HashMap<Enemy, double[]> enemies = new HashMap<>();
//
//		for (Entity e : permanentEntities) {
//			if (e instanceof Enemy) {
//				double dx = e.getX() - x;
//				double dy = e.getY() - y;
//
//				if (dx * dx + dy * dy > range * range) {
//					enemies.put((Enemy) e, new double[] {e.getX(), e.getY()});
//				}
//			}
//		}
//
//		return enemies;
//	}

	private static boolean lineCollides(Entity e, Projectile p, double radius) {
		double cx1, cx2, cy1, cy2, pta = p.getRotation();
		if (pta % Math.PI < Math.PI / 2) {
			cx1 = e.getX() - radius;
			cx2 = e.getX() + radius;
			cy1 = e.getY() + radius;
			cy2 = e.getY() - radius;
		} else {
			cx1 = e.getX() - radius;
			cx2 = e.getX() + radius;
			cy1 = e.getY() - radius;
			cy2 = e.getY() + radius;
		}

		if (pta > Math.PI) pta -= Math.PI * 2;

		double ta1 = Math.atan2(cy1 - p.getY(), cx1 - p.getX());
		double ta2 = Math.atan2(cy2 - p.getY(), cx2 - p.getX());

		return (cx2 < p.getX() && Math.abs(ta1 + ta2) < Math.PI) != (ta2 > ta1 ? (ta1 < pta && pta < ta2) : (ta2 < pta && pta < ta1));
	}

	private static boolean sectorCollides(Entity e, Projectile p) {
		final double PI2 = 2 * Math.PI;

		double ta = Math.atan2(e.getY() - p.getY(), e.getX() - p.getX());

		double ta1 = p.getRotation() - p.type.angle * 0.5;
		double ta2 = p.getRotation() + p.type.angle * 0.5;

		ta = (PI2 + ta) % PI2;
		ta1 = (PI2 + ta1) % PI2;
		ta2 = (PI2 + ta2) % PI2;

		if (ta1 < ta2)
			return ta1 <= ta && ta <= ta2;

		return ta1 <= ta || ta <= ta2;
	}

	public Level getCurrentLevel() {
		return currentLevel;
	}

	public World getCurrentWorld() {
		return currentWorld;
	}

	public int getMoney() {
		return money;
	}

	public Entity[] getPermanentEntities() {
		return permanentEntities;
	}

}
