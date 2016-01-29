package terraintd;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

		for (Mod mod : Mod.values()) {
			try (Stream<Path> files = Files.walk(mod.path)) {
				Iterator<Path> iter = files.iterator();
				while (iter.hasNext()) {
					Path path = iter.next();
					if (Files.isDirectory(path)) continue;

					String[] file = path.getFileName().toString().split("\\.");

					if (!file[1].equals("lang")) continue;

					Matcher m = Pattern.compile("^lang\\-name\\s*\\=\\s*(.+)$", Pattern.MULTILINE).matcher(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
					if (!m.find()) continue;

					ret.put(m.group(1), Locale.forLanguageTag(file[0]));
				}
			} catch (IOException e) {}
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

		if (lang == null) return "missing-key: " + key.toLowerCase();

		String s = lang.get(key.toLowerCase());

		if (s == null || s.isEmpty()) return "missing-key: " + key.toLowerCase();

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

					Pattern pattern = Pattern.compile("^([a-z0-9\\-]+)\\s*\\=\\s*(.+)$", Pattern.MULTILINE);

					for (String str : s) {
						Matcher matcher = pattern.matcher(str);
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
