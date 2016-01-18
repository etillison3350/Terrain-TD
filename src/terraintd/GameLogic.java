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
import terraintd.types.Language;
import terraintd.types.Level;
import terraintd.types.Level.Unit;
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

	public final Random rand = new Random();

	private final Window window;
	private final GamePanel panel;

	private State state = State.PLAYING;
	
	private List<Entity> permanentEntities;

	private List<Projectile> projectiles;

	private Level currentLevel;
	private World currentWorld;
	protected final PathFinder pathFinder;
	private HashMap<EnemyType, Node[][][]> nodes = new HashMap<>(EnemyType.values().length);

	private int money;
	private double health, maxHealth;

	private final Timer timer = new Timer((int) (1000 * FRAME_TIME), this), pauseTimer = new Timer((int) (1000 * FRAME_TIME), this);

	private double timeToNextEnemy;
	private int enemyIndex;

	private long t0, t1, t2;

	private boolean wasPaused = true;
	private CollidableType buying = null;

	private Entity selected = null;

	public GameLogic(GamePanel p, Window w) {
		this.panel = p;
		this.window = w;

		this.pathFinder = new PathFinder(this);

		this.reset();
	}

	public void start() {
		if (!this.timer.isRunning()) this.timer.start();
		this.pauseTimer.stop();
	}

	public void stop() {
		this.timer.stop();
		if (!this.pauseTimer.isRunning()) this.pauseTimer.start();
	}

	public void reset() {
		if (this.window.info != null) this.window.setButtonsEnabled(true);

		if (!this.isPaused()) this.window.pauseGame.doClick();
		this.stop();
		
		if (this.ff) this.window.fastForward.doClick();
		this.setFastForward(false);
		
		this.state = State.PLAYING;
		
		this.currentWorld = World.values()[2];
		this.currentLevel = Level.values()[0];

		this.money = this.currentLevel.money;
		this.health = this.maxHealth = this.currentLevel.health;
		
		this.timeToNextEnemy = currentLevel.units[0].delay;
		this.enemyIndex = 0;

		this.permanentEntities = new ArrayList<>();
		this.projectiles = new ArrayList<>();

		for (EnemyType type : EnemyType.values())
			this.nodes.put(type, this.pathFinder.calculatePaths(type));

		this.selected = null;
		
		this.window.repaint();
		if (this.window.buy != null) this.window.buy.updateButtons();
	}
	
	private boolean ff;
	
	public void setFastForward(boolean fastForward) {
		this.ff = fastForward;
		this.timer.setDelay((int) ((fastForward ? 500 : 1000) * FRAME_TIME));
	}

	/**
	 * Runs a single frame
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		if (e.getSource() == pauseTimer) {
			if (selected != null && selected instanceof CollidableEntity) {
				CollidableType type = ((CollidableEntity) selected).getType();
				this.panel.repaint((int) (panel.getDx() + selected.getX() * panel.getTile()), (int) (panel.getDy() + selected.getY() * panel.getTile()), (int) (panel.getTile() * type.width), (int) (panel.getTile() * type.height));
			}
		} else {
			t0 = System.nanoTime();
			processPermanents();
			t1 = System.nanoTime();
			processProjectiles();
			t2 = System.nanoTime();
			this.panel.repaint();
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
	public long[] getExecutionTimes() {
		return new long[] {t2 - t0, t1 - t0, t2 - t1};
	}

	private void processPermanents() {
		if (this.health <= 0) {
			this.health = 0;
			if (!this.isPaused()) window.pauseGame.doClick();
			this.stop();
			this.state = State.FAILED;
			this.window.buy.updateButtons();
			this.window.info.paintHealthBar();
			this.window.setButtonsEnabled(false);
			this.panel.repaint();
		} else if (enemyIndex == currentLevel.units.length && !permanentEntities.stream().anyMatch(e -> e instanceof Enemy)) {
			if (!this.isPaused()) window.pauseGame.doClick();
			this.stop();
			this.state = State.COMPLETE;
			this.window.buy.updateButtons();
			this.window.setButtonsEnabled(false);
			this.panel.repaint();
		}
		
		timeToNextEnemy -= FRAME_TIME;
		while (timeToNextEnemy < 0 && enemyIndex < currentLevel.units.length) {
			EnemyType et = typeOfUnit(currentLevel.units[enemyIndex]);
			if (et == null || enemyIndex == currentLevel.units.length) continue;

			ArrayList<Node> spawnPoints = new ArrayList<>();
			Node[][][] nodes = this.nodes.get(et);

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

						this.projectiles.add(p);
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
					this.health -= enemy.type.damage;
					enemy.damage(Float.MAX_VALUE);
					window.info.paintHealthBar();
				}
			}
		}
	}

	private void processProjectiles() {
		Entity[] permanents = permanentEntities.toArray(new Entity[permanentEntities.size()]);
		Projectile[] projectiles = this.projectiles.toArray(new Projectile[this.projectiles.size()]);
		for (Projectile p : projectiles) {
			if (p.getDeathTime() >= 0) {
				if (!p.fade()) this.projectiles.remove(p);
				continue;
			}

			if (!p.move()) { // The projectile is "dead"
				p.fade();

				if (p.type.delivery == DeliveryType.SINGLE_TARGET) {
					if (p.type.follow) {
						if (p.getTarget().damage(p)) {
							this.money += p.getTarget().type.reward;
							this.window.buy.updateButtons();
						}
						this.window.info.refreshDisplay();
					}

					if (p.type.explodeRadius > 0.00001) {
						for (Entity e : permanents) {
							if (!(e instanceof Enemy) || (p.type.follow && p.getTarget() == e)) continue;

							Enemy enemy = (Enemy) e;

							double dx = e.getX() - p.getX();
							double dy = e.getY() - p.getX();

							if (dy * dy + dx * dx <= p.type.explodeRadius * p.type.explodeRadius) {
								if (enemy.damage(p)) {
									this.money += enemy.type.reward;
									this.window.buy.updateButtons();
								}
								this.window.info.refreshDisplay();
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
							this.money += ((Enemy) e).type.reward;
							this.window.buy.updateButtons();
						}
						this.window.info.refreshDisplay();
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
	public void buyObject(CollidableType type) {
		this.wasPaused = !this.timer.isRunning();
		this.stop();
		buying = type;
		window.info.setDisplayedObject(type);
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
	public void buyObject(int x, int y) {
		permanentEntities.add(buying instanceof ObstacleType ? new Obstacle((ObstacleType) buying, x, y) : new Tower((TowerType) buying, x, y));

		for (EnemyType type : EnemyType.values())
			this.nodes.put(type, this.pathFinder.calculatePaths(type));

		Entity[] permanents = permanentEntities.toArray(new Entity[permanentEntities.size()]);
		for (Entity e : permanents) {
			if (!(e instanceof Enemy)) continue;

			((Enemy) e).resetNodes(this.nodes.get(((Enemy) e).type));
		}

		this.money -= buying.cost;

		cancelBuy();
		panel.repaint();
		window.buy.updateButtons();

		setSelectedEntity(permanentEntities.get(permanentEntities.size() - 1));
	}

	public void cancelBuy() {
		buying = null;
		if (!this.wasPaused) this.start();
		window.info.setDisplayedObject(null);
	}

	public Entity getSelectedEntity() {
		return selected;
	}

	public void setSelectedEntity(Entity selected) {
		this.selected = selected;

		if (selected instanceof CollidableEntity) {
			window.info.setDisplayedObject(selected);
		} else if (selected == null) {
			window.info.setDisplayedObject(null);
		}
	}

	public boolean canPlaceObject(CollidableType type, int x, int y) {
		if (x < 0 || y < 0 || x > currentWorld.getWidth() - type.width || y > currentWorld.getHeight() - type.height) return false;

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
		return permanentEntities.toArray(new Entity[permanentEntities.size()]);
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
	
	public enum State {
		PLAYING,
		COMPLETE,
		FAILED;
		
		public String toString() {
			return Language.get("level-" + this.name().toLowerCase());
		}
	}

	public State getState() {
		return state;
	}

}
