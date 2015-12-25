package terraintd.object;

import terraintd.types.CollidableType;

public abstract class CollidableEntity extends Entity {

	public abstract double getWidth();
	
	public abstract double getHeight();
	
	public abstract CollidableType getType();
}
