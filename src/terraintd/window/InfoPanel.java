package terraintd.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import terraintd.GameLogic;
import terraintd.Language;
import terraintd.object.Entity;
import terraintd.object.Weapon;
import terraintd.types.EffectType;
import terraintd.types.IdType;
import terraintd.types.ObstacleType;
import terraintd.types.ProjectileType;
import terraintd.types.TowerType;

public class InfoPanel extends JPanel {

	private static final long serialVersionUID = -7585731701891500747L;

	public static final InfoPanel infoPanel = new InfoPanel();
	
	private static JEditorPane info;
	private static Object displayedObject;

	private static BufferedImage playImage, pauseImage, ffImage, rewImage;

	private static HealthBar health;
	public static JToggleButton pause, fastForward;

	private InfoPanel() {
		try {
			playImage = ImageIO.read(Paths.get("terraintd/mods/base/images/icons/play.png").toFile());
		} catch (IOException e) {}
		try {
			pauseImage = ImageIO.read(Paths.get("terraintd/mods/base/images/icons/pause.png").toFile());
		} catch (IOException e) {}
		try {
			ffImage = ImageIO.read(Paths.get("terraintd/mods/base/images/icons/ff.png").toFile());
		} catch (IOException e) {}
		try {
			rewImage = ImageIO.read(Paths.get("terraintd/mods/base/images/icons/rew.png").toFile());
		} catch (IOException e) {}

		this.setLayout(new GridBagLayout());

		this.setPreferredSize(new Dimension(256, Short.MAX_VALUE));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridwidth = 2;
		c.gridy = 1;
		health = new HealthBar();
		this.add(health, c);

		c.gridy = 2;
		c.weighty = 1;
		info = new JEditorPane("text/html", "");
		info.setEditable(false);
		info.setBackground(Color.BLACK);
		JScrollPane scrollPane = new JScrollPane(info);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, c);

		c.gridwidth = 1;
		c.weighty = 0;
		c.weightx = 0.5;
		c.gridy = 0;
		pause = new JToggleButton(new ImageIcon(playImage));
		pause.setSelectedIcon(new ImageIcon(pauseImage));
		pause.setMargin(new Insets(0, 0, 0, 0));
		pause.setBackground(new Color(184, 207, 229));
		pause.setBorderPainted(false);
		pause.setFocusPainted(false);
		pause.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Window.pauseGame.isSelected() == pause.isSelected()) Window.pauseGame.doClick();
			}
		});
		this.add(pause, c);

		c.gridx = 1;
		fastForward = new JToggleButton(new ImageIcon(ffImage));
		fastForward.setSelectedIcon(new ImageIcon(rewImage));
		fastForward.setMargin(new Insets(0, 0, 0, 0));
		fastForward.setBackground(new Color(184, 207, 229));
		fastForward.setBorderPainted(false);
		fastForward.setFocusPainted(false);
		fastForward.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Window.fastForward.isSelected() != fastForward.isSelected()) Window.fastForward.doClick();
			}
		});
		this.add(fastForward, c);
	}

	public static void paintHealthBar() {
		health.repaint();
	}

	static String getMoneyString() {
		return String.format(Language.getCurrentLocale(), "<h2>%s: %d</h2>", Language.get("money"), GameLogic.getMoney());
	}

	static String getStringForTowerType(TowerType type) {
		return String.format(Language.getCurrentLocale(), "<h2>%s</h2><p>%s: %d x %d %s</p><p>%s: %.5g</p><p>%s: %.3g %s</p><hr /><h3>%s</h3>%s", Language.get(type.id), Language.get("area"), type.width, type.height, Language.get("tiles"), Language.get("dps"), getDamagePerSecond(type.projectiles), Language.get("detect-range"), type.range, Language.get("tiles"), Language.get("projectiles"), getStringFromProjectiles(type.projectiles));
	}

	static String getStringFromProjectiles(ProjectileType[] projectiles) {
		String ret = "";

		for (ProjectileType p : projectiles) {
			ret += String.format(Language.getCurrentLocale(), "<ul><li>%s: %s</li><li>%s: %.3g - %.3g</li><li>%s: %.4g %s</li><li>%s: %s</li><li style=\"font-weight: bold\">%s</li>", Language.get("delivery"), p.delivery.toString(), Language.get("damage"), p.damage, p.damage - p.falloff, Language.get("rate"), p.rate * 60, Language.get("rpm"), Language.get("range"), p.maxDist > 1e100 ? "\u221E" : String.format(Language.getCurrentLocale(), "%.3g %s", p.maxDist, Language.get("tiles")), Language.get("effects"));
			if (p.effects.length == 0) {
				ret += String.format("<ul><li>%s</li></ul>", Language.get("none"));
			} else {
				for (EffectType e : p.effects) {
					ret += String.format(Language.getCurrentLocale(), "<ul><li>%s %s, %.3gs</li></ul>", e.type, e.amplifier, e.duration);
				}
			}
			ret += "</ul><hr />";
		}

		return ret;
	}

	private static double getDamagePerSecond(ProjectileType[] projectiles) {
		double dps = 0;

		for (ProjectileType p : projectiles)
			dps += p.rate * (p.damage - (p.falloff * 0.5));

		return dps;
	}

	static String getStringForObstacleType(ObstacleType type) {
		return String.format(Language.getCurrentLocale(), "<h2>%s</h2><p>%s: %d x %d %s</p><hr />", Language.get(type.id), Language.get("area"), type.width, type.height, Language.get("tiles"));
	}
	
	static String getStringForWeapon(Weapon w) {
		return String.format("<br /><p>%s: %d</p><p>%s: %.5f</p><p>%s: %d</p><br />", Language.get("kills"), w.getGun().getKills(), Language.get("damage-dealt"), w.getGun().getDamageDone(), Language.get("projectiles-fired"), w.getGun().getProjectilesFired());
	}

	public static Object getDisplayedType() {
		return displayedObject;
	}

	public static void refreshDisplay() {
		setDisplayedObject(displayedObject);
	}

	protected static final String START_HTML = "<html><head><style type=\"text/css\">body{font-family:Arial,Helvetica,sans-serif; color:white;} p {margin:0;} ul {list-style-type:none; margin:0 0 0 15px;}</style></head><body>";
	protected static final String END_HTML = "</body></html>";

	public static void setDisplayedObject(Object obj) {
		displayedObject = obj;

		String str = START_HTML + getMoneyString();

		if (obj == null) {
			info.setText(str + END_HTML);
			return;
		}

		if (obj instanceof Weapon) {
			str += getStringForWeapon((Weapon) obj);
		}
		
		IdType type = null;
		
		if (obj instanceof IdType) {
			type = (IdType) obj;
		} else if (obj instanceof Entity) {
			type = ((Entity) obj).getType();
		}
		
		if (type instanceof TowerType) {
			str += getStringForTowerType((TowerType) type);
		} else if (type instanceof ObstacleType) {
			str += getStringForObstacleType((ObstacleType) type);
		}
		
		str += END_HTML;
		
		info.setText(str);
	}

	private static class HealthBar extends JComponent {

		private static final long serialVersionUID = -387557069684512486L;

		public HealthBar() {
			this.setBackground(Color.BLACK);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(256, 16);
		}

		@Override
		public void paintComponent(Graphics g) {
			if (g instanceof Graphics2D) ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 256, 16);

			g.setColor(Color.getHSBColor((float) (GameLogic.getHealth() / (GameLogic.getMaxHealth() * 3)), 1.0F, 0.375F));

			g.fillRect(0, 0, (int) (256 * GameLogic.getHealth() / GameLogic.getMaxHealth()), 16);

			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
			FontMetrics fm = g.getFontMetrics();

			g.setColor(Color.WHITE);

			String str = String.format(Language.getCurrentLocale(), "%s: %.1f%%", Language.get("health"), Math.max(0, 100 * GameLogic.getHealth() / GameLogic.getMaxHealth()));
			g.drawString(str, 128 - fm.stringWidth(str) / 2, 14);
		}

	}

}
