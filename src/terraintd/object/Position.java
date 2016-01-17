package terraintd.object;

import terraintd.types.IdType;

public class Position extends Entity {

	public final double x, y;

	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public IdType getType() {
		return null;
	}

}
