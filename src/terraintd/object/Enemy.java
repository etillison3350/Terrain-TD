package terraintd.object;

import terraintd.GameLogic;
import terraintd.object.Gun.TempProjectile;
import terraintd.pathfinder.Node;
import terraintd.types.EnemyType;
import terraintd.types.World;

public class Enemy extends Entity implements Weapon {

	public final EnemyType type;
	private final Gun gun;

	private double x, y;
	private double health;

	public final World world;

	private Node nextNode, prevNode;

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
		this.gun = type.projectiles != null && type.projectiles.length > 0 ? new Gun(this) : null;
	}

	@Override
	public Gun getGun() {
		return this.gun;
	}

	@Override
	public Projectile[] convertFromTempProjectiles(TempProjectile[] temps) {
		Projectile[] ps = new Projectile[temps.length];

		for (int n = 0; n < temps.length; n++) {
			ps[n] = new Projectile(temps[n].type, this);
		}

		return ps;
	}

	public boolean move() {
		return move(GameLogic.FRAME_TIME);
	}

	protected boolean move(double time) {
		int x = prevNode.x - (!prevNode.top && prevNode.x - nextNode.x == 1 ? 1 : 0);
		int y = prevNode.y - (prevNode.top && prevNode.y - nextNode.y == 1 ? 1 : 0);

		double speed = type.speed.get(world.tiles[y][x].terrain);
		double distance = speed * time;
		double d = Math.hypot(this.x - nextNode.getAbsX(), this.y - nextNode.getAbsY());

		if (distance > d) {
			this.x = nextNode.getAbsX();
			this.y = nextNode.getAbsY();
			this.prevNode = nextNode;
			this.nextNode = nextNode.getNextNode();
			return nextNode == null ? false : move((distance - d) / speed);
		}

		double dx = nextNode.getAbsX() - prevNode.getAbsX();
		double dy = nextNode.getAbsY() - prevNode.getAbsY();

		this.y += dy * distance;
		this.x += dx * distance;

		return true;
	}

	public void resetNodes(Node[][][] nodes) {
		if (this.nextNode != null) this.nextNode = nodes[nextNode.y][nextNode.x][nextNode.top ? 1 : 0];
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
	public int damage(double damage) {
		this.health -= damage;

		if (this.health <= 0.00001) {
			return this.type.reward;
		}

		return 0;
	}

	public double getHealth() {
		return this.health;
	}

}
