package terraintd.window;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import terraintd.GameLogic;
import terraintd.GameLogic.State;
import terraintd.types.InstantType;
import terraintd.types.ObstacleType;
import terraintd.types.Purchasable;
import terraintd.types.TowerType;
import terraintd.types.Upgrade;

public class BuyPanel extends JPanel {

	private static final long serialVersionUID = 3040182092852784456L;

	public static final BuyPanel buyPanel = new BuyPanel();

	private static JPanel buttonPanel, upgradePanel;
	private static BuyButton[] buttons;
	private static BuyButton[] upgradeButtons;

	private BuyPanel() {
		buttonPanel = new JPanel(new GridLayout(0, 3, 1, 1));
		upgradePanel = new JPanel(new GridLayout(0, 3, 1, 1));

		this.setBackground(Color.BLACK);
		buttonPanel.setBackground(Color.BLACK);
		upgradePanel.setBackground(Color.BLACK);

		this.setPreferredSize(new Dimension(256, 32767));
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		JScrollPane bScroll = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		bScroll.setBorder(BorderFactory.createEmptyBorder());
		bScroll.getVerticalScrollBar().setUI(new TDScrollBarUI());
		bScroll.getVerticalScrollBar().setUnitIncrement(14);
		this.add(bScroll, c);

		c.gridy = 2;
		JScrollPane uScroll = new JScrollPane(upgradePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		uScroll.setBorder(BorderFactory.createEmptyBorder());
		uScroll.getVerticalScrollBar().setUI(new TDScrollBarUI());
		uScroll.getVerticalScrollBar().setUnitIncrement(14);
		this.add(uScroll, c);

		c.gridy = 1;
		this.add(Box.createVerticalStrut(20), c);
		
		c.gridy = 3;
		c.weighty = 1;
		this.add(Box.createGlue(), c);

		this.createButtons();
		upgradeButtons = new BuyButton[0];
	}

	private void createButtons() {
		buttonPanel.removeAll();

		buttons = new BuyButton[TowerType.values().length + ObstacleType.values().length + InstantType.values().length];

		for (int t = 0; t < TowerType.values().length; t++) {
			buttons[t] = new BuyButton(TowerType.values()[t]);
			buttonPanel.add(buttons[t]);
		}
		for (int o = 0; o < ObstacleType.values().length; o++) {
			int n = TowerType.values().length + o;
			buttons[n] = new BuyButton(ObstacleType.values()[o]);
			buttonPanel.add(buttons[n]);
		}
		for (int i = 0; i < InstantType.values().length; i++) {
			int n = TowerType.values().length + ObstacleType.values().length + i;
			buttons[n] = new BuyButton(InstantType.values()[i]);
			buttonPanel.add(buttons[n]);
		}
	}

	public static void setUpgrades(Upgrade... upgrades) {
		upgradePanel.removeAll();
		
		upgradeButtons = new BuyButton[upgrades.length];
		
		for (int i = 0; i < upgrades.length; i++) {
			upgradeButtons[i] = new BuyButton(upgrades[i]);
			upgradePanel.add(upgradeButtons[i]);
		}
		
		updateButtons();
		buyPanel.revalidate();
		buyPanel.repaint();
	}

	public static void recreateButtons() {
		buyPanel.createButtons();
		BuyPanel.updateButtons();
		BuyPanel.setUpgrades();
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
		
		for (BuyButton b : upgradeButtons) {
			if (GameLogic.getState() != State.PLAYING) {
				b.setCancel(false);
				b.setEnabled(false);
			} else if (GameLogic.getBuyingType() == null) {
				b.setCancel(false);
				b.setEnabled(GameLogic.getMoney());
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

	private static void buy(Purchasable type) {
		GameLogic.buyObject(type);

		updateButtons();
	}

	public static class BuyButton extends JComponent {

		private static final long serialVersionUID = -307902500683318445L;

		private static final float PRESS_ALPHA = 0.25F, HOVER_ALPHA = 0.2F;
		private static final int SIZE = 82;

		private boolean pressed, hovered;
		private Point mousePoint = new Point(0, 0);

		private final Purchasable type;
		private BufferedImage image;
		private BufferedImage gray;
		private BufferedImage cancelImage;

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

		public BuyButton(Purchasable type) {
			super();

			this.type = type;

			this.addMouseListener(mouseListener);
			this.addMouseMotionListener(mouseListener);

			this.image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

			if (this.type.getIcon() != null) {
				Graphics2D g = this.image.createGraphics();
				g.drawImage(type.getIcon().image, 0, 0, SIZE - 4, SIZE - 4, null);
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

			g.setColor(this.cancel ? new Color(255, 128, 128) : (this.isEnabled() ? this.type.getBackgroundColor() : new Color(100, 100, 100)));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			g.drawImage(this.cancel ? cancelImage : (this.isEnabled() ? image : gray), 0, 0, this.getWidth(), this.getHeight(), null);

			if (this.pressed) {
				g.setPaint(new RadialGradientPaint(mousePoint, SIZE, new float[] {0.0F, 1.0F}, new Color[] {Color.GRAY, new Color(0, 0, 0, 128)}));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PRESS_ALPHA));
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
			} else if (this.hovered) {
				g.setPaint(new RadialGradientPaint(mousePoint, SIZE, new float[] {0.0F, 1.0F}, new Color[] {Color.WHITE, new Color(255, 255, 255, 0)}));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, HOVER_ALPHA));
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
			}

			g.setColor(Color.BLACK);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			if (!this.cancel) {
				g.drawString(Integer.toString(type.getCost()), 3, this.getHeight() - 3);

				g.setColor(Color.WHITE);
				g.drawString(Integer.toString(type.getCost()), 2, this.getHeight() - 2);
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
			boolean enabled = cost >= this.type.getCost();
			setEnabled(enabled);
			return enabled;
		}

		private boolean cancel = false;

		public void setCancel(boolean cancel) {
			this.cancel = cancel;
		}

	}

}
