package terraintd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
import terraintd.types.Level.Unit;
import terraintd.types.ObstacleType;
import terraintd.types.TowerType;
import terraintd.types.World;
import terraintd.window.BuyPanel;
import terraintd.window.GamePanel;
import terraintd.window.InfoPanel;
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

	private static final GameLogic logic = new GameLogic();

	public final static Random rand = new Random();

	public static final Config cfg = new Config();

	private static State state = State.PLAYING;

	private static List<Entity> permanentEntities;

	private static List<Projectile> projectiles;

	private static Level currentLevel;
	private static World currentWorld;
	private static HashMap<EnemyType, Node[][][]> nodes = new HashMap<>(EnemyType.values().length);

	private static int money;
	private static double health, maxHealth;

	private static final Timer timer = new Timer((int) (1000 * FRAME_TIME), logic), pauseTimer = new Timer((int) (1000 * FRAME_TIME), logic);

	private static double timeToNextEnemy;
	private static int enemyIndex;

	private static long t0, t1, t2;

	private static boolean wasPaused = true;
	private static CollidableType buying = null;

	private static Entity selected = null;

	public static void start() {
		if (!timer.isRunning()) timer.start();
		pauseTimer.stop();
	}

	public static void stop() {
		timer.stop();
		if (!pauseTimer.isRunning()) pauseTimer.start();
	}

	public static void reset() {
		if (InfoPanel.infoPanel != null) Window.setButtonsEnabled(true);

		if (!isPaused()) Window.pauseGame.doClick();
		stop();

		Window.fastForward.setSelected(false);
		InfoPanel.fastForward.setSelected(false);
		setFastForward(false);

		state = State.PLAYING;

		currentWorld = World.values()[2];
		currentLevel = Level.values()[0];

		money = currentLevel.money;
		health = maxHealth = currentLevel.health;

		timeToNextEnemy = currentLevel.units[0].delay;
		enemyIndex = 0;

		permanentEntities = new ArrayList<>();
		projectiles = new ArrayList<>();

		for (EnemyType type : EnemyType.values())
			nodes.put(type, PathFinder.calculatePaths(type));

		selected = null;
		buying = null;

		Window.repaintWindow();
		if (BuyPanel.buyPanel != null) BuyPanel.updateButtons();
	}

	public static void setFastForward(boolean fastForward) {
		timer.setDelay((int) ((fastForward ? 500 : 1000) * FRAME_TIME));
	}

	/**
	 * Runs a single frame
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		if (e.getSource() == pauseTimer) {
			if (selected != null && selected instanceof CollidableEntity) {
				CollidableType type = ((CollidableEntity) selected).getType();
				GamePanel.repaintPanel((int) (GamePanel.getDx() + selected.getX() * GamePanel.getTile()), (int) (GamePanel.getDy() + selected.getY() * GamePanel.getTile()), (int) (GamePanel.getTile() * type.width), (int) (GamePanel.getTile() * type.height));
			}
		} else {
			t0 = System.nanoTime();
			processPermanents();
			t1 = System.nanoTime();
			processProjectiles();
			t2 = System.nanoTime();
			GamePanel.repaintPanel();
			BuyPanel.updateButtons();
			InfoPanel.refreshDisplay();
		}

	}

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
	public static long[] getExecutionTimes() {
		return new long[] {t2 - t0, t1 - t0, t2 - t1};
	}

	private static void processPermanents() {
		if (health <= 0) {
			health = 0;
			if (!isPaused()) Window.pauseGame.doClick();
			stop();
			Window.fastForward.setSelected(false);
			InfoPanel.fastForward.setSelected(false);
			setFastForward(false);
			state = State.FAILED;
			BuyPanel.updateButtons();
			InfoPanel.paintHealthBar();
			Window.setButtonsEnabled(false);
			GamePanel.repaintPanel();
		} else if (enemyIndex == currentLevel.units.length && !permanentEntities.stream().anyMatch(e -> e instanceof Enemy)) {
			if (!isPaused()) Window.pauseGame.doClick();
			stop();
			Window.fastForward.setSelected(false);
			InfoPanel.fastForward.setSelected(false);
			setFastForward(false);
			state = State.COMPLETE;
			BuyPanel.updateButtons();
			Window.setButtonsEnabled(false);
			GamePanel.repaintPanel();
		}

		timeToNextEnemy -= FRAME_TIME;
		while (timeToNextEnemy < 0 && enemyIndex < currentLevel.units.length) {
			EnemyType et = typeOfUnit(currentLevel.units[enemyIndex]);
			if (et == null || enemyIndex == currentLevel.units.length) continue;

			ArrayList<Node> spawnPoints = new ArrayList<>();
			Node[][][] nodes = GameLogic.nodes.get(et);

			for (Node n : currentWorld.spawnpoints) {
				if (nodes[n.y][n.x][n.top ? 1 : 0].getNextNode() != null) spawnPoints.add(nodes[n.y][n.x][n.top ? 1 : 0]);
			}

			if (spawnPoints.size() == 0) continue; // TODO Remove obstacles?

			permanentEntities.add(new Enemy(et, spawnPoints.get(rand.nextInt(spawnPoints.size())), currentWorld));

			timeToNextEnemy += currentLevel.units[enemyIndex++].delay;
		}

		Entity[] permanents = permanentEntities.toArray(new Entity[permanentEntities.size()]);
		for (Entity e : permanents) {
			if (e instanceof Weapon) {
				Gun g = ((Weapon) e).getGun();
				if (g != null) {
					double minCost = Float.MAX_VALUE;
					Enemy target = null;

					for (Entity entity : permanents) {
						if (!(entity instanceof Enemy)) continue;

						Enemy enemy = (Enemy) entity;
						if (enemy.getFutureHealth() < 0) continue;

						double dx = enemy.getX() - g.shooter.getX();
						double dy = enemy.getY() - g.shooter.getY();

						if (dx * dx + dy * dy < g.range * g.range && enemy.getNextNode().getCost() < minCost) {
							target = enemy;
							minCost = enemy.getNextNode().getCost();
						}
					}

					((Weapon) e).target(target);

					for (Projectile p : ((Weapon) e).createProjectiles(g.fire())) {
						if (p.type.delivery == DeliveryType.SINGLE_TARGET && p.type.follow) p.getTarget().damageFuture(p);

						projectiles.add(p);
					}
				}
			}

			if (e instanceof Enemy) {
				Enemy enemy = (Enemy) e;
				if (enemy.getDead() != 0) {
					if (enemy.die()) permanentEntities.remove(enemy);
					continue;
				}

				if (!enemy.move()) {
					health -= enemy.getDamage();
					enemy.damage(Float.MAX_VALUE);
					InfoPanel.paintHealthBar();
				}

				if (enemy.damage(0)) {
					money += enemy.type.reward;
				}
			}
		}
	}

	private static void processProjectiles() {
		Entity[] permanents = permanentEntities.toArray(new Entity[permanentEntities.size()]);
		Projectile[] projectiles = GameLogic.projectiles.toArray(new Projectile[GameLogic.projectiles.size()]);
		for (Projectile p : projectiles) {
			if (p.getDeathTime() >= 0) {
				if (!p.fade()) GameLogic.projectiles.remove(p);
				continue;
			}

			if (!p.move()) { // The projectile is "dead"
				p.fade();

				if (p.type.delivery == DeliveryType.SINGLE_TARGET) {
					if (p.type.follow) {
						if (p.getTarget().damage(p)) {
							money += p.getTarget().type.reward;
						}
					}

					if (p.type.explodeRadius > 0.00001) {
						for (Entity e : permanents) {
							if (!(e instanceof Enemy) || (p.type.follow && p.getTarget() == e)) continue;

							Enemy enemy = (Enemy) e;

							double dx = e.getX() - p.getX();
							double dy = e.getY() - p.getX();

							if (dy * dy + dx * dx <= p.type.explodeRadius * p.type.explodeRadius) {
								if (enemy.damage(p)) {
									money += enemy.type.reward;
								}
							}
						}
					}
				}
			}

			if (p.type.delivery != DeliveryType.SINGLE_TARGET) {
				for (Entity e : permanents) {
					if (!(e instanceof Enemy)) continue;

					double dx = e.getX() - p.getX();
					double dy = e.getY() - p.getY();

					if (dy * dy + dx * dx > p.getRadius() * p.getRadius()) continue;

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
						if (((Enemy) e).damage(p)) {
							money += ((Enemy) e).type.reward;
						}
						p.hitTarget((Enemy) e);
					}
				}
			}
		}
	}

	private static EnemyType typeOfUnit(Unit unit) {
		return EnemyType.getTypeForId(unit.typeId);
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

		if (ta1 < ta2) return ta1 <= ta && ta <= ta2;

		return ta1 <= ta || ta <= ta2;
	}

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
	public static void buyObject(CollidableType type) {
		wasPaused = !timer.isRunning();
		stop();
		buying = type;
		InfoPanel.setDisplayedObject(type);
		selected = null;
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
	public static void buyObject(int x, int y) {
		permanentEntities.add(buying instanceof ObstacleType ? new Obstacle((ObstacleType) buying, x, y) : new Tower((TowerType) buying, x, y));

		for (EnemyType type : EnemyType.values())
			nodes.put(type, PathFinder.calculatePaths(type));

		Entity[] permanents = permanentEntities.toArray(new Entity[permanentEntities.size()]);
		for (Entity e : permanents) {
			if (!(e instanceof Enemy)) continue;

			((Enemy) e).resetNodes(nodes.get(((Enemy) e).type));
		}

		money -= buying.cost;

		cancelBuy();
		GamePanel.repaintPanel();
		BuyPanel.updateButtons();

		setSelectedEntity(permanentEntities.get(permanentEntities.size() - 1));
	}

	public static void cancelBuy() {
		buying = null;
		if (!wasPaused) start();
		InfoPanel.setDisplayedObject(null);
	}

	public static Entity getSelectedEntity() {
		return selected;
	}

	public static void setSelectedEntity(Entity selected) {
		GameLogic.selected = selected;

		if (selected instanceof CollidableEntity) {
			InfoPanel.setDisplayedObject(selected);
		} else if (selected == null) {
			InfoPanel.setDisplayedObject(null);
		}
	}

	public static boolean canPlaceObject(CollidableType type, int x, int y) {
		if (x < 0 || y < 0 || x > currentWorld.getWidth() - type.width || y > currentWorld.getHeight() - type.height) return false;

		if (currentWorld.goal.x >= x && currentWorld.goal.x - x - (currentWorld.goal.top ? 0 : 1) < type.width && currentWorld.goal.y >= y && currentWorld.goal.y - y - (currentWorld.goal.top ? 1 : 0) < type.height) return false;

		Entity[] permanents = permanentEntities.toArray(new Entity[permanentEntities.size()]);
		for (Entity e : permanents) {
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

	public static Level getCurrentLevel() {
		return currentLevel;
	}

	public static World getCurrentWorld() {
		return currentWorld;
	}

	public static Node[][][] getNodes(EnemyType type) {
		return nodes.get(type);
	}

	public static int getMoney() {
		return money;
	}

	public static double getHealth() {
		return health;
	}

	public static double getMaxHealth() {
		return maxHealth;
	}

	public static Entity[] getPermanentEntities() {
		return permanentEntities.toArray(new Entity[permanentEntities.size()]);
	}

	public static Projectile[] getProjectiles() {
		return projectiles.toArray(new Projectile[projectiles.size()]);
	}

	public static CollidableType getBuyingType() {
		return buying;
	}

	public static boolean isPaused() {
		return timer == null ? true : !timer.isRunning();
	}

	public static enum State {
		PLAYING,
		COMPLETE,
		FAILED;

		public String toString() {
			return Language.get("level-" + this.name().toLowerCase());
		}
	}

	public static State getState() {
		return state;
	}

}
