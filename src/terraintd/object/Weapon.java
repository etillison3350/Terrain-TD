package terraintd.object;

import terraintd.types.ProjectileType;

public interface Weapon {

	public Gun getGun();
	
	public Projectile[] createProjectiles(ProjectileType[] types);
	
}
