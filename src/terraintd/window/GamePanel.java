package terraintd.window;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import terraintd.GameLogic;
import terraintd.GameLogic.State;
import terraintd.Language;
import terraintd.object.CollidableEntity;
import terraintd.object.Enemy;
import terraintd.object.Entity;
import terraintd.object.Projectile;
import terraintd.object.Tower;
import terraintd.pathfinder.Node;
import terraintd.types.CollidableType;
import terraintd.types.EnemyType;
import terraintd.types.ImageType;
import terraintd.types.TowerType;
import terraintd.window.ImageManager.Resource;

public class GamePanel extends JPanel {

	private static final long serialVersionUID = 6409717885667732875L;

	static final GamePanel panel = new GamePanel();

	private static BufferedImage goal;

	private GamePanel() {
		this.setBackground(Color.BLACK);

		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				if (GameLogic.getCurrentWorld() == null) return;

				tile = Math.min(128, (int) Math.min((double) getWidth() / (double) (GameLogic.getCurrentWorld().getWidth() + 1), (double) getHeight() / (double) (GameLogic.getCurrentWorld().getHeight() + 1)));
				dx = ((double) getWidth() - (GameLogic.getCurrentWorld().getWidth() * tile)) * 0.5;
				dy = ((double) getHeight() - (GameLogic.getCurrentWorld().getHeight() * tile)) * 0.5;

				GameLogic.getCurrentWorld().recalculateImageForSize(tile);

				repaint();
			}

		});

		MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				mouseX = (e.getX() - dx) / tile;
				mouseY = (e.getY() - dy) / tile;

				if (GameLogic.getBuyingType() != null) repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (GameLogic.getBuyingType() != null) {
					int x = (int) Math.round(mouseX - GameLogic.getBuyingType().width * 0.5);
					int y = (int) Math.round(mouseY - GameLogic.getBuyingType().height * 0.5);

					if (GameLogic.canPlaceObject(GameLogic.getBuyingType(), x, y)) {
						GameLogic.buyObject(x, y);
						repaint();
					}
				} else {
					for (Entity entity : GameLogic.getPermanentEntities()) {
						Point2D p = new Point2D.Double((e.getX() - dx) / tile, (e.getY() - dy) / tile);

						if (entity.getRectangle().contains(p)) {
							GameLogic.setSelectedEntity(entity);
							repaint();
							return;
						}
					}

					GameLogic.setSelectedEntity(null);
					repaint();
				}
			}

		};

		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);

		try {
			goal = ImageIO.read(Paths.get("terraintd/mods/base/images/goal.png").toFile());
		} catch (IOException e1) {}
	}

	private static double mouseX, mouseY;

	private static double tile, dx, dy;

	public static void repaintPanel() {
		panel.repaint();
	}

	public static void repaintPanel(int x, int y, int width, int height) {
		panel.repaint(x, y, width, height);
	}

	@Override
	public void paintComponent(Graphics gg) {
		super.paintComponent(gg);

		if (!(gg instanceof Graphics2D) || GameLogic.getCurrentWorld() == null) return;

		Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(GameLogic.getCurrentWorld().getImage(), (int) dx, (int) dy, null);

		try {
			g.setColor(Color.WHITE);
			for (Node[][] nodess : GameLogic.getNodes(EnemyType.values()[0])) {
				for (Node[] nodes : nodess) {
					for (Node node : nodes) {
						if (node.getNextNode() == null) continue;
						g.drawLine((int) (dx + node.getAbsX() * tile), (int) (dy + node.getAbsY() * tile), (int) (dx + node.getNextNode().getAbsX() * tile), (int) (dy + node.getNextNode().getAbsY() * tile));
					}
				}
			}
		} catch (Exception e) {}

		g.setColor(new Color(255, 255, 192));
		List<Node> spawnpoints = Arrays.asList(GameLogic.getCurrentWorld().spawnpoints);
		for (Node spawn : spawnpoints) {
			int w = spawn.top && spawnpoints.contains(new Node(spawn.x + 1, spawn.y, true)) ? (int) tile : 7;
			int h = !spawn.top && spawnpoints.contains(new Node(spawn.x, spawn.y + 1, false)) ? (int) tile : 7;
			g.fillRect((int) (dx + spawn.getAbsX() * tile) - 3, (int) (dy + spawn.getAbsY() * tile) - 3, w, h);
		}

		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) (tile * 0.75)));
		g.setStroke(new BasicStroke((float) tile / 10.0F));
		for (Entity e : GameLogic.getPermanentEntities()) {
			if (!(e instanceof Enemy)) continue;

			Enemy en = (Enemy) e;

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - en.getDeathTime())));

			if (en.isDead()) {
				g.setColor(Color.GREEN);
				g.drawString(String.format(Language.getCurrentLocale(), "+%d", en.type.reward), (float) (dx + e.getX() * tile), (float) (dy + (e.getY() - en.getDeathTime()) * tile));
			}

			BufferedImage img = ImageManager.get(new Resource(en));

			AffineTransform trans = new AffineTransform();
			trans.translate(dx + tile * en.getX() - img.getWidth() * 0.5, dy + tile * en.getY() - img.getHeight() * 0.5);
			trans.rotate(en.getRotation(), img.getWidth() * 0.5, img.getHeight() * 0.5);
			g.drawImage(img, trans, null);

			g.setColor(Color.BLACK);
			g.drawLine((int) (dx + tile * (en.getX() - en.type.hbWidth / 2)), (int) (dy + tile * (en.getY() + en.type.hbY)), (int) (dx + tile * (en.getX() + en.type.hbWidth / 2)), (int) (dy + tile * (en.getY() + en.type.hbY)));
			if (en.getDead() == 0) {
				g.setColor(en.getStatusEffects().length > 0 ? new Resource(en).color : Color.getHSBColor((float) (0.333 * en.getHealth() / en.type.health), 1.0F, 1.0F));
				g.drawLine((int) (dx + tile * (en.getX() - en.type.hbWidth / 2)), (int) (dy + tile * (en.getY() + en.type.hbY)), (int) (dx + tile * (en.getX() + en.type.hbWidth * (en.getHealth() / en.type.health - 0.5))), (int) (dy + tile * (en.getY() + en.type.hbY)));
			}
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		for (Entity e : GameLogic.getPermanentEntities()) {
			if (!(e instanceof CollidableEntity)) continue;

			CollidableType type = ((CollidableEntity) e).getType();

			AffineTransform trans = new AffineTransform();
			trans.translate(dx + tile * (e.getX() - 0.5 * (type.image.width - type.width)), dy + tile * (e.getY() - 0.5 * (type.image.height - type.height)));
			if (e instanceof Tower) trans.rotate(((Tower) e).getRotation(), 0.5 * type.image.width * tile, 0.5 * type.image.height * tile);
			trans.scale(type.image.width * tile / (double) type.image.image.getWidth(), type.image.height * tile / (double) type.image.image.getHeight());

			g.drawImage(type.image.image, trans, null);
		}

		if (GameLogic.getSelectedEntity() != null) {
			if (GameLogic.getSelectedEntity() instanceof Tower) {
				TowerType tower = ((Tower) GameLogic.getSelectedEntity()).type;

				double r = tower.range;

				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(3));
				double cx = GameLogic.getSelectedEntity().getX() + tower.width / 2.0;
				double cy = GameLogic.getSelectedEntity().getY() + tower.height / 2.0;
				g.drawOval((int) (dx + (cx - r) * tile), (int) (dy + (cy - r) * tile), (int) (2 * r * tile), (int) (2 * r * tile));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25F));
				g.fillOval((int) (dx + (cx - r) * tile), (int) (dy + (cy - r) * tile), (int) (2 * r * tile), (int) (2 * r * tile));
			}

			float t = System.currentTimeMillis() % 1500;

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0008F * t - 0.00000053333F * t * t));
			g.setColor(Color.BLACK);
			Rectangle2D rect = GameLogic.getSelectedEntity().getRectangle();
			g.fillRect((int) (dx + rect.getX() * tile), (int) (dy + rect.getY() * tile), (int) (tile * rect.getWidth()), (int) (tile * rect.getHeight()));
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

		if (GameLogic.getBuyingType() != null && mouseX >= 0 && mouseX < GameLogic.getCurrentWorld().getWidth() && mouseY >= 0 && mouseY < GameLogic.getCurrentWorld().getHeight()) {
			int x = (int) Math.round(mouseX - GameLogic.getBuyingType().width / 2.0);
			int y = (int) Math.round(mouseY - GameLogic.getBuyingType().height / 2.0);

			if (GameLogic.getBuyingType() instanceof TowerType) {
				TowerType tower = (TowerType) GameLogic.getBuyingType();

				double r = tower.range;

				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(3));
				double cx = x + tower.width / 2.0;
				double cy = y + tower.height / 2.0;
				g.drawOval((int) (dx + (cx - r) * tile), (int) (dy + (cy - r) * tile), (int) (2 * r * tile), (int) (2 * r * tile));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25F));
				g.fillOval((int) (dx + (cx - r) * tile), (int) (dy + (cy - r) * tile), (int) (2 * r * tile), (int) (2 * r * tile));
			}

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
			g.setColor(GameLogic.canPlaceObject(GameLogic.getBuyingType(), x, y) ? Color.GREEN : Color.RED);
			g.fillRect((int) (dx + x * tile), (int) (dy + y * tile), (int) (tile * GameLogic.getBuyingType().width), (int) (tile * GameLogic.getBuyingType().height));
			g.drawImage(GameLogic.getBuyingType().image.image, (int) (dx + x * tile), (int) (dy + y * tile), (int) (tile * GameLogic.getBuyingType().width), (int) (tile * GameLogic.getBuyingType().height), null);
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

		Node goal = GameLogic.getCurrentWorld().goal;
		g.drawImage(GamePanel.goal, (int) (dx + tile * (goal.getAbsX() - 0.5)), (int) (dy + (tile * (goal.getAbsY() - 0.5))), (int) tile, (int) tile, null);

		for (Projectile p : GameLogic.getProjectiles()) {
			ImageType img = p.type.image;

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.type.dyingFade ? 1 - (float) (Math.max(0, p.getDeathTime()) / p.type.dyingFadeTime) : 1));
			AffineTransform trans = new AffineTransform();

			switch (p.type.delivery) {
				case AREA:
					g.drawImage(img.image, (int) (dx + tile * (p.getX() - p.getRadius())), (int) (dy + tile * (p.getY() - p.getRadius())), (int) (2 * tile * p.getRadius()), (int) (2 * tile * p.getRadius()), null);
					break;
				case LINE:
					trans.translate(dx + tile * (p.getX() - img.width * 0.5), dy + tile * (p.getY() - img.height * 0.5));
					trans.rotate(p.getRotation(), tile * img.width * 0.5, tile * img.height * 0.5);
					trans.scale(tile * p.getRadius() / img.image.getWidth(), tile * img.height / img.image.getHeight());
					g.drawImage(img.image, trans, null);
					break;
				case SECTOR:
					BufferedImage sec = new BufferedImage((int) (tile * p.getRadius() * 2), (int) (tile * p.getRadius() * 2), BufferedImage.TYPE_INT_ARGB);
					Graphics2D s = sec.createGraphics();
					s.drawImage(img.image, 0, 0, (int) (2 * tile * p.getRadius()), (int) (2 * tile * p.getRadius()), null);
					s.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));

					s.fillArc(0, 0, (int) (2 * tile * p.getRadius()), (int) (2 * tile * p.getRadius()), (int) Math.toDegrees(p.type.angle * 0.5 - p.getRotation()), (int) (360.0 - Math.toDegrees(p.type.angle)));
					g.drawImage(sec, (int) (dx + tile * (p.getX() - p.getRadius())), (int) (dy + tile * (p.getY() - p.getRadius())), null);
					break;
				case SINGLE_TARGET:
					trans.translate(dx + tile * (p.getX() - img.width * 0.5), dy + tile * (p.getY() - img.height * 0.5));
					trans.rotate(p.getRotation(), tile * img.width * 0.5, tile * img.height * 0.5);
					trans.scale(tile * img.width / img.image.getWidth(), tile * img.height / img.image.getHeight());
					g.drawImage(img.image, trans, null);
					break;
				default:
					break;
			}
		}

		if (GameLogic.getState() != State.PLAYING) {
			g.setColor(Color.BLACK);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			String str = GameLogic.getState().toString();

			g.setColor(Color.WHITE);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			FontMetrics fm;
			int fsize = 0;
			do {
				fm = g.getFontMetrics(new Font(Font.SANS_SERIF, Font.BOLD, ++fsize));
			} while (fm.stringWidth(str) < this.getWidth() * 0.9);
			g.setFont(fm.getFont());
			g.drawString(str, 0.5F * (this.getWidth() - fm.stringWidth(str)), this.getHeight() - (0.5F * (this.getHeight() - fm.getAscent())));
		}
	}

	public static double getTile() {
		return tile;
	}

	public static double getDx() {
		return dx;
	}

	public static double getDy() {
		return dy;
	}

	public static double getMouseX() {
		return mouseX;
	}

	public static double getMouseY() {
		return mouseY;
	}

}
