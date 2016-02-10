package terraintd.pathfinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

	private PathFinder() {}

	static final Comparator<Node> nodeComp = new Comparator<Node>() {

		@Override
		public int compare(Node o1, Node o2) {
			return o1 == o2 ? 0 : Double.compare(o1.getCost() + 0.0001, o2.getCost());
		}

	};

	public static Node[][][] calculatePaths(EnemyType type) {
		return calculatePaths(type, GameLogic.getPermanentEntities(), GameLogic.getCurrentWorld());
	}

	public static Node[][][] calculatePaths(EnemyType type, Entity[] entities, World world) {
		Node[][][] nodes = new Node[world.getHeight() + 1][world.getWidth() + 1][2];

		TreeSet<Node> nodeSet = new TreeSet<>(nodeComp);

		for (int y = 0; y <= world.getHeight(); y++) {
			for (int x = 0; x <= world.getWidth(); x++) {
				nodes[y][x][0] = new Node(x, y, false);
				nodes[y][x][1] = new Node(x, y, true);
			}
		}

		for (Entity e : entities) {
			if (!(e instanceof CollidableEntity)) continue;

			CollidableEntity c = (CollidableEntity) e;

			for (int y = 0; y <= c.getHeight(); y++) {
				for (int x = 0; x <= c.getWidth(); x++) {
					if (y < c.getHeight()) nodes[(int) (c.getY() + y)][(int) (c.getX() + x)][0].close();
					if (x < c.getWidth()) nodes[(int) (c.getY() + y)][(int) (c.getX() + x)][1].close();
				}
			}
		}

//		if (nodes[world.goal.y][world.goal.x][world.goal.top ? 1 : 0].isClosed()) return nodes;

		for (Node goal : world.goals)
			nodeSet.add(nodes[goal.y][goal.x][goal.top ? 1 : 0]);

		while (nodeSet.size() > 0) {
			Node node = nodeSet.first();

			ArrayList<Node> nodeList = new ArrayList<>(nodeSet);
			nodeList.remove(node);
			nodeSet.clear();
			nodeSet.addAll(nodeList);
			node.explore();

//			Node[] next = new Node[6];
//			if (node.top) {
//				if (node.y != 0) {
//					next[0] = nodes[node.y - 1][node.x][1];
//					next[2] = nodes[node.y - 1][node.x][0];
//					if (node.x != world.getWidth()) next[4] = nodes[node.y - 1][node.x + 1][0];
//				}
//
//				if (node.y != world.getHeight()) {
//					next[1] = nodes[node.y + 1][node.x][1];
//					next[3] = nodes[node.y][node.x][0];
//					if (node.x != world.getWidth()) next[5] = nodes[node.y][node.x + 1][0];
//				}
//			} else {
//				if (node.x != 0) {
//					next[0] = nodes[node.y][node.x - 1][0];
//					next[2] = nodes[node.y][node.x - 1][1];
//					if (node.y != world.getHeight()) next[4] = nodes[node.y + 1][node.x - 1][1];
//				}
//
//				if (node.x != world.getWidth()) {
//					next[1] = nodes[node.y][node.x + 1][0];
//					next[3] = nodes[node.y][node.x][1];
//					if (node.y != world.getHeight()) next[5] = nodes[node.y + 1][node.x][1];
//				}
//			}
//
//			for (int i = 0; i < 6; i++) {
//				Node n = next[i];
//
//				if (n == null || n.isClosed()) continue;
//
//				int y = node.y - (node.top && (i % 2 == 0) ? 1 : 0);
//				int x = node.x - (!node.top && (i % 2 == 0) ? 1 : 0);
//				double speed = type.speed.get(world.tiles[y][x].terrain);
//
//				if (speed < Double.MIN_VALUE) continue;
//
//				double newCost = node.getCost() + (i < 2 ? 1 : SQRT2D2) / speed;
//				if (n.getCost() == 0 || newCost < n.getCost()) {
//					n.setCost(newCost);
//					n.setNext(node);
//				}
//				nodeSet.add(n);
//			}
			
			for (Node n : getNeighbors(nodes, node)) {
				if (n.isClosed()) continue;
				
//				int y = node.y - (node.top && (i % 2 == 0) ? 1 : 0);
//				int x = node.x - (!node.top && (i % 2 == 0) ? 1 : 0);
				int x = node.x - (!node.top && node.x - n.x == 1 ? 1 : 0);
				int y = node.y - (node.top && node.y - n.y == 1 ? 1 : 0);
				double speed = type.speed.get(world.tiles[y][x].terrain);

				if (speed < Double.MIN_VALUE) continue;

				double newCost = node.getCost() + (n.top == node.top ? 1 : SQRT2D2) / speed;
				if (n.getCost() == 0 || newCost < n.getCost()) {
					n.setCost(newCost);
					n.setNext(node);
				}
				nodeSet.add(n);
			}
		}

		return nodes;
	}
	
	public static final Node[] getNeighbors(Node[][][] nodes, Node node) {
		return getNeighbors(nodes, node.x, node.y, node.top);
	}
	
	public static final Node[] getNeighbors(Node[][][] nodes, int x, int y, boolean top) {
		List<Node> neighbors = new ArrayList<>();
		if (top) {
			if (y != 0) {
				neighbors.add(nodes[y - 1][x][1]);
				neighbors.add(nodes[y - 1][x][0]);
				if (x != nodes[0].length - 1) neighbors.add(nodes[y - 1][x + 1][0]);
			}

			if (y != nodes.length - 1) {
				neighbors.add(nodes[y + 1][x][1]);
				neighbors.add(nodes[y][x][0]);
				if (x != nodes[0].length - 1) neighbors.add(nodes[y][x + 1][0]);
			}
		} else {
			if (x != 0) {
				neighbors.add(nodes[y][x - 1][0]);
				neighbors.add(nodes[y][x - 1][1]);
				if (y != nodes.length - 1) neighbors.add(nodes[y + 1][x - 1][1]);
			}

			if (x != nodes[0].length - 1) {
				neighbors.add(nodes[y][x + 1][0]);
				neighbors.add(nodes[y][x][1]);
				if (y != nodes.length - 1) neighbors.add(nodes[y + 1][x][1]);
			}
		}
		
		return neighbors.toArray(new Node[neighbors.size()]);
	}

}
