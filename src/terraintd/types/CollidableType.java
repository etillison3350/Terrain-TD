package terraintd.types;

public abstract class CollidableType extends IdType{
	public final int width;
	public final int height;
	public final int cost;
	public final ImageType image;
	public final ImageType icon;

	public CollidableType(String id, int width, int height, int cost, ImageType image, ImageType icon) {
		super(id);
		
		this.width = width;
		this.height = height;
		this.cost = cost;
		this.image = image;
		this.icon = icon;
	}
}
