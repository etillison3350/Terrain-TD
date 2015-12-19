package terraintd.types;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EffectType {

	public final StatusEffectType type;
	public final double duration;
	public final double amplifier;

	EffectType(StatusEffectType type, double duration, double amplifier) {
		this.type = type;
		this.duration = duration;
		this.amplifier = amplifier;
	}

	EffectType(String s) {
		HashMap<String, String> properties = new HashMap<>();

//		Matcher listM = Pattern.compile("([a-z\\-_\\+]+)=[\\[\\{](.+?)[\\]\\}]").matcher(s);
//
//		String s2 = s;
//
//		while (listM.find()) {
//			properties.put(listM.group(1).replaceAll("[\\-_]", ""), listM.group(2));
//			s2 = s2.replace(listM.group(), "");
//		}

		Pattern kv = Pattern.compile("([a-z\\-_\\+]+)\\=([0-9A-Za-z_\\-/\"\\.]+),");

		Matcher kvM = kv.matcher(s);

		while (kvM.find()) {
			properties.put(kvM.group(1), kvM.group(2));
		}
		
		StatusEffectType type;
		try {
			type = StatusEffectType.valueOf(properties.get("type").toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		}
		this.type = type;
		
		this.duration = Double.parseDouble(properties.getOrDefault("duration", "1"));
		this.amplifier = Double.parseDouble(properties.getOrDefault("amplifier", "1"));
	}
}
