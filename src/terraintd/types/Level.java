package terraintd.types;

public class Level extends Type {

	public final Unit[] units;
	public final double health;
	public final int money;

	public Level(String id, Unit[] units, double health, int money) {
		super(id);
		
		this.units = units;
		this.health = health;
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

	public static Level[] values() {
		return TypeGenerator.levels();
	}

}
