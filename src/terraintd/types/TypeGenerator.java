package terraintd.types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import terraintd.files.JSON;
import terraintd.files.ModListReader;
import terraintd.pathfinder.Node;
import terraintd.pathfinder.PathFinder;
import terraintd.types.Level.Unit;
import terraintd.types.World.Tile;

public final class TypeGenerator {

	private static final Path MODS_PATH = Paths.get("terraintd/mods");

	private TypeGenerator() {}

	private static Mod[] mods;
	
	private static TowerType[] towers;
	private static EnemyType[] enemies;
	private static ObstacleType[] obstacles;
	private static World[] worlds;
	private static Level[] levels;

	public static void generateValues() {
		try {
			Files.createDirectories(MODS_PATH);
		} catch (IOException e) {}

		ModListReader.read();
		
		List<Mod> mods = new ArrayList<>();

		try {
			Files.walk(MODS_PATH, 1).filter(Files::isDirectory).forEach(p -> {
				try {
					Path path = p.resolve("info.json");

					List<?> json = JSON.parseJSON(new String(Files.readAllBytes(path)));

					if (!(json.get(0) instanceof Map<?, ?>)) return;

					Map<?, ?> mod = (Map<?, ?>) json.get(0);

					String id = (String) mod.get("id");
					if (id == null || id.isEmpty()) return;

					String version = String.format("%s", mod.get("version"));

					List<String> authors = new ArrayList<>();
					if (mod.get("authors") instanceof List<?>) ((List<?>) mod.get("authors")).stream().forEach(o -> authors.add(String.format("%s", o)));

					String contact = String.format("%s", mod.get("contact"));

					String homepage = String.format("%s", mod.get("homepage"));

					String description = String.format("%s", mod.get("description"));

					String icon = String.format("%s", mod.get("icon"));
					
					mods.add(new Mod(id, p, version, authors.toArray(new String[authors.size()]), contact, homepage, description, icon));
				} catch (Exception e) {}
			});
		} catch (IOException e) {}

		mods.sort(new Comparator<Mod>() {

			@Override
			public int compare(Mod o1, Mod o2) {
				if (o1.id.equals("base")) {
					return -1;
				} else if (o2.id.equals("base")) {
					return 1;
				} else {
					return o1.id.compareTo(o2.id);
				}
			}
			
		});
		
		List<TowerType> newTowers = new ArrayList<>();
		List<EnemyType> newEnemies = new ArrayList<>();
		List<ObstacleType> newObstacles = new ArrayList<>();
		List<World> newWorlds = new ArrayList<>();
		List<Level> newLevels = new ArrayList<>();

		for (Mod mod : mods) {
			if (!ModListReader.isEnabled(mod.id)) continue;
			
			try (Stream<Path> files = Files.walk(mod.path)) {
				files.forEach(path -> {
					if (Files.isDirectory(path) || !path.toString().replaceAll(".+\\.", "").equals("json") || path.getFileName().toString().equals("info.json")) return;

					List<?> json;
					try {
						json = JSON.parseJSON(new String(Files.readAllBytes(path)));
					} catch (Exception e) {
						return;
					}

					if (json.size() == 1 && json.get(0) instanceof List<?>) json = (ArrayList<?>) json.get(0);

					for (Object o : json) {
						if (!(o instanceof Map<?, ?>)) continue;

						Map<?, ?> obj = (Map<?, ?>) o;

						if (obj.get("type") == null || !(obj.get("type") instanceof String)) continue;

						try {
							if (obj.get("type").equals("tower")) {
								newTowers.add(parseTower(obj, mod));
							} else if (obj.get("type").equals("enemy")) {
								newEnemies.add(parseEnemy(obj, mod));
							} else if (obj.get("type").equals("obstacle")) {
								newObstacles.add(parseObstacle(obj, mod));
							} else if (obj.get("type").equals("world")) {
								newWorlds.add(parseWorld(obj, mod));
							} else if (obj.get("type").equals("level")) {
								newLevels.add(parseLevel(obj, mod));
							}
						} catch (Exception e) {}
					}
				});
			} catch (IOException e) {}
		}

		ModListReader.write();
		
		TypeGenerator.mods = mods.toArray(new Mod[mods.size()]);
		
		towers = newTowers.toArray(new TowerType[newTowers.size()]);
		enemies = newEnemies.toArray(new EnemyType[newEnemies.size()]);
		obstacles = newObstacles.toArray(new ObstacleType[newObstacles.size()]);
		worlds = newWorlds.toArray(new World[newWorlds.size()]);
		levels = newLevels.toArray(new Level[newLevels.size()]);
	}

	static TowerType parseTower(Map<?, ?> map, Mod mod) {
		String id = (String) map.get("id");
		if (id == null) throw new IllegalArgumentException();

		int cost = map.get("cost") instanceof Number ? ((Number) map.get("cost")).intValue() : 1;
		
		int sellCost = map.get("sell-cost") instanceof Number ? ((Number) map.get("sell-cost")).intValue() : (int) (cost * 0.75);

		Object collision = map.get("collision");
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

		if (map.get("terrain") instanceof List<?>) {
			for (Object ter : (List<?>) map.get("terrain")) {
				if (ter instanceof String) {
					switch ((String) ter) {
						case "all":
							for (Terrain t : Terrain.values())
								terrain.put(t, true);
							break;
						case "water":
							for (Terrain t : Terrain.values()) {
								if (t.name().toLowerCase().endsWith("water")) terrain.put(t, true);
							}
							break;
						case "land":
							for (Terrain t : Terrain.values()) {
								if (!t.name().toLowerCase().endsWith("water")) terrain.put(t, true);
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

		boolean onHill = map.get("on-hill") instanceof Boolean ? (Boolean) map.get("on-hill") : false;

		boolean rotate = map.get("rotate") instanceof Boolean ? (Boolean) map.get("rotate") : true;

		double range = map.get("range") instanceof Number ? ((Number) map.get("range")).doubleValue() : 1;

		ImageType image = map.get("image") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("image"), mod) : null;

		ImageType icon = map.get("icon") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("icon"), mod) : null;

		List<ProjectileType> projectiles = new ArrayList<>();
		if (map.get("projectiles") instanceof List<?>) {
			for (Object p : (List<?>) map.get("projectiles")) {
				if (p instanceof Map<?, ?>) projectiles.add(parseProjectile((Map<?, ?>) p, mod));
			}
		}

		return new TowerType(mod, id, cost, sellCost, width, height, terrain, onHill, range, rotate, image, icon, projectiles.toArray(new ProjectileType[projectiles.size()]));
	}

	static EnemyType parseEnemy(Map<?, ?> map, Mod mod) {
		String id = (String) map.get("id");
		if (id == null) throw new IllegalArgumentException();

		HashMap<Terrain, Double> speed = new HashMap<>();
		Object spd = map.get("speed");
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
							if (t.name().toLowerCase().endsWith("water")) speed.put(t, spm.get(key).doubleValue());
						}
						break;
					case "land":
						for (Terrain t : Terrain.values()) {
							if (!t.name().toLowerCase().endsWith("water")) speed.put(t, spm.get(key).doubleValue());
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

		Object collision = map.get("vertical-speed");
		double upSpeed = 1;
		double downSpeed = 1;
		if (collision instanceof List<?>) {
			upSpeed = ((List<?>) collision).get(0) instanceof Number ? ((Number) ((List<?>) collision).get(0)).doubleValue() : 1;
			downSpeed = ((List<?>) collision).get(1) instanceof Number ? ((Number) ((List<?>) collision).get(1)).doubleValue() : 1;
		} else if (collision instanceof Map<?, ?>) {
			upSpeed = ((Map<?, ?>) collision).get("up") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("up")).doubleValue() : 1;
			downSpeed = ((Map<?, ?>) collision).get("down") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("down")).doubleValue() : 1;
		}

		double health = map.get("health") instanceof Number ? ((Number) map.get("health")).doubleValue() : 1.0;

		double damage = map.get("damage") instanceof Number ? ((Number) map.get("damage")).doubleValue() : 1.0;

		int reward = map.get("reward") instanceof Number ? ((Number) map.get("reward")).intValue() : 1;

		double range = map.get("range") instanceof Number ? ((Number) map.get("range")).doubleValue() : 1;

		Object healthBar = map.get("health-bar");
		double hbWidth = 1, hbY = PathFinder.SQRT2D2;
		if (healthBar instanceof Map<?, ?>) {
			Map<?, ?> hb = (Map<?, ?>) healthBar;

			if (hb.get("width") instanceof Number) hbWidth = ((Number) hb.get("width")).doubleValue();
			if (hb.get("offset") instanceof Number) hbY = ((Number) hb.get("offset")).doubleValue();
		}

		ImageType image = map.get("image") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("image"), mod) : null;

		ImageType death = map.get("death") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("death"), mod) : null;

		List<ProjectileType> projectiles = new ArrayList<>();
		if (map.get("projectiles") instanceof List<?>) {
			for (Object p : (List<?>) map.get("projectiles")) {
				if (p instanceof Map<?, ?>) projectiles.add(parseProjectile((Map<?, ?>) p, mod));
			}
		}

		return new EnemyType(mod, id, speed, upSpeed, downSpeed, health, damage, reward, range, hbWidth, hbY, image, death, projectiles.toArray(new ProjectileType[projectiles.size()]));
	}

	static ObstacleType parseObstacle(Map<?, ?> map, Mod mod) {
		String id = (String) map.get("id");
		if (id == null) throw new IllegalArgumentException();

		Object collision = map.get("collision");
		int width = 1;
		int height = 1;
		if (collision instanceof List<?>) {
			width = ((List<?>) collision).get(0) instanceof Number ? ((Number) ((List<?>) collision).get(0)).intValue() : 1;
			height = ((List<?>) collision).get(1) instanceof Number ? ((Number) ((List<?>) collision).get(1)).intValue() : 1;
		} else if (collision instanceof Map<?, ?>) {
			width = ((Map<?, ?>) collision).get("width") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("width")).intValue() : 1;
			height = ((Map<?, ?>) collision).get("height") instanceof Number ? ((Number) ((Map<?, ?>) collision).get("height")).intValue() : 1;
		}

		int cost = map.get("cost") instanceof Number ? ((Number) map.get("cost")).intValue() : 1;

		int removeCost = map.get("sell-cost") instanceof Number ? ((Number) map.get("sell-cost")).intValue() : (int) (cost * 0.75);

		Object spr = map.get("spawn-rate");
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
							if (t.name().toLowerCase().endsWith("water")) spawnRates.put(t, srm.get(key).doubleValue());
						}
						break;
					case "land":
						for (Terrain t : Terrain.values()) {
							if (!t.name().toLowerCase().endsWith("water")) spawnRates.put(t, srm.get(key).doubleValue());
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

		ImageType image = map.get("image") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("image"), mod) : null;

		ImageType icon = map.get("icon") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("icon"), mod) : null;

		return new ObstacleType(mod, id, width, height, cost, removeCost, spawnRates, image, icon);

	}

	static ProjectileType parseProjectile(Map<?, ?> map, Mod mod) {
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

		Object md = map.get("max-dist");
		double maxDist = md instanceof Number ? ((Number) md).doubleValue() : (md instanceof String && ((String) md).matches("^inf(?:init[ey])?$") ? 1e300 : 1);

		double offset = map.get("offset") instanceof Number ? ((Number) map.get("offset")).doubleValue() : 0;
		
		double angle = map.get("angle") instanceof Number ? ((Number) map.get("angle")).doubleValue() : 0;

		double rotation = map.get("rotation") instanceof Number ? ((Number) map.get("rotation")).doubleValue() : 0;

		boolean absRotation = map.get("abs-rotation") instanceof Boolean ? (Boolean) map.get("abs-rotation") : false;

		double dyingFadeTime = map.get("dying-fade-time") instanceof Number ? ((Number) map.get("dying-fade-time")).doubleValue() : 0.1;

		boolean dyingFade = map.get("dying-fade") instanceof Boolean ? (Boolean) map.get("dying-fade") : true;

		boolean damageFade = map.get("damage-fade") instanceof Boolean ? (Boolean) map.get("damage-fade") : true;

		ImageType image = map.get("image") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("image"), mod) : null;

		ImageType explosion = map.get("explosion") instanceof Map<?, ?> ? parseImage((Map<?, ?>) map.get("explosion"), mod) : null;

		List<EffectType> effects = new ArrayList<>();
		if (map.get("effects") instanceof List<?>) {
			for (Object p : (List<?>) map.get("effects")) {
				if (p instanceof Map<?, ?>) {
					try {
						effects.add(parseEffect((Map<?, ?>) p));
					} catch (Exception e) {}
				}
			}
		}

		return new ProjectileType(delivery, explodeRadius, speed, damage, falloff, rate, follow, maxDist, offset, angle, rotation, absRotation, dyingFadeTime, dyingFade, damageFade, image, explosion, effects.toArray(new EffectType[effects.size()]));
	}

	static EffectType parseEffect(Map<?, ?> map) {
		StatusEffectType type = StatusEffectType.valueOf(((String) map.get("type")).toUpperCase());

		double duration = map.get("duration") instanceof Number ? ((Number) map.get("duration")).doubleValue() : 1;

		double amplifier = map.get("amplifier") instanceof Number ? ((Number) map.get("amplifier")).doubleValue() : 1;

		if (amplifier <= 0) throw new IllegalArgumentException();

		return new EffectType(type, duration, amplifier);
	}

	static ImageType parseImage(Map<?, ?> map, Mod mod) {
		String src = (String) map.get("src");
		if (src == null) return null;

		double width = map.get("width") instanceof Number ? ((Number) map.get("width")).doubleValue() : 1;
		double height = map.get("height") instanceof Number ? ((Number) map.get("height")).doubleValue() : 1;

		double x = map.get("x") instanceof Number ? ((Number) map.get("x")).doubleValue() : 0;
		double y = map.get("y") instanceof Number ? ((Number) map.get("y")).doubleValue() : 0;

		return new ImageType(mod.path.resolve(src), width, height, x, y);
	}

	static World parseWorld(Map<?, ?> map, Mod mod) {
		String id = (String) map.get("id");
		if (id == null || !(map.get("terrain") instanceof List<?>)) throw new IllegalArgumentException();

		List<?> terrain = (List<?>) map.get("terrain");

		while (!(terrain.get(0) instanceof List<?>))
			terrain.remove(0);

		Terrain[][] terrains = new Terrain[terrain.size()][((List<?>) terrain.get(0)).size()];

		for (int y = 0; y < terrains.length; y++) {
			for (int x = 0; x < terrains[0].length; x++) {
				if (!(terrain.get(y) instanceof List<?>) || x >= ((List<?>) terrain.get(y)).size() || !(((List<?>) terrain.get(y)).get(x) instanceof Number)) {
					terrains[y][x] = Terrain.DEEP_WATER;
				} else {
					try {
						terrains[y][x] = Terrain.values()[((Number) ((List<?>) terrain.get(y)).get(x)).intValue()];
					} catch (ArrayIndexOutOfBoundsException e) {
						terrains[y][x] = Terrain.DEEP_WATER;
					}
				}
			}
		}

		Object goal = map.get("goal");
		int goalX = 0, goalY = 0;
		boolean goalTop = true;
		if (goal instanceof List<?>) {
			List<?> g = (List<?>) goal;

			try {
				if (g.get(0) instanceof Number) goalX = ((Number) g.get(0)).intValue();
				if (g.get(1) instanceof Number) goalY = ((Number) g.get(1)).intValue();
				if (g.get(2) instanceof Boolean) goalTop = (Boolean) g.get(2);
			} catch (IndexOutOfBoundsException e) {}
		} else if (goal instanceof Map<?, ?>) {
			Map<?, ?> g = (Map<?, ?>) goal;

			if (g.get("x") instanceof Number) goalX = ((Number) g.get("x")).intValue();
			if (g.get("y") instanceof Number) goalY = ((Number) g.get("y")).intValue();
			if (g.get("top") instanceof Boolean) goalTop = (Boolean) g.get("top");
		}

		Object spawnpoints = map.get("spawnpoints");
		List<?> top = null, left = null, bottom = null, right = null;
		if (spawnpoints instanceof List<?>) {
			List<?> s = (List<?>) spawnpoints;

			try {
				if (s.get(0) instanceof List<?>) top = (List<?>) s.get(0);
				if (s.get(1) instanceof List<?>) left = (List<?>) s.get(1);
				if (s.get(2) instanceof List<?>) bottom = (List<?>) s.get(2);
				if (s.get(3) instanceof List<?>) right = (List<?>) s.get(3);
			} catch (IndexOutOfBoundsException e) {}
		} else if (spawnpoints instanceof Map<?, ?>) {
			Map<?, ?> s = (Map<?, ?>) spawnpoints;

			if (s.get("top") instanceof List<?>) top = (List<?>) s.get("top");
			if (s.get("left") instanceof List<?>) left = (List<?>) s.get("left");
			if (s.get("bottom") instanceof List<?>) bottom = (List<?>) s.get("bottom");
			if (s.get("right") instanceof List<?>) right = (List<?>) s.get("right");
		}

		ArrayList<Node> spawnNodes = new ArrayList<>();

		if (top != null) {
			for (Object o : top) {
				if (o instanceof Number) spawnNodes.add(new Node(((Number) o).intValue(), 0, true));
			}
		}

		if (left != null) {
			for (Object o : left) {
				if (o instanceof Number) spawnNodes.add(new Node(0, ((Number) o).intValue(), false));
			}
		}

		if (bottom != null) {
			for (Object o : bottom) {
				if (o instanceof Number) spawnNodes.add(new Node(((Number) o).intValue(), terrains.length, true));
			}
		}

		if (right != null) {
			for (Object o : right) {
				if (o instanceof Number) spawnNodes.add(new Node(terrains[0].length, ((Number) o).intValue(), false));
			}
		}

		int[][] elevations = new int[terrains.length][terrains[0].length];
		if (map.get("elevations") instanceof List<?>) {
			List<?> elevs = (List<?>) map.get("elevations");

			for (int y = 0; y < elevations.length; y++) {
				for (int x = 0; x < elevations[0].length; x++) {
					if (!(elevs.get(y) instanceof List<?>) || x >= ((List<?>) elevs.get(y)).size() || !(((List<?>) elevs.get(y)).get(x) instanceof Number)) {
						elevations[y][x] = 0;
					} else {
						try {
							elevations[y][x] = ((Number) ((List<?>) elevs.get(y)).get(x)).intValue();
						} catch (ArrayIndexOutOfBoundsException e) {
							elevations[y][x] = 0;
						}
					}
				}
			}
		}

		Tile[][] tiles = new Tile[terrains.length][terrains[0].length];
		for (int y = 0; y < tiles.length; y++) {
			for (int x = 0; x < tiles[0].length; x++) {
				tiles[y][x] = new Tile(terrains[y][x], elevations[y][x]);
			}
		}

		return new World(mod, id, tiles, new Node(goalX, goalY, goalTop), spawnNodes.toArray(new Node[spawnNodes.size()]));
	}

	static Level parseLevel(Map<?, ?> map, Mod mod) {
		String id = (String) map.get("id");
		if (id == null) throw new IllegalArgumentException();

		double health = map.get("health") instanceof Number ? ((Number) map.get("health")).doubleValue() : 1000;
		int money = map.get("money") instanceof Number ? ((Number) map.get("money")).intValue() : 1000;

		ArrayList<Unit> units = new ArrayList<>();

		if (map.get("enemies") instanceof List<?>) {
			List<?> enemies = (List<?>) map.get("enemies");
			for (Object enemy : enemies) {
				String typeId = "";
				double delay = 1000;
				int count = 1;

				if (enemy instanceof List<?>) {
					List<?> unit = (List<?>) enemy;

					if (unit.size() <= 0 || !(unit.get(0) instanceof String)) continue;

					typeId = (String) unit.get(0);
					delay = unit.size() > 1 && unit.get(1) instanceof Number ? ((Number) unit.get(1)).doubleValue() : 1000;
					count = unit.size() > 2 && unit.get(2) instanceof Number ? ((Number) unit.get(2)).intValue() : 1;
				} else if (enemy instanceof Map<?, ?>) {
					Map<?, ?> unit = (Map<?, ?>) enemy;

					if (!(unit.get("type") instanceof String)) continue;

					typeId = (String) unit.get("type");
					delay = unit.get("delay") instanceof Number ? ((Number) unit.get("delay")).doubleValue() : 1000;
					count = unit.get("count") instanceof Number ? ((Number) unit.get("count")).intValue() : 1;
				}

				for (int c = 0; c < count; c++)
					units.add(new Unit(typeId, delay));
			}
		}

		if (units.size() <= 0) throw new IllegalArgumentException();

		return new Level(mod, id, units.toArray(new Unit[units.size()]), health, money);
	}

	static Mod[] mods() {
		if (mods == null) generateValues();
		
		return mods;
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

	static World[] worlds() {
		if (worlds == null) generateValues();

		return worlds;
	}

	static Level[] levels() {
		if (levels == null) generateValues();

		return levels;
	}

}
