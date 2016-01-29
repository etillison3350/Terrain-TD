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
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;

import terraintd.GameLogic;
import terraintd.GameLogic.State;
import terraintd.Language;
import terraintd.object.CollidableEntity;
import terraintd.object.Enemy;
import terraintd.object.Entity;
import terraintd.object.Gun;
import terraintd.object.StatusEffect;
import terraintd.object.Tower;
import terraintd.object.Weapon;
import terraintd.types.CollidableType;
import terraintd.types.DeliveryType;
import terraintd.types.EffectType;
import terraintd.types.EnemyType;
import terraintd.types.IdType;
import terraintd.types.ModdedType;
import terraintd.types.ProjectileType;
import terraintd.types.TargetType;
import terraintd.types.TowerType;

public class InfoPanel extends JPanel {

	private static final long serialVersionUID = -7585731701891500747L;

	public static final InfoPanel infoPanel = new InfoPanel();

	private static final ActionListener sellListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			GameLogic.sell((CollidableEntity) displayedObject);
			InfoPanel.setDisplayedObject(null);
		}
	};

	private static Object displayedObject;

	private static BufferedImage playImage, pauseImage, ffImage, rewImage;

	public static JToggleButton pause, fastForward;
	private static HealthBar health;
	private static JLabel money;

	private static JPanel panel;

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

		c.gridy = 3;
		c.weighty = 1;
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.BLACK);
		JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, c);

		c.weighty = 0;
		c.gridy = 2;
		money = createLabel("", 5, 0);
		money.setBackground(Color.BLACK);
		money.setOpaque(true);
		this.add(money, c);

		c.gridwidth = 1;
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

	private static JLabel createLabel(String format, int header, int indent, Object... args) {
		JLabel label = new JLabel(String.format(Language.getCurrentLocale(), format, args));
		label.setFont(new Font(Font.SANS_SERIF, header > 0 ? Font.BOLD : Font.PLAIN, header > 0 ? (26 - 2 * header) : 14));
		label.setForeground(Color.WHITE);
		label.setBorder(BorderFactory.createEmptyBorder(2, 2 + indent * 10, 2, 2));

		return label;
	}

	private static JButton createSellButton(CollidableEntity entity) {
		JButton button = new JButton(String.format(Language.getCurrentLocale(), "%s     %d", Language.get("sell"), entity.getType().sellCost));

		button.setMaximumSize(new Dimension(256, button.getPreferredSize().height * 3 / 2));
		button.setMinimumSize(new Dimension(256, button.getPreferredSize().height * 3 / 2));
		button.setPreferredSize(new Dimension(256, button.getPreferredSize().height * 3 / 2));

		button.setBackground(Color.DARK_GRAY);
		button.setForeground(Color.WHITE);
		button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		button.setMargin(new Insets(10, 3, 10, 3));
		button.setFocusPainted(false);
		button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

		button.addActionListener(sellListener);

		return button;
	}

	public static void paintHealthBar() {
		health.repaint();
	}

	private static double getDamagePerSecond(ProjectileType[] projectiles) {
		double dps = 0;

		for (ProjectileType p : projectiles)
			dps += p.rate * (p.damage - (p.falloff * 0.5));

		return dps;
	}

	public static Object getDisplayedType() {
		return displayedObject;
	}

	public static void refreshDisplay() {
		setDisplayedObject(displayedObject);
	}

	public static void setDisplayedObject(Object obj) {
		addLabels(obj);
		infoPanel.revalidate();
		infoPanel.repaint();
	}

	private static void addLabels(Object obj) {
		displayedObject = obj;

		money.setText(String.format(Language.getCurrentLocale(), "%s: %d\n", Language.get("money"), GameLogic.getMoney()));

		panel.removeAll();
		if (obj == null) return;

		panel.add(Box.createVerticalStrut(10));

		IdType type = null;

		if (obj instanceof Entity) {
			type = ((Entity) obj).getType();
		} else if (obj instanceof IdType) {
			type = (IdType) obj;
		}

		if (type != null) {
			panel.add(createLabel("%s", 3, 0, Language.get(type.id)));
			if (type instanceof ModdedType)
				panel.add(createLabel("%s", 0, 0, Language.get(((ModdedType) type).mod.id))).setForeground(new Color(255, 255, 128));
		}
		
		if (obj instanceof Weapon) {
			Gun gun = ((Weapon) obj).getGun();
			if (gun != null) {
				panel.add(createLabel("%s: %d", 0, 0, Language.get("kills"), gun.getKills()));
				panel.add(createLabel("%s: %.5f", 0, 0, Language.get("damage-dealt"), gun.getDamageDone()));
				panel.add(createLabel("%s: %d", 0, 0, Language.get("projectiles-fired"), gun.getProjectilesFired()));
				panel.add(Box.createVerticalStrut(15));
			}

			if (obj instanceof Tower && GameLogic.getState() == State.PLAYING) {
				panel.add(new TargetCycleButton(((Weapon) obj).getGun()));
				panel.add(Box.createVerticalStrut(15));
			}
		}

		ProjectileType[] projectiles = null;
		if (type instanceof TowerType) {
			projectiles = ((TowerType) type).projectiles;
		} else if (type instanceof EnemyType) {
			projectiles = ((EnemyType) type).projectiles;
		}

		if (type instanceof CollidableType) panel.add(createLabel("%s: %d x %d %s", 0, 0, Language.get("area"), ((CollidableType) type).width, ((CollidableType) type).height, Language.get("tiles")));
		if (projectiles != null && projectiles.length > 0) panel.add(createLabel("%s: %.5g", 0, 0, Language.get("dps"), getDamagePerSecond(projectiles)));
		if (type instanceof TowerType) panel.add(createLabel("%s: %.3g %s", 0, 0, Language.get("detect-range"), ((TowerType) type).range, Language.get("tiles")));

		if (obj instanceof Enemy) {
			Enemy enemy = (Enemy) obj;
			panel.add(createLabel("%s: %.2f/%.2f", 0, 0, Language.get("health"), Math.max(0, enemy.getHealth()), enemy.type.health));
			panel.add(createLabel("%s: %d", 0, 0, Language.get("reward"), enemy.type.reward));
			panel.add(createLabel("%s: %.4g%%", 0, 0, Language.get("damage"), 100 * enemy.type.damage / GameLogic.getMaxHealth()));
			if (enemy.getDead() == 0) {
				panel.add(createLabel("%s", 5, 0, Language.get("active-effects")));
				StatusEffect[] effects = enemy.getStatusEffects();
				if (effects.length <= 0) {
					panel.add(createLabel("%s", 0, 1, Language.get("none")));
				} else {
					for (StatusEffect e : effects) {
						panel.add(createLabel("%s %s, %.3gs", 0, 2, e.type, e.amplifier, e.getDuration()));
					}
				}
			}
		}
		
		panel.add(new JSeparator()).setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		if (projectiles != null) {
			panel.add(createLabel("%s", 5, 0, Language.get("projectiles")));
			if (projectiles == null || projectiles.length == 0) {
				panel.add(createLabel("%s", 0, 1, Language.get("none")));
			} else {
				for (ProjectileType p : projectiles) {
					panel.add(createLabel("%s: %s", 0, 1, Language.get("delivery"), p.delivery.toString()));
					panel.add(createLabel("%s: %.3g - %.3g", 0, 1, Language.get("damage"), p.damage, p.damage - p.falloff));
					panel.add(createLabel("%s: %.4g %s", 0, 1, Language.get("rate"), p.rate * 60, Language.get("rpm")));
					panel.add(createLabel("%s: %s", 0, 1, Language.get("range"), p.maxDist > 1e100 ? "\u221E" : String.format(Language.getCurrentLocale(), "%.3g %s", p.maxDist, Language.get("tiles"))));
					panel.add(createLabel("%s", 6, 1, Language.get("effects")));
					if (p.effects.length == 0) {
						panel.add(createLabel("%s", 0, 2, Language.get("none")));
					} else {
						for (EffectType e : p.effects) {
							panel.add(createLabel("%s %s, %.3gs", 0, 2, e.type, e.amplifier, e.duration));
						}
					}
					panel.add(new JSeparator()).setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
				}
			}
		}

		if (obj instanceof CollidableEntity && GameLogic.getState() == State.PLAYING) {
			panel.add(Box.createVerticalStrut(5));
			panel.add(createSellButton((CollidableEntity) obj));
		}
	}

	private static final ActionListener targetCycleListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!(e.getSource() instanceof TargetCycleButton)) return;

			TargetCycleButton button = (TargetCycleButton) e.getSource();
			if (!button.canTarget()) return;

			button.index++;
			button.gun.setTargetType(TargetType.values()[button.index %= TargetType.values().length]);
			button.setText(Language.get("target") + ": " + button.gun.getTargetType());
		}
	};

	private static class TargetCycleButton extends JButton {

		private static final long serialVersionUID = 4273518145203398359L;

		public final Gun gun;
		public int index;

		public TargetCycleButton(Gun g) {
			this.gun = g;
			this.index = Arrays.asList(TargetType.values()).indexOf(gun.getTargetType());

			this.setText(Language.get("target") + ": " + (canTarget() ? gun.getTargetType() : Language.get("target-na")));

			this.setMaximumSize(new Dimension(256, this.getPreferredSize().height));

			this.setBackground(Color.DARK_GRAY);
			this.setForeground(Color.WHITE);
			this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.setFocusPainted(false);
			this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

			this.addActionListener(targetCycleListener);
		}

		boolean canTarget() {
			for (ProjectileType projectile : gun.projectiles) {
				if (projectile.delivery != DeliveryType.AREA && !projectile.absRotation) return true;
			}
			return false;
		}
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
		public Dimension getMinimumSize() {
			return new Dimension(128, 16);
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
