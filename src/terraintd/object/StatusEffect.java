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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inflictor == null) ? 0 : inflictor.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		StatusEffect other = (StatusEffect) obj;
		if (inflictor == null) {
			if (other.inflictor != null) return false;
		} else if (!inflictor.equals(other.inflictor)) {
			return false;
		}
		if (type != other.type) return false;
		return true;
	}

}
