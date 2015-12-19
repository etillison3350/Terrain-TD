package terraintd.window;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import terraintd.GameLogic;
import terraintd.types.Language;

public class Window extends JFrame {

	private static final long serialVersionUID = -7720164204721511141L;

	private final GamePanel panel;
	private final InfoPanel info;
	
	final GameLogic logic;
	
	public Window() {
		super(Language.get("title"));
		
		this.setSize(960, 640);
		
		panel = new GamePanel(this);
		this.add(panel);

		this.logic = panel.getLogic();
		
		this.info = new InfoPanel(this);
		this.add(this.info, BorderLayout.LINE_START);
		
		this.add(new BuyPanel(), BorderLayout.LINE_END);
		
		this.setVisible(true);
	}
	
}
