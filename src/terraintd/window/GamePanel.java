package terraintd.window;

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

public class GamePanel extends JPanel {

	private static final long serialVersionUID = 6409717885667732875L;

	private final GameLogic logic;

	private final Window window;

	public GamePanel(Window window) {
		this.window = window;

		this.setBackground(Color.BLACK);

		this.logic = new GameLogic(this);

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
				mouseX = (int) ((e.getX() - dx) / tile);
				mouseY = (int) ((e.getY() - dy) / tile);
			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

		};

		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);
	}

	private int mouseX, mouseY;

	private double tile, dx, dy;

	@Override
	public void paintComponent(Graphics gg) {
		super.paintComponent(gg);

		if (!(gg instanceof Graphics2D)) return;

		Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(logic.getCurrentWorld().getImage(), (int) dx, (int) dy, null);
		
		if (logic.getBuyingType() != null) {
			g.setColor(logic.canPlaceObject(logic.getBuyingType(), mouseX, mouseY) ? Color.GREEN : Color.RED);
			g.fillRect((int) (dx + mouseX * tile), (int) (dy + mouseY * tile), (int) tile, (int) tile);
			g.drawImage(logic.getBuyingType().image.image, (int) (dx + mouseX * tile), (int) (dy + mouseY * tile), (int) tile, (int) tile, null);
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

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

}
