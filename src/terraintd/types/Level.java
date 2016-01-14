package terraintd.types;

public class Level extends Type {

	public final Unit[] units;
	public final double health;

	public Level(String id, Unit[] units, double health) {
		super(id);
		
		this.units = units;
		this.health = health;
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
