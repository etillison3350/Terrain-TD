package terraintd.types;

public abstract class CollidableType extends ModdedType implements Sellable {

	public final int width;
	public final int height;
	public final int cost;
	public final int sellCost;
	public final ImageType image;
	public final ImageType icon;

	protected CollidableType(Mod mod, String id, int width, int height, int cost, int sellCost, ImageType image, ImageType icon) {
		super(mod, id);

		this.width = width;
		this.height = height;
		this.cost = cost;
		this.sellCost = sellCost;
		this.image = image;
		this.icon = icon;
	}

	@Override
	public int getCost() {
		return cost;
	}

	@Override
	public int getSellCost() {
		return sellCost;
	}

	@Override
	public ImageType getIcon() {
		return icon;
	}
}
