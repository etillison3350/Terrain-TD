package terraintd.window;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import terraintd.GameLogic;
import terraintd.object.CollidableEntity;
import terraintd.object.Entity;
import terraintd.pathfinder.Node;
import terraintd.types.CollidableType;
import terraintd.types.EnemyType;
import terraintd.types.ProjectileType;
import terraintd.types.TowerType;
import terraintd.types.World;

public class GamePanel extends JPanel {

	private static final long serialVersionUID = 6409717885667732875L;

	private final GameLogic logic;

	private final Window window;

	public GamePanel(Window window) {
		this.window = window;

		this.setBackground(Color.BLACK);

		this.logic = new GameLogic(this, window);

		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				//				tile = getWidth()
				tile = Math.min(128, (int) Math.min((double) getWidth() / (double) logic.getCurrentWorld().getWidth(), (double) getHeight() / (double) logic.getCurrentWorld().getHeight()));
				dx = ((double) getWidth() - (logic.getCurrentWorld().getWidth() * tile)) * 0.5;
				dy = ((double) getHeight() - (logic.getCurrentWorld().getHeight() * tile)) * 0.5;

				logic.getCurrentWorld().recalculateImageForSize(tile);

				repaint();
			}

		});

		MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				mouseX = (e.getX() - dx) / tile;
				mouseY = (e.getY() - dy) / tile;

				if (logic.getBuyingType() != null)
					repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (logic.getBuyingType() == null) return;

				int x = (int) Math.round(mouseX - logic.getBuyingType().width / 2.0);
				int y = (int) Math.round(mouseY - logic.getBuyingType().height / 2.0);

				if (logic.canPlaceObject(logic.getBuyingType(), x, y)) {
					logic.buyObject(x, y);
					repaint();
				}
			}

		};

		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);
	}

	private double mouseX, mouseY;

	private double tile, dx, dy;

	@Override
	public void paintComponent(Graphics gg) {
		super.paintComponent(gg);

		if (!(gg instanceof Graphics2D)) return;

		Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(logic.getCurrentWorld().getImage(), (int) dx, (int) dy, null);

		g.setColor(Color.RED);
		World world = logic.getCurrentWorld();
		for (Node[][] nodess : logic.getNodes(EnemyType.values()[0])) {
			for (Node[] nodes : nodess) {
				for (Node node : nodes) {
					try {
						double nodeElev;
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
						g.drawString(String.format("%.3g %.3g", node.getCost(), nodeElev), (int) (dx + node.getAbsX() * tile) - 25, (int) (dy + node.getAbsY() * tile) + 5);
					} catch (ArrayIndexOutOfBoundsException e) {}
					if (node.getNextNode() == null) continue;
					g.drawLine((int) (dx + node.getAbsX() * tile), (int) (dy + node.getAbsY() * tile), (int) (dx + node.getNextNode().getAbsX() * tile), (int) (dy + node.getNextNode().getAbsY() * tile));
				}
			}
		}

		for (Entity e : logic.getPermanentEntities()) {
			if (!(e instanceof CollidableEntity)) continue;

			CollidableType type = ((CollidableEntity) e).getType();
			g.drawImage(type.image.image, (int) (dx + e.getX() * tile), (int) (dy + e.getY() * tile), (int) (tile * type.width), (int) (tile * type.height), null);
		}

		if (logic.getBuyingType() != null && mouseX >= 0 && mouseX < logic.getCurrentWorld().getWidth() && mouseY >= 0 && mouseY < logic.getCurrentWorld().getHeight()) {
			int x = (int) Math.round(mouseX - logic.getBuyingType().width / 2.0);
			int y = (int) Math.round(mouseY - logic.getBuyingType().height / 2.0);

			g.setColor(logic.canPlaceObject(logic.getBuyingType(), x, y) ? Color.GREEN : Color.RED);
			g.fillRect((int) (dx + x * tile), (int) (dy + y * tile), (int) (tile * logic.getBuyingType().width), (int) (tile * logic.getBuyingType().height));
			g.drawImage(logic.getBuyingType().image.image, (int) (dx + x * tile), (int) (dy + y * tile), (int) (tile * logic.getBuyingType().width), (int) (tile * logic.getBuyingType().height), null);

			if (logic.getBuyingType() instanceof TowerType) {
				TowerType tower = (TowerType) logic.getBuyingType();

				double r = 0;
				for (ProjectileType p : tower.projectiles) {
					if (p.range > r) r = p.range;
				}

				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(3));
				double cx = x + tower.width / 2.0;
				double cy = y + tower.height / 2.0;
				g.drawOval((int) (dx + (cx - r) * tile), (int) (dy + (cy - r) * tile), (int) (2 * r * tile), (int) (2 * r * tile));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25F));
				g.fillOval((int) (dx + (cx - r) * tile), (int) (dy + (cy - r) * tile), (int) (2 * r * tile), (int) (2 * r * tile));
			}
		}
	}

	public double getTile() {
		return tile;
	}

	public double getDx() {
		return dx;
	}

	public double getDy() {
		return dy;
	}

	public GameLogic getLogic() {
		return logic;
	}

	public double getMouseX() {
		return mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}

}
