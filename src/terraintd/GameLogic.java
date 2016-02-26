package terraintd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.swing.Timer;

import terraintd.files.Config;
import terraintd.files.JSON;
import terraintd.object.CollidableEntity;
import terraintd.object.Enemy;
import terraintd.object.Entity;
import terraintd.object.Gun;
import terraintd.object.Instant;
import terraintd.object.Obstacle;
import terraintd.object.Projectile;
import terraintd.object.StatusEffect;
import terraintd.object.Tower;
import terraintd.object.Weapon;
import terraintd.pathfinder.Node;
import terraintd.pathfinder.PathFinder;
import terraintd.types.CollidableType;
import terraintd.types.DeliveryType;
import terraintd.types.EnemyType;
import terraintd.types.InstantType;
import terraintd.types.Level;
import terraintd.types.LevelSet;
import terraintd.types.ObstacleType;
import terraintd.types.ProjectileType;
import terraintd.types.Purchasable;
import terraintd.types.StatusEffectType;
import terraintd.types.TargetType;
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

	private static boolean saved = true;
	private static Path lastSaveLocation = null;

	private static List<Entity> entities;
	private static List<Projectile> projectiles;

	private static LevelSet currentLevelSet;
	private static int levelIndex;
	private static World currentWorld;
	private static HashMap<EnemyType, Node[][][]> nodes = new HashMap<>(EnemyType.values().length);

	private static int money;
	private static double health, maxHealth;

	private static final Timer timer = new Timer((int) (1000 * FRAME_TIME), logic), pauseTimer = new Timer((int) (1000 * FRAME_TIME), logic);

	private static double timeToNextEnemy;
	private static int enemyIndex;

	private static long t0, t1, t2;

	private static boolean wasPaused = true;
	private static boolean pauseOnBuy = true;
	private static Purchasable buying = null;

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

		stop();

		Window.selectButton(0);
		setSpeed(1);

		state = State.PLAYING;

		saved = true;
		lastSaveLocation = null;

		currentWorld = World.values()[5];
		levelIndex = 0;
		currentLevelSet = LevelSet.values()[0];

		money = currentLevelSet.levels[levelIndex].money;
		health = maxHealth = currentLevelSet.health;

		timeToNextEnemy = currentLevelSet.levels[levelIndex].units[0].delay;
		enemyIndex = 0;

		entities = new ArrayList<>();
		projectiles = new ArrayList<>();

		for (EnemyType type : EnemyType.values())
			nodes.put(type, PathFinder.calculatePaths(type));

		selected = null;
		buying = null;

		GamePanel.resetView();
		Window.repaintWindow();
		Window.updateLevel();
		InfoPanel.setDisplayedObject(null);
		if (BuyPanel.buyPanel != null) BuyPanel.updateButtons();
	}

	protected static void nextLevel() {
		levelIndex++;

		if (InfoPanel.infoPanel != null) Window.setButtonsEnabled(true);

		state = State.PLAYING;

		money += currentLevelSet.levels[levelIndex].money;

		timeToNextEnemy = currentLevelSet.levels[levelIndex].units[0].delay;
		enemyIndex = 0;

		projectiles = new ArrayList<>();

		wasPaused = true;

		Window.repaintWindow();
		Window.updateLevel();
		if (BuyPanel.buyPanel != null) BuyPanel.updateButtons();
	}

	public static void setSpeed(double mult) {
		timer.setDelay((int) (FRAME_TIME * 1000.0 / mult));
	}

	/**
	 * Runs a single frame
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		if (e.getSource() == pauseTimer) {
			if (selected != null) {
				Rectangle2D rect = selected.getRectangle();
				GamePanel.repaintPanel((int) (GamePanel.getDx() + rect.getX() * GamePanel.getTile()), (int) (GamePanel.getDy() + rect.getY() * GamePanel.getTile()), (int) (GamePanel.getTile() * rect.getWidth()), (int) (GamePanel.getTile() * rect.getHeight()));
			}
		} else {
			saved = false;
			t0 = System.nanoTime();
			processEntities();
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
	 *         <li>1: the time to process entities</li>
	 *         <li>2: the time to process projectiles</li>
	 *         </ul>
	 *         of the last frame/cycle of this logic, in nanoseconds.</li>
	 *         </ul>
	 */
	public static long[] getExecutionTimes() {
		return new long[] {t2 - t0, t1 - t0, t2 - t1};
	}

	private static final Runnable nextLevel = new Runnable() {

		@Override
		public void run() {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {}
			if (!overrideNext) nextLevel();
			overrideNext = false;
		}
	};

	private static boolean overrideNext = false;

	public static void overrideNextLevel() {
		overrideNext = true;
		nextLevel();
	}

	private static void processEntities() {
		if (health <= 0) {
			health = 0;
			stop();
			Window.selectButton(0);
			setSpeed(1);
			state = State.FAILED;
			BuyPanel.updateButtons();
			InfoPanel.paintHealthBar();
			Window.setButtonsEnabled(false);
			GamePanel.repaintPanel();
			return;
		} else if (enemyIndex == currentLevelSet.levels[levelIndex].units.length && !entities.stream().anyMatch(e -> e instanceof Enemy)) {
			stop();
			Window.selectButton(0);
			setSpeed(1);
			BuyPanel.updateButtons();
			Window.setButtonsEnabled(false);
			GamePanel.repaintPanel();
			if (currentLevelSet.levels.length - 1 == levelIndex) {
				state = State.WON;
			} else {
				new Thread(nextLevel).start();
				state = State.COMPLETE;
			}
			return;
		}

		timeToNextEnemy -= FRAME_TIME;
		while (timeToNextEnemy < 0 && enemyIndex < currentLevelSet.levels[levelIndex].units.length) {
			EnemyType et = EnemyType.valueOf(currentLevelSet.levels[levelIndex].units[enemyIndex].typeId);
			if (et == null || enemyIndex == currentLevelSet.levels[levelIndex].units.length) continue;

			ArrayList<Node> spawnPoints = new ArrayList<>();
			Node[][][] nodes = GameLogic.nodes.get(et);

			for (Node n : currentWorld.spawnpoints) {
				if (nodes[n.y][n.x][n.top ? 1 : 0].getNextNode() != null) spawnPoints.add(nodes[n.y][n.x][n.top ? 1 : 0]);
			}

			if (spawnPoints.size() == 0) continue; // TODO Remove obstacles?

			entities.add(new Enemy(et, spawnPoints.get(rand.nextInt(spawnPoints.size()))));

			timeToNextEnemy += currentLevelSet.levels[levelIndex].units[enemyIndex++].delay;
		}

		if (selected != null && !entities.contains(selected)) {
			setSelectedEntity(null);
			InfoPanel.refreshDisplay();
		} else if (selected instanceof Enemy) {
			InfoPanel.refreshDisplay();
		}

		Entity[] ents = entities.toArray(new Entity[entities.size()]);
		for (Entity e : ents) {
			if (e instanceof Weapon) {
				Gun g = ((Weapon) e).getGun();
				if (g != null) {
					double min = !g.getTargetType().max ? Float.MAX_VALUE : Integer.MIN_VALUE;
					Enemy target = null;

					for (Entity entity : ents) {
						if (!(entity instanceof Enemy)) continue;

						Enemy enemy = (Enemy) entity;
						if (enemy.getFutureHealth() < 0) continue;

						final double d = distanceSq(enemy.getX(), g.shooter.getX(), enemy.getY(), g.shooter.getY());
						if (d > g.range * g.range) continue;

						switch (g.getTargetType()) {
							case FARTHEST:
								if (d > min) {
									min = d;
									target = enemy;
								}
								break;
							case FIRST:
								if (enemy.getNextNode().getCost() < min) {
									min = enemy.getNextNode().getCost();
									target = enemy;
								}
								break;
							case LAST:
								if (enemy.getNextNode().getCost() > min) {
									min = enemy.getNextNode().getCost();
									target = enemy;
								}
								break;
							case NEAREST:
								if (d < min) {
									min = d;
									target = enemy;
								}
								break;
							case STRONGEST:
								if (enemy.getHealth() > min) {
									min = enemy.getHealth();
									target = enemy;
								}
								break;
							case VALUABLEST:
								if (enemy.type.reward > min) {
									min = enemy.type.reward;
									target = enemy;
								}
								break;
							case WEAKEST:
								if (enemy.getHealth() < min) {
									min = enemy.getHealth();
									target = enemy;
								}
								break;
							case WORTHLESSEST:
								if (enemy.type.reward < min) {
									min = enemy.type.reward;
									target = enemy;
								}
								break;
						}
					}

					((Weapon) e).target(target);

					for (Projectile p : ((Weapon) e).createProjectiles(g.fire())) {
						if (p.type.delivery == DeliveryType.SINGLE_TARGET && p.type.follow && p.getTarget() instanceof Enemy) ((Enemy) p.getTarget()).damageFuture(p);

						projectiles.add(p);
					}
				}
			}

			if (e instanceof Enemy) {
				Enemy enemy = (Enemy) e;
				if (enemy.getDead() != 0) {
					if (enemy.die()) entities.remove(enemy);
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
			
			if (e instanceof Instant) {
				if (((Instant) e).isDone()) {
					entities.remove(e);
					continue;
				}
				
				for (Projectile p : ((Instant) e).fire()) {
					if (p.type.delivery == DeliveryType.SINGLE_TARGET && p.type.follow && p.getTarget() instanceof Enemy) ((Enemy) p.getTarget()).damageFuture(p);

					projectiles.add(p);
				}
			}
		}
	}

	private static void processProjectiles() {
		Entity[] ents = entities.toArray(new Entity[entities.size()]);
		Projectile[] projectiles = GameLogic.projectiles.toArray(new Projectile[GameLogic.projectiles.size()]);
		for (Projectile p : projectiles) {
			if (p.getDeathTime() >= 0) {
				if (!p.fade()) GameLogic.projectiles.remove(p);
				continue;
			}

			if (!p.move()) { // The projectile is "dead"
				p.fade();

				if (p.type.delivery == DeliveryType.SINGLE_TARGET) {
					if (p.type.follow && p.getTarget() instanceof Enemy) {
						if (((Enemy) p.getTarget()).damage(p)) money += ((Enemy) p.getTarget()).type.reward;
					}

					if (p.type.explodeRadius > 0.00001) {
						for (Entity e : ents) {
							if (!(e instanceof Enemy) || (p.type.follow && p.getTarget() == e)) continue;

							if (distanceSq(e.getX(), p.getX(), e.getY(), p.getY()) <= p.type.explodeRadius * p.type.explodeRadius) {
								if (((Enemy) e).damage(p)) money += ((Enemy) e).type.reward;
							}
						}
					}
				}
			}

			if (p.type.delivery != DeliveryType.SINGLE_TARGET) {
				for (Entity e : ents) {
					if (!(e instanceof Enemy)) continue;

					if (distanceSq(e.getX(), p.getX(), e.getY(), p.getY()) > p.getRadius() * p.getRadius()) continue;

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
						if (((Enemy) e).damage(p)) money += ((Enemy) e).type.reward;
						p.hitTarget((Enemy) e);
					}
				}
			} else if (!p.type.follow) {
				Optional<Entity> oe = Arrays.stream(ents).filter(e -> e instanceof Enemy && e.getRectangle().contains(p.getX(), p.getY())).sorted(new Comparator<Entity>() {

					@Override
					public int compare(Entity o1, Entity o2) {
						int i = Double.compare(distanceSq(o1.getX(), p.getX(), o1.getY(), p.getY()), distanceSq(o2.getX(), p.getX(), o2.getY(), p.getY()));

						if (i == 0) return Integer.compare(o1.hashCode(), o2.hashCode());

						return i;
					}
				}).findFirst();

				if (oe.isPresent() && oe.get() instanceof Enemy) {
					Enemy en = (Enemy) oe.get();

					if (en.damage(p)) money += en.type.reward;
					p.hitTarget(en);

					if (p.type.explodeRadius > 0.00001) {
						for (Entity e : ents) {
							if (!(e instanceof Enemy) || (en == e)) continue;

							if (distanceSq(e.getX(), p.getX(), e.getY(), p.getY()) <= p.type.explodeRadius * p.type.explodeRadius) {
								if (((Enemy) e).damage(p)) money += ((Enemy) e).type.reward;
							}
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
	public static void buyObject(Purchasable type) {
		wasPaused = !timer.isRunning();
		if (pauseOnBuy) {
			stop();
		}
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
		saved = false;

		if (buying instanceof CollidableType) {
			entities.add(buying instanceof ObstacleType ? new Obstacle((ObstacleType) buying, x, y) : new Tower((TowerType) buying, x, y));

			for (EnemyType type : EnemyType.values())
				nodes.put(type, PathFinder.calculatePaths(type));

			Entity[] ents = entities.toArray(new Entity[entities.size()]);
			for (Entity e : ents) {
				if (!(e instanceof Enemy)) continue;

				((Enemy) e).resetNodes(nodes.get(((Enemy) e).type));
			}

			setSelectedEntity(entities.get(entities.size() - 1));
		} else if (buying instanceof InstantType) {
			entities.add(new Instant((InstantType) buying, x, y));
		}

		money -= buying.getCost();

		cancelBuy();
		BuyPanel.updateButtons();
	}

	public static void cancelBuy() {
		buying = null;
		if (!wasPaused) start();
		InfoPanel.setDisplayedObject(null);
		GamePanel.repaintPanel();
	}

	public static void sell(CollidableEntity entity) {
		if (entity != null && entities.contains(entity)) {
			entities.remove(entity);
			money += entity.getType().sellCost;
			if (entity == selected) setSelectedEntity(null);

			for (EnemyType type : EnemyType.values())
				nodes.put(type, PathFinder.calculatePaths(type));

			Entity[] ents = entities.toArray(new Entity[entities.size()]);
			for (Entity e : ents) {
				if (!(e instanceof Enemy)) continue;

				((Enemy) e).resetNodes(nodes.get(((Enemy) e).type));
			}

			GamePanel.repaintPanel();
			BuyPanel.updateButtons();
		}
	}

	public static boolean pausesOnBuy() {
		return pauseOnBuy;
	}

	public static void setPauseOnBuy(boolean pauseOnBuy) {
		GameLogic.pauseOnBuy = pauseOnBuy;
	}

	public static Entity getSelectedEntity() {
		return selected;
	}

	public static void setSelectedEntity(Entity selected) {
		GameLogic.selected = selected;

		InfoPanel.setDisplayedObject(selected);
	}

	public static boolean canPlaceObject(Purchasable pType, int x, int y) {
		if (pType instanceof CollidableType) {
			CollidableType type = (CollidableType) pType;
			if (x < 0 || y < 0 || x > currentWorld.getWidth() - type.width || y > currentWorld.getHeight() - type.height) return false;

			for (Node goal : currentWorld.goals)
				if (goal.x >= x && goal.x - x - (goal.top ? 0 : 1) < type.width && goal.y >= y && goal.y - y - (goal.top ? 1 : 0) < type.height) return false;

			Entity[] ents = entities.toArray(new Entity[entities.size()]);
			for (Entity e : ents) {
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
		} else if (x < 0 || y < 0 || x > currentWorld.getWidth() || y > currentWorld.getHeight()) {
			return false;
		}

		return true;
	}

	public static Level getCurrentLevel() {
		return currentLevelSet.levels[levelIndex];
	}

	public static LevelSet getCurrentLevelSet() {
		return currentLevelSet;
	}

	public static int getLevelIndex() {
		return levelIndex;
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

	public static Entity[] getEntities() {
		return entities.toArray(new Entity[entities.size()]);
	}

	public static Projectile[] getProjectiles() {
		return projectiles.toArray(new Projectile[projectiles.size()]);
	}

	public static Purchasable getBuyingType() {
		return buying;
	}

	public static boolean isPaused() {
		return timer == null ? true : !timer.isRunning();
	}

	public static enum State {
		PLAYING,
		COMPLETE,
		FAILED,
		WON;

		public String toString() {
			return Language.get("state-" + this.name().toLowerCase());
		}
	}

	public static State getState() {
		return state;
	}

	public static final double distanceSq(double x1, double x2, double y1, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;

		return dx * dx + dy * dy;
	}

	public static boolean isSaved() {
		return saved;
	}

	public static Path getLastSaveLocation() {
		return lastSaveLocation;
	}

	public static boolean save() {
		if (lastSaveLocation != null) {
			save(lastSaveLocation);
			return true;
		}

		return false;
	}

	public static synchronized void save(Path path) {
		lastSaveLocation = path;

		HashMap<String, Object> map = new HashMap<>();
		map.put("money", money);
		map.put("health", health);
		map.put("world", currentWorld.id);
		map.put("level", currentLevelSet.id);
		map.put("enemy-index", enemyIndex);
		map.put("time-to-next", timeToNextEnemy);
		map.put("state", state.name().toLowerCase());

		LinkedHashMap<Entity, HashMap<String, Object>> entityMap = new LinkedHashMap<>();
		for (Entity e : entities) {
			HashMap<String, Object> ejson = new HashMap<>();
			ejson.put("x", e.getX());
			ejson.put("y", e.getY());
			ejson.put("id", e.getType().id);
			if (e instanceof Enemy) {
				ejson.put("type", "enemy");
				ejson.put("health", ((Enemy) e).getHealth());
				ejson.put("death-time", ((Enemy) e).getDeathTime());
				ejson.put("next-node", ((Enemy) e).getNextNode());
				ejson.put("prev-node", ((Enemy) e).getPrevNode());
				List<HashMap<String, Object>> effects = new ArrayList<>();
				for (StatusEffect effect : ((Enemy) e).getStatusEffects()) {
					HashMap<String, Object> ef = new HashMap<>();
					ef.put("type", effect.type.name().toLowerCase());
					ef.put("amplifier", effect.amplifier);
					ef.put("duration", effect.getDuration());
					ef.put("orig-duration", effect.origDuration);
					ef.put("inflictor", entities.indexOf(effect.inflictor));
					effects.add(ef);
				}
				ejson.put("effects", effects);
			} else if (e instanceof Obstacle) {
				ejson.put("type", "obstacle");
			} else if (e instanceof Tower) {
				ejson.put("type", "tower");
			}

			if (e instanceof Weapon) {
				if (((Weapon) e).getGun() != null && e instanceof Tower) {
					Gun g = ((Weapon) e).getGun();
					ejson.put("target-type", g.getTargetType().name().toLowerCase());
					ejson.put("kills", g.getKills());
					ejson.put("damage-done", g.getDamageDone());
					ejson.put("projectiles-fired", g.getProjectilesFired());
				}
				ejson.put("projectiles", new ArrayList<HashMap<String, Object>>());
			}
			entityMap.put(e, ejson);
		}

		for (Projectile p : projectiles) {
			HashMap<String, Object> pmap = new HashMap<>();
			pmap.put("x", p.getX());
			pmap.put("y", p.getY());
			pmap.put("start-x", p.startX);
			pmap.put("start-y", p.startY);
			pmap.put("target-x", p.targetX);
			pmap.put("target-y", p.targetY);
			pmap.put("rotation", p.getRotation());
			pmap.put("radius", p.getRadius());
			pmap.put("death-time", p.getDeathTime());
			pmap.put("type", Arrays.asList(p.shootingEntity.getGun().projectiles).indexOf(p.type));
			pmap.put("target", entities.indexOf(p.getTarget()));
			List<Integer> hitTargets = new ArrayList<>();
			p.getHitTargets().stream().mapToInt(e -> entities.indexOf(e)).sorted().forEachOrdered(hitTargets::add);
			pmap.put("hit-targets", hitTargets);
			List<Object> projectiles = new ArrayList<>();
			projectiles.addAll((List<?>) entityMap.get(p.shootingEntity).get("projectiles"));
			projectiles.add(pmap);
			entityMap.get(p.shootingEntity).put("projectiles", projectiles);
		}

		map.put("entities", entityMap.values());

		String obf = obfuscate(JSON.writeJSON(map));
		try {
			Files.write(path, obf.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {}
	}

	public static synchronized void open(Path path) throws IOException {
		List<?> json;
		try {
			json = JSON.parseJSON(deobfuscate(new String(Files.readAllBytes(path), StandardCharsets.UTF_8)));
		} catch (NumberFormatException e) {
			throw new IOException();
		}

		if (!(json.get(0) instanceof Map<?, ?>)) throw new IOException();

		Map<?, ?> game = (Map<?, ?>) json.get(0);

		LevelSet levelSet = LevelSet.valueOf(String.format("%s", game.get("level")));
		if (levelSet == null) levelSet = LevelSet.values()[0];

		World world = World.valueOf(String.format("%s", game.get("world")));
		if (world == null) world = World.values()[0];

		int money = game.get("money") instanceof Number ? ((Number) game.get("money")).intValue() : levelSet.levels[0].money;
		double gameHealth = game.get("health") instanceof Number ? ((Number) game.get("health")).doubleValue() : levelSet.health;
		int enemyIndex = game.get("enemy-index") instanceof Number ? ((Number) game.get("enemy-index")).intValue() : 0;
		double timeToNextEnemy = game.get("time-to-next") instanceof Number ? ((Number) game.get("time-to-next")).doubleValue() : 0;
		State state;
		try {
			state = State.valueOf(String.format("%s", game.get("state")).toUpperCase());
		} catch (Exception e) {
			state = State.PLAYING;
		}

		List<Entity> ents = new ArrayList<>();

		List<SavedProjectile> savedProjectiles = new ArrayList<>();
		List<SavedEffect> savedEffects = new ArrayList<>();

		if (game.get("entities") instanceof List<?>) {
			List<?> entities = (List<?>) game.get("entities");

			for (int i = 0; i < entities.size(); i++) {
				Object o = entities.get(i);

				if (!(o instanceof Map<?, ?>)) continue;
				Map<?, ?> e = (Map<?, ?>) o;

				String id = String.format("%s", e.get("id"));
				String typeStr = String.format("%s", e.get("type"));
				if (typeStr.equals("tower")) {
					TowerType type = TowerType.valueOf(id);
					if (type == null) continue;

					double x = e.get("x") instanceof Number ? ((Number) e.get("x")).doubleValue() : 0;
					double y = e.get("y") instanceof Number ? ((Number) e.get("y")).doubleValue() : 0;
					TargetType targetType = TargetType.FIRST;
					try {
						targetType = TargetType.valueOf(String.format("%s", e.get("target-type")).toUpperCase());
					} catch (Exception exception) {}
					int kills = e.get("kills") instanceof Number ? ((Number) e.get("kills")).intValue() : 0;
					double damageDone = e.get("damage-done") instanceof Number ? ((Number) e.get("damage-done")).doubleValue() : 0;
					int projectilesFired = e.get("projectiles-fired") instanceof Number ? ((Number) e.get("projectiles-fired")).intValue() : 0;

					ents.add(new Tower(type, x, y, targetType, kills, damageDone, projectilesFired));

					if (e.get("projectiles") instanceof List<?>) {
						for (Object obj : (List<?>) e.get("projectiles")) {
							if (!(obj instanceof Map<?, ?>)) continue;
							Map<?, ?> proj = (Map<?, ?>) obj;

							ProjectileType ptype;
							try {
								ptype = type.projectiles[((Number) proj.get("type")).intValue()];
							} catch (Exception exception) {
								continue;
							}

							double px = proj.get("x") instanceof Number ? ((Number) proj.get("x")).doubleValue() : 0;
							double py = proj.get("y") instanceof Number ? ((Number) proj.get("y")).doubleValue() : 0;
							double startX = proj.get("start-x") instanceof Number ? ((Number) proj.get("start-x")).doubleValue() : px;
							double startY = proj.get("start-y") instanceof Number ? ((Number) proj.get("start-y")).doubleValue() : py;
							double targetX = proj.get("target-x") instanceof Number ? ((Number) proj.get("target-x")).doubleValue() : 0;
							double targetY = proj.get("target-y") instanceof Number ? ((Number) proj.get("target-y")).doubleValue() : 0;
							double rotation = proj.get("rotation") instanceof Number ? ((Number) proj.get("rotation")).doubleValue() : 0;
							double deathTime = proj.get("death-time") instanceof Number ? ((Number) proj.get("death-time")).doubleValue() : -1;
							double radius = proj.get("radius") instanceof Number ? ((Number) proj.get("radius")).doubleValue() : 0;
							int target = proj.get("target") instanceof Number ? ((Number) proj.get("target")).intValue() : -1;
							List<Integer> hitTargets = new ArrayList<>();
							if (proj.get("hit-targets") instanceof List<?>) {
								((List<?>) proj.get("hit-targets")).stream().filter(n -> n instanceof Number).forEach(n -> hitTargets.add(((Number) n).intValue()));
							}
							int[] hits = new int[hitTargets.size()];
							for (int h = 0; h < hits.length; h++) {
								hits[h] = hitTargets.get(h);
							}
							savedProjectiles.add(new SavedProjectile(i, ptype, px, py, startX, startY, targetX, targetY, rotation, deathTime, radius, target, hits));
						}
					}
				} else if (typeStr.equals("enemy")) {
					EnemyType type = EnemyType.valueOf(id);
					if (type == null) continue;

					double x = e.get("x") instanceof Number ? ((Number) e.get("x")).doubleValue() : 0;
					double y = e.get("y") instanceof Number ? ((Number) e.get("y")).doubleValue() : 0;
					double health = e.get("health") instanceof Number ? ((Number) e.get("health")).doubleValue() : 0;
					double deathTime = e.get("death-time") instanceof Number ? ((Number) e.get("death-time")).doubleValue() : 0;

					int nextX = 0;
					int nextY = 0;
					boolean nextTop = false;
					if (e.get("next-node") instanceof Map<?, ?>) {
						Map<?, ?> next = (Map<?, ?>) e.get("next-node");

						if (next.get("x") instanceof Number) nextX = ((Number) next.get("x")).intValue();
						if (next.get("y") instanceof Number) nextY = ((Number) next.get("y")).intValue();
						if (next.get("top") instanceof Boolean) nextTop = (Boolean) next.get("top");
					}
					Node nextNode = new Node(nextX, nextY, nextTop);

					int prevX = 0;
					int prevY = 0;
					boolean prevTop = false;
					if (e.get("prev-node") instanceof Map<?, ?>) {
						Map<?, ?> prev = (Map<?, ?>) e.get("prev-node");

						if (prev.get("x") instanceof Number) prevX = ((Number) prev.get("x")).intValue();
						if (prev.get("y") instanceof Number) prevY = ((Number) prev.get("y")).intValue();
						if (prev.get("top") instanceof Boolean) prevTop = (Boolean) prev.get("top");
					}
					Node prevNode = new Node(prevX, prevY, prevTop);

					ents.add(new Enemy(type, prevNode, nextNode, x, y, deathTime, health));

					if (e.get("effects") instanceof List<?>) {
						List<?> effects = (List<?>) e.get("effects");
						for (Object obj : effects) {
							if (obj instanceof Map<?, ?>) {
								Map<?, ?> ef = (Map<?, ?>) obj;

								try {
									StatusEffectType efType = StatusEffectType.valueOf(String.format("%s", ef.get("type")).toUpperCase());
									double duration = ((Number) ef.get("duration")).doubleValue();
									if (duration <= 0) continue;
									double amplifier = ((Number) ef.get("amplifier")).doubleValue();
									if (amplifier <= 0) continue;
									int inflictor = ((Number) ef.get("inflictor")).intValue();
									if (inflictor <= 0) continue;
									double origDuration = ef.get("orig-duration") instanceof Number ? ((Number) ef.get("orig-duration")).doubleValue() : duration;
									savedEffects.add(new SavedEffect(efType, duration, amplifier, origDuration, inflictor, i));
								} catch (Exception exception) {
									exception.printStackTrace();
									continue;
								}

							}
						}
					}
				} else if (typeStr.equals("obstacle")) {
					ObstacleType type = ObstacleType.valueOf(id);
					if (type == null) continue;

					double x = e.get("x") instanceof Number ? ((Number) e.get("x")).doubleValue() : 0;
					double y = e.get("y") instanceof Number ? ((Number) e.get("y")).doubleValue() : 0;

					ents.add(new Obstacle(type, x, y));
				}
			}
		}

		List<Projectile> projectiles = new ArrayList<>();

		for (SavedProjectile p : savedProjectiles) {
			if (ents.get(p.shootingIndex) instanceof Weapon && ents.get(p.target) instanceof Enemy) {
				List<Enemy> hitTargets = new ArrayList<>();
				Arrays.stream(p.hitTargets).forEach(i -> {
					if (ents.get(i) instanceof Enemy) hitTargets.add((Enemy) ents.get(i));
				});
				Enemy target = (Enemy) ents.get(p.target);
				Projectile proj = new Projectile((Weapon) ents.get(p.shootingIndex), p.ptype, p.x, p.y, p.startX, p.startY, p.targetX, p.targetY, p.rotation, p.deathTime, p.radius, target, hitTargets);
				projectiles.add(proj);
				if (p.deathTime < 0) target.damageFuture(proj);
			}
		}

		for (SavedEffect e : savedEffects) {
			if (ents.get(e.affectedIndex) instanceof Enemy && ents.get(e.inflictorIndex) instanceof Weapon) ((Enemy) ents.get(e.affectedIndex)).addStatusEffect(new StatusEffect((Weapon) ents.get(e.inflictorIndex), e.type, e.amplifier, e.duration, e.origDuration));
		}

		GameLogic.reset();
		GameLogic.currentLevelSet = levelSet;
		GameLogic.currentWorld = world;
		GameLogic.enemyIndex = enemyIndex;
		GameLogic.money = money;
		GameLogic.health = gameHealth;
		GameLogic.maxHealth = levelSet.health;
		GameLogic.timeToNextEnemy = timeToNextEnemy;
		GameLogic.state = state;
		GameLogic.entities = new ArrayList<>(ents);
		GameLogic.projectiles = new ArrayList<>(projectiles);

		for (EnemyType type : EnemyType.values())
			nodes.put(type, PathFinder.calculatePaths(type));

		Entity[] entityArray = entities.toArray(new Entity[entities.size()]);
		for (Entity e : entityArray) {
			if (!(e instanceof Enemy)) continue;

			((Enemy) e).resetNodes(nodes.get(((Enemy) e).type));
		}

		Window.repaintWindow();
		InfoPanel.refreshDisplay();
		BuyPanel.updateButtons();
	}

	private static class SavedEffect {

		final StatusEffectType type;
		final double duration;
		final double amplifier;
		final double origDuration;
		final int inflictorIndex;
		final int affectedIndex;

		public SavedEffect(StatusEffectType type, double duration, double amplifier, double origDuration, int inflictorIndex, int affectedIndex) {
			this.type = type;
			this.duration = duration;
			this.amplifier = amplifier;
			this.origDuration = origDuration;
			this.inflictorIndex = inflictorIndex;
			this.affectedIndex = affectedIndex;
		}
	}

	private static class SavedProjectile {

		final int shootingIndex;
		final ProjectileType ptype;
		final double x, y, startX, startY, targetX, targetY, rotation;
		final double deathTime;
		final double radius;
		final int target;
		final int[] hitTargets;

		public SavedProjectile(int shootingIndex, ProjectileType ptype, double x, double y, double startX, double startY, double targetX, double targetY, double rotation, double deathTime, double radius, int target, int[] hitTargets) {
			this.shootingIndex = shootingIndex;
			this.ptype = ptype;
			this.x = x;
			this.y = y;
			this.startX = startX;
			this.startY = startY;
			this.targetX = targetX;
			this.targetY = targetY;
			this.rotation = rotation;
			this.deathTime = deathTime;
			this.radius = radius;
			this.target = target;
			this.hitTargets = hitTargets;
		}
	}

	public static String obfuscate(String string) {
		long seed = System.nanoTime() % 60466176;
		Random random = new Random(seed);

		String seedStr = Long.toString(seed, 36);
		String ret = String.format("%s%s", "00000".substring(seedStr.length()), seedStr);

		for (char c : string.toCharArray()) {
			ret += (char) (random.nextInt(256) ^ (int) c);
		}

		return ret;
	}

	public static String deobfuscate(String string) {
		long seed = Long.parseLong(string.substring(0, 5), 36);
		Random random = new Random(seed);

		String ret = "";

		for (char c : string.substring(5).toCharArray()) {
			ret += (char) (random.nextInt(256) ^ (int) c);
		}

		return ret;
	}

}
