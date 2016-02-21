package terraintd.window;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

import terraintd.GameLogic;
import terraintd.GameLogic.State;
import terraintd.types.CollidableType;
import terraintd.types.ObstacleType;
import terraintd.types.TowerType;

public class BuyPanel extends JPanel {

	private static final long serialVersionUID = 3040182092852784456L;

	public static final BuyPanel buyPanel = new BuyPanel();

	private static BuyButton[] buttons;

	private BuyPanel() {
		this.setBackground(Color.BLACK);

		this.setPreferredSize(new Dimension(256, 32767));
		this.setLayout(new FlowLayout(FlowLayout.LEADING, 1, 1));

		this.createButtons();
	}

	private void createButtons() {
		this.removeAll();

		buttons = new BuyButton[TowerType.values().length + ObstacleType.values().length];

		for (int t = 0; t < TowerType.values().length; t++) {
			buttons[t] = new BuyButton(TowerType.values()[t]);
			this.add(buttons[t]);
		}
		for (int o = 0; o < ObstacleType.values().length; o++) {
			int n = TowerType.values().length + o;
			buttons[n] = new BuyButton(ObstacleType.values()[o]);
			this.add(buttons[n]);
		}
	}

	public static void recreateButtons() {
		buyPanel.createButtons();
		BuyPanel.updateButtons();
	}

	public static void updateButtons() {
		for (BuyButton b : buttons) {
			if (GameLogic.getState() != State.PLAYING) {
				b.setCancel(false);
				b.setEnabled(false);
			} else if (GameLogic.getBuyingType() == null) {
				b.setCancel(false);
				b.setEnabled(GameLogic.getMoney());
			} else if (GameLogic.getBuyingType() == b.type) {
				b.setCancel(true);
				b.setEnabled(true);
			} else {
				b.setEnabled(false);
			}

			b.repaint();
		}
	}

	private static void cancelBuy() {
		GameLogic.cancelBuy();

		updateButtons();
	}

	private static void buy(CollidableType type) {
		GameLogic.buyObject(type);

		updateButtons();
	}

	public static class BuyButton extends JComponent {

		private static final long serialVersionUID = -307902500683318445L;

		private static final float PRESS_ALPHA = 0.25F, HOVER_ALPHA = 0.2F;
		private static final int SIZE = 80;

		private boolean pressed, hovered;
		private Point mousePoint = new Point(0, 0);

		private final CollidableType type;
		private BufferedImage image;
		private BufferedImage gray;
		private BufferedImage cancelImage;
		private int cost;

		private final MouseAdapter mouseListener = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (isEnabled()) {
					pressed = true;
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (pressed && isEnabled()) {
					if (cancel)
						cancelBuy();
					else
						buy(type);
				}

				pressed = false;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				pressed = false;
				hovered = false;
				if (GameLogic.getBuyingType() == null) InfoPanel.setDisplayedObject(GameLogic.getSelectedEntity() == null ? null : GameLogic.getSelectedEntity());
				repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				hovered = true;
				mousePoint = e.getPoint();
				if (GameLogic.getBuyingType() == null) InfoPanel.setDisplayedObject(type);
				repaint();
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				mousePoint = e.getPoint();
				repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				mousePoint = e.getPoint();
				repaint();
			}

		};

		public BuyButton(CollidableType type) {
			super();

			this.type = type;

			this.addMouseListener(mouseListener);
			this.addMouseMotionListener(mouseListener);

			this.image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

			if (this.type.icon != null) {
				Graphics2D g = this.image.createGraphics();
				g.drawImage(type.icon.image, 0, 0, SIZE, SIZE, null);
				g.dispose();
			}

			this.gray = new BufferedImage(this.image.getWidth(), this.image.getHeight(), BufferedImage.TYPE_INT_ARGB);

			Graphics2D ig = gray.createGraphics();
			ig.drawImage(this.image, 0, 0, null);
			ig.dispose();

			ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			op.filter(gray, gray);

			try {
				this.cancelImage = ImageIO.read(Paths.get("terraintd/mods/base/images/icons/cancel.png").toFile());
			} catch (IOException e) {
				this.cancelImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
			}

			this.cost = this.type.cost;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(SIZE, SIZE);
		}

		@Override
		protected void paintComponent(Graphics graph) {
			if (!(graph instanceof Graphics2D)) return;

			Graphics2D g = (Graphics2D) graph;

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(this.cancel ? new Color(1.0F, 0.5F, 0.5F) : (this.isEnabled() ? (this.type instanceof TowerType ? new Color(0.375F, 0.375F, 0.75F) : new Color(0.375F, 0.75F, 0.75F)/*new Color(0.5F, 0.5F, 1.0F)*/) : Color.GRAY));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			g.drawImage(this.cancel ? cancelImage : (this.isEnabled() ? image : gray), 0, 0, this.getWidth(), this.getHeight(), null);

			if (this.pressed) {
//				g.setColor(Color.BLACK);
				g.setPaint(new RadialGradientPaint(mousePoint, SIZE, new float[] {0.0F, 1.0F}, new Color[] {Color.GRAY, new Color(0, 0, 0, 128)}));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PRESS_ALPHA));
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
			} else if (this.hovered) {
//				g.setColor(Color.WHITE);
				g.setPaint(new RadialGradientPaint(mousePoint, SIZE, new float[] {0.0F, 1.0F}, new Color[] {Color.WHITE, new Color(255, 255, 255, 0)}));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, HOVER_ALPHA));
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
			}

			g.setColor(Color.BLACK);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			if (!this.cancel) {
				g.drawString(Integer.toString(cost), 3, this.getHeight() - 3);

				g.setColor(Color.WHITE);
				g.drawString(Integer.toString(cost), 2, this.getHeight() - 2);
			}
		}

		/**
		 * <ul>
		 * <li><b><i>setEnabled</i></b><br>
		 * <br>
		 * {@code public boolean setEnabled(int cost)}<br>
		 * <br>
		 * Sets this button's enabled state based on its type's cost.<br>
		 * @param cost - The current amount of money
		 * @return The resulting enabled state of the button
		 *         </ul>
		 */
		public boolean setEnabled(int cost) {
			boolean enabled = cost >= this.cost;
			setEnabled(enabled);
			return enabled;
		}

		private boolean cancel = false;

		public void setCancel(boolean cancel) {
			this.cancel = cancel;
		}

	}

}
