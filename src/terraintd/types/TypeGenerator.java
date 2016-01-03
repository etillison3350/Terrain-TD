package terraintd.types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TypeGenerator {

	private TypeGenerator() {}

	private static TowerType[] towers;
	private static EnemyType[] enemies;
	private static ObstacleType[] obstacles;

	public static void generateValues() {
		try {
			Files.createDirectories(Paths.get("terraintd/mods"));
		} catch (IOException e) {}

		ArrayList<TowerType> newTowers = new ArrayList<>();
		ArrayList<EnemyType> newEnemies = new ArrayList<>();
		ArrayList<ObstacleType> newObstacles = new ArrayList<>();

		try (Stream<Path> files = Files.walk(Paths.get("terraintd/mods"))) {
			Iterator<Path> iter = files.iterator();
			while (iter.hasNext()) {
				Path path = iter.next();
				if (Files.isDirectory(path) || !path.toString().replaceAll(".+\\.", "").equals("proto")) continue;

				ArrayList<?> json = parseJSON(new String(Files.readAllBytes(path)));

				for (Object o : json) {
					if (!(o instanceof Map<?, ?>)) return;

					Map<?, ?> obj = (Map<?, ?>) o;

					if (obj.get("type") == null || !(obj.get("type") instanceof String)) return;

					if (obj.get("type").equals("tower")) {
						String id = (String) obj.get("id");
						if (id == null) continue;

						int cost = obj.get("cost") instanceof Number ? ((Number) obj.get("cost")).intValue() : 1;

						Object collision = obj.get("collision");
						int width = 1;
						int height = 1;
						if (collision instanceof List<?>) {
							width = ((List<?>) collision).get(0) instanceof Number ? ((Number) ((List<?>) collision).get(0)).intValue() : 1;
							height = ((List<?>) collision).get(1) instanceof Number ? ((Number) ((List<?>) collision).get(1)).intValue() : 1;
						} else if (collision instanceof Map<?, ?>) {
							width = ((Map<?, ?>) collision).get("width") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("width")).intValue() : 1;
							height = ((Map<?, ?>) collision).get("height") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("height")).intValue() : 1;
						}

						HashMap<Terrain, Boolean> terrain = new HashMap<>(Terrain.values().length);
						for (Terrain t : Terrain.values())
							terrain.put(t, false);

						if (obj.get("terrain") instanceof List<?>) {
							for (Object ter : (List<?>) obj.get("terrain")) {
								if (ter instanceof String) {
									switch ((String) ter) {
										case "all":
											for (Terrain t : Terrain.values())
												terrain.put(t, true);
											break;
										case "water":
											for (Terrain t : Terrain.values()) {
												if (t.name().toLowerCase().endsWith("water"))
													terrain.put(t, true);
											}
											break;
										case "land":
											for (Terrain t : Terrain.values()) {
												if (!t.name().toLowerCase().endsWith("water"))
													terrain.put(t, true);
											}
											break;
										default:
											try {
												terrain.put(Terrain.valueOf(((String) ter).toUpperCase()), true);
											} catch (IllegalArgumentException e) {}
											break;
									}
								}
							}
						}

						boolean onHill = obj.get("on-hill") instanceof Boolean ? (Boolean) obj.get("on-hill") : false;

						boolean rotate = obj.get("rotate") instanceof Boolean ? (Boolean) obj.get("rotate") : true;

						ImageType image = obj.get("image") instanceof Map<?, ?> ? parseImage((Map<?, ?>) obj.get("image")) : null;

						ImageType icon = obj.get("icon") instanceof Map<?, ?> ? parseImage((Map<?, ?>) obj.get("icon")) : null;

						List<ProjectileType> projectiles = new ArrayList<>();
						if (obj.get("projectiles") instanceof List<?>) {
							for (Object p : (List<?>) obj.get("projectiles")) {
								if (p instanceof Map<?, ?>)
									projectiles.add(parseProjectile((Map<?, ?>) p));
							}
						}

						newTowers.add(new TowerType(id, cost, width, height, terrain, onHill, rotate, image, icon, projectiles.toArray(new ProjectileType[projectiles.size()])));
					} else if (obj.get("type").equals("enemy")) {
						HashMap<Terrain, Double> speed = new HashMap<>();
						Object spd = obj.get("speed");
						if (spd instanceof List<?>) {
							List<Object> spl = new ArrayList<>((List<?>) spd);

							if (spl.size() == 1) {
								for (Terrain t : Terrain.values())
									speed.put(t, ((Number) spl.get(0)).doubleValue());
							} else if (spl.size() == 2) {
								if (!(spl.get(0) instanceof Number)) spl.set(0, 1.0);
								if (!(spl.get(1) instanceof Number)) spl.set(1, 1.0);

								for (Terrain t : Terrain.values())
									speed.put(t, ((Number) spl.get(t.name().toLowerCase().endsWith("water") ? 1 : 0)).doubleValue());
							} else {
								for (int i = 0; i < spl.size() && i < Terrain.values().length; i++) {
									if (spl.get(i) instanceof Number)
										speed.put(Terrain.values()[i], ((Number) spl.get(i)).doubleValue());
									else
										speed.put(Terrain.values()[i], 1.0);
								}
							}
						} else if (spd instanceof Map<?, ?>) {
							Map<?, ?> sm = (Map<?, ?>) spd;
							Map<String, Number> spm = new HashMap<>(sm.size());
							for (Object k : sm.keySet()) {
								Object v = sm.get(k);
								spm.put(k.toString(), v instanceof Number ? (Number) v : 1);
							}

							for (Terrain t : Terrain.values())
								speed.put(t, 1.0);

							for (String key : spm.keySet()) {
								switch (key) {
									case "all":
										for (Terrain t : Terrain.values())
											speed.put(t, spm.get(key).doubleValue());
										break;
									case "water":
										for (Terrain t : Terrain.values()) {
											if (t.name().toLowerCase().endsWith("water"))
												speed.put(t, spm.get(key).doubleValue());
										}
										break;
									case "land":
										for (Terrain t : Terrain.values()) {
											if (!t.name().toLowerCase().endsWith("water"))
												speed.put(t, spm.get(key).doubleValue());
										}
										break;
									default:
										try {
											speed.put(Terrain.valueOf(key.toUpperCase()), spm.get(key).doubleValue());
										} catch (IllegalArgumentException e) {}
										break;
								}
							}
						}

						Object collision = obj.get("vertical-speed");
						double upSpeed = 1;
						double downSpeed = 1;
						if (collision instanceof List<?>) {
							upSpeed = ((List<?>) collision).get(0) instanceof Number ? ((Number) ((List<?>) collision).get(0)).doubleValue() : 1;
							downSpeed = ((List<?>) collision).get(1) instanceof Number ? ((Number) ((List<?>) collision).get(1)).doubleValue() : 1;
						} else if (collision instanceof Map<?, ?>) {
							upSpeed = ((Map<?, ?>) collision).get("up") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("up")).doubleValue() : 1;
							downSpeed = ((Map<?, ?>) collision).get("down") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("down")).doubleValue() : 1;
						}

						double health = obj.get("health") instanceof Number ? ((Number) obj.get("health")).doubleValue() : 1.0;

						double damage = obj.get("damage") instanceof Number ? ((Number) obj.get("damage")).doubleValue() : 1.0;

						int reward = obj.get("reward") instanceof Number ? ((Number) obj.get("reward")).intValue() : 1;

						ImageType image = obj.get("image") instanceof Map<?, ?> ? parseImage((Map<?, ?>) obj.get("image")) : null;

						ImageType death = obj.get("death") instanceof Map<?, ?> ? parseImage((Map<?, ?>) obj.get("death")) : null;

						List<ProjectileType> projectiles = new ArrayList<>();
						if (obj.get("projectiles") instanceof List<?>) {
							for (Object p : (List<?>) obj.get("projectiles")) {
								if (p instanceof Map<?, ?>)
									projectiles.add(parseProjectile((Map<?, ?>) p));
							}
						}

						newEnemies.add(new EnemyType(speed, upSpeed, downSpeed, health, damage, reward, image, death, projectiles.toArray(new ProjectileType[projectiles.size()])));
					} else if (obj.get("type").equals("obstacle")) {
						String id = (String) obj.get("id");
						if (id == null) continue;

						Object collision = obj.get("collision");
						int width = 1;
						int height = 1;
						if (collision instanceof List<?>) {
							width = ((List<?>) collision).get(0) instanceof Number ? ((Number) ((List<?>) collision).get(0)).intValue() : 1;
							height = ((List<?>) collision).get(1) instanceof Number ? ((Number) ((List<?>) collision).get(1)).intValue() : 1;
						} else if (collision instanceof Map<?, ?>) {
							width = ((Map<?, ?>) collision).get("width") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("width")).intValue() : 1;
							height = ((Map<?, ?>) collision).get("height") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("height")).intValue() : 1;
						}

						int cost = obj.get("cost") instanceof Number ? ((Number) obj.get("cost")).intValue() : 1;

						int removeCost = obj.get("remove-cost") instanceof Number ? ((Number) obj.get("remove-cost")).intValue() : 1;

						Object spr = obj.get("spawn-rate");
						HashMap<Terrain, Double> spawnRates = new HashMap<>();
						if (spr instanceof Number) {
							for (Terrain t : Terrain.values())
								spawnRates.put(t, ((Number) spr).doubleValue());
						} else if (spr instanceof Map<?, ?>) {
							Map<?, ?> sm = (Map<?, ?>) spr;
							Map<String, Number> srm = new HashMap<>(sm.size());
							for (Object k : sm.keySet()) {
								Object v = sm.get(k);
								srm.put(k.toString(), v instanceof Number ? (Number) v : 1);
							}

							for (Terrain t : Terrain.values())
								spawnRates.put(t, 1.0);

							for (String key : srm.keySet()) {
								switch (key) {
									case "all":
										for (Terrain t : Terrain.values())
											spawnRates.put(t, srm.get(key).doubleValue());
										break;
									case "water":
										for (Terrain t : Terrain.values()) {
											if (t.name().toLowerCase().endsWith("water"))
												spawnRates.put(t, srm.get(key).doubleValue());
										}
										break;
									case "land":
										for (Terrain t : Terrain.values()) {
											if (!t.name().toLowerCase().endsWith("water"))
												spawnRates.put(t, srm.get(key).doubleValue());
										}
										break;
									default:
										try {
											spawnRates.put(Terrain.valueOf(key.toUpperCase()), srm.get(key).doubleValue());
										} catch (IllegalArgumentException e) {}
										break;
								}
							}
						} else if (spr instanceof List<?>) {
							List<Object> srl = new ArrayList<>((List<?>) spr);

							if (srl.size() == 1) {
								for (Terrain t : Terrain.values())
									spawnRates.put(t, ((Number) srl.get(0)).doubleValue());
							} else if (srl.size() == 2) {
								if (!(srl.get(0) instanceof Number)) srl.set(0, 1.0);
								if (!(srl.get(1) instanceof Number)) srl.set(1, 1.0);

								for (Terrain t : Terrain.values())
									spawnRates.put(t, ((Number) srl.get(t.name().toLowerCase().endsWith("water") ? 1 : 0)).doubleValue());
							} else {
								for (int i = 0; i < srl.size() && i < Terrain.values().length; i++) {
									if (srl.get(i) instanceof Number)
										spawnRates.put(Terrain.values()[i], ((Number) srl.get(i)).doubleValue());
									else
										spawnRates.put(Terrain.values()[i], 1.0);
								}
							}
						}

						ImageType image = obj.get("image") instanceof Map<?, ?> ? parseImage((Map<?, ?>) obj.get("image")) : null;

						ImageType icon = obj.get("icon") instanceof Map<?, ?> ? parseImage((Map<?, ?>) obj.get("icon")) : null;

						newObstacles.add(new ObstacleType(id, width, height, cost, removeCost, spawnRates, image, icon));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		towers = newTowers.toArray(new TowerType[newTowers.size()]);
		enemies = newEnemies.toArray(new EnemyType[newEnemies.size()]);
		obstacles = newObstacles.toArray(new ObstacleType[newObstacles.size()]);
	}

	static ProjectileType parseProjectile(Map<?, ?> map) {
		DeliveryType delivery = DeliveryType.SINGLE_TARGET;
		try {
			delivery = DeliveryType.valueOf(((String) map.get("delivery")).toUpperCase());
		} catch (Exception e) {}

		double explodeRadius = map.get("explosion-radius") instanceof Number ? ((Number) map.get("explosion-radius")).doubleValue() : 0;

		double speed = map.get("speed") instanceof Number ? ((Number) map.get("speed")).doubleValue() : 1;

		Object dmg = map.get("damage");
		double damage = 1;
		double far = 1;
		if (dmg instanceof List<?>) {
			damage = ((List<?>) dmg).get(0) instanceof Number ? ((Number) ((List<?>) dmg).get(0)).doubleValue() : 1;
			far = ((List<?>) dmg).get(1) instanceof Number ? ((Number) ((List<?>) dmg).get(1)).doubleValue() : 1;
		} else if (dmg instanceof Map<?, ?>) {
			damage = ((Map<?, ?>) dmg).get("min") instanceof Number ? ((Number) ((Map<?, ?>) dmg).get("min")).intValue() : 1;
			far = ((Map<?, ?>) dmg).get("max") instanceof Number ? ((Number) ((Map<?, ?>) dmg).get("max")).intValue() : 1;
		}

		double falloff = damage - far;

		double rate = map.get("rate") instanceof Number ? ((Number) map.get("rate")).doubleValue() : 1;

		boolean follow = map.get("follow") instanceof Boolean ? (Boolean) map.get("follow") : true;

		double range = map.get("range") instanceof Number ? ((Number) map.get("range")).doubleValue() : 1;

		Object md = map.get("max-dist");
		double maxDist = md instanceof Number ? ((Number) md).doubleValue() : (md instanceof String && ((String) md).matches("^inf(?:init[ey])?$") ? 1e300 : 1);

		double angle = map.get("angle") instanceof Number ? ((Number) map.get("angle")).doubleValue() : 0;

		double rotation = map.get("rotation") instanceof Number ? ((Number) map.get("rotation")).doubleValue() : 0;

		boolean absRotation = map.get("abs-rotation") instanceof Boolean ? (Boolean) map.get("abs-rotation") : false;

		double dyingFadeTime = map.get("dying-fade-time") instanceof Number ? ((Number) map.get("dying-fade-time")).doubleValue() : 0.1;

		boolean dyingFade = map.get("dying-fade") instanceof Boolean ? (Boolean) map.get("dying-fade") : true;

		boolean damageFade = map.get("damage-fade") instanceof Boolean ? (Boolean) map.get("damage-fade") : true;

		ImageType image = map.get("image") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("image")) : null;

		ImageType explosion = map.get("explosion") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("explosion")) : null;

		List<EffectType> effects = new ArrayList<>();
		if (map.get("effects") instanceof List<?>) {
			for (Object p : (List<?>) map.get("effects")) {
				if (p instanceof Map<?, ?>)
					effects.add(parseEffect((Map<?, ?>) p));
			}
		}

		return new ProjectileType(delivery, explodeRadius, speed, damage, falloff, rate, follow, range, maxDist, angle, rotation, absRotation, dyingFadeTime, dyingFade, damageFade, image, explosion, effects.toArray(new EffectType[effects.size()]));
	}

	static EffectType parseEffect(Map<?, ?> map) {
		StatusEffectType type = StatusEffectType.valueOf(((String) map.get("type")).toUpperCase());

		double duration = map.get("duration") instanceof Number ? ((Number) map.get("duration")).doubleValue() : 1;

		double amplifier = map.get("amplifier") instanceof Number ? ((Number) map.get("amplifier")).doubleValue() : 1;

		return new EffectType(type, duration, amplifier);
	}

	static ImageType parseImage(Map<?, ?> map) {
		String src = (String) map.get("src");
		if (src == null) return null;

		Object collision = map.get("collision");
		double width = 1;
		double height = 1;
		if (collision instanceof List<?>) {
			width = ((List<?>) collision).get(0) instanceof Number ? ((Number) ((List<?>) collision).get(0)).doubleValue() : 1;
			height = ((List<?>) collision).get(1) instanceof Number ? ((Number) ((List<?>) collision).get(1)).doubleValue() : 1;
		} else if (collision instanceof Map<?, ?>) {
			width = ((Map<?, ?>) collision).get("width") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("width")).doubleValue() : 1;
			height = ((Map<?, ?>) collision).get("height") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("height")).doubleValue() : 1;
		}

		Object origin = map.get("origin");
		double x = 1;
		double y = 1;
		if (origin instanceof List<?>) {
			x = ((List<?>) origin).get(0) instanceof Number ? ((Number) ((List<?>) origin).get(0)).doubleValue() : 1;
			y = ((List<?>) origin).get(1) instanceof Number ? ((Number) ((List<?>) origin).get(1)).doubleValue() : 1;
		} else if (origin instanceof Map<?, ?>) {
			x = ((Map<?, ?>) origin).get("x") instanceof Number ? ((Number) ((Map<?, ?>) origin).get("x")).doubleValue() : 1;
			y = ((Map<?, ?>) origin).get("y") instanceof Number ? ((Number) ((Map<?, ?>) origin).get("y")).doubleValue() : 1;
		}

		return new ImageType(src, width, height, x, y);
	}

	static ArrayList<Object> parseJSON(String json) {
		HashMap<String, Object> braceObjs = new HashMap<>();

		String s = json;

		int lastAddress = (int) (System.currentTimeMillis() % 32768);

		Pattern quotes = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");
		Matcher m = quotes.matcher(s);
		while (m.find()) {
			String v = m.group(1);
			String k = "@" + lastAddress++;

			braceObjs.put(k, v.replaceAll("\\\\(.)", "$1").toLowerCase());
			s = s.replace(m.group(), k);

			m = quotes.matcher(s);
		}

		s = s.replaceAll("\\s+", "");

		Pattern braces = Pattern.compile("\\[[^\\[\\]\\{\\}]*\\]|\\{[^\\[\\]\\{\\}]*?\\}");

		m = braces.matcher(s);
		while (m.find()) {
			String g = m.group();
			if (g.charAt(0) == '[') {
				String[] terms = g.split("[\\[\\]\\,]");
				ArrayList<Object> termList = new ArrayList<>(terms.length);
				for (String term : terms) {
					if (term.isEmpty()) continue;

					if (term.startsWith("\"")) {
						termList.add(term.substring(1, term.length() - 1));
					} else if (term.startsWith("@")) {
						termList.add(braceObjs.get(term));
					} else if (term.equals("true")) {
						termList.add(true);
					} else if (term.equals("false")) {
						termList.add(false);
					} else {
						try {
							termList.add(Double.parseDouble(term));
						} catch (NumberFormatException e) {
							termList.add(null);
						}
					}
				}

				String k = "@" + lastAddress++;

				s = s.replace(g, k);
				if (s.equals(k)) return termList;
				braceObjs.put(k, termList);
			} else {
				String[] terms = g.split("[\\{\\}\\,]");
				Map<String, Object> termList = new HashMap<>();
				for (String term : terms) {
					if (term.isEmpty()) continue;

					String[] kvs = term.split(":");
					String k = (kvs[0].startsWith("@") ? (String) braceObjs.get(kvs[0]) : kvs[0].replace("\"", "")).toLowerCase();
					if (kvs[1].startsWith("\"")) {
						termList.put(k, kvs[1].substring(1, kvs[1].length() - 1));
					} else if (kvs[1].startsWith("@")) {
						termList.put(k, braceObjs.get(kvs[1]));
					} else if (kvs[1].equals("true")) {
						termList.put(k, true);
					} else if (kvs[1].equals("false")) {
						termList.put(k, false);
					} else {
						try {
							termList.put(k, Double.parseDouble(kvs[1]));
						} catch (NumberFormatException e) {
							termList.put(k, null);
						}
					}
				}

				String k = "@" + lastAddress++;

				s = s.replace(g, k);
				if (s.equals(k)) {
					ArrayList<Object> ret = new ArrayList<>(1);
					ret.add(termList);
					return ret;
				}
				braceObjs.put(k, termList);
			}

			m = braces.matcher(s);
		}

		return null;
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

}
