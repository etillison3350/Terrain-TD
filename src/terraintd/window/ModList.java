package terraintd.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;

import terraintd.GameLogic;
import terraintd.Language;
import terraintd.files.ModListReader;
import terraintd.types.Mod;
import terraintd.types.TypeGenerator;

public class ModList extends JDialog {

	private static final long serialVersionUID = -4204900324206949102L;

	private JList<Mod> mods;
	private JEditorPane info;
	private JButton disable;

	private HashMap<String, Boolean> enabled;

	private ModList() {
		this.setTitle(Language.get("mods"));

		if (!GameLogic.isSaved()) {
			JLabel warning = new JLabel(Language.get("warning-mod-change"));
			warning.setForeground(Color.RED);
			this.add(warning, BorderLayout.PAGE_START);
		}

		Vector<Mod> modList = new Vector<>();
		Arrays.stream(Mod.values()).sorted(new Comparator<Mod>() {

			@Override
			public int compare(Mod o1, Mod o2) {
				if (o1.id.equals("base")) {
					return -1;
				} else if (o2.id.equals("base")) {
					return 1;
				} else {
					return Language.get(o1.id).compareTo(Language.get(o2.id));
				}
			}

		}).forEachOrdered(modList::add);

		mods = new JList<>(modList);
		mods.setCellRenderer(new ModCellRenderer());
		mods.setFixedCellWidth(192);
		mods.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				refresh();
			}
		});

		JPanel panel = new JPanel(new BorderLayout());

		info = new JEditorPane("text/html", "");
		info.setEditorKit(new WrapEditorKit());
		info.setEditable(false);
		info.setBorder(new EmptyBorder(10, 10, 10, 10));
		info.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == EventType.ACTIVATED) {
					try {
						if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException exception) {}
				}
			}
		});

		disable = new JButton(Language.get("disable"));
		disable.setEnabled(false);
		disable.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				enabled.put(mods.getSelectedValue().id, !ModListReader.isEnabled(mods.getSelectedValue().id));
				refresh();
				repaint();
			}
		});

		this.add(new JScrollPane(mods, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.LINE_START);
		final JScrollPane infoScroll = new JScrollPane(info);
		infoScroll.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				info.setSize(infoScroll.getViewport().getSize());
			}
		});
		panel.add(infoScroll);

		panel.add(disable, BorderLayout.PAGE_END);

		this.add(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout());

		JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));

		JButton accept = new JButton(Language.get("accept"));
		accept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int i = GameLogic.isSaved() ? 1 : JOptionPane.showOptionDialog(Window.window, Language.get("confirm-new"), Language.get("title-confirm-new"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {Language.get("save"), Language.get("dont-save"), Language.get("cancel")}, Language.get("save"));

				if (i == 0) {
					Window.saveGame.doClick();
				} else if (i != 1) {
					return;
				}

				ModListReader.setAll(enabled);
				ModListReader.write();
				TypeGenerator.generateValues();
				Language.clear();
				GameLogic.reset();
				GameLogic.getCurrentWorld().recalculateImageForSize(GamePanel.getTile() * 2);
				Window.repaintWindow();
				BuyPanel.recreateButtons();

				setVisible(false);
			}
		});
		buttons.add(accept);

		JButton cancel = new JButton(Language.get("cancel"));
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttons.add(cancel);

		buttonPanel.add(buttons);

		this.add(buttonPanel, BorderLayout.PAGE_END);

		enabled = ModListReader.getMap();

		this.setSize(600, 400);

		this.setModalityType(ModalityType.APPLICATION_MODAL);
	}

	private void refresh() {
		disable.setEnabled(mods.getSelectedIndex() >= 0 && !mods.getSelectedValue().id.equals("base"));

		String str = "<html><head><style type=\"text/css\">body{font-family:sans-serif;}</style></head><body>";
		if (mods.getSelectedIndex() >= 0) {
			Mod mod = mods.getSelectedValue();
			str += "<h2>" + Language.get(mod.id) + "</h2>";
			if (!mod.version.equals("null")) str += "<p>" + Language.get("version") + ": " + mod.version + "</p>";

			if (!mod.description.equals("null")) str += "<p>" + Language.get(mod.description) + "</p>";

			if (mod.authors.length > 0) {
				String authors = Language.get("authors") + ": ";
				for (String author : mod.authors)
					authors += Language.get(author);
				str += "<p>" + authors + "</p>";
			}

			if (!mod.contact.equals("null")) str += "<p>" + Language.get("contact") + ": " + Language.get(mod.contact) + "</p>";

			if (!mod.homepage.equals("null")) str += String.format("<p>%2$s: <a href=\"%1$s\">%1$s</a></p>", mod.homepage, Language.get("homepage"));

			disable.setText(enabled.get(mod.id) ? Language.get("disable") : Language.get("enable"));
		}
		info.setText(str);
	}

	private class ModCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 15921663840946983L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (!(c instanceof JLabel) || !(value instanceof Mod)) return c;

			JLabel label = (JLabel) c;

			Mod mod = (Mod) value;

			label.setText(Language.get(mod.id));
			if (enabled.get(mod.id)) {
				label.setIcon(mod.icon);
			} else {
				label.setForeground(Color.GRAY);
				label.setIcon(mod.gray);
			}

			return label;
		}

	}

	private static class WrapEditorKit extends HTMLEditorKit {

		private static final long serialVersionUID = -6575459183085693216L;

		private final ViewFactory defaultFactory = new WrapColumnFactory();

		public ViewFactory getViewFactory() {
			return defaultFactory;
		}
	}

	private static class WrapColumnFactory implements ViewFactory {

		private static final ViewFactory html = new HTMLEditorKit().getViewFactory();

		public View create(Element elem) {
			if (elem.getName().equals(AbstractDocument.ContentElementName)) return new WrapLabelView(elem);

			return html.create(elem);
		}
	}

	private static class WrapLabelView extends InlineView {

		public WrapLabelView(Element elem) {
			super(elem);
		}

		public float getMinimumSpan(int axis) {
			if (axis == View.X_AXIS) return 0;
			return super.getMinimumSpan(axis);
		}
	}

	public static void showDialog(Component parent) {
		ModList ml = new ModList();
		ml.setLocationRelativeTo(parent);
		ml.setVisible(true);
	}
}
