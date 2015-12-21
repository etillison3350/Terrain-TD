package terraintd.window;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

import javax.swing.JComponent;
import javax.swing.JPanel;

import terraintd.types.TowerType;

public class BuyPanel extends JPanel {

	private static final long serialVersionUID = 3040182092852784456L;

	private BuyButton[] buttons;

	public BuyPanel() {
		this.setPreferredSize(new Dimension(256, 32767));
		this.setLayout(new FlowLayout(FlowLayout.LEADING, 1, 1));

		for (TowerType t : TowerType.values()) {
			this.add(new BuyButton(t));
		}
	}

	public class BuyButton extends JComponent {

		private static final long serialVersionUID = -307902500683318445L;

		private static final float PRESS_ALPHA = 0.25F, HOVER_ALPHA = 0.25F;

		private boolean pressed, hovered;

		private final TowerType type;
		private BufferedImage image;
		private BufferedImage gray;
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
				pressed = false;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				pressed = false;
				hovered = false;
				repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				hovered = true;
				repaint();
			}

		};

		public BuyButton(TowerType type) {
			super();

			this.type = type;

			this.addMouseListener(mouseListener);

//			this.image = image;
			this.image = new BufferedImage(63, 63, BufferedImage.TYPE_INT_ARGB);

			if (this.type.icon != null) {
				Graphics2D g = this.image.createGraphics();
				g.drawImage(type.icon.image, 0, 0, 63, 63, null);
				g.dispose();
			}

			this.gray = new BufferedImage(this.image.getWidth(null), this.image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

			Graphics2D ig = gray.createGraphics();
			ig.drawImage(this.image, 0, 0, null);
			ig.dispose();

			ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			op.filter(gray, gray);

			this.preferredSize = new Dimension(this.image.getWidth(null), this.image.getHeight(null));
			this.cost = this.type.cost;
		}

		private Dimension preferredSize;

		@Override
		public Dimension getPreferredSize() {
			return preferredSize;
		}

		@Override
		public void setPreferredSize(Dimension preferredSize) {
			this.preferredSize = preferredSize;
		}

		@Override
		protected void paintComponent(Graphics graph) {
//			super.paintComponent(g);

			if (!(graph instanceof Graphics2D)) return;

			Graphics2D g = (Graphics2D) graph;

			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g.setColor(!this.isEnabled() ? Color.GRAY : new Color(0.5F, 0.5F, 1.0F));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			g.drawImage(this.isEnabled() ? image : gray, 0, 0, this.getWidth(), this.getHeight(), null);

			g.setColor(Color.BLACK);
			if (this.pressed) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PRESS_ALPHA));
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
			}

			g.setColor(Color.WHITE);
			if (this.hovered) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, HOVER_ALPHA));
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
			}

			g.setColor(Color.BLACK);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			g.drawString(Integer.toString(cost), 1, this.getHeight() - 1);

			g.setColor(Color.WHITE);
			g.drawString(Integer.toString(cost), 0, this.getHeight());
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

	}

}
