package terraintd.types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

public class TypeGenerator {

	private TypeGenerator() {}

	private static TowerType[] towers;
	private static EnemyType[] enemies;
	private static ObstacleType[] obstacles;

	/**
	 * <ul>
	 * <li><b><i>generateValues</i></b><br>
	 * <br>
	 * {@code public static void generateValues()}<br>
	 * <br>
	 * Reads the mods folder for towers/enemies/obstacles. The values are stored, and need not be calculated more than once.<br>
	 * </ul>
	 */
	public static void generateValues() {
		try {
			Files.createDirectories(Paths.get("terraintd/mods"));
		} catch (IOException e) {}

		List<String> prototypes = new ArrayList<>();

		Pattern p = Pattern.compile("([\\{\\}])[^\\{\\}]*");

		try (Stream<Path> files = Files.walk(Paths.get("terraintd/mods"))) {
			Iterator<Path> iter = files.iterator();
			while (iter.hasNext()) {
				Path path = iter.next();
				if (Files.isDirectory(path) || !path.toString().replaceAll(".+\\.", "").equals("proto")) continue;

				List<String> protos = new ArrayList<>();

				String s = new String(Files.readAllBytes(path)).replaceAll("(?:\\-\\-|#).+", "");

				Matcher m = p.matcher(s);

				int indent = 0;
				String str = "";
				while (m.find()) {
					str += m.group();
					indent -= m.group(1).charAt(0) - 124;
					if (indent == 0) {
						protos.add(str.replaceAll("\\s", "").replaceAll("([^\\,])([\\]\\}])", "$1,$2"));
						str = "";
					}
				}

				if (indent == 0) {
					prototypes.addAll(protos);
				}
			}
		} catch (IOException e) {}

		if (prototypes.size() == 0) {
			try {
				Files.createDirectories(Paths.get("terraintd/mods/base"));
				Files.write(Files.createFile(Paths.get("terraintd/mods/base/tower.proto")), "{\n\ttype=tower,\n\tid=tower,\n}".getBytes());
				// TODO
			} catch (IOException e) {}

			generateValues();
			return;
		}

		ArrayList<TowerType> newTowers = new ArrayList<>();
		ArrayList<EnemyType> newEnemies = new ArrayList<>();
		ArrayList<ObstacleType> newObstacles = new ArrayList<>();

		Pattern pSq = Pattern.compile("([\\[\\]])[^\\[\\]]*");

		Pattern p1 = Pattern.compile("([a-z\\-_\\+]+)\\=([\\[\\{])");
		Pattern p2 = Pattern.compile("([a-z\\-_\\+]+)\\=([0-9A-Za-z_\\-/\"\\.]+),");

		for (String s : prototypes) {
			HashMap<String, String> properties = new HashMap<>();

			Matcher m = p1.matcher(s);

			String s2 = s;

			while (m.find()) {
				final int charNum = m.group(2).charAt(0) + 1;

				int indent = 0;

				String str = "";

				Matcher indM = (charNum == 124 ? p : pSq).matcher(s.substring(m.start()));

				while (true) {
					if (!indM.find()) break;

					str += indM.group();

					indent -= indM.group(1).charAt(0) - charNum;
					if (indent == 0) {
						properties.put(m.group(1).replaceAll("[\\-_]", "").toLowerCase(), str.substring(0, str.lastIndexOf(charNum + 1) + 1));
						s2 = s2.replace(str, "");
						break;
					}
				}
			}

			m = p2.matcher(s2);

			while (m.find()) {
				properties.put(m.group(1).replaceAll("[\\-_]", "").toLowerCase(), m.group(2).replaceAll("\"", "").toLowerCase());
			}

			String type = properties.get("type");

			if (type == null) {
				continue;
			} else if (type.equals("tower")) {
				try {
					String id = properties.get("id");
					if (id == null) continue;

					int cost = Integer.parseUnsignedInt(properties.getOrDefault("cost", "1"));

					String collision = properties.getOrDefault("collision", "[1,1,]");
					int width = 1, height = 1;
					if (collision.matches("[hw]")) {
						Matcher whM = Pattern.compile("(width|height)=\\+?([0-9]+),").matcher(collision);
						while (whM.find()) {
							if (whM.group(1).equals("width")) {
								width = Integer.parseUnsignedInt(whM.group(2));
							} else {
								height = Integer.parseUnsignedInt(whM.group(2));
							}
						}
					} else {
						Matcher whM = Pattern.compile("\\[\\+?([0-9]+),\\+?([0-9]+)\\,?\\]").matcher(collision);
						if (whM.find()) {
							width = Integer.parseUnsignedInt(whM.group(1));
							height = Integer.parseUnsignedInt(whM.group(2));
						}
					}

					HashMap<Terrain, Boolean> terrain = new HashMap<>(Terrain.values().length);

					for (Terrain terra : Terrain.values()) {
						terrain.put(terra, false);
					}

					for (String terra : properties.getOrDefault("terrain", "land").replaceAll("[\\[\\]]", "").split("\\,")) {
						if (terra.equals("all")) {
							for (Terrain ter : Terrain.values()) {
									terrain.put(ter, true);
							}
						} else if (terra.equals("land") || terra.equals("water")) {
							boolean land = terra.charAt(0) == 'l';
							for (Terrain ter : Terrain.values()) {
								if (ter.name().contains("WATER") != land) {
									terrain.put(ter, true);
								}
							}
						} else {
							try {
								Terrain ter = Terrain.valueOf(terra);
								if (ter != null) {
									terrain.put(ter, true);
								}
							} catch (IllegalArgumentException e) {}
						}
					}

					boolean onHill = Boolean.parseBoolean(properties.getOrDefault("onhill", "true"));
					boolean rotate = Boolean.parseBoolean(properties.getOrDefault("rotate", "true"));
					ImageType image = new ImageType(properties.getOrDefault("image", ""));
					ImageType icon = new ImageType(properties.getOrDefault("icon", ""));

					ProjectileType[] projectiles;
					if (properties.get("projectiles") != null) {
						ArrayList<ProjectileType> projList = new ArrayList<>();

						String projStr = properties.get("projectiles");

						List<String> projs = new ArrayList<>();

						Matcher pm = p.matcher(projStr);

						int indent = 0;
						String str = "";
						while (pm.find()) {
							str += pm.group();
							indent -= pm.group(1).charAt(0) - 124;
							if (indent == 0) {
								projs.add(str.replaceAll("\\s", "").replaceAll("([^\\,])([\\]\\}])", "$1,$2"));
								str = "";
							}
						}

						if (indent != 0) {
							projectiles = new ProjectileType[0];
						} else {
							for (String proj : projs) {
								projList.add(new ProjectileType(proj));
							}

							projectiles = projList.toArray(new ProjectileType[projList.size()]);
						}
					} else {
						projectiles = new ProjectileType[0];
					}

					newTowers.add(new TowerType(id, cost, width, height, terrain, onHill, rotate, image, icon, projectiles));
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Error reading prototype", JOptionPane.ERROR_MESSAGE);
				}
			} else if (type.equals("enemy")) {
				try {
					String speed = properties.getOrDefault("speed", "{land=1,water=0}");

					HashMap<Terrain, Double> speeds = new HashMap<>();

					if (speed.contains("=")) {
						Matcher spdM = p2.matcher(speed);

						while (spdM.find()) {
							String terra = spdM.group(1);
							if (terra.equals("all")) {
								for (Terrain ter : Terrain.values()) {
									speeds.put(ter, Double.parseDouble(spdM.group(2)));
								}
							}
							if (terra.equals("land") || terra.equals("water")) {
								boolean land = terra.charAt(0) == 'l';
								for (Terrain ter : Terrain.values()) {
									if (ter.name().contains("WATER") != land) {
										speeds.put(ter, Double.parseDouble(spdM.group(2)));
									}
								}
							} else {
								try {
									Terrain ter = Terrain.valueOf(terra);
									if (ter != null) {
										speeds.put(ter, Double.parseDouble(spdM.group(2)));
									}
								} catch (IllegalArgumentException e) {}
							}
						}
					} else {
						String[] spdsa = speed.split("[\\[\\{,\\}\\]]"), spds;
						if (spdsa.length > 0 && spdsa[0].isEmpty()) {
							spds = new String[spdsa.length - 1];
							System.arraycopy(spdsa, 1, spds, 0, spds.length);
						} else {
							spds = spdsa;
						}

						switch (spds.length) {
							case 1:
								for (Terrain ter : Terrain.values()) {
									speeds.put(ter, Double.parseDouble(spds[0]));
								}
								break;
							case 2:
								for (int l = 0; l < 2; l++) {
									for (Terrain ter : Terrain.values()) {
										if (ter.name().contains("WATER") != (l == 0)) {
											speeds.put(ter, Double.parseDouble(spds[l]));
										}
									}
								}
								break;
							default:
								for (int i = 0; i < spds.length; i++) {
									speeds.put(Terrain.values()[i], Double.parseDouble(spds[i]));
								}
								break;
						}
					}

					String vertSpeed = properties.getOrDefault("verticalspeed", "[1,1,]");
					int upSpeed = 1, downSpeed = 1;
					if (vertSpeed.matches("[ud]")) {
						Matcher whM = Pattern.compile("(up|down)=\\+?([0-9]+),").matcher(vertSpeed);
						while (whM.find()) {
							if (whM.group(1).equals("up")) {
								upSpeed = Integer.parseUnsignedInt(whM.group(2));
							} else {
								downSpeed = Integer.parseUnsignedInt(whM.group(2));
							}
						}
					} else {
						Matcher whM = Pattern.compile("\\[\\+?([0-9]+),\\+?([0-9]+)\\,?\\]").matcher(vertSpeed);
						if (whM.find()) {
							upSpeed = Integer.parseUnsignedInt(whM.group(1));
							downSpeed = Integer.parseUnsignedInt(whM.group(2));
						}
					}

					double health = Double.parseDouble(properties.getOrDefault("health", "1"));
					double damage = Double.parseDouble(properties.getOrDefault("damage", "0"));
					int reward = Integer.parseInt(properties.getOrDefault("reward", "0"));
					ImageType image = new ImageType(properties.getOrDefault("image", ""));
					ImageType death = new ImageType(properties.getOrDefault("death", ""));

					ProjectileType[] projectiles;
					if (properties.get("projectiles") != null) {
						ArrayList<ProjectileType> projList = new ArrayList<>();

						String projStr = properties.get("projectiles");

						List<String> projs = new ArrayList<>();

						Matcher pm = p.matcher(projStr);

						int indent = 0;
						String str = "";
						while (pm.find()) {
							str += pm.group();
							indent -= pm.group(1).charAt(0) - 124;
							if (indent == 0) {
								projs.add(str.replaceAll("\\s", "").replaceAll("([^\\,])([\\]\\}])", "$1,$2"));
								str = "";
							}
						}

						if (indent != 0) {
							projectiles = new ProjectileType[0];
						} else {
							for (String proj : projs) {
								projList.add(new ProjectileType(proj));
							}

							projectiles = projList.toArray(new ProjectileType[projList.size()]);
						}
					} else {
						projectiles = new ProjectileType[0];
					}

					newEnemies.add(new EnemyType(speeds, upSpeed, downSpeed, health, damage, reward, image, death, projectiles));
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Error reading prototype", JOptionPane.ERROR_MESSAGE);
				}
			} else if (type.equals("obstacle")) {
				try {
					String id = properties.get("id");
					if (id == null) continue;

					int cost = Integer.parseUnsignedInt(properties.getOrDefault("cost", "1"));

					String collsion = properties.getOrDefault("collsion", "[1,1,]");
					int width = 1, height = 1;
					if (collsion.matches("[hw]")) {
						Matcher whM = Pattern.compile("(width|height)=\\+?([0-9]+),").matcher(collsion);
						while (whM.find()) {
							if (whM.group(1).equals("width")) {
								width = Integer.parseUnsignedInt(whM.group(2));
							} else {
								height = Integer.parseUnsignedInt(whM.group(2));
							}
						}
					} else {
						Matcher whM = Pattern.compile("\\[\\+?([0-9]+),\\+?([0-9]+)\\,?\\]").matcher(collsion);
						if (whM.find()) {
							width = Integer.parseUnsignedInt(whM.group(1));
							height = Integer.parseUnsignedInt(whM.group(2));
						}
					}

					double spawnRate = Double.parseDouble(properties.getOrDefault("spawnrate", "0"));
					ImageType image = new ImageType(properties.getOrDefault("image", ""));
					ImageType icon = new ImageType(properties.getOrDefault("icon", ""));

					newObstacles.add(new ObstacleType(id, width, height, cost, spawnRate, image, icon));
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Error reading prototype", JOptionPane.ERROR_MESSAGE);
				}
			}

		}

		towers = newTowers.toArray(new TowerType[newTowers.size()]);
		enemies = newEnemies.toArray(new EnemyType[newEnemies.size()]);
		obstacles = newObstacles.toArray(new ObstacleType[newObstacles.size()]);
	}

	static TowerType[] towers() {
		if (towers == null) generateValues();

		return towers;
	}

	static EnemyType[] enemies() {
		if (enemies == null) generateValues();

		return enemies;
	}

	static ObstacleType[] obstacles() {
		if (obstacles == null) generateValues();

		return obstacles;
	}

	/*
	 * @Deprecated public static class ProjectileType {
	 * 
	 * public final DeliveryType delivery; public final boolean explode; public final double speed; public final double damage;
	 * public final double falloff; public final double rate; public final boolean follow; public final double maxDist; public
	 * final double rotation; public final boolean absRotation; public final double dyingFadeTime; public final boolean
	 * dyingFade; public final boolean damageFade; public final ImageType image; public final ImageType explosion; public final
	 * EffectType[] effects;
	 * 
	 * public ProjectileType(DeliveryType delivery, boolean explode, double speed, double damage, double falloff, double rate,
	 * boolean follow, double maxDist, double rotation, boolean absRotation, double dyingFadeTime, boolean dyingFade, boolean
	 * damageFade, ImageType image, ImageType explosion, EffectType[] effects) { this.delivery = delivery; this.explode =
	 * explode; this.speed = speed; this.damage = damage; this.falloff = falloff; this.rate = rate; this.follow = follow;
	 * this.maxDist = maxDist; this.rotation = rotation; this.absRotation = absRotation; this.dyingFadeTime = dyingFadeTime;
	 * this.dyingFade = dyingFade; this.damageFade = damageFade; this.image = image; this.explosion = explosion; this.effects =
	 * effects; }
	 * 
	 * public ProjectileType(String s) { HashMap<String, String> properties = new HashMap<>();
	 * 
	 * Matcher listM = Pattern.compile("([a-z\\-_\\+]+)=[\\[\\{](.+?)[\\]\\}]").matcher(s);
	 * 
	 * String s2 = s;
	 * 
	 * while (listM.find()) { properties.put(listM.group(1).replaceAll("[\\-_]", ""), listM.group(2)); s2 =
	 * s2.replace(listM.group(), ""); }
	 * 
	 * Pattern kv = Pattern.compile("([a-z\\-_\\+]+)\\=([0-9A-Za-z_\\-/\"\\.]+),");
	 * 
	 * Matcher kvM = kv.matcher(s2);
	 * 
	 * while (kvM.find()) { properties.put(kvM.group(1), kvM.group(2)); }
	 * 
	 * DeliveryType delivery; try { delivery = DeliveryType.valueOf(properties.get("delivery")); } catch
	 * (IllegalArgumentException | NullPointerException e) { delivery = DeliveryType.SINGLE_TARGET; } this.delivery = delivery;
	 * this.explode = Boolean.parseBoolean(properties.getOrDefault("explode", "false")); this.speed =
	 * Double.parseDouble(properties.getOrDefault("speed", "1"));
	 * 
	 * String damage = properties.getOrDefault("damage", "[0,0]"); int near = 0, far = 0; if (damage.matches("[mnf]")) { Matcher
	 * whM = Pattern.compile("(min|max|near|far)=\\+?([0-9]+),").matcher(damage); while (whM.find()) { if
	 * (whM.group(1).matches("^(min|near)$")) { near = Integer.parseUnsignedInt(whM.group(2)); } else { far =
	 * Integer.parseUnsignedInt(whM.group(2)); } } } else { Matcher whM =
	 * Pattern.compile("\\[\\+?([0-9]+),\\+?([0-9]+)\\]").matcher(damage); if (whM.find()) { near =
	 * Integer.parseUnsignedInt(whM.group(1)); far = Integer.parseUnsignedInt(whM.group(2)); } } this.damage = near;
	 * this.falloff = near - far;
	 * 
	 * this.rate = Double.parseDouble(properties.getOrDefault("rate", "1")); this.follow =
	 * Boolean.parseBoolean(properties.getOrDefault("follow", "true")); String md = properties.getOrDefault("maxdist", "1"); if
	 * (md.matches("^inf(?:init[ey])?$")) md = "1e2000"; this.maxDist = Double.parseDouble(md);
	 * 
	 * String rotation = properties.getOrDefault("rotation", "1"); this.rotation = Double.parseDouble(rotation.replace("pi",
	 * "")) * (rotation.endsWith("pi") ? Math.PI : 1); this.absRotation =
	 * Boolean.parseBoolean(properties.getOrDefault("absrotation", "false")); this.dyingFadeTime =
	 * Double.parseDouble(properties.getOrDefault("dyingfadetime", "0.1")); this.dyingFade =
	 * Boolean.parseBoolean(properties.getOrDefault("dyingfade", "true")); this.damageFade =
	 * Boolean.parseBoolean(properties.getOrDefault("damagefade", "true")); this.image = new
	 * ImageType(properties.getOrDefault("image", "")); this.explosion = new ImageType(properties.getOrDefault("explosion",
	 * ""));
	 * 
	 * EffectType[] effects; if (properties.get("effects") != null) { ArrayList<ProjectileType> effList = new ArrayList<>();
	 * 
	 * String effStr = properties.get("effects");
	 * 
	 * List<String> effs = new ArrayList<>();
	 * 
	 * Matcher pm = Pattern.compile("([\\{\\}])[^\\{\\}]*").matcher(effStr);
	 * 
	 * int indent = 0; String str = ""; while (pm.find()) { str += pm.group(); indent -= pm.group(1).charAt(0) - 124; if (indent
	 * == 0) { effs.add(str.replaceAll("\\s", "").replaceAll("([^\\,])([\\]\\}])", "$1,$2")); str = ""; } }
	 * 
	 * if (indent != 0) { effects = new EffectType[0]; } else { for (String proj : effs) { effList.add(new
	 * ProjectileType(proj)); }
	 * 
	 * effects = effList.toArray(new EffectType[effList.size()]); } } else { effects = new EffectType[0]; } this.effects =
	 * effects; }
	 * 
	 * }
	 * 
	 * @Deprecated public static class TowerType {
	 * 
	 * public final String id; public final int cost; public final int width; public final int height; public final
	 * HashMap<Terrain, Boolean> terrain; public final boolean onHill; public final boolean rotate; public final ImageType
	 * image; public final ImageType icon; public final ProjectileType[] projectiles;
	 * 
	 * private TowerType(String id, int cost, int width, int height, HashMap<Terrain, Boolean> terrain, boolean onHill, boolean
	 * rotate, ImageType image, ImageType icon, ProjectileType[] projectiles) { this.id = id; this.cost = cost; this.width =
	 * width; this.height = height; this.terrain = terrain; this.onHill = onHill; this.rotate = rotate; this.image = image;
	 * this.icon = icon; this.projectiles = projectiles; }
	 * 
	 * }
	 * 
	 * @Deprecated public static class EnemyType {
	 * 
	 * public final HashMap<Terrain, Double> speed; public final double upSpeed; public final double downSpeed; public final
	 * double health; public final double damage; public final int reward; public final ImageType image; public final ImageType
	 * death; public final ProjectileType[] projectiles;
	 * 
	 * private EnemyType(HashMap<Terrain, Double> speed, double upSpeed, double downSpeed, double health, double damage, int
	 * reward, ImageType image, ImageType death, ProjectileType[] projectiles) { this.speed = speed; this.upSpeed = upSpeed;
	 * this.downSpeed = downSpeed; this.health = health; this.damage = damage; this.reward = reward; this.image = image;
	 * this.death = death; this.projectiles = projectiles; }
	 * 
	 * }
	 * 
	 * @Deprecated public static class ObstacleType {
	 * 
	 * public final String id; public final int width; public final int height; public final int cost; public final double
	 * spawnRate; public final ImageType image; public final ImageType icon;
	 * 
	 * public ObstacleType(String id, int width, int height, int cost, double spawnRate, ImageType image, ImageType icon) {
	 * this.id = id; this.width = width; this.height = height; this.cost = cost; this.spawnRate = spawnRate; this.image = image;
	 * this.icon = icon; }
	 * 
	 * }
	 * 
	 * @Deprecated public static class EffectType {
	 * 
	 * public final StatusEffectType type; public final double duration; public final double amplifier;
	 * 
	 * private EffectType(StatusEffectType type, double duration, double amplifier) { this.type = type; this.duration =
	 * duration; this.amplifier = amplifier; }
	 * 
	 * }
	 * 
	 * @Deprecated public static class ImageType {
	 * 
	 * public final String src; public final double width; public final double height; public final double x; public final
	 * double y;
	 * 
	 * private ImageType(String src, double width, double height, double x, double y) { this.src = src; this.width = width;
	 * this.height = height; this.x = x; this.y = y; }
	 * 
	 * private ImageType(String s) { Matcher srcM = Pattern.compile("src=([0-9a-z_\\-/\"\\.]+)").matcher(s); this.src =
	 * srcM.find() ? srcM.group(1) : "";
	 * 
	 * Matcher collM = Pattern.compile("collision=([\\[\\{][0-9a-z\\,\\.][\\]\\}])").matcher(s);
	 * 
	 * String collsion = collM.find() ? collM.group(1) : "[1,1]"; double width = 1, height = 1; if (collsion.matches("[hw]")) {
	 * Matcher whM = Pattern.compile("(width|height)=\\+?([0-9\\.]+),").matcher(collsion); while (whM.find()) { if
	 * (whM.group(1).equals("width")) { width = Integer.parseUnsignedInt(whM.group(2)); } else { height =
	 * Integer.parseUnsignedInt(whM.group(2)); } } } else { Matcher whM =
	 * Pattern.compile("\\[\\+?([0-9\\.]+),\\+?([0-9\\.]+)\\]").matcher(collsion); if (whM.find()) { width =
	 * Integer.parseUnsignedInt(whM.group(1)); height = Integer.parseUnsignedInt(whM.group(2)); } }
	 * 
	 * this.width = width; this.height = height;
	 * 
	 * Matcher oriM = Pattern.compile("origin=([\\[\\{][0-9a-z\\,\\.][\\]\\}])").matcher(s);
	 * 
	 * String origin = oriM.find() ? oriM.group(1) : "[0,0]"; int x = 0, y = 0; if (collsion.matches("[xy]")) { Matcher xyM =
	 * Pattern.compile("([xy])=\\+?([0-9\\.]+),").matcher(origin); while (xyM.find()) { if (xyM.group(1).equals("x")) { x =
	 * Integer.parseUnsignedInt(xyM.group(2)); } else { y = Integer.parseUnsignedInt(xyM.group(2)); } } } else { Matcher xyM =
	 * Pattern.compile("\\[\\+?([0-9\\.]+),\\+?([0-9\\.]+)\\]").matcher(origin); if (xyM.find()) { x =
	 * Integer.parseUnsignedInt(xyM.group(1)); y = Integer.parseUnsignedInt(xyM.group(2)); } }
	 * 
	 * this.x = x; this.y = y; }
	 * 
	 * }
	 */

}
