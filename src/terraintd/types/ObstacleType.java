package terraintd.types;

public class ObstacleType {

	public final String id;
	public final int width;
	public final int height;
	public final int cost;
	public final double spawnRate;
	public final ImageType image;
	public final ImageType icon;

	ObstacleType(String id, int width, int height, int cost, double spawnRate, ImageType image, ImageType icon) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.cost = cost;
		this.spawnRate = spawnRate;
		this.image = image;
		this.icon = icon;
	}
	
	public static ObstacleType[] values() {
		return TypeGenerator.obstacles();
	}
}
