package terraintd.types;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import terraintd.pathfinder.Node;

public class World extends ModdedType {

	public final Tile[][] tiles;
	public final Node[] spawnpoints;
	public final Node[] goals;

	private BufferedImage image;

	static World[] values;

	protected World(Mod mod, String id, Tile[][] tiles, Node[] goals, Node[] spawnpoints) {
		super(mod, id);

		typeIds.put(id, this);
		
		this.tiles = tiles;
		this.goals = goals;
		this.spawnpoints = spawnpoints;
	}

	static final HashMap<String, World> typeIds = new HashMap<>();

	public static World valueOf(String id) {
		return typeIds.get(id);
	}

	public static World[] values() {
		return TypeGenerator.worlds();
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
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(Color.BLACK);

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
				g.drawImage(tiles[y][x].terrain.getImage(), (int) (size * x), (int) (size * y), (int) size, (int) size, null);

				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75F));

				if (y != 0 && tiles[y][x].elev - tiles[y - 1][x].elev > 0) g.drawLine((int) (size * x), (int) (size * y), (int) (size * (x + 1)), (int) (size * y));

				if (x != 0 && tiles[y][x].elev - tiles[y][x - 1].elev > 0) g.drawLine((int) (size * x), (int) (size * y), (int) (size * x), (int) (size * (y + 1)));

				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.75F));

				boolean b = y != getHeight() - 1 && tiles[y][x].elev - tiles[y + 1][x].elev > 0;

				if (b) g.fillRect((int) (size * x), (int) (size * (y + 1)), (int) size, (int) size / 3);

				if (x != getWidth() - 1 && tiles[y][x].elev - tiles[y][x + 1].elev > 0) g.fillRect((int) (size * (x + 1)), (int) (size * y), (int) size / 5, 4 * (int) size / (b && tiles[y][x].elev - tiles[y + 1][x + 1].elev > 0 ? 3 : 4));
			}
		}

		g.dispose();

		return this.image;
	}

	public static class Tile {

		public final Terrain terrain;
		public final int elev;

		Tile(Terrain terrain, int elevation) {
			this.terrain = terrain;
			this.elev = elevation;
		}

	}

}
