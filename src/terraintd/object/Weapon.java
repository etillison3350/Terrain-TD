package terraintd.object;

import terraintd.types.ProjectileType;

public interface Weapon {

	public Gun getGun();
	
	public Projectile[] createProjectiles(ProjectileType[] types);
	
	public default void target(Enemy e) {
		this.getGun().target(e);
	}
	
}
