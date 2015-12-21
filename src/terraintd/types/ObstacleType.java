package terraintd.types;

import java.util.HashMap;

public class ObstacleType {

	public final String id;
	public final int width;
	public final int height;
	public final int cost;
	public final int removeCost;
	public final HashMap<Terrain, Double> spawnRates;
	public final ImageType image;
	public final ImageType icon;

	ObstacleType(String id, int width, int height, int cost, int removeCost, HashMap<Terrain, Double> spawnRates, ImageType image, ImageType icon) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.cost = cost;
		this.removeCost = removeCost;
		this.spawnRates = spawnRates;
		this.image = image;
		this.icon = icon;
	}
	
	public static ObstacleType[] values() {
		return TypeGenerator.obstacles();
	}
}
