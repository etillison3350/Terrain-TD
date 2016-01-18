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
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import terraintd.GameLogic;
import terraintd.Language;
import terraintd.GameLogic.State;
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

public class GamePanel extends JPanel {

	private static final long serialVersionUID = 6409717885667732875L;

	private final GameLogic logic;

	private final Window window;

	public GamePanel(Window window) {
		this.window = window;

		this.setBackground(Color.BLACK);

		this.logic = new GameLogic(this, this.window);

		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				tile = Math.min(128, (int) Math.min((double) getWidth() / (double) (logic.getCurrentWorld().getWidth() + 1), (double) getHeight() / (double) (logic.getCurrentWorld().getHeight() + 1)));
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

				if (logic.getBuyingType() != null) repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (logic.getBuyingType() != null) {
					int x = (int) Math.round(mouseX - logic.getBuyingType().width / 2.0);
					int y = (int) Math.round(mouseY - logic.getBuyingType().height / 2.0);

					if (logic.canPlaceObject(logic.getBuyingType(), x, y)) {
						logic.buyObject(x, y);
						repaint();
					}
				} else {
					for (Entity entity : logic.getPermanentEntities()) {
						if (!(entity instanceof CollidableEntity)) continue;

						CollidableEntity ce = (CollidableEntity) entity;

						Point2D p = new Point2D.Double((e.getX() - dx) / tile, (e.getY() - dy) / tile);

						if (ce.getRectangle().contains(p)) {
							logic.setSelectedEntity(ce);
							repaint();
							return;
						}
					}

					logic.setSelectedEntity(null);
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

		g.setColor(Color.WHITE);
		for (Node[][] nodess : logic.getNodes(EnemyType.values()[0])) {
			for (Node[] nodes : nodess) {
				for (Node node : nodes) {
					if (node.getNextNode() == null) continue;
					g.drawLine((int) (dx + node.getAbsX() * tile), (int) (dy + node.getAbsY() * tile), (int) (dx + node.getNextNode().getAbsX() * tile), (int) (dy + node.getNextNode().getAbsY() * tile));
				}
			}
		}

		g.setColor(new Color(255, 255, 192));
		List<Node> spawnpoints = Arrays.asList(logic.getCurrentWorld().spawnpoints);
		for (Node spawn : spawnpoints) {
			int w = spawn.top && spawnpoints.contains(new Node(spawn.x + 1, spawn.y, true)) ? (int) tile : 7;
			int h = !spawn.top && spawnpoints.contains(new Node(spawn.x, spawn.y + 1, false)) ? (int) tile : 7;
			g.fillRect((int) (dx + spawn.getAbsX() * tile) - 3, (int) (dy + spawn.getAbsY() * tile) - 3, w, h);
		}

		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) (tile * 0.75)));
		g.setStroke(new BasicStroke((float) tile / 10.0F));
		for (Entity e : logic.getPermanentEntities()) {
			if (!(e instanceof Enemy)) continue;

			Enemy en = (Enemy) e;

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - en.getDeathTime())));

			ImageType img;
			if (en.isDead()) {
				g.setColor(Color.GREEN);
				g.drawString(String.format(Language.getCurrentLocale(), "+%d", en.type.reward), (float) (dx + e.getX() * tile), (float) (dy + (e.getY() - en.getDeathTime()) * tile));
				img = en.type.death;
			} else {
				img = en.type.image;
			}

			AffineTransform trans = new AffineTransform();
			trans.translate(dx + tile * (en.getX() - img.width * 0.5), dy + tile * (en.getY() - img.height * 0.5));
			trans.rotate(en.getRotation(), tile * img.width * 0.5, tile * img.height * 0.5);
			trans.scale(tile * img.width / img.image.getWidth(), tile * img.height / img.image.getHeight());
			g.drawImage(img.image, trans, null);

			g.setColor(Color.BLACK);
			g.drawLine((int) (dx + tile * (en.getX() - en.type.hbWidth / 2)), (int) (dy + tile * (en.getY() + en.type.hbY)), (int) (dx + tile * (en.getX() + en.type.hbWidth / 2)), (int) (dy + tile * (en.getY() + en.type.hbY)));
			if (!en.isDead()) {
				g.setColor(Color.getHSBColor((float) (0.333 * en.getHealth() / en.type.health), 1.0F, 1.0F));
				g.drawLine((int) (dx + tile * (en.getX() - en.type.hbWidth / 2)), (int) (dy + tile * (en.getY() + en.type.hbY)), (int) (dx + tile * (en.getX() + en.type.hbWidth * (en.getHealth() / en.type.health - 0.5))), (int) (dy + tile * (en.getY() + en.type.hbY)));
			}
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		for (Entity e : logic.getPermanentEntities()) {
			if (!(e instanceof CollidableEntity)) continue;

			CollidableType type = ((CollidableEntity) e).getType();

			AffineTransform trans = new AffineTransform();
			trans.translate(dx + tile * (e.getX() - 0.5 * (type.image.width - type.width)), dy + tile * (e.getY() - 0.5 * (type.image.height - type.height)));
			if (e instanceof Tower) trans.rotate(((Tower) e).getRotation(), 0.5 * type.image.width * tile, 0.5 * type.image.height * tile);
			trans.scale(type.image.width * tile / (double) type.image.image.getWidth(), type.image.height * tile / (double) type.image.image.getHeight());

			g.drawImage(type.image.image, trans, null);
		}

		if (logic.getSelectedEntity() != null && logic.getSelectedEntity() instanceof CollidableEntity) {
			if (logic.getSelectedEntity() instanceof Tower) {
				TowerType tower = ((Tower) logic.getSelectedEntity()).type;

				double r = tower.range;

				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(3));
				double cx = logic.getSelectedEntity().getX() + tower.width / 2.0;
				double cy = logic.getSelectedEntity().getY() + tower.height / 2.0;
				g.drawOval((int) (dx + (cx - r) * tile), (int) (dy + (cy - r) * tile), (int) (2 * r * tile), (int) (2 * r * tile));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25F));
				g.fillOval((int) (dx + (cx - r) * tile), (int) (dy + (cy - r) * tile), (int) (2 * r * tile), (int) (2 * r * tile));
			}

			float t = System.currentTimeMillis() % 1500;

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0008F * t - 0.00000053333F * t * t));
			g.setColor(Color.BLACK);
			double width = ((CollidableEntity) logic.getSelectedEntity()).getWidth();
			double height = ((CollidableEntity) logic.getSelectedEntity()).getHeight();
			g.fillRect((int) (dx + logic.getSelectedEntity().getX() * tile), (int) (dy + logic.getSelectedEntity().getY() * tile), (int) (tile * width), (int) (tile * height));
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

		if (logic.getBuyingType() != null && mouseX >= 0 && mouseX < logic.getCurrentWorld().getWidth() && mouseY >= 0 && mouseY < logic.getCurrentWorld().getHeight()) {
			int x = (int) Math.round(mouseX - logic.getBuyingType().width / 2.0);
			int y = (int) Math.round(mouseY - logic.getBuyingType().height / 2.0);

			if (logic.getBuyingType() instanceof TowerType) {
				TowerType tower = (TowerType) logic.getBuyingType();

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
			g.setColor(logic.canPlaceObject(logic.getBuyingType(), x, y) ? Color.GREEN : Color.RED);
			g.fillRect((int) (dx + x * tile), (int) (dy + y * tile), (int) (tile * logic.getBuyingType().width), (int) (tile * logic.getBuyingType().height));
			g.drawImage(logic.getBuyingType().image.image, (int) (dx + x * tile), (int) (dy + y * tile), (int) (tile * logic.getBuyingType().width), (int) (tile * logic.getBuyingType().height), null);
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

		for (Projectile p : logic.getProjectiles()) {
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

		if (logic.getState() != State.PLAYING) {
			g.setColor(Color.BLACK);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			String str = logic.getState().toString();

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
