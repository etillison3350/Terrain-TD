package terraintd.object;

import java.util.ArrayList;
import java.util.List;

import terraintd.GameLogic;
import terraintd.pathfinder.Node;
import terraintd.types.EffectType;
import terraintd.types.EnemyType;
import terraintd.types.IdType;
import terraintd.types.ProjectileType;
import terraintd.types.StatusEffectType;
import terraintd.types.World;

public class Enemy extends Entity implements Weapon {

	public final EnemyType type;
	private final Gun gun;

	private double x, y;
	private double health;
	private double deathTime = 0;
	private int dead;
	public List<Projectile> futureDamage;

	public final World world;

	private Node nextNode, prevNode;

	private Node[][][] newNodeSet;
	
	private List<StatusEffect> statusEffects;

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}

	public Enemy(EnemyType type, Node location, World world) {
		this.type = type;
		this.x = location.getAbsX();
		this.y = location.getAbsY();
		this.prevNode = location;
		this.nextNode = location.getNextNode();
		this.world = world;
		this.health = type.health;
		this.gun = type.projectiles != null && type.projectiles.length > 0 ? new Gun(this) : null;

		this.statusEffects = new ArrayList<>();

		this.futureDamage = new ArrayList<>();
	}

	@Override
	public Gun getGun() {
		return this.gun;
	}

	@Override
	public Projectile[] createProjectiles(ProjectileType[] types) {
		Projectile[] ps = new Projectile[types.length];

		for (int n = 0; n < types.length; n++)
			ps[n] = new Projectile(types[n], this);

		return ps;
	}

	public boolean move() {
		double speedMultiplier = 1;

		StatusEffect[] effects = statusEffects.toArray(new StatusEffect[statusEffects.size()]);
		for (StatusEffect effect : effects) {
			if (effect.fade()) {
				statusEffects.remove(effect);
				continue;
			}

			switch (effect.type) {
				case FIRE:
					this.health -= .125 * effect.amplifier + 1.125;
					effect.inflictor.getGun().registerDamage(.125 * effect.amplifier + 1.125);
					break;
				case FROST:
					if (effect.amplifier >= 10.999) return true;
					speedMultiplier *= ((effect.amplifier - 11) * effect.origDuration + 11 * effect.getDuration()) / ((effect.amplifier - 11) * effect.origDuration);
					if (speedMultiplier <= 0.001) return true;
					break;
				case PARALYSIS:
					return true;
				case POISON:
//					this.health -= .375 * effect.amplifier + .25;
					double lastHealth = this.health;
					this.health = this.health * (1 - effect.amplifier * 0.0021) - 0.05 * effect.amplifier;
					effect.inflictor.getGun().registerDamage(lastHealth - this.health);
					break;
				case SLOWNESS:
					speedMultiplier *= 1 - 0.09090909 * effect.amplifier;
					break;
				default:
					break;
			}
		}
		return move(GameLogic.FRAME_TIME, speedMultiplier);
	}

	protected boolean move(double time, double speedMult) {
		int x = prevNode.x - (!prevNode.top && prevNode.x - nextNode.x == 1 ? 1 : 0);
		int y = prevNode.y - (prevNode.top && prevNode.y - nextNode.y == 1 ? 1 : 0);

		double speed = speedMult * type.speed.get(world.tiles[y][x].terrain);
		double distance = speed * time;
		double d = Math.hypot(this.x - nextNode.getAbsX(), this.y - nextNode.getAbsY());

		for (StatusEffect effect : statusEffects) {
			if (effect.type == StatusEffectType.BLEED) {
				this.health -= 10 * effect.amplifier * distance;
				effect.inflictor.getGun().registerDamage(10 * effect.amplifier * distance);
			}
		}
		
		if (distance > d) {
			this.x = nextNode.getAbsX();
			this.y = nextNode.getAbsY();
			this.prevNode = nextNode;
			this.nextNode = nextNode.getNextNode();
			if (newNodeSet != null) resetNodes(newNodeSet);
			boolean ret = nextNode == null ? false : move((distance - d) / speed, speedMult);
			if (!ret) dead = 2;
			return ret;
		}

		double dx = nextNode.getAbsX() - prevNode.getAbsX();
		double dy = nextNode.getAbsY() - prevNode.getAbsY();

		this.y += dy * distance;
		this.x += dx * distance;

		return true;
	}

	public void resetNodes(Node[][][] nodes) {
		if (this.nextNode != null) {
			Node newNextNode = nodes[nextNode.y][nextNode.x][nextNode.top ? 1 : 0];
			Node newPrevNode = nodes[prevNode.y][prevNode.x][prevNode.top ? 1 : 0];
			
			if (newNextNode.isExplored()) {
				this.nextNode = newNextNode;
				this.newNodeSet = null;
			} else if (newPrevNode.isExplored()) {
				this.nextNode = newPrevNode.getNextNode();
				this.x = newPrevNode.getAbsX();
				this.y = newPrevNode.getAbsY();
			} else {
				this.newNodeSet = nodes;
			}
		}
	}

	/**
	 * <ul>
	 * <li><b><i>damage</i></b><br>
	 * <br>
	 * {@code public int damage(double damage)}<br>
	 * <br>
	 * Damages this enemy for the given amount of damage.<br>
	 * @param damage The amount to damage this enemy by
	 * @return The amount of money given to the player as a result of damaging this enemy. Returns 0 if the enemy did not die.
	 *         </ul>
	 */
	public boolean damage(double damage) {
		this.health -= damage;

		if (this.health <= 0.00001 && this.dead == 0) {
			this.dead = 1;
			return true;
		}
		return false;
	}

	public boolean damage(Projectile p) {
		this.futureDamage.remove(p);

		double h = this.health;
		double dm = 1;
		for (StatusEffect effect : statusEffects) {
			if (effect.type == StatusEffectType.WEAKNESS) dm *= (1 + 0.25 * effect.amplifier);
		}
		boolean kill = this.damage(dm * p.damageForEntity(this));
		if (kill) p.shootingEntity.getGun().registerKill();
		p.shootingEntity.getGun().registerDamage(Math.max(0, h) - Math.max(0, this.health));

		if (p.type.effects != null) {
			for (EffectType effect : p.type.effects) {
				this.addStatusEffect(new StatusEffect(p.shootingEntity, effect));
			}
		}
		
		return kill;
	}

	public double getHealth() {
		return this.health;
	}

	public void damageFuture(Projectile p) {
		this.futureDamage.add(p);
	}

	public double getFutureHealth() {
		double ret = this.health;

		for (Projectile p : futureDamage)
			ret -= p.type.damage - (p.type.falloff < 0 ? 0 : p.type.falloff);

		return ret;
	}

	public Node getNextNode() {
		return nextNode;
	}

	public boolean die() {
		this.deathTime += GameLogic.FRAME_TIME;
		return this.deathTime >= 1;
	}

	public double getDeathTime() {
		return deathTime;
	}

	public boolean isDead() {
		return dead == 1;
	}

	public int getDead() {
		return dead;
	}

	@Override
	public IdType getType() {
		return this.type;
	}

	public double getRotation() {
		return nextNode == null ? 0 : Math.atan2(nextNode.getAbsY() - prevNode.getAbsY(), nextNode.getAbsX() - prevNode.getAbsX());
	}

	public void addStatusEffect(StatusEffect effect) {
		this.statusEffects.add(effect);
	}

	public StatusEffect[] getStatusEffects() {
		return statusEffects.toArray(new StatusEffect[statusEffects.size()]);
	}

	public double getDamage() {
		double dm = 1;
		for (StatusEffect effect : statusEffects) {
			if (effect.type == StatusEffectType.BLUNTNESS) dm /= (1 + 0.25 * effect.amplifier);
		}
		return dm * this.type.damage;
	}

}
