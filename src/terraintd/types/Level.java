package terraintd.types;

import java.util.HashMap;

public class Level extends ModdedType {

	public final Unit[] units;
	public final double health;
	public final int money;

	public Level(Mod mod, String id, Unit[] units, double health, int money) {
		super(mod, id);
		
		typeIds.put(id, this);
		
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
	
	static final HashMap<String, Level> typeIds = new HashMap<>();
	
	public static Level valueOf(String id) {
		return typeIds.get(id);
	}


	public static Level[] values() {
		return TypeGenerator.levels();
	}

}
