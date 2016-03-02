package terraintd.types;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

public class EffectUpgrade extends ModdedType {

	public final String upgradeId;

	protected EffectUpgrade(Mod mod, String id, String upgradeId) {
		super(mod, id);

		this.upgradeId = upgradeId;
	}

	public static EffectType[] upgrade(EffectType[] effects, EffectUpgrade[] upgrades) {
		TreeSet<EffectType> ret = new TreeSet<>(new Comparator<EffectType>() {

			@Override
			public int compare(EffectType o1, EffectType o2) {
				return Long.compare(o1.uid, o2.uid);
			}
		});
		Arrays.stream(effects).forEach(ret::add);

		EffectUpgrade[] us = Arrays.stream(upgrades).sorted((o1, o2) -> (o1.getClass().toString() + o1.upgradeId).compareTo(o2.getClass().toString() + o2.upgradeId)).toArray(size -> new EffectUpgrade[size]);

		for (EffectUpgrade u : us) {
			if (u instanceof Add) {
				ret.add(new EffectType((Add) u, ((Add) u).type));
			} else if (u instanceof Change) {
				EffectType[] retArray = ret.toArray(new EffectType[ret.size()]);
				for (EffectType p : retArray) {
					if (u.upgradeId.equals("all") || p.id.equals(u.upgradeId)) {
						EffectType n = ((Change) u).upgrade(ret.floor(p));
						ret.remove(p);
						ret.add(n);
					}
				}
			} else {
				ret.removeIf(p -> p.id.equals(u.upgradeId));
			}
		}

		return ret.toArray(new EffectType[ret.size()]);
	}

	public static class Change extends EffectUpgrade {

		public final double amplifier;
		public final double duration;

		protected Change(Mod mod, String id, String upgradeId, double amplifier, double duration) {
			super(mod, id, upgradeId);

			this.amplifier = amplifier;
			this.duration = duration;
		}

		protected EffectType upgrade(EffectType effect) {
			return new EffectType(effect.uid, effect.base, effect.id, effect.mod, effect.type, effect.duration + duration, effect.amplifier + amplifier);
		}
	}

	public static class Add extends EffectUpgrade {

		public final EffectType type;

		protected Add(Mod mod, String id, String upgradeId, EffectType type) {
			super(mod, id, upgradeId);

			this.type = type;
		}

	}

	public static class Remove extends EffectUpgrade {

		protected Remove(Mod mod, String id, String upgradeId) {
			super(mod, id, upgradeId);
		}

	}

}
