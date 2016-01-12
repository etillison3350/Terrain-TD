package terraintd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Timer;

import terraintd.object.CollidableEntity;
import terraintd.object.Enemy;
import terraintd.object.Entity;
import terraintd.object.Gun;
import terraintd.object.Obstacle;
import terraintd.object.Projectile;
import terraintd.object.Tower;
import terraintd.object.Weapon;
import terraintd.pathfinder.Node;
import terraintd.pathfinder.PathFinder;
import terraintd.types.CollidableType;
import terraintd.types.DeliveryType;
import terraintd.types.EnemyType;
import terraintd.types.Level;
import terraintd.types.ObstacleType;
import terraintd.types.TowerType;
import terraintd.types.World;
import terraintd.window.GamePanel;
import terraintd.window.Window;

public class GameLogic implements ActionListener {

	/**
	 * <ul>
	 * <li><b><i>FRAME_RATE</i></b><br>
	 * <br>
	 * {@code public static final int FRAME_RATE}<br>
	 * <br>
	 * The frame rate of GameLogic objects in fps (the number of calculations per second)<br>
	 * </ul>
	 */
	public static final int FRAME_RATE = 40;

	/**
	 * <ul>
	 * <li><b><i>FRAME_TIME</i></b><br>
	 * <br>
	 * {@code public static final double FRAME_TIME}<br>
	 * <br>
	 * The length of a frame for GameLogic objects in seconds (the number of seconds between calculations)<br>
	 * </ul>
	 */
	public static final double FRAME_TIME = 1.0 / FRAME_RATE;

	private final Window window;
	private final GamePanel panel;

	private Entity[] permanentEntities;

	private ArrayList<Projectile> projectiles;

	private Level currentLevel;
	private World currentWorld;
	protected final PathFinder pathFinder;
	private HashMap<EnemyType, Node[][][]> nodes = new HashMap<>(EnemyType.values().length);

	private int money;
	private double health, maxHealth;

	private final Timer timer = new Timer((int) (1000 * FRAME_TIME), this);

	public GameLogic(GamePanel p, Window w) {
		this.panel = p;
		this.window = w;

		this.pathFinder = new PathFinder(this);

		this.reset();
	}

	public void start() {
		if (!this.timer.isRunning())
			this.timer.start();
	}

	public void stop() {
		this.timer.stop();
	}

	public void reset() {
		this.timer.stop();
		this.money = 1000;
		this.health = this.maxHealth = 10000;

		this.currentWorld = World.values()[0];
		this.currentLevel = new Level();

		// TODO init enemies
		this.permanentEntities = new Entity[] {};
		this.projectiles = new ArrayList<>();

		for (EnemyType type : EnemyType.values()) {
			this.nodes.put(type, this.pathFinder.calculatePaths(type));
		}

		this.panel.repaint();
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
	 *         <li>1: the time to process "permanent" objects, i.e. towers, enemies, and obstacles</li>
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

	private boolean wasPaused = true;
	private CollidableType buying = null;

	/**
	 * <ul>
	 * <li><b><i>buyObject</i></b><br>
	 * <br>
	 * {@code public void buyObject(CollidableType type)}<br>
	 * <br>
	 * Called when the buy button of <code>type</code> is pressed<br>
	 * @param type
	 *        </ul>
	 */
	public void buyObject(CollidableType type) {
		this.wasPaused = !this.timer.isRunning();
		this.stop();
		buying = type;
		window.info.setDisplayedType(type);
	}

	/**
	 * <ul>
	 * <li><b><i>buyObject</i></b><br>
	 * <br>
	 * {@code public void buyObject(int x, int y)}<br>
	 * <br>
	 * Purchases the given object and positions it at the given position<br>
	 * @param x
	 * @param y
	 *        </ul>
	 */
	public void buyObject(int x, int y) {
		Entity[] newEntities = new Entity[permanentEntities.length + 1];
		System.arraycopy(permanentEntities, 0, newEntities, 0, permanentEntities.length);
		newEntities[permanentEntities.length] = buying instanceof ObstacleType ? new Obstacle((ObstacleType) buying, x, y) : new Tower((TowerType) buying, x, y);
		permanentEntities = newEntities;

		for (EnemyType type : EnemyType.values())
			this.nodes.put(type, this.pathFinder.calculatePaths(type));
		
		this.money -= buying.cost;

		cancelBuy();
		panel.repaint();
		window.info.setDisplayedType(null);
		window.buy.updateButtons();
	}

	public void cancelBuy() {
		buying = null;
		if (!this.wasPaused)
			this.start();
		window.info.setDisplayedType(null);
	}

	public boolean canPlaceObject(CollidableType type, int x, int y) {
		if (x < 0 || y < 0 || x > currentWorld.getWidth() - type.width || y > currentWorld.getHeight() - type.height) return false;

		for (Entity e : permanentEntities) {
			if (!(e instanceof CollidableEntity)) continue;

			if (((CollidableEntity) e).getRectangle().intersects(x, y, type.width, type.height)) return false;
		}

		if (type instanceof TowerType) {
			TowerType tower = (TowerType) type;

			int e = currentWorld.tiles[y][x].elev;
			for (int xx = 0; xx < type.width; xx++) {
				for (int yy = 0; yy < type.height; yy++) {
					if (!tower.terrain.get(currentWorld.tiles[yy + y][xx + x].terrain)) return false;
					if (!tower.onHill && currentWorld.tiles[yy + y][xx + x].elev != e) return false;
				}
			}
		}

		return true;
	}

	public Level getCurrentLevel() {
		return currentLevel;
	}

	public World getCurrentWorld() {
		return currentWorld;
	}

	public Node[][][] getNodes(EnemyType type) {
		return nodes.get(type);
	}
	
	public int getMoney() {
		return money;
	}

	public double getHealth() {
		return this.health;
	}

	public double getMaxHealth() {
		return this.maxHealth;
	}

	public Entity[] getPermanentEntities() {
		return permanentEntities;
	}
	
	public Projectile[] getProjectiles() {
		return projectiles.toArray(new Projectile[projectiles.size()]);
	}

	public CollidableType getBuyingType() {
		return buying;
	}

	public boolean isPaused() {
		return !this.timer.isRunning();
	}

}
