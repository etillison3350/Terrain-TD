package terraintd.object;

import java.util.ArrayList;
import java.util.List;

import terraintd.GameLogic;
import terraintd.types.ProjectileType;
import terraintd.types.TargetType;
import terraintd.types.TowerType;
import terraintd.types.TowerUpgrade;

public class Tower extends CollidableEntity implements Weapon {

	public final TowerType baseType;
	private TowerType type;
	private double x, y;
	private double rotation;

	private final List<TowerUpgrade> appliedUpgrades;

	private Gun gun;

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}

	@Override
	public double getWidth() {
		return this.type.width;
	}

	@Override
	public double getHeight() {
		return this.type.height;
	}

	/**
	 * <ul>
	 * <li><b><i>getRotation</i></b><br>
	 * <br>
	 * {@code double getRotation()}<br>
	 * <br>
	 * @return this tower's rotation in radians.
	 *         </ul>
	 */
	public double getRotation() {
		return this.rotation;
	}

	public Tower(TowerType type, double x, double y) {
		this.baseType = this.type = type;
		this.x = x;
		this.y = y;
		this.gun = new Gun(type.projectiles, type.range, x + 0.5 * type.width, y + 0.5 * type.height);

		this.appliedUpgrades = new ArrayList<>();
	}

	/**
	 * <b>THIS CONSTRUCTOR FOR USE IN {@link GameLogic#open(java.nio.file.Path)} ONLY</b>
	 */
	public Tower(TowerType type, double x, double y, TargetType targetType, int kills, double damageDone, int projectilesFired) {
		this(type, x, y);
		this.gun = new Gun(type.projectiles, type.range, x + 0.5 * type.width, y + 0.5 * type.height, targetType, kills, damageDone, projectilesFired);
	}

	public void upgrade(TowerUpgrade upgrade) {
		this.type = upgrade.upgradeTower(this);
		this.gun = new Gun(type.projectiles, type.range, x + 0.5 * type.width, y + 0.5 * type.height, this.gun.getTargetType(), this.gun.getKills(), this.gun.getDamageDone(), this.gun.getProjectilesFired());
		
		this.appliedUpgrades.add(upgrade);
	}

	public TowerUpgrade[] getAppliedUpgrades() {
		return appliedUpgrades.toArray(new TowerUpgrade[appliedUpgrades.size()]);
	}
	
	@Override
	public TowerType getType() {
		return this.type;
	}

	@Override
	public Gun getGun() {
		return gun;
	}

	@Override
	public Projectile[] createProjectiles(ProjectileType[] types) {
		Projectile[] ps = new Projectile[types.length];

		for (int n = 0; n < types.length; n++)
			ps[n] = new Projectile(types[n], this);

		return ps;
	}

	@Override
	public void target(Enemy e) {
		Weapon.super.target(e);
		if (e != null) this.rotation = this.gun.getRotation();
	}

}
