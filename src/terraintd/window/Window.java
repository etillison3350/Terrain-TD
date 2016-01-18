package terraintd.window;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import terraintd.GameLogic;
import terraintd.Language;

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
	public final JCheckBoxMenuItem pauseGame;
	public final JCheckBoxMenuItem fastForward;
	public final JMenuItem exit;

	public final JMenu help;

	private final ActionListener menuListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == newGame) {
				boolean wasPaused = logic.isPaused();
				if (!wasPaused) pauseGame.doClick();
				logic.stop();

				int i = JOptionPane.showOptionDialog(Window.this, Language.get("confirm-new"), Language.get("title-confirm-new"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {Language.get("save"), Language.get("dont-save"), Language.get("cancel")}, Language.get("save"));

				if (i == 0) {
					actionPerformed(new ActionEvent(saveGame, 0, "Save"));
					logic.reset();
				} else if (i == 1) {
					logic.reset();
				} else {
					if (!wasPaused) logic.start();
					return;
				}
			} else if (e.getSource() == openGame) {

			} else if (e.getSource() == saveGame) {

			} else if (e.getSource() == saveGameAs) {

			} else if (e.getSource() == pauseGame) {
				info.pause.setSelected(!pauseGame.isSelected());
				
				if (pauseGame.isSelected()) {
					logic.stop();
				} else {
					logic.start();
				}
			} else if (e.getSource() == fastForward) {
				logic.setFastForward(fastForward.isSelected());
			} else if (e.getSource() == exit) {

			}
		}
	};

	public Window() {
		super(Language.get("title"));

		this.setSize(960, 640);
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

		menuBar = new JMenuBar();

		game = new JMenu(Language.get("game"));

		newGame = new JMenuItem(Language.get("new"));
		newGame.addActionListener(menuListener);
		game.add(newGame);

		openGame = new JMenuItem(Language.get("open"));
		openGame.addActionListener(menuListener);
		game.add(openGame);

		saveGame = new JMenuItem(Language.get("save"));
		saveGame.addActionListener(menuListener);
		game.add(saveGame);

		saveGameAs = new JMenuItem(Language.get("save-as"));
		saveGameAs.addActionListener(menuListener);
		game.add(saveGameAs);

		game.addSeparator();

		pauseGame = new JCheckBoxMenuItem(Language.get("pause"), true);
		pauseGame.addActionListener(menuListener);
		game.add(pauseGame);
		
		fastForward = new JCheckBoxMenuItem(Language.get("fast-forward"), false);
		fastForward.addActionListener(menuListener);
		game.add(fastForward);

		game.addSeparator();

		exit = new JMenuItem(Language.get("exit"));
		exit.addActionListener(menuListener);
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
	
	public void renameButtons() {
		game.setText(Language.get("game"));
		newGame.setText(Language.get("new"));
		openGame.setText(Language.get("open"));
		saveGame.setText(Language.get("save"));
		saveGameAs.setText(Language.get("save-as"));
		pauseGame.setText(Language.get("pause"));
		fastForward.setText(Language.get("fast-forward"));
		exit.setText(Language.get("exit"));
		help.setText(Language.get("help"));
	}
	
	public void setButtonsEnabled(boolean enabled) {
		this.pauseGame.setEnabled(enabled);
		this.fastForward.setEnabled(enabled);
		this.info.pause.setEnabled(enabled);
		this.info.fastForward.setEnabled(enabled);
	}

}
