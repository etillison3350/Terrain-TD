package terraintd.types;

import java.util.HashMap;

public class ObstacleType extends CollidableType {

	public final HashMap<Terrain, Double> spawnRates;

	ObstacleType(Mod mod, String id, int width, int height, int cost, int sellCost, HashMap<Terrain, Double> spawnRates, ImageType image, ImageType icon) {
		super(mod, id, width, height, cost, sellCost, image, icon);
		
		typeIds.put(id, this);
		
		this.spawnRates = spawnRates;
	}
	
	static final HashMap<String, ObstacleType> typeIds = new HashMap<>();
	
	public static ObstacleType valueOf(String id) {
		return typeIds.get(id);
	}
	
	public static ObstacleType[] values() {
		return TypeGenerator.obstacles();
	}
}
