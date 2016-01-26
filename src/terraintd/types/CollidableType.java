package terraintd.types;

public abstract class CollidableType extends ModdedType {

	public final int width;
	public final int height;
	public final int cost;
	public final ImageType image;
	public final ImageType icon;

	public CollidableType(Mod mod, String id, int width, int height, int cost, ImageType image, ImageType icon) {
		super(mod, id);

		this.width = width;
		this.height = height;
		this.cost = cost;
		this.image = image;
		this.icon = icon;
	}
}
