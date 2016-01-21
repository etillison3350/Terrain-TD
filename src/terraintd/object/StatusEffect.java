package terraintd.object;

import terraintd.GameLogic;
import terraintd.types.EffectType;
import terraintd.types.StatusEffectType;

public class StatusEffect {

	public final Weapon inflictor;
	public final StatusEffectType type;
	public final double origDuration, amplifier;
	private double duration;

	public StatusEffect(Weapon inflictor, StatusEffectType type, double amplifier, double duration) {
		this.inflictor = inflictor;
		this.type = type;
		this.amplifier = amplifier;
		this.origDuration = this.duration = duration;
	}
	
	public StatusEffect(Weapon inflictor, EffectType type) {
		this(inflictor, type.type, type.amplifier, type.duration);
	}

	public boolean fade() {
		this.duration -= GameLogic.FRAME_TIME;
		return this.duration <= 0;
	}

	public double getDuration() {
		return duration;
	}

}
