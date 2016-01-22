package terraintd.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import terraintd.Language;
import terraintd.window.BuyPanel;
import terraintd.window.GamePanel;
import terraintd.window.InfoPanel;
import terraintd.window.Window;

public class Config {

	public static final Path DEFAULT_PATH = Paths.get("terraintd/settings.cfg");

	public final Path path;

	public Locale language;

	public Config(Path path) {
		this.path = path;
		this.read();
	}

	public Config() {
		this(DEFAULT_PATH);
	}

	public void read() {
		try {
			Files.createFile(path);
		} catch (IOException e) {}

		try {
			List<String> lines = Files.readAllLines(path);

			HashMap<String, String> entries = new HashMap<>();

			for (String line : lines) {
				if (line.trim().startsWith("#")) continue;
				String[] kv = line.split("=", 2);
				entries.put(kv[0].toLowerCase(), kv[1]);
			}

			language = entries.containsKey("language") ? Locale.forLanguageTag(entries.get("language")) : Locale.US;
		} catch (IOException e) {
			return;
		}
	}

	public void apply() {
		Language.setCurrentLocale(language);
		InfoPanel.refreshDisplay();
		InfoPanel.paintHealthBar();
		BuyPanel.updateButtons();
		Window.renameButtons();
		GamePanel.repaintPanel();
	}

	public void setValues() {
		language = Language.getCurrentLocale();
	}

	public void write() {
		try {
			Files.delete(path);
			Files.createFile(path);
			Files.write(path, String.format("language=%s\n", language.toLanguageTag()).getBytes());
		} catch (IOException e) {}
	}

}
