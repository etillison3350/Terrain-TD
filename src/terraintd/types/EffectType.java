package terraintd.types;

public class EffectType {

	public final StatusEffectType type;
	public final double duration;
	public final double amplifier;

	protected EffectType(StatusEffectType type, double duration, double amplifier) {
		this.type = type;
		this.duration = duration;
		this.amplifier = amplifier;
	}

}
