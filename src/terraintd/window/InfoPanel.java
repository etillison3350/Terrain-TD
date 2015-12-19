package terraintd.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import terraintd.types.EffectType;
import terraintd.types.Language;
import terraintd.types.ProjectileType;
import terraintd.types.TowerType;

public class InfoPanel extends JPanel {

	private static final long serialVersionUID = -7585731701891500747L;

	private JEditorPane info;

	private final Window window;

	public InfoPanel(Window window) {
		this.window = window;

//		this.setBackground(Color.BLACK);

		this.setLayout(new GridLayout(0, 1));

		this.setPreferredSize(new Dimension(256, Short.MAX_VALUE));

		info = new JEditorPane("text/html", getStringForTowerType(TowerType.values()[0]));
		info.setEditable(false);
//		info.setForeground(Color.WHITE);
		info.setBackground(Color.BLACK);
		this.add(new JScrollPane(info));
//		info.setDisabledTextColor(Color.BLACK);
	}

	String getStringForTowerType(TowerType type) {
		String ret = "<html><head><style type=\"text/css\">body{font-family:Arial,Helvetica,sans-serif; color:white;} p {margin:0;} ul {list-style-type:none; margin:0 0 0 15px;}</style></head>"
				+ "<body><h2>" + Language.get(type.id) + "</h2>" + String.format(Language.getCurrentLocale(), "<p>%s: %d x %d %s</p><p>%s: %.5g</p>", Language.get("area"), type.width, type.height, Language.get("tiles"), Language.get("dps"), getDamagePerSecond(type.projectiles))
				+ "<hr /><h3>" + Language.get("projectiles") + "</h3>";

		for (ProjectileType p : type.projectiles) {
			ret += String.format(Language.getCurrentLocale(), "<ul><li>%s: %s</li><li>%s: %.3g - %.3g</li><li>%s: %.4g %s</li><li>%s: %s</li><li>%s: %.3g %s</li><li style=\"font-weight: bold\">%s</li>", Language.get("delivery"), p.delivery.toString(), Language.get("damage"), p.damage, p.damage - p.falloff, Language.get("rate"), p.rate * 60, Language.get("rpm"), Language.get("range"), p.maxDist > 1e100 ? "\u221E" : String.format(Language.getCurrentLocale(), "%.3g %s", p.maxDist, Language.get("tiles")), Language.get("detect-range"), p.range, Language.get("tiles"), Language.get("effects"));
			if (p.effects.length == 0) {
				ret += "<ul><li>None</li></ul>";
			}
			for (EffectType e : p.effects) {
				ret += String.format(Language.getCurrentLocale(), "<ul><li>%s %s, %.3gs</li></ul>", e.type, e.amplifier, e.duration);
			}
			ret += "</ul><hr />";
		}

		ret += "</body></html>";

		return ret;
	}

	private double getDamagePerSecond(ProjectileType[] projectiles) {
		double dps = 0;

		for (ProjectileType p : projectiles) {
			dps += p.rate * (p.damage - (p.falloff * 0.5));
		}

		return dps;
	}

}
