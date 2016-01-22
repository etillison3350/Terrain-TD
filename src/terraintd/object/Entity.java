package terraintd.object;

import java.awt.geom.Rectangle2D;

import terraintd.types.IdType;

public abstract class Entity {

	/**
	 * <ul>
	 * <li><b><i>getX</i></b><br><br>
	 * {@code public abstract double getX()}<br><br>
	 * @return this Entity's x position, i.e. the horizontal distance, in tiles, of the center from the left side of the game panel. 
	 * </ul>
	 */
	public abstract double getX();
	
	/**
	 * <ul>
	 * <li><b><i>getY</i></b><br><br>
	 * {@code public abstract double getY()}<br><br>
	 * @return this Entity's y position, i.e. the vertical distance, in tiles, of the center from the top of the game panel. 
	 * </ul>
	 */
	public abstract double getY();
	
	public abstract IdType getType();
	
	public Rectangle2D getRectangle() {
		return new Rectangle2D.Double();
	}
	
}
