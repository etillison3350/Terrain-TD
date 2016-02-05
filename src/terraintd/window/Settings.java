package terraintd.window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import terraintd.GameLogic;
import terraintd.Language;

public class Settings extends JDialog {

	private static final long serialVersionUID = 5945083864642354553L;

	private static Settings settings;

	private static JLabel languageLabel;
	private static JComboBox<String> languages;

	private Settings() {
		JPanel panel = new JPanel(new GridLayout(0, 2));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		languageLabel = new JLabel(Language.get("language"));
		panel.add(languageLabel);

		languages = new JComboBox<>(Language.localeNameList());
		panel.add(languages);

		this.add(panel, BorderLayout.PAGE_START);

		JButton accept = new JButton(Language.get("accept"));
		accept.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		JPanel acceptPanel = new JPanel(new FlowLayout());
		acceptPanel.add(accept);
		this.add(acceptPanel, BorderLayout.PAGE_END);
		
		this.setModalityType(ModalityType.APPLICATION_MODAL);
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);

		if (!b) {
			GameLogic.cfg.language = Language.getLocaleNames().get(languages.getSelectedItem());

			GameLogic.cfg.apply();
			GameLogic.cfg.write();
		}
	}

	public static void showDialog() {
		settings = new Settings();
		
		languages.setSelectedItem(Language.get("lang-name"));
		settings.setSize(480, 320);
		settings.setLocationRelativeTo(Window.window);
		settings.setVisible(true);
	}

}
