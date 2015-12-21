package terraintd.types;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import terraintd.pathfinder.Node;

public class World {

	public final Tile[][] tiles;
	public final Point goal;

	private BufferedImage image;

	static World[] values;

	public World(Tile[][] tiles, Point goal) {
		this.tiles = tiles;
		this.goal = goal;
	}

	public World(Tile[][] tiles, int goalX, int goalY) {
		this(tiles, new Point(goalX, goalY));
	}

	public static World[] values() {
		if (values == null) generateValues();

		return values;
	}

	static void generateValues() {
		try {
			Files.createDirectories(Paths.get("terraintd/mods"));
		} catch (IOException e) {}

		List<World> newValues = new ArrayList<>();

		try (Stream<Path> files = Files.walk(Paths.get("terraintd/mods"))) {
			Iterator<Path> iter = files.iterator();

			while (iter.hasNext()) {
				Path path = iter.next();

				if (Files.isDirectory(path) || !path.toString().replaceAll(".+\\.", "").equals("world")) continue;

				String world[] = new String(Files.readAllBytes(path)).split("\n[ \t\\x0B\\f\\r]*\n");

				String[] types = world[0].split("\n");
				String[] elev = world[1].split("\n");

				final int r = types.length;
				final int c = types[0].split(" +").length;

				int[][] elevations = new int[r][c];
				Terrain[][] terrains = new Terrain[r][c];
				boolean[][] spawnPoints = new boolean[r][c];

				int goalX = 0, goalY = 0;

				int minElev = Integer.MAX_VALUE;

				for (int row = 0; row < r; row++) {
					String[] type = types[row].split(" +");
					String[] elv = elev[row].split(" ");

					for (int col = 0; col < c; col++) {
						try {
							terrains[row][col] = Terrain.values()[Integer.parseUnsignedInt(type[col].replaceAll("\\D+", ""))];
						} catch (Exception e) {
							terrains[row][col] = Terrain.DEEP_WATER;
						}

						try {
							if (type[col].toLowerCase().contains("s")) {
								spawnPoints[row][col] = true;
							} else if (type[col].toLowerCase().contains("x")) {
								goalX = col;
								goalY = row;
							}
						} catch (ArrayIndexOutOfBoundsException e) {}

						try {
							elevations[row][col] = Integer.parseInt(elv[col]);
							if (elevations[row][col] < minElev) minElev = elevations[row][col];
						} catch (Exception e) {
							elevations[row][col] = 0;
						}
					}
				}

				Tile[][] tiles = new Tile[r][c];
				for (int row = 0; row < r; row++) {
					for (int col = 0; col < c; col++) {
						tiles[row][col] = new Tile(terrains[row][col], elevations[row][col] - minElev, spawnPoints[row][col]);
					}
				}

				newValues.add(new World(tiles, goalX, goalY));
			}
		} catch (IOException e) {}

		values = newValues.toArray(new World[newValues.size()]);
	}

	public int getWidth() {
		return tiles[0].length;
	}

	public int getHeight() {
		return tiles.length;
	}

	public BufferedImage getImage() {
		return image;
	}

	public BufferedImage recalculateImageForSize(double size) {
		image = new BufferedImage(Math.max(1, (int) (size * getWidth())), Math.max(1, (int) (size * getHeight())), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();

//		g.setColor(Color.GREEN);

		g.setColor(Color.BLACK);

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
				g.drawImage(tiles[y][x].terrain.getImage(), (int) (size * x), (int) (size * y), (int) size, (int) size, null);
//				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) tiles[y][x].elev / 8.0F));

				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75F));

				if (y != 0 && tiles[y][x].elev - tiles[y - 1][x].elev > 0)
					g.drawLine((int) (size * x), (int) (size * y), (int) (size * (x + 1)), (int) (size * y));

				if (x != 0 && tiles[y][x].elev - tiles[y][x - 1].elev > 0)
					g.drawLine((int) (size * x), (int) (size * y), (int) (size * x), (int) (size * (y + 1)));

				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.75F));

				boolean b = y != getHeight() - 1 && tiles[y][x].elev - tiles[y + 1][x].elev > 0;

				if (b)
					g.fillRect((int) (size * x), (int) (size * (y + 1)), (int) size, (int) size / 3);

				if (x != getWidth() - 1 && tiles[y][x].elev - tiles[y][x + 1].elev > 0)
					g.fillRect((int) (size * (x + 1)), (int) (size * y), (int) size / 5, 4 * (int) size / (b && tiles[y][x].elev - tiles[y + 1][x + 1].elev > 0 ? 3 : 4));

//				try {
//					d = tiles[y][x].elev - tiles[y - 1][x].elev;
//					if (d > 0) {
//						g.setColor(Color.WHITE);
//						g.drawLine((int) (size * x), (int) (size * y), (int) (size * (x + 1)), (int) (size * y));
//					}
//				} catch (ArrayIndexOutOfBoundsException e) {}
//				try {
//					d = tiles[y][x].elev - tiles[y + 1][x].elev;
//					if (d > 0) {
//						g.setColor(Color.BLACK);
//						g.drawLine((int) (size * x), (int) (size * y), (int) (size * x), (int) (size * (y + 1)));
//					}
//				} catch (ArrayIndexOutOfBoundsException e) {}
//				try {
//					d = tiles[y][x].elev - tiles[y][x - 1].elev;
//					if (d > 0) {
//						g.setColor(Color.WHITE);
//						g.drawLine((int) (size * x), (int) (size * (y + 1)), (int) (size * (x + 1)), (int) (size * (y + 1)));
//					}
//				} catch (ArrayIndexOutOfBoundsException e) {}
//				try {
//					d = tiles[y][x].elev - tiles[y][x + 1].elev;
//					if (d > 0) {
//						g.setColor(Color.BLACK);
//						g.drawLine((int) (size * (x + 1)), (int) (size * y), (int) (size * (x + 1)), (int) (size * (y + 1)));
//					}
//				} catch (ArrayIndexOutOfBoundsException e) {}
			}
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g.setColor(Color.WHITE);

		Node node = n;
		if (node != null) {
			while (node.getNextNode() != null) {
				Node nx = node.getNextNode();

				g.drawLine((int) (size * node.getAbsX()), (int) (size * node.getAbsY()), (int) (size * nx.getAbsX()), (int) (size * nx.getAbsY()));
				node = nx;
			}
		}

		g.dispose();

		return this.image;
	}

	public static class Tile {

		public final Terrain terrain;
		public final int elev;
		public final boolean spawn;

		Tile(Terrain terrain, int elevation, boolean spawnPoint) {
			this.terrain = terrain;
			this.elev = elevation;
			this.spawn = spawnPoint;
		}

	}

	Node n;

	public void setNode(Node n) {
		this.n = n;
	}

}
