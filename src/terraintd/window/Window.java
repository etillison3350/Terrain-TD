package terraintd.window;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	public static final JMenuBar menuBar = new JMenuBar();

	public static final JMenu game = new JMenu();
	public static final JMenuItem newGame = new JMenuItem();
	public static final JMenuItem openGame = new JMenuItem();
	public static final JMenuItem saveGame = new JMenuItem();
	public static final JMenuItem saveGameAs = new JMenuItem();
	public static final JCheckBoxMenuItem pauseGame = new JCheckBoxMenuItem();
	public static final JCheckBoxMenuItem fastForward = new JCheckBoxMenuItem();
	public static final JMenuItem language = new JMenuItem();
	public static final JMenuItem exit = new JMenuItem();

	public static final JMenu help = new JMenu();

//	private static JFileChooser fc = new JFileChooser();

	private static final ActionListener menuListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == newGame) {
				boolean wasPaused = GameLogic.isPaused();
				if (!wasPaused) pauseGame.doClick();
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
				Path path = FileChooser.showOpenDialog(window, GameLogic.getLastSaveLocation() != null ? GameLogic.getLastSaveLocation() : Paths.get("").toAbsolutePath());
				try {
					GameLogic.open(path);
				} catch (IOException e1) {
				}
//				fc = new JFileChooser();
//				if (GameLogic.getLastSaveLocation() == null) {
//					fc.setCurrentDirectory(null);
//					fc.setSelectedFile(null);
//				} else {
//					fc.setCurrentDirectory(GameLogic.getLastSaveLocation().toFile());
//					fc.setSelectedFile(GameLogic.getLastSaveLocation().toFile());
//				}
//				fc.setAcceptAllFileFilterUsed(false);
//				fc.setFileFilter(new FileNameExtensionFilter(Language.get("extension-name"), "game"));
//				if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
//					Path path = fc.getSelectedFile().toPath();
//					if (!path.toString().replaceAll(".+\\.", "").equals("game")) {
//						path = Paths.get(path.toString() + ".game");
//					}
//
//					try {
//						GameLogic.open(path);
//					} catch (IOException exception) {
//						JOptionPane.showOptionDialog(window, String.format(Language.getCurrentLocale(), "<html>%s<br />%s<br />%s</html>", path.toString(), Language.get("cannot-read"), Language.get("invalid-file")), Language.get("invalid-title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] {Language.get("accept")}, 0);
//					}
//				}
			} else if ((e.getSource() == saveGame && GameLogic.save()) || e.getSource() == saveGameAs) {
//				JFileChooser.setDefaultLocale(Locale.CHINA);
//				fc = new JFileChooser();
//				if (GameLogic.getLastSaveLocation() == null) {
//					fc.setCurrentDirectory(null);
//					fc.setSelectedFile(null);
//				} else {
//					fc.setCurrentDirectory(GameLogic.getLastSaveLocation().toFile());
//					fc.setSelectedFile(GameLogic.getLastSaveLocation().toFile());
//				}
//				fc.setAcceptAllFileFilterUsed(false);
//				fc.setFileFilter(new FileNameExtensionFilter("Game (*.game)", "game"));
//				if (fc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
//					Path path = fc.getSelectedFile().toPath();
//					if (!path.toString().replaceAll(".+\\.", "").equals("json")) {
//						path = Paths.get(path.toString() + ".game");
//						System.out.println(path);
//					}
//
//					GameLogic.save(path);
//				}
			} else if (e.getSource() == pauseGame) {
				InfoPanel.pause.setSelected(!pauseGame.isSelected());

				if (pauseGame.isSelected()) {
					GameLogic.stop();
				} else {
					GameLogic.start();
				}
			} else if (e.getSource() == fastForward) {
				GameLogic.setFastForward(fastForward.isSelected());
			} else if (e.getSource() == language) {
				Settings.setShowing(true);
			} else if (e.getSource() == exit) {
				boolean wasPaused = GameLogic.isPaused();
				if (!wasPaused) pauseGame.doClick();
				GameLogic.stop();

				int i = GameLogic.isSaved() ? 1 : JOptionPane.showOptionDialog(window, Language.get("confirm-new"), Language.get("title-confirm-new"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {Language.get("save"), Language.get("dont-save"), Language.get("cancel")}, Language.get("save"));

				if (i == 0) {
					actionPerformed(new ActionEvent(saveGame, 0, "Save"));
					System.exit(0);
				} else if (i == 1) {
					System.exit(0);
				} else {
					if (!wasPaused) GameLogic.start();
					return;
				}
			}
		}
	};

	static final Window window = new Window();

	private Window() {
		super(Language.get("title"));

		this.setSize(960, 640);
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

		pauseGame.addActionListener(menuListener);
		pauseGame.setSelected(true);
		game.add(pauseGame);

		fastForward.addActionListener(menuListener);
		game.add(fastForward);

		game.addSeparator();

		language.addActionListener(menuListener);
		game.add(language);

		game.addSeparator();

		exit.addActionListener(menuListener);
		game.add(exit);

		menuBar.add(game);

		menuBar.add(help);

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
		pauseGame.setText(Language.get("pause"));
		fastForward.setText(Language.get("fast-forward"));
		language.setText(Language.get("language"));
		exit.setText(Language.get("exit"));
		help.setText(Language.get("help"));
	}

	public static void setButtonsEnabled(boolean enabled) {
		pauseGame.setEnabled(enabled);
		fastForward.setEnabled(enabled);
		InfoPanel.pause.setEnabled(enabled);
		InfoPanel.fastForward.setEnabled(enabled);
	}

	public static void repaintWindow() {
		window.repaint();
	}

	public static void setWindowVisible(boolean b) {
		window.setVisible(true);
	}

	public static void setWindowDefaultCloseOperation(int operation) {
		window.setDefaultCloseOperation(operation);
	}

}
