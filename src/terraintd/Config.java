package terraintd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

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
	}
	
	public void apply() {
		
	}
	
	public void write() {
		
	}
	
}
