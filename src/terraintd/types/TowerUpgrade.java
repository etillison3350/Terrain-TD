package terraintd.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import terraintd.object.Tower;

public class TowerUpgrade extends Upgrade {

	public final double range;
	public final boolean rotate;
	public final ImageType image;
	public final ProjectileUpgrade[] projectiles;

	protected TowerUpgrade(String id, Mod mod, int cost, int sellCost, ImageType icon, double range, boolean rotate, ImageType image, ProjectileUpgrade[] projectiles) {
		super(id, mod, cost, sellCost, icon);
		
		this.range = range;
		this.rotate = rotate;
		this.image = image;
		this.projectiles = projectiles;
	}
	
	public TowerType upgradeTower(Tower tower) {
		TowerType type = tower.getType();
		
		List<TowerUpgrade> remainingUpgrades = new ArrayList<>();
		for (TowerUpgrade u : type.upgrades) {
			if (u != this) remainingUpgrades.add(u);
		}
		
		List<ProjectileUpgrade> projectileUpgrades = new ArrayList<>();
		for (TowerUpgrade u : tower.getAppliedUpgrades()) {
			projectileUpgrades.addAll(Arrays.asList(u.projectiles));
		}
		projectileUpgrades.addAll(Arrays.asList(this.projectiles));
		
		return new TowerType(type.mod, type.id, type.cost + cost, type.sellCost + sellCost, type.width, type.height, type.terrain, type.onHill, type.range + range, rotate, image == ImageType.BLANK ? type.image : image, type.icon, ProjectileUpgrade.upgrade(tower.baseType.projectiles, projectileUpgrades.toArray(new ProjectileUpgrade[projectileUpgrades.size()])), remainingUpgrades.toArray(new TowerUpgrade[remainingUpgrades.size()]));
	}

}
