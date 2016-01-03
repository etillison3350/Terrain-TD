package terraintd.pathfinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import terraintd.GameLogic;
import terraintd.object.CollidableEntity;
import terraintd.object.Entity;
import terraintd.types.EnemyType;
import terraintd.types.World;

public class PathFinder {

	/**
	 * The best <code>double</code> representation of one-half of the positive square root of two
	 */
	public static final double SQRT2D2 = .7071067811865476;

	static final Comparator<Node> nodeComp = new Comparator<Node>() {

		@Override
		public int compare(Node o1, Node o2) {
			return o1 == o2 ? 0 : Double.compare(o1.getCost() + 0.0001, o2.getCost());
		}

	};

	public final GameLogic logic;

	public PathFinder(GameLogic logic) {
		this.logic = logic;
	}

	public Node[][][] calculatePaths(EnemyType type) {
		World world = logic.getCurrentWorld();

		Node[][][] nodes = new Node[world.getHeight() + 1][world.getWidth() + 1][2];

		TreeSet<Node> nodeSet = new TreeSet<>(nodeComp);

		for (int y = 0; y <= world.getHeight(); y++) {
			for (int x = 0; x <= world.getWidth(); x++) {
				nodes[y][x][0] = new Node(x, y, false);
				nodes[y][x][1] = new Node(x, y, true);
			}
		}

		for (Entity e : logic.getPermanentEntities()) {
			if (!(e instanceof CollidableEntity)) continue;

			CollidableEntity c = (CollidableEntity) e;

			for (int y = 0; y <= c.getHeight(); y++) {
				for (int x = 0; x <= c.getWidth(); x++) {
					if (y < c.getHeight()) nodes[(int) (c.getY() + y)][(int) (c.getX() + x)][0].setClosed(true);
					if (x < c.getWidth()) nodes[(int) (c.getY() + y)][(int) (c.getX() + x)][1].setClosed(true);
				}
			}
		}

		if (nodes[world.goal.y][world.goal.x][0].isClosed()) return nodes;

		nodeSet.add(nodes[world.goal.y][world.goal.x][0]);

		while (nodeSet.size() > 0) {
			Node node = nodeSet.iterator().next();

			ArrayList<Node> nodeList = new ArrayList<>(nodeSet);
			nodeList.remove(node);
			nodeSet.clear();
			nodeSet.addAll(nodeList);
			node.setClosed(true);

			Node[] next = new Node[6];
			if (node.top) {
				if (node.y != 0) {
					next[0] = nodes[node.y - 1][node.x][1];
					next[2] = nodes[node.y - 1][node.x][0];
					if (node.x != world.getWidth()) next[4] = nodes[node.y - 1][node.x + 1][0];
				}

				if (node.y != world.getHeight()) {
					next[1] = nodes[node.y + 1][node.x][1];
					next[3] = nodes[node.y][node.x][0];
					if (node.x != world.getWidth()) next[5] = nodes[node.y][node.x + 1][0];
				}
			} else {
				if (node.x != 0) {
					next[0] = nodes[node.y][node.x - 1][0];
					next[2] = nodes[node.y][node.x - 1][1];
					if (node.y != world.getHeight()) next[4] = nodes[node.y + 1][node.x - 1][1];
				}

				if (node.x != world.getWidth()) {
					next[1] = nodes[node.y][node.x + 1][0];
					next[3] = nodes[node.y][node.x][1];
					if (node.y != world.getHeight()) next[5] = nodes[node.y + 1][node.x][1];
				}
			}

			for (int i = 0; i < 6; i++) {
				Node n = next[i];

				if (n == null || n.isClosed()) continue;

				int y = node.y - (node.top && (i % 2 == 0) ? 1 : 0);
				int x = node.x - (!node.top && (i % 2 == 0) ? 1 : 0);
				double speed = type.speed.get(world.tiles[y][x].terrain);

				if (speed < Double.MIN_VALUE) continue;
				
//				double nodeElev = 0.5 * (double) (world.tiles[node.y][node.x].elev + ((node.top ? node.y : node.x) <= 0 ? world.tiles[node.y][node.x].elev : (node.top ? world.tiles[node.y - 1][node.x].elev : world.tiles[node.y][node.x - 1].elev)));
//				double nElev = 0.5 * (double) (world.tiles[n.y][n.x].elev + ((n.top ? n.y : n.x) <= 0 ? world.tiles[n.y][n.x].elev : (n.top ? world.tiles[n.y - 1][n.x].elev : world.tiles[n.y][n.x - 1].elev)));
				double nodeElev, nElev;
				if (node.top) {
					if (node.y <= 0)
						nodeElev = world.tiles[node.y][node.x].elev;
					else if (node.y >= world.getHeight())
						nodeElev = world.tiles[node.y - 1][node.x].elev;
					else
						nodeElev = 0.5 * (double) (world.tiles[node.y][node.x].elev + world.tiles[node.y - 1][node.x].elev);
				} else {
					if (node.x <= 0)
						nodeElev = world.tiles[node.y][node.x].elev;
					else if (node.x >= world.getWidth())
						nodeElev = world.tiles[node.y][node.x - 1].elev;
					else
						nodeElev = 0.5 * (double) (world.tiles[node.y][node.x].elev + world.tiles[node.y][node.x - 1].elev);
				}

				if (n.top) {
					if (n.y <= 0)
						nElev = world.tiles[n.y][n.x].elev;
					else if (n.y >= world.getHeight())
						nElev = world.tiles[n.y - 1][n.x].elev;
					else
						nElev = 0.5 * (double) (world.tiles[n.y][n.x].elev + world.tiles[n.y - 1][n.x].elev);
				} else {
					if (n.x <= 0)
						nElev = world.tiles[n.y][n.x].elev;
					else if (n.x >= world.getWidth())
						nElev = world.tiles[n.y][n.x - 1].elev;
					else
						nElev = 0.5 * (double) (world.tiles[n.y][n.x].elev + world.tiles[n.y][n.x - 1].elev);
				}

				if (nElev > nodeElev)
					speed *= Math.pow(type.downSpeed, nElev - nodeElev);
				else if (nodeElev > nElev)
					speed *= Math.pow(type.upSpeed, nodeElev - nElev);

				if (speed < Double.MIN_VALUE) continue;
				
				double newCost = node.getCost() + (i < 2 ? 1 : SQRT2D2) / speed;
				if (n.getCost() == 0 || newCost < n.getCost()) {
					n.setCost(newCost);
					n.setNext(node);
				}
				nodeSet.add(n);
			}
		}

		return nodes;
	}

}
