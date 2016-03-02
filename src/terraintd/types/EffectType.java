package terraintd.types;

import terraintd.Language;

public class EffectType extends ModdedType implements Identifiable {

	private static long lastUID;

	public final long uid;
	public final EffectType base;

	public final StatusEffectType type;
	public final double duration;
	public final double amplifier;

	protected EffectType(long uid, EffectType base, String id, Mod mod, StatusEffectType type, double duration, double amplifier) {
		super(mod, id);

		this.uid = uid;
		this.base = base == null ? this : base;

		this.type = type;
		this.duration = duration;
		this.amplifier = amplifier;
	}

	protected EffectType(String id, Mod mod, StatusEffectType type, double duration, double amplifier) {
		this(lastUID++, null, id, mod, type, duration, amplifier);
	}

	protected EffectType(EffectUpgrade.Add srcUpgrade, EffectType effect) {
		this(effect.uid, null, effect.id, effect.mod, effect.type, effect.duration, effect.amplifier);
	}

	@Override
	public String toString() {
		return String.format("%s%s, %.3gs", type, type.amplifiable ? String.format(Language.getCurrentLocale(), " %.2f", amplifier) : "", duration);
	}

	@Override
	public long getUID() {
		return uid;
	}

}
