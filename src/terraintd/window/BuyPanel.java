package terraintd.window;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

import terraintd.GameLogic.State;
import terraintd.types.CollidableType;
import terraintd.types.TowerType;

public class BuyPanel extends JPanel {

	private static final long serialVersionUID = 3040182092852784456L;

	private BuyButton[] buttons;

	private final Window window;

	public BuyPanel(Window window) {
		this.window = window;

		this.setBackground(Color.BLACK);

		this.setPreferredSize(new Dimension(256, 32767));
		this.setLayout(new FlowLayout(FlowLayout.LEADING, 1, 1));

		buttons = new BuyButton[TowerType.values().length];

		for (int b = 0; b < buttons.length; b++) {
			buttons[b] = new BuyButton(TowerType.values()[b]);
			this.add(buttons[b]);
		}

		updateButtons();
	}

	public void updateButtons() {
		for (BuyButton b : buttons) {
			if (window.logic.getState() != State.PLAYING) {
				b.setCancel(false);
				b.setEnabled(false);
			} else if (window.logic.getBuyingType() == null) {
				b.setCancel(false);
				b.setEnabled(window.logic.getMoney());
			} else if (window.logic.getBuyingType() == b.type) {
				b.setCancel(true);
				b.setEnabled(true);
			} else {
				b.setEnabled(false);
			}

			b.repaint();
		}
	}

	private void cancelBuy() {
		window.logic.cancelBuy();

		updateButtons();
	}

	private void buy(CollidableType type) {
		window.logic.buyObject(type);

		updateButtons();
	}

	public class BuyButton extends JComponent {

		private static final long serialVersionUID = -307902500683318445L;

		private static final float PRESS_ALPHA = 0.25F, HOVER_ALPHA = 0.2F;

		private boolean pressed, hovered;

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
				if (window.logic.getBuyingType() == null) window.info.setDisplayedObject(window.logic.getSelectedEntity() == null ? null : window.logic.getSelectedEntity());
				repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				hovered = true;
				window.info.setDisplayedObject(type);
				repaint();
			}

		};

		public BuyButton(CollidableType type) {
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

			try {
				this.cancelImage = ImageIO.read(Paths.get("terraintd/mods/base/images/icons/cancel.png").toFile());
			} catch (IOException e) {
				this.cancelImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
			}

			this.cost = this.type.cost;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(64, 64);
		}

		@Override
		protected void paintComponent(Graphics graph) {
			if (!(graph instanceof Graphics2D)) return;

			Graphics2D g = (Graphics2D) graph;

			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g.setColor(this.cancel ? new Color(1.0F, 0.5F, 0.5F) : (this.isEnabled() ? (this.type instanceof TowerType ? new Color(0.375F, 0.375F, 0.75F) : new Color(0.5F, 0.5F, 1.0F)) : Color.GRAY));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			g.drawImage(this.cancel ? cancelImage : (this.isEnabled() ? image : gray), 0, 0, this.getWidth(), this.getHeight(), null);

			if (this.pressed) {
				g.setColor(Color.BLACK);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PRESS_ALPHA));
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
			} else if (this.hovered) {
				g.setColor(Color.WHITE);
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
