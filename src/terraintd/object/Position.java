package terraintd.object;

import java.awt.geom.Point2D;

import terraintd.types.IdType;

public class Position extends Entity {

	public final double x, y;

	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Position(Point2D pos) {
		this.x = pos.getX();
		this.y = pos.getY();
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
