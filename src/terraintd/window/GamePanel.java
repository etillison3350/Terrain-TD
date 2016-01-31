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
import java.awt.event.MouseWheelEvent;
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

	private static double startX, startY, sox, soy;
	private static double mouseX, mouseY;

	private static double tile, dx, dy, scale, ox, oy;

	private GamePanel() {
		this.setBackground(Color.BLACK);

		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				if (GameLogic.getCurrentWorld() == null) return;

				tile = Math.min(128, (int) Math.min((double) getWidth() / (double) (GameLogic.getCurrentWorld().getWidth() + 1), (double) getHeight() / (double) (GameLogic.getCurrentWorld().getHeight() + 1)));
				dx = ((double) getWidth() - (GameLogic.getCurrentWorld().getWidth() * tile)) * 0.5;
				dy = ((double) getHeight() - (GameLogic.getCurrentWorld().getHeight() * tile)) * 0.5;
				ImageManager.clear();
				GameLogic.getCurrentWorld().recalculateImageForSize(tile);

				repaint();
			}

		});

		scale = 1;

		MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				mouseX = (e.getX() - getOriginX()) / getTileSize();
				mouseY = (e.getY() - getOriginY()) / getTileSize();

				if (GameLogic.getBuyingType() != null || true) repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				double mouseX = e.getX() / getTileSize();
				double mouseY = e.getY() / getTileSize();

				double dx = startX - mouseX;
				double dy = startY - mouseY;
				if (dx * dx + dy * dy > 0.25) {
					ox = sox - dx;
					oy = soy - dy;
					repaint();
				} else if (GameLogic.getBuyingType() != null) {
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouseX = Short.MIN_VALUE;
				mouseY = Short.MIN_VALUE;

				if (GameLogic.getBuyingType() != null) repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				startX = e.getX() / getTileSize();
				startY = e.getY() / getTileSize();
				sox = ox;
				soy = oy;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				double mx = e.getX() / getTileSize();
				double my = e.getY() / getTileSize();
				double dx = startX - mx;
				double dy = startY - my;
				if (dx * dx + dy * dy < 0.25) {
					if (GameLogic.getBuyingType() != null) {
						int x = (int) Math.round(mouseX - GameLogic.getBuyingType().width * 0.5);
						int y = (int) Math.round(mouseY - GameLogic.getBuyingType().height * 0.5);

						if (GameLogic.canPlaceObject(GameLogic.getBuyingType(), x, y)) {
							GameLogic.buyObject(x, y);
							repaint();
						}
					} else {
						for (Entity entity : GameLogic.getPermanentEntities()) {
							Point2D p = new Point2D.Double((e.getX() - getOriginX()) / getTileSize(), (e.getY() - getOriginY()) / getTileSize());

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
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				double mx = (e.getX() - getOriginX()) / getTileSize();
				double my = (e.getY() - getOriginY()) / getTileSize();

				if (e.getWheelRotation() < 0) {
					if (scale < 16) scale *= 1.5;
				} else if (scale > 1) {
					scale /= 1.5;
				}

				if (scale < 1.00001) {
					scale = 1;
					ox = 0;
					oy = 0;
				} else {
					double nmx = (e.getX() - getOriginX()) / getTileSize();
					double nmy = (e.getY() - getOriginY()) / getTileSize();

					ox += nmx - mx;
					oy += nmy - my;
				}

				mouseMoved(new MouseEvent(e.getComponent(), MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, e.getX(), e.getY(), 1, false));

				ImageManager.clear();
				repaint();
			}

		};

		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);
		this.addMouseWheelListener(ma);

		try {
			goal = ImageIO.read(Paths.get("terraintd/mods/base/images/goal.png").toFile());
		} catch (IOException e1) {}
	}

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

		double tile = getTileSize();
		double dx = getOriginX();
		double dy = getOriginY();

		g.drawImage(GameLogic.getCurrentWorld().getImage(), (int) dx, (int) dy, (int) (GameLogic.getCurrentWorld().getWidth() * tile), (int) (GameLogic.getCurrentWorld().getHeight() * tile), null);

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
			if (e instanceof Enemy) {
				Enemy en = (Enemy) e;

				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - en.getDeathTime())));

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

				if (en.isDead()) {
					g.setColor(Color.GREEN);
					String str = String.format(Language.getCurrentLocale(), "+%d", en.type.reward);
					g.drawString(str, (float) (dx + e.getX() * tile) - 0.5F * g.getFontMetrics().stringWidth(str), (float) (dy + (e.getY() - en.getDeathTime()) * tile));
				}
			} else if (e instanceof CollidableEntity) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

				CollidableType type = ((CollidableEntity) e).getType();

				AffineTransform trans = new AffineTransform();
				trans.translate(dx + tile * (e.getX() - 0.5 * (type.image.width - type.width)), dy + tile * (e.getY() - 0.5 * (type.image.height - type.height)));
				if (e instanceof Tower) trans.rotate(((Tower) e).getRotation(), 0.5 * type.image.width * tile, 0.5 * type.image.height * tile);
				trans.scale(type.image.width * tile / (double) type.image.image.getWidth(), type.image.height * tile / (double) type.image.image.getHeight());

				g.drawImage(type.image.image, trans, null);
			}
		}

		if (GameLogic.getSelectedEntity() != null) {
			if (GameLogic.getSelectedEntity() instanceof Tower) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

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
					trans.scale(tile * (p.getRadius() - p.type.offset) / img.image.getWidth(), tile * img.height / img.image.getHeight());
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

	/**
	 * <ul>
	 * <li><b><i>getTileSize</i></b><br>
	 * <br>
	 * {@code double getTileSize()}<br>
	 * <br>
	 * @return The scaled tile size
	 *         </ul>
	 */
	public static double getTileSize() {
		return tile * scale;
	}

	public static double getTile() {
		return tile;
	}

	public static double getOriginX() {
		return dx + getTileSize() * ox;
	}

	public static double getDx() {
		return dx;
	}

	public static double getOriginY() {
		return dy + getTileSize() * oy;
	}

	public static double getDy() {
		return dy;
	}

	public static void resetView() {
		scale = 1;
		ox = oy = 0;
		panel.repaint();
	}

	public static double getMouseX() {
		return mouseX;
	}

	public static double getMouseY() {
		return mouseY;
	}

}
