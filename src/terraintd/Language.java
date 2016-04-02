package terraintd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import terraintd.types.Mod;

public class Language {

	private static final Pattern PATTERN = Pattern.compile("^([a-zA-Z0-9\\-]+)\\s*\\=\\s*(.+)$", Pattern.MULTILINE);

	private static Locale currentLocale = Locale.US;

	private Language() {}

	private static HashMap<Locale, HashMap<String, String>> terms = new HashMap<>();
	private static HashMap<String, Locale> localeNames;

	public static Vector<String> localeNameList() {
		if (localeNames == null) getLocaleNames();

		return new Vector<>(new TreeSet<>(localeNames.keySet()));
	}

	public static HashMap<String, Locale> getLocaleNames() {
		if (localeNames != null) return localeNames;

		HashMap<String, Locale> ret = new HashMap<>();

		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get("terraintd/mods/base/locale/names"));
		} catch (IOException e) {
			return ret;
		}

		for (String line : lines) {
			Matcher matcher = PATTERN.matcher(line);
			if (!matcher.find()) continue;
			ret.put(matcher.group(2), Locale.forLanguageTag(matcher.group(1)));
		}

		localeNames = ret;

		return ret;
	}

	public static Locale getCurrentLocale() {
		return currentLocale;
	}

	public static void setCurrentLocale(Locale currentLocale) {
		if (currentLocale == null) throw new NullPointerException();

		Language.currentLocale = currentLocale;

		generateValuesForLocale(Language.currentLocale);
	}

	public static String get(String key) {
		if (terms.get(currentLocale) == null) generateValuesForLocale(currentLocale);

		HashMap<String, String> lang = terms.get(currentLocale);

		if (lang == null) return "missing-key-" + key.toLowerCase();

		String s = lang.get(key.toLowerCase());

		if (s == null || s.isEmpty()) return "missing-key-" + key.toLowerCase();

		return s;
	}

	static void generateValuesForLocale(Locale locale) {
		HashMap<String, String> terms = new HashMap<>();

		for (Mod mod : Mod.values()) {
			try (Stream<Path> files = Files.walk(mod.path)) {
				Iterator<Path> iter = files.iterator();
				while (iter.hasNext()) {
					Path path = iter.next();
					if (Files.isDirectory(path)) continue;

					String[] file = path.getFileName().toString().split("\\.");

					if (!locale.equals(Locale.forLanguageTag(file[0])) || !file[1].equals("lang")) continue;

					List<String> s = Files.readAllLines(path);

					for (String str : s) {
						Matcher matcher = PATTERN.matcher(str);
						if (!matcher.find()) continue;

						String key = matcher.group(1).toLowerCase();
						if (terms.containsKey(key)) continue;

						terms.put(key, matcher.group(2));
					}
				}
			} catch (IOException e) {}
		}
		Language.terms.put(locale, terms);
	}

	public static void clear() {
		terms.clear();
	}

}
