package terraintd.object;

import terraintd.types.ObstacleType;

public class Obstacle extends CollidableEntity {

	public final ObstacleType type;
	private double x, y;

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}

	@Override
	public double getWidth() {
		return this.type.width;
	}

	@Override
	public double getHeight() {
		return this.type.height;
	}

	public Obstacle(ObstacleType type, double x, double y) {
		this.type = type;
		this.x = x;
		this.y = y;
	}

}
