package terraintd.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import terraintd.GameLogic;
import terraintd.Language;

public class Window extends JFrame {

	private static final long serialVersionUID = -7720164204721511141L;

	public static final JMenuBar menuBar = new JMenuBar();

	public static final JMenu game = new JMenu();
	public static final JMenuItem newGame = new JMenuItem();
	public static final JMenuItem openGame = new JMenuItem();
	public static final JMenuItem saveGame = new JMenuItem();
	public static final JMenuItem saveGameAs = new JMenuItem();
	public static final JMenu speed = new JMenu();
	public static final JRadioButtonMenuItem[] speeds = {new JRadioButtonMenuItem(), new JRadioButtonMenuItem(), new JRadioButtonMenuItem(), new JRadioButtonMenuItem(), new JRadioButtonMenuItem()};
	public static final JMenuItem language = new JMenuItem();
	public static final JMenuItem modList = new JMenuItem();
	public static final JMenuItem exit = new JMenuItem();

	public static final JMenu help = new JMenu();

	public static final JLabel levelLabel = new JLabel();

	private static final ActionListener menuListener = new ActionListener() {

		@Override
		public synchronized void actionPerformed(ActionEvent e) {
			if (e.getSource() == newGame) {
				boolean wasPaused = GameLogic.isPaused();
				if (!wasPaused) speeds[0].doClick();
				GameLogic.stop();

				int i = GameLogic.isSaved() ? 1 : JOptionPane.showOptionDialog(window, Language.get("confirm-new"), Language.get("title-confirm-new"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {Language.get("save"), Language.get("dont-save"), Language.get("cancel")}, Language.get("save"));

				if (i == 0) {
					actionPerformed(new ActionEvent(saveGame, 0, "Save"));
					GameLogic.reset();
				} else if (i == 1) {
					GameLogic.reset();
				} else {
					if (!wasPaused) GameLogic.start();
					return;
				}
			} else if (e.getSource() == openGame) {
				Path p = GameLogic.getLastSaveLocation() != null ? GameLogic.getLastSaveLocation() : Paths.get("").toAbsolutePath();
				while (true) {
					Path path = FileChooser.showOpenDialog(window, p);
					if (path != null) {
						try {
							GameLogic.open(path);
						} catch (IOException exception) {
							JOptionPane.showOptionDialog(window, String.format("<html>%s<br />%s<br />%s</html>", path.toString(), Language.get("cannot-read"), Language.get("invalid-file")), Language.get("title-invalid-file"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] {Language.get("accept")}, null);
							p = path;
							continue;
						}
					}
					break;
				}
			} else if ((e.getSource() == saveGame && !GameLogic.isSaved() && !GameLogic.save()) || e.getSource() == saveGameAs) {
				Path path = FileChooser.showSaveDialog(window, Paths.get("").toAbsolutePath());
				if (path != null) GameLogic.save(path);
			} else if (e.getSource() == language) {
				if (!GameLogic.isPaused()) speeds[0].doClick();
				GameLogic.stop();

				Settings.showDialog();
			} else if (e.getSource() == modList) {
				if (!GameLogic.isPaused()) speeds[0].doClick();
				GameLogic.stop();

				ModList.showDialog(window);
			} else if (e.getSource() == exit) {
				boolean wasPaused = GameLogic.isPaused();
				if (!wasPaused) speeds[0].doClick();
				GameLogic.stop();

				int i = GameLogic.isSaved() ? 1 : JOptionPane.showOptionDialog(window, Language.get("confirm-exit"), Language.get("title-confirm-exit"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {Language.get("save"), Language.get("dont-save"), Language.get("cancel")}, Language.get("save"));

				if (i == 0) {
					actionPerformed(new ActionEvent(saveGame, 0, "Save"));
					System.exit(0);
				} else if (i == 1) {
					System.exit(0);
				} else {
					if (!wasPaused) GameLogic.start();
					return;
				}
			} else {
				for (int i = 0; i < 5; i++) {
					if (e.getSource() == speeds[i]) {
						if (i == 0) {
							GameLogic.stop();
						} else {
							GameLogic.setSpeed(i);
							GameLogic.start();
						}
					}
				}
			}
		}
	};

	static final Window window = new Window();

	private Window() {
		super(Language.get("title"));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(100 * (int) Math.floor((dim.width - 50) / 100), 100 * (int) Math.floor((dim.height - 50) / 100));
		this.setLocationRelativeTo(null);
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

		newGame.addActionListener(menuListener);
		game.add(newGame);

		openGame.addActionListener(menuListener);
		game.add(openGame);

		saveGame.addActionListener(menuListener);
		game.add(saveGame);

		saveGameAs.addActionListener(menuListener);
		game.add(saveGameAs);

		game.addSeparator();

		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < 5; i++) {
			speeds[i].addActionListener(menuListener);
			speed.add(speeds[i]);
			group.add(speeds[i]);
		}

		speeds[0].setSelected(true);

		game.add(speed);

		game.addSeparator();

		language.addActionListener(menuListener);
		game.add(language);

		modList.addActionListener(menuListener);
		game.add(modList);

		game.addSeparator();

		exit.addActionListener(menuListener);
		game.add(exit);

		menuBar.add(game);

		menuBar.add(help);

		menuBar.add(Box.createHorizontalGlue());

		menuBar.add(levelLabel);

		menuBar.add(Box.createHorizontalStrut(5));

		this.setJMenuBar(menuBar);

		renameButtons();

		this.add(GamePanel.panel);

		this.add(InfoPanel.infoPanel, BorderLayout.LINE_START);

		this.add(BuyPanel.buyPanel, BorderLayout.LINE_END);
	}

	public static void renameButtons() {
		if (window != null) window.setTitle(Language.get("title"));

		game.setText(Language.get("game"));
		newGame.setText(Language.get("new"));
		openGame.setText(Language.get("open"));
		saveGame.setText(Language.get("save"));
		saveGameAs.setText(Language.get("save-as"));
		speed.setText(Language.get("game-speed"));
		speeds[0].setText(Language.get("pause"));
		for (int i = 1; i < 5; i++) {
			speeds[i].setText(i + "\u00D7");
		}
		language.setText(Language.get("settings"));
		modList.setText(Language.get("mods"));
		exit.setText(Language.get("exit"));
		help.setText(Language.get("help"));
	}

	public static void setButtonsEnabled(boolean enabled) {
		for (JRadioButtonMenuItem b : speeds) {
			b.setEnabled(enabled);
		}
		InfoPanel.enableButtons(enabled);
	}

	public static void selectButton(int button) {
		if (button >= 5 || button < 0) return;

		speeds[button].doClick();
		InfoPanel.buttons[button].setSelected(true);
	}

	public static void updateLevel() {
		levelLabel.setText(String.format(Language.getCurrentLocale(), "%s  |  %s %d  |  %s", Language.get(GameLogic.getCurrentLevelSet().id), Language.get("level"), GameLogic.getLevelIndex() + 1, Language.get(GameLogic.getCurrentWorld().id)));
	}

	public static void repaintWindow() {
		window.repaint();
	}

	public static void setWindowVisible(boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				window.setVisible(true);
			}
		});
	}
}
