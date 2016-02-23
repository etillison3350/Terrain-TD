package terraintd.types;

import terraintd.Language;

public class EffectType {

	public final StatusEffectType type;
	public final double duration;
	public final double amplifier;

	protected EffectType(StatusEffectType type, double duration, double amplifier) {
		this.type = type;
		this.duration = duration;
		this.amplifier = amplifier;
	}
	
	@Override
	public String toString() {
		return String.format("%s%s, %.3gs", type, type.amplifiable ? String.format(Language.getCurrentLocale(), " %f", amplifier) : "", duration);
	}

}
