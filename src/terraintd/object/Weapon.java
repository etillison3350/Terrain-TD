package terraintd.object;

public interface Weapon {

	public Gun getGun();
	
	public Projectile[] convertFromTempProjectiles(Gun.TempProjectile[] temps);
	
}
