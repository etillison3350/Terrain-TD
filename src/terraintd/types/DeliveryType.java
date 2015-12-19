package terraintd.types;

public enum DeliveryType {
	SINGLE_TARGET,
	AREA,
	LINE,
	SECTOR;
	
	public String toString() {
		return Language.get("delivery-" + this.name().replace("_", ""));
	}
}
