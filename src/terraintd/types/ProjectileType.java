package terraintd.types;

public class ProjectileType {

	public final DeliveryType delivery;
	public final double explodeRadius;
	public final double speed;
	public final double damage;
	public final boolean isHealing;
	public final double falloff;
	public final double rate;
	public final boolean follow;
	public final double maxDist;
	public final double angle;
	public final double rotation;
	public final boolean absRotation;
	public final double dyingFadeTime;
	public final boolean dyingFade;
	public final boolean damageFade;
	public final ImageType image;
	public final ImageType explosion;
	public final EffectType[] effects;

	ProjectileType(DeliveryType delivery, double explodeRadius, double speed, double damage, double falloff, double rate, boolean follow, double maxDist, double angle, double rotation, boolean absRotation, double dyingFadeTime, boolean dyingFade, boolean damageFade, ImageType image, ImageType explosion, EffectType[] effects) {
		this.delivery = delivery;
		this.explodeRadius = explodeRadius;
		this.speed = speed;
		this.damage = damage;
		this.falloff = falloff;
		this.rate = rate;
		this.follow = follow;
		this.maxDist = maxDist;
		this.angle = angle;
		this.rotation = rotation;
		this.absRotation = absRotation;
		this.dyingFadeTime = dyingFadeTime;
		this.dyingFade = dyingFade;
		this.damageFade = damageFade;
		this.image = image;
		this.explosion = explosion;
		this.effects = effects;

		this.isHealing = this.damage < 0;
	}

}
