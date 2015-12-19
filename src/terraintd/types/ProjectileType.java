package terraintd.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectileType {

	public final DeliveryType delivery;
	public final double explodeRadius;
	public final double speed;
	public final double damage;
	public final boolean isHealing;
	public final double falloff;
	public final double rate;
	public final boolean follow;
	public final double range;
	public final double maxDist;
	public final double angle;
	public final double rotation;
	public final boolean absRotation;
	public final double dyingFadeTime;
	public final boolean dyingFade;
	public final boolean damageFade;
	public final ImageType image;
	public final ImageType explosion;
	public final EffectType[] effects;

	ProjectileType(DeliveryType delivery, double explodeRadius, double speed, double damage, double falloff, double rate, boolean follow, double range, double maxDist, double angle, double rotation, boolean absRotation, double dyingFadeTime, boolean dyingFade, boolean damageFade, ImageType image, ImageType explosion, EffectType[] effects) {
		this.delivery = delivery;
		this.explodeRadius = explodeRadius;
		this.speed = speed;
		this.damage = damage;
		this.falloff = falloff;
		this.rate = rate;
		this.follow = follow;
		this.range = range;
		this.maxDist = maxDist;
		this.angle = angle;
		this.rotation = rotation;
		this.absRotation = absRotation;
		this.dyingFadeTime = dyingFadeTime;
		this.dyingFade = dyingFade;
		this.damageFade = damageFade;
		this.image = image;
		this.explosion = explosion;
		this.effects = effects;

		this.isHealing = this.damage < 0;
	}

	ProjectileType(String s) {
		HashMap<String, String> properties = new HashMap<>();

//		Matcher listM = Pattern.compile("([a-z\\-_\\+]+)=([\\[\\{])").matcher(s);//[\\[\\{](.+?)[\\]\\}]").matcher(s);
//
//		String s2 = s;
//
////		while (listM.find()) {
////			properties.put(listM.group(1).replaceAll("[\\-_]", ""), listM.group(2));
////			s2 = s2.replace(listM.group(), "");
////		}
//
		Pattern cuPat = Pattern.compile("([\\{\\}])([^\\{\\}]*)");
		Pattern sqPat = Pattern.compile("([\\[\\]])([^\\[\\]]*)");
//		
//		while (listM.find()) {
//			final int charNum = listM.group(2).charAt(0) + 1;
//
//			int indent = 0;
//
//			String str = "";
//
//			Matcher indM = (charNum == 124 ? cuPat : sqPat).matcher(s.substring(listM.start()));
//
//			while (true) {
//				if (!indM.find()) break;
//
//				str += indM.group();
//
//				indent -= indM.group(1).charAt(0) - charNum;
//				if (indent == 0) {
//					properties.put(listM.group(1).replaceAll("[\\-_]", "").toLowerCase(), str.substring(0, str.lastIndexOf(charNum + 1) + 1));
//					s2 = s2.replace(str, "");
//					break;
//				}
//			}
//		}
		
		Matcher m = Pattern.compile("([a-z\\-_\\+]+)=([\\[\\{])").matcher(s);

		String s2 = s;

		while (m.find()) {
			final int charNum = m.group(2).charAt(0) + 1;

			int indent = 0;

			String str = "";

			Matcher indM = (charNum == 124 ? cuPat : sqPat).matcher(s.substring(m.start()));

			while (true) {
				if (!indM.find()) break;

				str += indM.group();

				indent -= indM.group(1).charAt(0) - charNum;
				if (indent == 0) {
					String v = str.substring(0, str.lastIndexOf(charNum + 1) + 1);
					
					properties.put(m.group(1).replaceAll("[\\-_]", "").toLowerCase(), v);
					s2 = s2.replace(v, "");
					break;
				}
			}
		}

		Pattern kv = Pattern.compile("([a-z\\-_\\+]+)\\=([0-9A-Za-z_\\-/\"\\.]+),");

		Matcher kvM = kv.matcher(s2);

		while (kvM.find()) {
			properties.put(kvM.group(1).replaceAll("[\\-_]", ""), kvM.group(2));
		}

		DeliveryType delivery;
		try {
			delivery = DeliveryType.valueOf(properties.get("delivery").replace('-', '_').toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			delivery = DeliveryType.SINGLE_TARGET;
		}
		this.delivery = delivery;
		this.explodeRadius = Double.parseDouble(properties.getOrDefault("explosionradius", "0"));
		this.speed = Double.parseDouble(properties.getOrDefault("speed", "1"));

		String damage = properties.getOrDefault("damage", "[0,0,]");
		int near = 0, far = 0;
		if (damage.matches("[mnf]")) {
			Matcher whM = Pattern.compile("(min|max|near|far)=\\+?([0-9]+),").matcher(damage);
			while (whM.find()) {
				if (whM.group(1).matches("^(min|near)$")) {
					near = Integer.parseUnsignedInt(whM.group(2));
				} else {
					far = Integer.parseUnsignedInt(whM.group(2));
				}
			}
		} else {
			Matcher whM = Pattern.compile("\\[?\\+?([0-9\\.]+),\\+?([0-9\\.]+)\\,?\\]?").matcher(damage);
			if (whM.find()) {
				near = Integer.parseUnsignedInt(whM.group(1));
				far = Integer.parseUnsignedInt(whM.group(2));
			}
		}
		this.damage = near;
		this.falloff = near - far;

		this.rate = Double.parseDouble(properties.getOrDefault("rate", "1"));
		this.follow = Boolean.parseBoolean(properties.getOrDefault("follow", "true"));

		String md = properties.getOrDefault("maxdist", "1");
		if (md.matches("^inf(?:init[ey])?$")) md = "1e308";
		this.maxDist = Double.parseDouble(md);
		this.range = Double.parseDouble(properties.getOrDefault("range", "1"));

		String rotation = properties.getOrDefault("rotation", "0");
		this.rotation = Double.parseDouble(rotation.replace("pi", "")) * (rotation.endsWith("pi") ? Math.PI : 1);

		String angle = properties.getOrDefault("angle", "1");
		this.angle = Double.parseDouble(angle.replace("pi", "")) * (angle.endsWith("pi") ? Math.PI : 1);

		this.absRotation = Boolean.parseBoolean(properties.getOrDefault("absrotation", "false"));
		this.dyingFadeTime = Double.parseDouble(properties.getOrDefault("dyingfadetime", "0.1"));
		this.dyingFade = Boolean.parseBoolean(properties.getOrDefault("dyingfade", "true"));
		this.damageFade = Boolean.parseBoolean(properties.getOrDefault("damagefade", "true"));
		this.image = new ImageType(properties.getOrDefault("image", ""));
		this.explosion = new ImageType(properties.getOrDefault("explosion", ""));

		EffectType[] effects;
		if (properties.get("effects") != null) {
			ArrayList<EffectType> effList = new ArrayList<>();

			String effStr = properties.get("effects");

			List<String> effs = new ArrayList<>();

			Matcher pm = Pattern.compile("([\\{\\}])[^\\{\\}]*").matcher(effStr);

			int indent = 0;
			String str = "";
			while (pm.find()) {
				str += pm.group();
				indent -= pm.group(1).charAt(0) - 124;
				if (indent == 0) {
					effs.add(str.replaceAll("\\s", "").replaceAll("([^\\,])([\\]\\}])", "$1,$2"));
					str = "";
				}
			}

			if (indent != 0) {
				effects = new EffectType[0];
			} else {
				for (String e : effs) {
					try {
						effList.add(new EffectType(e));
					} catch (IllegalArgumentException exception) {}
				}

				effects = effList.toArray(new EffectType[effList.size()]);
			}
		} else {
			effects = new EffectType[0];
		}
		this.effects = effects;

		this.isHealing = this.damage < 0;
	}

}
