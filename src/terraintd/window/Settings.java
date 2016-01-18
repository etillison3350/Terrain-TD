package terraintd.window;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import terraintd.GameLogic;
import terraintd.Language;

public class Settings extends JDialog {

	private static final long serialVersionUID = 5945083864642354553L;

	private static Settings settings = new Settings();

	private static JLabel languageLabel;
	private static JComboBox<String> languages;
	
	private Settings() {
		JPanel panel = new JPanel(new GridLayout(0, 2));
		
		languageLabel = new JLabel(Language.get("language"));
		panel.add(languageLabel);
		
		languages = new JComboBox<>(Language.localeNameList());
		panel.add(languages);
		
		this.add(panel, BorderLayout.NORTH);
		
		this.setModal(true);
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
	
	public static void setShowing(boolean visible) {
		languageLabel.setText(Language.get("language"));
		languages.setSelectedItem(Language.get("lang-name"));
		settings.setSize(480, 320);
		settings.setLocationRelativeTo(Window.window);
		settings.setVisible(visible);
	}

}
