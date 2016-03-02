package terraintd.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectileType extends ModdedType implements Identifiable {

	private static long lastUID;

	public final long uid;
	public final ProjectileType base;
	private final List<ProjectileUpgrade> appliedUpgrades;

	public final DeliveryType delivery;
	public final double explodeRadius;
	public final double speed;
	public final double maxDamage;
	public final double minDamage;
	public final boolean isHealing;
	public final double rate;
	public final boolean follow;
	public final double maxDist;
	public final double offset;
	public final double angle;
	public final double rotation;
	public final boolean absRotation;
	public final double dyingFadeTime;
	public final boolean dyingFade;
	public final boolean damageFade;
	public final ImageType image;
	public final ImageType explosion;
	public final EffectType[] effects;

	protected ProjectileType(long uid, ProjectileType base, List<ProjectileUpgrade> appliedUpgrades, String id, Mod mod, DeliveryType delivery, double explodeRadius, double speed, double maxDamage, double minDamage, double rate, boolean follow, double maxDist, double offset, double angle, double rotation, boolean absRotation, double dyingFadeTime, boolean dyingFade, boolean damageFade, ImageType image, ImageType explosion, EffectType[] effects) {
		super(mod, id);

		this.uid = uid;
		this.base = base == null ? this : base;
		this.appliedUpgrades = new ArrayList<>(appliedUpgrades);

		this.delivery = delivery;
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

		this.isHealing = this.maxDamage < 0;
	}

	protected ProjectileType(ProjectileUpgrade.Add srcUpgrade, ProjectileType projectile) {
		this(projectile.uid, null, Arrays.asList(new ProjectileUpgrade[] {srcUpgrade}), projectile.id, projectile.mod, projectile.delivery, projectile.explodeRadius, projectile.speed, projectile.maxDamage, projectile.minDamage, projectile.rate, projectile.follow, projectile.maxDist, projectile.offset, projectile.angle, projectile.rotation, projectile.absRotation, projectile.dyingFadeTime, projectile.dyingFade, projectile.damageFade, projectile.image, projectile.explosion, projectile.effects);
	}

	protected ProjectileType(String id, Mod mod, DeliveryType delivery, double explodeRadius, double speed, double maxDamage, double minDamage, double rate, boolean follow, double maxDist, double offset, double angle, double rotation, boolean absRotation, double dyingFadeTime, boolean dyingFade, boolean damageFade, ImageType image, ImageType explosion, EffectType[] effects) {
		this(lastUID++, null, new ArrayList<ProjectileUpgrade>(), id, mod, delivery, explodeRadius, speed, maxDamage, minDamage, rate, follow, maxDist, offset, angle, rotation, absRotation, dyingFadeTime, dyingFade, damageFade, image, explosion, effects);
	}

	public ProjectileUpgrade[] getAppliedUpgrades() {
		return appliedUpgrades.toArray(new ProjectileUpgrade[appliedUpgrades.size()]);
	}

	@Override
	public long getUID() {
		return uid;
	}

}
