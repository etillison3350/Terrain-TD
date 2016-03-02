package terraintd.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public abstract class ProjectileUpgrade extends ModdedType {

	public final String upgradeId;

	protected ProjectileUpgrade(Mod mod, String id, String upgradeId) {
		super(mod, id);

		this.upgradeId = upgradeId;
	}

	public static ProjectileType[] upgrade(ProjectileType[] projectiles, ProjectileUpgrade[] upgrades) {
		TreeSet<ProjectileType> ret = new TreeSet<>(new Comparator<ProjectileType>() {

			@Override
			public int compare(ProjectileType o1, ProjectileType o2) {
				return Long.compare(o1.uid, o2.uid);
			}
		});
		Arrays.stream(projectiles).forEach(ret::add);

		ProjectileUpgrade[] us = Arrays.stream(upgrades).sorted((o1, o2) -> (o1.getClass().toString() + o1.upgradeId).compareTo(o2.getClass().toString() + o2.upgradeId)).toArray(size -> new ProjectileUpgrade[size]);

		for (ProjectileUpgrade u : us) {
			if (u instanceof Add) {
				ret.add(new ProjectileType((Add) u, ((Add) u).type));
			} else if (u instanceof Change) {
				ProjectileType[] retArray = ret.toArray(new ProjectileType[ret.size()]);
				for (ProjectileType p : retArray) {
					if (u.upgradeId.equals("all") || p.id.equals(u.upgradeId)) {
						ProjectileType n = ((Change) u).upgrade(ret.floor(p));
						ret.remove(p);
						ret.add(n);
					}
				}
			} else {
				ret.removeIf(p -> p.id.equals(u.upgradeId));
			}
		}

		return ret.toArray(new ProjectileType[ret.size()]);

	}

	public static class Change extends ProjectileUpgrade {

		public final double explodeRadius;
		public final double speed;
		public final double maxDamage;
		public final double minDamage;
		public final double rate;
		public final int follow;
		public final double maxDist;
		public final double offset;
		public final double angle;
		public final double rotation;
		public final int absRotation;
		public final double dyingFadeTime;
		public final int dyingFade;
		public final int damageFade;
		public final ImageType image;
		public final ImageType explosion;
		public final EffectUpgrade[] effects;

		protected Change(Mod mod, String id, String upgradeId, double explodeRadius, double speed, double maxDamage, double minDamage, double rate, int follow, double maxDist, double offset, double angle, double rotation, int absRotation, double dyingFadeTime, int dyingFade, int damageFade, ImageType image, ImageType explosion, EffectUpgrade[] effects) {
			super(mod, id, upgradeId);

			this.explodeRadius = explodeRadius;
			this.speed = speed;
			this.maxDamage = maxDamage;
			this.minDamage = minDamage;
			this.rate = rate;
			this.follow = follow;
			this.maxDist = maxDist;
			this.offset = offset;
			this.angle = angle;
			this.rotation = rotation;
			this.absRotation = absRotation;
			this.dyingFadeTime = dyingFadeTime;
			this.dyingFade = dyingFade;
			this.damageFade = damageFade;
			this.image = image;
			this.explosion = explosion;
			this.effects = effects;
		}

		protected ProjectileType upgrade(ProjectileType projectile) {
			List<ProjectileUpgrade> appliedUpgrades = new ArrayList<>(Arrays.asList(projectile.getAppliedUpgrades()));
			appliedUpgrades.add(this);

			List<EffectUpgrade> effectUpgrades = new ArrayList<>();
			for (ProjectileUpgrade u : appliedUpgrades) {
				if (u instanceof Change) {
					effectUpgrades.addAll(Arrays.asList(((Change) u).effects));
				}
			}
			
			return new ProjectileType(projectile.uid, projectile.base, appliedUpgrades, projectile.id, projectile.mod, projectile.delivery, projectile.explodeRadius + explodeRadius, projectile.speed + speed, projectile.maxDamage + maxDamage, projectile.minDamage + minDamage, projectile.rate + rate, follow < 2 ? follow == 1 : projectile.follow, projectile.maxDist + maxDist, projectile.offset + offset, projectile.angle + angle, projectile.rotation + rotation, absRotation < 2 ? absRotation == 1 : projectile.absRotation, projectile.dyingFadeTime + dyingFadeTime, dyingFade < 2 ? dyingFade == 1 : projectile.dyingFade, damageFade < 2 ? damageFade == 1 : projectile.damageFade, image == ImageType.BLANK ? projectile.image : image, explosion == ImageType.BLANK ? projectile.explosion : explosion, EffectUpgrade.upgrade(projectile.base.effects, effectUpgrades.toArray(new EffectUpgrade[effectUpgrades.size()])));
		}
	}

	public static class Add extends ProjectileUpgrade {

		public final ProjectileType type;

		protected Add(Mod mod, String id, String upgradeId, ProjectileType type) {
			super(mod, id, upgradeId);

			this.type = type;
		}

	}

	public static class Remove extends ProjectileUpgrade {

		protected Remove(Mod mod, String id, String upgradeId) {
			super(mod, id, upgradeId);
		}

	}

}
