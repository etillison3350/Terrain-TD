package terraintd.types;

import java.util.HashMap;

public class LevelSet extends ModdedType {

	public final Level[] levels;
	public final double health;

	protected LevelSet(Mod mod, String id, Level[] levels, double health) {
		super(mod, id);

		typeIds.put(id, this);

		this.levels = levels;
		this.health = health;
	}

	static final HashMap<String, LevelSet> typeIds = new HashMap<>();

	public static LevelSet valueOf(String id) {
		return typeIds.get(id);
	}

	public static LevelSet[] values() {
		return TypeGenerator.levelSets();
	}

}
