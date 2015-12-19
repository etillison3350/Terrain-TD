package terraintd.pathfinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import terraintd.GameLogic;
import terraintd.object.CollidableEntity;
import terraintd.object.Entity;
import terraintd.types.World;

public class PathFinder {

	public static final double SQRT2D2 = .7071067811865476;
	
	public final GameLogic logic;

	public PathFinder(GameLogic logic) {
		this.logic = logic;
	}

	private Node[][][] nodes;

	public boolean calculatePaths() {
		World world = logic.getCurrentWorld();

		Node[][][] nodes = new Node[world.getHeight() + 1][world.getWidth() + 1][2];

		TreeSet<Node> nodeSet = new TreeSet<>(new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return o1 == o2 ? 0 : Double.compare(o1.getCost() + 0.0001, o2.getCost());
			}

		});

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
					nodes[(int) (c.getY() + y)][(int) (c.getX() + x)][0].setClosed(true);
					nodes[(int) (c.getY() + y)][(int) (c.getX() + x)][1].setClosed(true);
				}
			}
		}

		if (nodes[world.goal.x][world.goal.y][0].isClosed()) return false;

		nodeSet.add(nodes[world.goal.x][world.goal.y][0]);

		while (nodeSet.size() > 0) {
			System.out.println(nodeSet.size());
			
			Node node = nodeSet.iterator().next();

			ArrayList<Node> nodeList = new ArrayList<>(nodeSet); 
			nodeList.remove(node);
			nodeSet.clear();
			nodeSet.addAll(nodeList);
			node.setClosed(true);
			
			Node[] next = new Node[6];
			if (node.top) {
				if (node.y != 0) {
					next[0] = nodes[node.y - 1][node.x][0];
					next[2] = nodes[node.y - 1][node.x][1];
					if (node.x != world.getWidth()) next[4] = nodes[node.y - 1][node.x + 1][1];
				}

				if (node.y != world.getHeight()) {
					next[1] = nodes[node.y + 1][node.x][0];
					next[3] = nodes[node.y][node.x][1];
					if (node.x != world.getWidth()) next[5] = nodes[node.y][node.x + 1][1];
				}
			} else {
				if (node.x != 0) {
					next[0] = nodes[node.y][node.x - 1][1];
					next[2] = nodes[node.y][node.x - 1][0];
					if (node.y != world.getHeight()) next[4] = nodes[node.y + 1][node.x - 1][0];
				}

				if (node.x != world.getWidth()) {
					next[1] = nodes[node.y][node.x + 1][1];
					next[3] = nodes[node.y][node.x][0];
					if (node.y != world.getHeight()) next[5] = nodes[node.y + 1][node.x][0];
				}
			}
			
			for (int i = 0; i < 6; i++) {
				Node n = next[i];
				if (n == null || n.isClosed()) continue;
				
				n.setNext(node);
				n.setCost(node.getCost() + (i < 2 ? 1 : SQRT2D2));
				nodeSet.add(n);
				System.out.println(nodeSet.size());
			}
		}

		this.nodes = nodes;
		return true;
	}

	public Node[][][] getNodes() {
		return nodes;
	}

}
