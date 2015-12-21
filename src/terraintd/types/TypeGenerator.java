package terraintd.types;

import java.io.File;
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

//	/**
//	 * <ul>
//	 * <li><b><i>generateValues</i></b><br>
//	 * <br>
//	 * {@code public static void generateValues()}<br>
//	 * <br>
//	 * Reads the mods folder for towers/enemies/obstacles. The values are stored, and need not be calculated more than once.<br>
//	 * </ul>
//	 */
//	@Deprecated
//	public static void generateValuesOld() {
//		try {
//			Files.createDirectories(Paths.get("terraintd/mods"));
//		} catch (IOException e) {}
//
//		List<String> prototypes = new ArrayList<>();
//
//		Pattern p = Pattern.compile("([\\{\\}])[^\\{\\}]*");
//
//		try (Stream<Path> files = Files.walk(Paths.get("terraintd/mods"))) {
//			Iterator<Path> iter = files.iterator();
//			while (iter.hasNext()) {
//				Path path = iter.next();
//				if (Files.isDirectory(path) || !path.toString().replaceAll(".+\\.", "").equals("proto")) continue;
//
//				List<String> protos = new ArrayList<>();
//
//				String s = new String(Files.readAllBytes(path)).replaceAll("(?:\\-\\-|#).+", "");
//
//				Matcher m = p.matcher(s);
//
//				int indent = 0;
//				String str = "";
//				while (m.find()) {
//					str += m.group();
//					indent -= m.group(1).charAt(0) - 124;
//					if (indent == 0) {
//						protos.add(str.replaceAll("\\s", "").replaceAll("([^\\,])([\\]\\}])", "$1,$2"));
//						str = "";
//					}
//				}
//
//				if (indent == 0) {
//					prototypes.addAll(protos);
//				}
//			}
//		} catch (IOException e) {}
//
//		if (prototypes.size() == 0) {
//			try {
//				Files.createDirectories(Paths.get("terraintd/mods/base"));
//				Files.write(Files.createFile(Paths.get("terraintd/mods/base/tower.proto")), "{\n\ttype=tower,\n\tid=tower,\n}".getBytes());
//				// TODO
//			} catch (IOException e) {}
//
//			generateValuesOld();
//			return;
//		}
//
//		ArrayList<TowerType> newTowers = new ArrayList<>();
//		ArrayList<EnemyType> newEnemies = new ArrayList<>();
//		ArrayList<ObstacleType> newObstacles = new ArrayList<>();
//
//		Pattern pSq = Pattern.compile("([\\[\\]])[^\\[\\]]*");
//
//		Pattern p1 = Pattern.compile("([a-z\\-_\\+]+)\\=([\\[\\{])");
//		Pattern p2 = Pattern.compile("([a-z\\-_\\+]+)\\=([0-9A-Za-z_\\-/\"\\.]+),");
//
//		for (String s : prototypes) {
//			HashMap<String, String> properties = new HashMap<>();
//
//			Matcher m = p1.matcher(s);
//
//			String s2 = s;
//
//			while (m.find()) {
//				final int charNum = m.group(2).charAt(0) + 1;
//
//				int indent = 0;
//
//				String str = "";
//
//				Matcher indM = (charNum == 124 ? p : pSq).matcher(s.substring(m.start()));
//
//				while (true) {
//					if (!indM.find()) break;
//
//					str += indM.group();
//
//					indent -= indM.group(1).charAt(0) - charNum;
//					if (indent == 0) {
//						properties.put(m.group(1).replaceAll("[\\-_]", "").toLowerCase(), str.substring(0, str.lastIndexOf(charNum + 1) + 1));
//						s2 = s2.replace(str, "");
//						break;
//					}
//				}
//			}
//
//			m = p2.matcher(s2);
//
//			while (m.find()) {
//				properties.put(m.group(1).replaceAll("[\\-_]", "").toLowerCase(), m.group(2).replaceAll("\"", "").toLowerCase());
//			}
//
//			String type = properties.get("type");
//
//			if (type == null) {
//				continue;
//			} else if (type.equals("tower")) {
//				try {
//					String id = properties.get("id");
//					if (id == null) continue;
//
//					int cost = Integer.parseUnsignedInt(properties.getOrDefault("cost", "1"));
//
//					String collision = properties.getOrDefault("collision", "[1,1,]");
//					int width = 1, height = 1;
//					if (collision.matches("[hw]")) {
//						Matcher whM = Pattern.compile("(width|height)=\\+?([0-9]+),").matcher(collision);
//						while (whM.find()) {
//							if (whM.group(1).equals("width")) {
//								width = Integer.parseUnsignedInt(whM.group(2));
//							} else {
//								height = Integer.parseUnsignedInt(whM.group(2));
//							}
//						}
//					} else {
//						Matcher whM = Pattern.compile("\\[\\+?([0-9]+),\\+?([0-9]+)\\,?\\]").matcher(collision);
//						if (whM.find()) {
//							width = Integer.parseUnsignedInt(whM.group(1));
//							height = Integer.parseUnsignedInt(whM.group(2));
//						}
//					}
//
//					HashMap<Terrain, Boolean> terrain = new HashMap<>(Terrain.values().length);
//
//					for (Terrain terra : Terrain.values()) {
//						terrain.put(terra, false);
//					}
//
//					for (String terra : properties.getOrDefault("terrain", "land").replaceAll("[\\[\\]]", "").split("\\,")) {
//						if (terra.equals("all")) {
//							for (Terrain ter : Terrain.values()) {
//								terrain.put(ter, true);
//							}
//						} else if (terra.equals("land") || terra.equals("water")) {
//							boolean land = terra.charAt(0) == 'l';
//							for (Terrain ter : Terrain.values()) {
//								if (ter.name().contains("WATER") != land) {
//									terrain.put(ter, true);
//								}
//							}
//						} else {
//							try {
//								Terrain ter = Terrain.valueOf(terra);
//								if (ter != null) {
//									terrain.put(ter, true);
//								}
//							} catch (IllegalArgumentException e) {}
//						}
//					}
//
//					boolean onHill = Boolean.parseBoolean(properties.getOrDefault("onhill", "true"));
//					boolean rotate = Boolean.parseBoolean(properties.getOrDefault("rotate", "true"));
//					ImageType image = new ImageType(properties.getOrDefault("image", ""));
//					ImageType icon = new ImageType(properties.getOrDefault("icon", ""));
//
//					ProjectileType[] projectiles;
//					if (properties.get("projectiles") != null) {
//						ArrayList<ProjectileType> projList = new ArrayList<>();
//
//						String projStr = properties.get("projectiles");
//
//						List<String> projs = new ArrayList<>();
//
//						Matcher pm = p.matcher(projStr);
//
//						int indent = 0;
//						String str = "";
//						while (pm.find()) {
//							str += pm.group();
//							indent -= pm.group(1).charAt(0) - 124;
//							if (indent == 0) {
//								projs.add(str.replaceAll("\\s", "").replaceAll("([^\\,])([\\]\\}])", "$1,$2"));
//								str = "";
//							}
//						}
//
//						if (indent != 0) {
//							projectiles = new ProjectileType[0];
//						} else {
//							for (String proj : projs) {
//								projList.add(new ProjectileType(proj));
//							}
//
//							projectiles = projList.toArray(new ProjectileType[projList.size()]);
//						}
//					} else {
//						projectiles = new ProjectileType[0];
//					}
//
//					newTowers.add(new TowerType(id, cost, width, height, terrain, onHill, rotate, image, icon, projectiles));
//				} catch (Exception e) {
//					JOptionPane.showMessageDialog(null, e.getMessage(), "Error reading prototype", JOptionPane.ERROR_MESSAGE);
//				}
//			} else if (type.equals("enemy")) {
//				try {
//					String speed = properties.getOrDefault("speed", "{land=1,water=0}");
//
//					HashMap<Terrain, Double> speeds = new HashMap<>();
//
//					if (speed.contains("=")) {
//						Matcher spdM = p2.matcher(speed);
//
//						while (spdM.find()) {
//							String terra = spdM.group(1);
//							if (terra.equals("all")) {
//								for (Terrain ter : Terrain.values()) {
//									speeds.put(ter, Double.parseDouble(spdM.group(2)));
//								}
//							}
//							if (terra.equals("land") || terra.equals("water")) {
//								boolean land = terra.charAt(0) == 'l';
//								for (Terrain ter : Terrain.values()) {
//									if (ter.name().contains("WATER") != land) {
//										speeds.put(ter, Double.parseDouble(spdM.group(2)));
//									}
//								}
//							} else {
//								try {
//									Terrain ter = Terrain.valueOf(terra);
//									if (ter != null) {
//										speeds.put(ter, Double.parseDouble(spdM.group(2)));
//									}
//								} catch (IllegalArgumentException e) {}
//							}
//						}
//					} else {
//						String[] spdsa = speed.split("[\\[\\{,\\}\\]]"), spds;
//						if (spdsa.length > 0 && spdsa[0].isEmpty()) {
//							spds = new String[spdsa.length - 1];
//							System.arraycopy(spdsa, 1, spds, 0, spds.length);
//						} else {
//							spds = spdsa;
//						}
//
//						switch (spds.length) {
//							case 1:
//								for (Terrain ter : Terrain.values()) {
//									speeds.put(ter, Double.parseDouble(spds[0]));
//								}
//								break;
//							case 2:
//								for (int l = 0; l < 2; l++) {
//									for (Terrain ter : Terrain.values()) {
//										if (ter.name().contains("WATER") != (l == 0)) {
//											speeds.put(ter, Double.parseDouble(spds[l]));
//										}
//									}
//								}
//								break;
//							default:
//								for (int i = 0; i < spds.length; i++) {
//									speeds.put(Terrain.values()[i], Double.parseDouble(spds[i]));
//								}
//								break;
//						}
//					}
//
//					String vertSpeed = properties.getOrDefault("verticalspeed", "[1,1,]");
//					int upSpeed = 1, downSpeed = 1;
//					if (vertSpeed.matches("[ud]")) {
//						Matcher whM = Pattern.compile("(up|down)=\\+?([0-9]+),").matcher(vertSpeed);
//						while (whM.find()) {
//							if (whM.group(1).equals("up")) {
//								upSpeed = Integer.parseUnsignedInt(whM.group(2));
//							} else {
//								downSpeed = Integer.parseUnsignedInt(whM.group(2));
//							}
//						}
//					} else {
//						Matcher whM = Pattern.compile("\\[\\+?([0-9]+),\\+?([0-9]+)\\,?\\]").matcher(vertSpeed);
//						if (whM.find()) {
//							upSpeed = Integer.parseUnsignedInt(whM.group(1));
//							downSpeed = Integer.parseUnsignedInt(whM.group(2));
//						}
//					}
//
//					double health = Double.parseDouble(properties.getOrDefault("health", "1"));
//					double damage = Double.parseDouble(properties.getOrDefault("damage", "0"));
//					int reward = Integer.parseInt(properties.getOrDefault("reward", "0"));
//					ImageType image = new ImageType(properties.getOrDefault("image", ""));
//					ImageType death = new ImageType(properties.getOrDefault("death", ""));
//
//					ProjectileType[] projectiles;
//					if (properties.get("projectiles") != null) {
//						ArrayList<ProjectileType> projList = new ArrayList<>();
//
//						String projStr = properties.get("projectiles");
//
//						List<String> projs = new ArrayList<>();
//
//						Matcher pm = p.matcher(projStr);
//
//						int indent = 0;
//						String str = "";
//						while (pm.find()) {
//							str += pm.group();
//							indent -= pm.group(1).charAt(0) - 124;
//							if (indent == 0) {
//								projs.add(str.replaceAll("\\s", "").replaceAll("([^\\,])([\\]\\}])", "$1,$2"));
//								str = "";
//							}
//						}
//
//						if (indent != 0) {
//							projectiles = new ProjectileType[0];
//						} else {
//							for (String proj : projs) {
//								projList.add(new ProjectileType(proj));
//							}
//
//							projectiles = projList.toArray(new ProjectileType[projList.size()]);
//						}
//					} else {
//						projectiles = new ProjectileType[0];
//					}
//
//					newEnemies.add(new EnemyType(speeds, upSpeed, downSpeed, health, damage, reward, image, death, projectiles));
//				} catch (Exception e) {
//					JOptionPane.showMessageDialog(null, e.getMessage(), "Error reading prototype", JOptionPane.ERROR_MESSAGE);
//				}
//			} else if (type.equals("obstacle")) {
//				try {
//					String id = properties.get("id");
//					if (id == null) continue;
//
//					int cost = Integer.parseUnsignedInt(properties.getOrDefault("cost", "1"));
//
//					String collsion = properties.getOrDefault("collsion", "[1,1,]");
//					int width = 1, height = 1;
//					if (collsion.matches("[hw]")) {
//						Matcher whM = Pattern.compile("(width|height)=\\+?([0-9]+),").matcher(collsion);
//						while (whM.find()) {
//							if (whM.group(1).equals("width")) {
//								width = Integer.parseUnsignedInt(whM.group(2));
//							} else {
//								height = Integer.parseUnsignedInt(whM.group(2));
//							}
//						}
//					} else {
//						Matcher whM = Pattern.compile("\\[\\+?([0-9]+),\\+?([0-9]+)\\,?\\]").matcher(collsion);
//						if (whM.find()) {
//							width = Integer.parseUnsignedInt(whM.group(1));
//							height = Integer.parseUnsignedInt(whM.group(2));
//						}
//					}
//
//					double spawnRate = Double.parseDouble(properties.getOrDefault("spawnrate", "0"));
//					ImageType image = new ImageType(properties.getOrDefault("image", ""));
//					ImageType icon = new ImageType(properties.getOrDefault("icon", ""));
//
//					newObstacles.add(new ObstacleType(id, width, height, cost, spawnRate, image, icon));
//				} catch (Exception e) {
//					JOptionPane.showMessageDialog(null, e.getMessage(), "Error reading prototype", JOptionPane.ERROR_MESSAGE);
//				}
//			}
//
//		}
//
//		towers = newTowers.toArray(new TowerType[newTowers.size()]);
//		enemies = newEnemies.toArray(new EnemyType[newEnemies.size()]);
//		obstacles = newObstacles.toArray(new ObstacleType[newObstacles.size()]);
//	}

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
						/*
						 * type=enemy, speed=[1,1,1,1,1,1,1,0], vertical-speed=[1,1], health=100, damage=10, reward=25,
						 * image={see images}, death={see images}, projectiles=[see projectiles]
						 */

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

						Object collision = obj.get("collision");
						double upSpeed = 1;
						double downSpeed = 1;
						if (collision instanceof List<?>) {
							upSpeed = ((List<?>) collision).get(0) instanceof Number ? ((Number) ((List<?>) collision).get(0)).intValue() : 1;
							downSpeed = ((List<?>) collision).get(1) instanceof Number ? ((Number) ((List<?>) collision).get(1)).intValue() : 1;
						} else if (collision instanceof Map<?, ?>) {
							upSpeed = ((Map<?, ?>) collision).get("up") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("up")).intValue() : 1;
							downSpeed = ((Map<?, ?>) collision).get("down") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("down")).intValue() : 1;
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
		
//		System.out.println("Q" + new File(src));
		
		// TODO
		double width = 1;
		double height = 1;
		double x = 0;
		double y = 0;
		
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
