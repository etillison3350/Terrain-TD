package terraintd.object;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import terraintd.GameLogic;
import terraintd.pathfinder.Node;
import terraintd.pathfinder.PathFinder;
import terraintd.types.EffectType;
import terraintd.types.EnemyType;
import terraintd.types.IdType;
import terraintd.types.ProjectileType;
import terraintd.types.StatusEffectType;

public class Enemy extends Entity implements Weapon {

	public final EnemyType type;
	private final Gun gun;

	private double x, y;
	private double health;
	private double deathTime = 0;
	private int dead;
	public List<Projectile> futureDamage;

	private Node nextNode, prevNode, oldPrev;

	private Node[][][] newNodeSet;

	private Set<StatusEffect> statusEffects;

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}

	private final double width;

	public Enemy(EnemyType type, Node location) {
		this.type = type;
		this.x = location.getAbsX();
		this.y = location.getAbsY();
		this.prevNode = location;
		this.nextNode = location.getNextNode();
		this.health = type.health;
		this.gun = type.projectiles != null && type.projectiles.length > 0 ? new Gun(this) : null;

		this.width = Math.hypot(type.image.width, type.image.height);

		this.statusEffects = new HashSet<>();

		this.futureDamage = new ArrayList<>();
	}

	/**
	 * <b>THIS CONSTRUCTOR FOR USE IN {@link GameLogic#open(java.nio.file.Path)} ONLY</b>
	 */
	public Enemy(EnemyType type, Node prevNode, Node nextNode, double x, double y, double deathTime, double health) {
		this.type = type;
		this.prevNode = prevNode;
		this.nextNode = nextNode;
		this.x = x;
		this.y = y;
		this.deathTime = deathTime;
		this.health = health;
		this.dead = health < 0.00001 ? (nextNode == null ? 2 : 1) : 0;

		this.gun = type.projectiles != null && type.projectiles.length > 0 ? new Gun(this) : null;

		this.width = Math.hypot(type.image.width, type.image.height);

		this.statusEffects = new HashSet<>();

		this.futureDamage = new ArrayList<>();
	}

	@Override
	public Gun getGun() {
		return this.gun;
	}

	@Override
	public Rectangle2D getRectangle() {
		return new Rectangle2D.Double(this.x + this.type.image.x - 0.5 * width, this.y + this.type.image.y - 0.5 * width, width, width);
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
					if (this.health < 0.00001) effect.inflictor.getGun().registerKill();
					break;
				case FROST:
					if (effect.amplifier >= 10.999) return true;
					speedMultiplier *= ((effect.amplifier - 11) * effect.origDuration + 11 * effect.getDuration()) / ((effect.amplifier - 11) * effect.origDuration);
					if (speedMultiplier <= 0.001) return true;
					break;
				case PARALYSIS:
					return true;
				case POISON:
					double lastHealth = this.health;
					this.health = this.health * (1 - effect.amplifier * 0.0021) - 0.05 * effect.amplifier;
					effect.inflictor.getGun().registerDamage(lastHealth - this.health);
					if (this.health < 0.00001) effect.inflictor.getGun().registerKill();
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

		double speed = speedMult * type.speed.get(GameLogic.getCurrentWorld().tiles[y][x].terrain);
		double distance = speed * time;
		double d = Math.hypot(this.x - nextNode.getAbsX(), this.y - nextNode.getAbsY());

		for (StatusEffect effect : statusEffects) {
			if (effect.type == StatusEffectType.BLEED) {
				this.health -= 10 * effect.amplifier * distance;
				effect.inflictor.getGun().registerDamage(10 * effect.amplifier * distance);
				if (this.health < 0.00001) effect.inflictor.getGun().registerKill();
			}
		}

		if (distance > d) {
			this.x = nextNode.getAbsX();
			this.y = nextNode.getAbsY();
			this.oldPrev = prevNode;
			this.prevNode = nextNode;
			this.nextNode = findNextNode();//nextNode.getNextNode();
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
	
	int chance = 2;
	
	private Node findNextNode() {
		if (GameLogic.rand.nextInt(chance / 2) == 0) {
			chance++;
			return prevNode.getNextNode();
		}

		chance = 2;
		
		Node next = prevNode.getNextNode();
		if (next == null) return null;
		if (next.getNextNode() == null) return next;
		
		Node[] nodes = PathFinder.getNeighbors(GameLogic.getNodes(type), prevNode);
		
		Object[] ns = Arrays.stream(nodes).filter(n -> !n.isBlocked() && n != oldPrev && n.getNextNode() != prevNode && n.getCost() - next.getCost() < 0.05 * Math.pow(n.getCost() * 3, 0.25)).toArray();
		return ns.length <= 0 ? next : (Node) ns[GameLogic.rand.nextInt(ns.length)];
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

	public Node getPrevNode() {
		return this.prevNode;
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
		this.statusEffects.remove(effect);
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
