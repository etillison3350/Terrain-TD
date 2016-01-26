package terraintd.types;

import java.nio.file.Path;

public class Mod extends IdType {

	public final Path path;
	public final String version;
	public final String[] authors;
	public final String[] contacts;
	public final String homepage;
	public final String description;

	public Mod(String id, Path path, String version, String[] authors, String[] contacts, String homepage, String description) {
		super(id);

		this.path = path;
		this.version = version;
		this.authors = authors;
		this.contacts = contacts;
		this.homepage = homepage;
		this.description = description;
	}

	public static Mod[] values() {
		return TypeGenerator.mods();
	}

}
