package terraintd.window;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import terraintd.GameLogic;
import terraintd.types.Language;

public class Window extends JFrame {

	private static final long serialVersionUID = -7720164204721511141L;

	public final GamePanel panel;
	public final InfoPanel info;
	public final BuyPanel buy;

	final GameLogic logic;

	public final JMenuBar menuBar;

	public final JMenu game;
	public final JMenuItem newGame;
	public final JMenuItem openGame;
	public final JMenuItem saveGame;
	public final JMenuItem saveGameAs;
	public final JMenuItem exit;

	public final JMenu help;

	public Window() {
		super(Language.get("title"));

		this.setSize(960, 640);
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

		menuBar = new JMenuBar();
		
		game = new JMenu(Language.get("game"));
		
		newGame = new JMenuItem(Language.get("new"));
		game.add(newGame);
		
		openGame = new JMenuItem(Language.get("open"));
		game.add(openGame);
		
		saveGame = new JMenuItem(Language.get("save"));
		game.add(saveGame);
		
		saveGameAs = new JMenuItem(Language.get("save-as"));
		game.add(saveGameAs);
		
		game.addSeparator();

		exit = new JMenuItem(Language.get("exit"));
		game.add(exit);
		
		menuBar.add(game);

		help = new JMenu(Language.get("help"));
		menuBar.add(help);

		setJMenuBar(menuBar);

		panel = new GamePanel(this);
		this.add(panel);

		this.logic = panel.getLogic();

		this.info = new InfoPanel(this);
		this.add(this.info, BorderLayout.LINE_START);

		this.buy = new BuyPanel(this);
		this.add(buy, BorderLayout.LINE_END);

		this.setVisible(true);
	}

}
