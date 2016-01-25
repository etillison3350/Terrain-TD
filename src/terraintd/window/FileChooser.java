package terraintd.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

import terraintd.Language;

public class FileChooser extends JDialog implements ActionListener, WindowListener {

	private static final long serialVersionUID = -5170709290883896064L;

	private final boolean save;

	private Path currentPath;

	private List<Path> files;

	private JPanel fileButtonPanel, topPanel, navButtonPanel, bottomPanel;
	private JTextField searchBar, fileName;
	private PathButton[] fileButtons;
	private JButton confirm, cancel;
	private JComboBox<String> filters;
	private JButton refresh;

	private int selectedIndex = -1;

	private int state = -1;

	private FileChooser(boolean save) {
		this.setTitle(Language.get(save ? "save" : "open"));

		this.save = save;

		this.setSize(784, 480);

		this.setModalityType(ModalityType.APPLICATION_MODAL);

		this.setLayout(new BorderLayout(0, 3));

		this.topPanel = new JPanel();
		this.topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		this.topPanel.setPreferredSize(new Dimension(1024, 32));

		topPanel.add(Box.createHorizontalStrut(5));

		JButton upButton = new JButton(new ImageIcon("terraintd/mods/base/images/icons/arrowup.png"));
		upButton.setMargin(new Insets(1, 1, 1, 1));
		upButton.setBackground(new Color(238, 238, 238));
		upButton.setFocusPainted(false);
		upButton.setBorderPainted(false);
		upButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentPath.getParent() != null) setCurrentPath(currentPath.getParent());
			}
		});
		topPanel.add(upButton);

		navButtonPanel = new JPanel();
		navButtonPanel.setLayout(new BoxLayout(navButtonPanel, BoxLayout.X_AXIS));
		navButtonPanel.setBackground(Color.WHITE);
		navButtonPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		navButtonPanel.setMaximumSize(new Dimension(1024, 24));

		refresh = new JButton(new ImageIcon("terraintd/mods/base/images/icons/refresh.png"));
		refresh.setMargin(new Insets(1, 1, 1, 1));
		refresh.setBackground(Color.WHITE);
		refresh.setFocusPainted(false);
		refresh.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		refresh.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentPath(currentPath);
			}
		});

		topPanel.add(navButtonPanel);

		topPanel.add(Box.createHorizontalStrut(5));

		JPanel searchField = new JPanel(new BorderLayout());
		searchField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		searchField.setBackground(Color.WHITE);

		searchBar = new JTextField(16);
		searchBar.setBorder(BorderFactory.createEmptyBorder());
		searchBar.getDocument().addDocumentListener(new DocumentListener() {

			private void update() {
				setCurrentPath(currentPath);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				this.update();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				this.update();
			}
		});
		searchField.add(searchBar, BorderLayout.LINE_START);

		searchField.add(new JLabel(new ImageIcon("terraintd/mods/base/images/icons/search.png")));
		searchField.add(Box.createHorizontalStrut(5), BorderLayout.LINE_END);

		searchField.setMaximumSize(new Dimension(searchField.getPreferredSize().width, 24));
		topPanel.add(searchField);

		topPanel.add(Box.createHorizontalStrut(5));

		this.add(topPanel, BorderLayout.PAGE_START);

		this.fileButtonPanel = new JPanel();
		this.fileButtonPanel.setLayout(new PathButtonLayout());
		JScrollPane scroll = new JScrollPane(fileButtonPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.getHorizontalScrollBar().setUnitIncrement(96);
		this.add(scroll);

		this.bottomPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridy = 1;
		c.gridx = 2;
		c.weightx = 0;
		c.weighty = 0.5;
		c.insets = new Insets(3, 3, 3, 3);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		confirm = new JButton(Language.get(this.save ? "save" : "open"));
		confirm.addActionListener(this);
		buttonPanel.add(confirm);

		cancel = new JButton(Language.get("cancel"));
		cancel.addActionListener(this);
		cancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "name");
		cancel.getActionMap().put("name", new AbstractAction() {

			private static final long serialVersionUID = 2646758765996702622L;

			@Override
			public void actionPerformed(ActionEvent e) {
				cancel.doClick();
			}

		});
		buttonPanel.add(cancel);

		this.bottomPanel.add(buttonPanel, c);

		c.gridy = 0;
		filters = new JComboBox<String>(save ? new String[] {Language.get("extension-name")} : new String[] {Language.get("extension-name"), Language.get("all-files")});
		filters.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				setCurrentPath(currentPath);
			}
		});
		this.bottomPanel.add(filters, c);

		c.gridx = 0;
		this.bottomPanel.add(new JLabel(Language.get("file-name") + ":"), c);

		c.gridx = 1;
		c.weightx = 1;
		fileName = new JTextField(20);
		fileName.addActionListener(this);
		this.bottomPanel.add(fileName, c);

		this.add(bottomPanel, BorderLayout.PAGE_END);

		this.add(Box.createHorizontalGlue(), BorderLayout.LINE_END);

		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
	}

	private static final Comparator<Path> pathComp = new Comparator<Path>() {

		@Override
		public int compare(Path o1, Path o2) {
			boolean b1 = Files.isDirectory(o1);
			boolean b2 = Files.isDirectory(o2);

			if (b1 == b2) {
				return o1.compareTo(o2);
			} else {
				return b1 ? -1 : 1;
			}
		}
	};

	public void setCurrentPath(Path path) {
		if (path == null) return;

		Path newPath = path;
		while (!Files.isDirectory(newPath)) {
			newPath = newPath.getParent();
		}

		if (currentPath != newPath) {
			searchBar.setText("");
		}
		currentPath = newPath;

		this.files = new ArrayList<>();
		try {
			Files.walk(this.currentPath, 1).filter(p -> {
				try {
					return !p.equals(this.currentPath) && !Files.isHidden(p) && Files.isReadable(p) && !Files.isSymbolicLink(p) && p.getFileName().toString().toLowerCase().startsWith(this.searchBar.getText().toLowerCase()) && (Files.isDirectory(p) || filters.getSelectedIndex() == 1 || p.toString().endsWith(".game"));
				} catch (Exception e) {
					return false;
				}
			}).sorted(pathComp).forEachOrdered(this.files::add);
		} catch (Exception e) {}
		recreateButtons();
	}

	void recreateButtons() {
		fileButtonPanel.removeAll();
		fileButtons = new PathButton[files.size()];
		for (int i = 0; i < fileButtons.length; i++) {
			PathButton p = new PathButton(files.get(i));
			fileButtons[i] = p;
			JPanel panel = (JPanel) fileButtonPanel.add(new JPanel());
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(p);
		}
		fileButtonPanel.revalidate();
		fileButtonPanel.repaint();

		navButtonPanel.removeAll();
		navButtonPanel.add(Box.createHorizontalStrut(5));
		navButtonPanel.add(new JLabel(FileSystemView.getFileSystemView().getSystemIcon(currentPath.toFile())));

		List<AbstractButton> buttons = new ArrayList<>();

		int i = 480;
		Path path = currentPath;
		while (path != null && path.getNameCount() > 0) {
			NavButton button = new NavButton(path);
			if ((i -= button.getMaximumSize().width) < 0) break;
			if (path != currentPath || this.files.stream().anyMatch(p -> Files.isDirectory(p))) {
				NavArrow arrow = new NavArrow(path);
				if ((i -= arrow.getMaximumSize().width) < 0) break;
				buttons.add(0, arrow);
			}
			buttons.add(0, button);
			path = path.getParent();
		}

		for (AbstractButton button : buttons) {
			navButtonPanel.add(button);
		}

		navButtonPanel.add(Box.createHorizontalGlue());
		navButtonPanel.add(refresh);

		navButtonPanel.revalidate();
		navButtonPanel.repaint();

		this.revalidate();
		this.repaint();
	}

	private static final ImageIcon leftArrow = new ImageIcon("terraintd/mods/base/images/icons/triangleleft.png");
	private static final ImageIcon downArrow = new ImageIcon("terraintd/mods/base/images/icons/triangledown.png");

	private class NavArrow extends JToggleButton implements ActionListener, FocusListener, ListSelectionListener {

		private static final long serialVersionUID = -4858402812855470527L;

		private Popup popup;
		private JList<Path> list;

		private final Path path;

		private final ListRenderer renderer = new ListRenderer();

		public NavArrow(Path path) {
			super(leftArrow);

			this.path = path;

			this.setSelectedIcon(downArrow);
			this.setBorderPainted(false);
			this.setFocusPainted(false);
			this.setBackground(Color.WHITE);
			this.setMargin(new Insets(1, 1, 1, 1));
			this.setMaximumSize(new Dimension(this.getPreferredSize().width, 128));

			this.addActionListener(this);
			this.addFocusListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (this.isSelected()) {
				Point p = this.getLocationOnScreen();
				try {
					Vector<Path> files = new Vector<>();
					Files.walk(path, 1).filter(path -> {
						try {
							return path != this.path && Files.isDirectory(path) && !Files.isHidden(path) && Files.isReadable(path) && !Files.isSymbolicLink(path);
						} catch (Exception e1) {
							return false;
						}
					}).sorted().forEachOrdered(files::add);
					list = new JList<>(files);
					list.setVisibleRowCount(Math.min(files.size(), 10));
					list.setCellRenderer(renderer);
					list.addListSelectionListener(this);
					popup = PopupFactory.getSharedInstance().getPopup(null, new JScrollPane(list), p.x, p.y + this.getHeight());
				} catch (IllegalArgumentException | IOException exception) {}
				popup.show();
			} else {
				popup.hide();
				this.popup = null;
			}
		}

		private class ListRenderer extends DefaultListCellRenderer {

			private static final long serialVersionUID = 3262250561173734331L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (!(c instanceof JLabel) || !(value instanceof Path)) return c;

				JLabel label = (JLabel) c;
				Path path = (Path) value;
				label.setIcon(FileSystemView.getFileSystemView().getSystemIcon(path.toFile()));
				label.setText(path.getFileName().toString() + "  ");
//				label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				if (!currentPath.startsWith(path)) label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				return label;
			}

		}

		@Override
		public void focusGained(FocusEvent e) {}

		@Override
		public void focusLost(FocusEvent e) {
			this.setSelected(false);
			if (this.popup != null) {
				this.popup.hide();
				this.popup = null;
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			setCurrentPath(list.getSelectedValue());
		}
	}

	private class NavButton extends JButton implements ActionListener {

		private static final long serialVersionUID = 2432930920855860995L;

		private final Path path;

		public NavButton(Path path) {
			super(path.getFileName().toString());

			this.path = path;
			this.setBorderPainted(false);
			this.setFocusPainted(false);
			this.setBackground(Color.WHITE);
			this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			this.setToolTipText(path.getFileName().toString());
			this.setMargin(new Insets(1, 1, 1, 1));
			this.setMaximumSize(new Dimension(this.getPreferredSize().width, 128));

			this.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setCurrentPath(this.path);
		}

	}

	private class PathButton extends JToggleButton implements MouseListener, ActionListener, KeyListener {

		private static final long serialVersionUID = -1880981783003097901L;

		private final Path path;

		public PathButton(Path path) {
			super(path.getFileName().toString(), FileSystemView.getFileSystemView().getSystemIcon(path.toFile()));

			this.path = path;

			this.setToolTipText(path.getFileName().toString());

			this.setBorderPainted(false);
			this.setFocusPainted(false);
			this.setMargin(new Insets(3, 3, 3, 3));
			this.setPreferredSize(new Dimension(192, 24));
			this.setMaximumSize(new Dimension(192, 192));
			this.setBackground(new Color(238, 238, 238));
			this.setHorizontalAlignment(SwingConstants.LEFT);

			this.addActionListener(this);
			this.addMouseListener(this);
			this.addKeyListener(this);
		}

		@Override
		public void setSelected(boolean b) {
			this.setBorderPainted(b);
			super.setSelected(b);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.setSelected(true);
			this.requestFocus();

			if (!Files.isDirectory(this.path)) fileName.setText(this.path.getFileName().toString());

			fileButtonPanel.scrollRectToVisible(getParent().getBounds());

			selectedIndex = files.indexOf(((PathButton) e.getSource()).path);

			for (PathButton button : fileButtons) {
				if (button == e.getSource()) continue;
				button.setSelected(false);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getClickCount() >= 2) {
				if (Files.isDirectory(this.path)) FileChooser.this.setCurrentPath(this.path);
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_DOWN:
					e.consume();
					if (selectedIndex < 0 || selectedIndex >= files.size() - 1)
						fileButtons[0].doClick();
					else
						fileButtons[++selectedIndex].doClick();
					break;
				case KeyEvent.VK_UP:
					e.consume();
					if (selectedIndex <= 0 || selectedIndex >= files.size() - 1)
						fileButtons[0].doClick();
					else
						fileButtons[--selectedIndex].doClick();
					break;
				case KeyEvent.VK_LEFT:
					e.consume();
					if (selectedIndex < 0)
						fileButtons[0].doClick();
					else
						fileButtons[Math.max(0, selectedIndex - fileButtonPanel.getHeight() / 24)].doClick();
					break;
				case KeyEvent.VK_RIGHT:
					e.consume();
					if (selectedIndex < 0)
						fileButtons[0].doClick();
					else
						fileButtons[Math.min(fileButtons.length - 1, selectedIndex + fileButtonPanel.getHeight() / 24)].doClick();
					break;
				case KeyEvent.VK_ENTER:
					e.consume();
					this.mouseReleased(new MouseEvent(this, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 0, 0, 2, false));
					break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}

		@Override
		public void keyTyped(KeyEvent e) {}

	}

	public Path getCurrentPath() {
		return currentPath;
	}

	public static Path showOpenDialog(Component parent, Path currentPath) {
		FileChooser fc = new FileChooser(false);
		synchronized (fc) {
			fc.setCurrentPath(currentPath);
			fc.setLocationRelativeTo(parent);
			fc.setVisible(true);
			while (fc.state < 0) {
				try {
					fc.wait();
				} catch (InterruptedException e) {}
			}
			Path path = Paths.get(currentPath.toString() + "/" + fc.fileName.getText());
			fc.dispose();
			return fc.state > 0 ? path : null;
		}
	}

	public static Path showSaveDialog(Component parent, Path currentPath) {
		FileChooser fc = new FileChooser(true);
		synchronized (fc) {
			fc.setCurrentPath(currentPath);
			fc.setLocationRelativeTo(parent);
			fc.setVisible(true);
			while (fc.state < 0) {
				try {
					fc.wait();
				} catch (InterruptedException e) {}
			}
			Path path = Paths.get(currentPath.toString() + "/" + fc.fileName.getText());
			fc.dispose();
			return fc.state > 0 ? path : null;
		}
	}

	private class PathButtonLayout implements LayoutManager {

		@Override
		public void addLayoutComponent(String name, Component comp) {}

		@Override
		public void layoutContainer(Container parent) {
			int n = parent.getComponentCount();

			int rc = parent.getHeight() / 24;

			for (int i = 0; i < n; i++) {
				Component comp = parent.getComponent(i);
				int r = i % rc;
				int c = i / rc;
				comp.setBounds(c * 192, r * 24, 192, 24);
			}
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(parent.getHeight() <= 0 ? 0 : (int) (192 * Math.ceil((double) parent.getComponentCount() * 24.0 / (double) parent.getHeight())), parent.getHeight());
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return minimumLayoutSize(parent);
		}

		@Override
		public void removeLayoutComponent(Component comp) {}

	}

	private synchronized void close() {
		this.setVisible(false);
		if (state < 0) state = 0;
		notifyAll();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancel) {
			this.close();
		} else if (e.getSource() == confirm || e.getSource() == fileName) {
			if (fileName.getText().isEmpty()) return;

			if (save && Files.exists(Paths.get(currentPath.toString() + "/" + fileName.getText())) && JOptionPane.showOptionDialog(this, String.format("<html>%s %s<br />%s</html>", fileName.getText(), Language.get("already-exists"), Language.get("confirm-replace")), Language.get("title-confirm-replace"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] {Language.get("yes"), Language.get("cancel")}, Language.get("cancel")) != 0) return;

			if (!save && !Files.exists(Paths.get(currentPath.toString() + "/" + fileName.getText()))) {
				JOptionPane.showOptionDialog(this, String.format("<html>%s<br />%s<br />%s</html>", fileName.getText(), Language.get("file-not-found"), Language.get("check-file-name")), Language.get("title-file-not-found"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] {Language.get("accept")}, null);
				return;
			}

			this.state = 1;
			this.close();
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		this.close();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

}
