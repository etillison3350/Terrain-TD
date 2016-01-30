package terraintd.types;

public abstract class ModdedType extends IdType {

	public final Mod mod;

	protected ModdedType(Mod mod, String id) {
		super(id);

		this.mod = mod;
	}

}
