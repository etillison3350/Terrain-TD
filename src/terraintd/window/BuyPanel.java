package terraintd.window;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class BuyPanel extends JPanel {

	private static final long serialVersionUID = 3040182092852784456L;

	private JButton buttons;

	int i = 0;

	public BuyPanel() {
		this.setPreferredSize(new Dimension(256, 32767));
		this.setLayout(new SquareLayout());

//		BufferedImage image = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D g = image.createGraphics();
//		g.setColor(Color.GREEN);
//		g.fill3DRect(5, 5, 25, 25, true);
//		g.dispose();

		BufferedImage im = null;
		try {
			im = ImageIO.read(new File("C:\\Users\\etillison\\workspace\\towerdefense\\resources\\spirit.png"));
		} catch (IOException e) {}
		BufferedImage image = im;

//		new Timer(500, new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				add(new BuyButton("500", image));
//				BuyPanel.this.validate();
//			}
//		}).start();

		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				add(new BuyButton(i++, image)).setEnabled(i % 2 == 0);
				BuyPanel.this.validate();
			}

		});
	}

	public class BuyButton extends JComponent {

		private static final long serialVersionUID = -307902500683318445L;

		private boolean pressed;

		private Image image;
		private BufferedImage gray;
		private int cost;

		public BuyButton(int cost, Image image) {
			super();

			this.addMouseListener(new MouseAdapter() {

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
					repaint();
				}

			});

			this.image = image;

			this.gray = new BufferedImage(this.image.getWidth(null), this.image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

			Graphics2D ig = gray.createGraphics();
			ig.drawImage(this.image, 0, 0, null);
			ig.dispose();

			ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			op.filter(gray, gray);

			this.preferredSize = new Dimension(this.image.getWidth(null), this.image.getHeight(null));
			this.cost = cost;
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
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25F));
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
			}

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			g.drawString(Integer.toString(cost), 1, this.getHeight() - 1);
			
			g.setColor(Color.WHITE);
			g.drawString(Integer.toString(cost), 0, this.getHeight());
		}

	}

}
