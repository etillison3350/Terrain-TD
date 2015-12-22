package terraintd.types;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Language {

	private static Locale currentLocale = Locale.US;

	private Language() {}

	private static HashMap<Locale, HashMap<String, String>> terms = new HashMap<>();

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

		String s = lang.get(key.toLowerCase());

		if (s == null || s.isEmpty()) return "missing-key: " + key.toLowerCase();

		return s;
	}

	static void generateValuesForLocale(Locale locale) {
		try {
			Files.createDirectories(Paths.get("terraintd/mods"));
		} catch (IOException e) {}

		try (Stream<Path> files = Files.walk(Paths.get("terraintd/mods"))) {
			Iterator<Path> iter = files.iterator();
			while (iter.hasNext()) {
				Path path = iter.next();
				if (Files.isDirectory(path)) continue;

				String[] file = path.getFileName().toString().split("\\.");

				if (!locale.equals(Locale.forLanguageTag(file[0])) || !file[1].equals("lang")) continue;

//				String s = new String(Files.readAllBytes(path));
				List<String> s = Files.readAllLines(path, StandardCharsets.UTF_8);

				Pattern pattern = Pattern.compile("^([a-z\\-]+)\\s*\\=\\s*(.+)$", Pattern.MULTILINE);

				HashMap<String, String> terms = new HashMap<>();

//				while (matcher.find()) {
//					String key = matcher.group(1).toLowerCase();
//
//					if (terms.containsKey(key)) continue;
//
//					terms.put(key, matcher.group(2));
//				}

				for (String str : s) {
					Matcher matcher = pattern.matcher(str);
					if (!matcher.find()) continue;
					
					String key = matcher.group(1).toLowerCase();
					if (terms.containsKey(key)) continue;
					
					terms.put(key, matcher.group(2));
				}

				Language.terms.put(locale, terms);
			}
		} catch (IOException e) {}
	}

}
