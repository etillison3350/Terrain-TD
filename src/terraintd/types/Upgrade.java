package terraintd.types;

import java.awt.Color;

public abstract class Upgrade extends ModdedType implements Purchasable {

	public final int cost;
	public final int sellCost;
	public final ImageType icon;
	
	public Upgrade(String id, Mod mod, int cost, int sellCost, ImageType icon) {
		super(mod, id);
		
		this.cost = cost;
		this.sellCost = sellCost;
		this.icon = icon;
	}
	
	@Override
	public int getCost() {
		return cost;
	}

	@Override
	public ImageType getIcon() {
		return icon;
	}

	@Override
	public Color getBackgroundColor() {
		return Color.PINK;
	}
	
}
