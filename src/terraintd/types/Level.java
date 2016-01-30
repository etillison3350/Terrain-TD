package terraintd.types;

public class Level {

	public final int index;
	public final Unit[] units;
	public final int money;

	protected Level(int index, Unit[] units, int money) {
		this.index = index;
		this.units = units;
		this.money = money;
	}

	public static class Unit {

		public final String typeId;
		public final double delay;

		public Unit(String typeId, double delay) {
			this.typeId = typeId;
			this.delay = delay;
		}
	}

}
