package terraintd.object;

import java.awt.geom.Rectangle2D;

import terraintd.types.CollidableType;

public abstract class CollidableEntity extends Entity {

	public abstract double getWidth();
	
	public abstract double getHeight();
	
	public abstract CollidableType getType();
	
	public Rectangle2D getRectangle() {
		return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
	}
}
