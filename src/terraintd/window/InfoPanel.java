package terraintd.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

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
import terraintd.types.Identifiable;
import terraintd.types.InstantType;
import terraintd.types.ModdedType;
import terraintd.types.ProjectileType;
import terraintd.types.Sellable;
import terraintd.types.TargetType;
import terraintd.types.TowerType;
import terraintd.types.TowerUpgrade;
import terraintd.types.Upgrade;

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

	public static JToggleButton[] buttons;
	private static BufferedImage[] images;

	private static HealthBar health;
	private static JLabel money;

	private static JPanel panel;
	private static JPanel bottom;

	private InfoPanel() {
		images = new BufferedImage[5];
		try {
			images[0] = ImageIO.read(Paths.get("terraintd/mods/base/images/icons/pause.png").toFile());
		} catch (IOException e) {}

		for (int i = 1; i < 5; i++) {
			try {
				images[i] = ImageIO.read(Paths.get("terraintd/mods/base/images/icons/sp" + i + ".png").toFile());
			} catch (IOException e) {}
		}

		this.setLayout(new GridBagLayout());

		this.setPreferredSize(new Dimension(256, Short.MAX_VALUE));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridwidth = 5;
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
		scrollPane.getVerticalScrollBar().setUI(new TDScrollBarUI());
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		this.add(scrollPane, c);

		c.weighty = 0;
		c.gridy = 4;
		bottom = new JPanel();
		bottom.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		bottom.setBackground(Color.BLACK);
		bottom.setBorder(new EmptyBorder(0, 0, 5, 0));
		this.add(bottom, c);

		c.gridy = 2;
		money = createLabel("", 5, 0);
		money.setBackground(Color.BLACK);
		money.setOpaque(true);
		this.add(money, c);

		final ActionListener buttonListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				((JToggleButton) e.getSource()).setSelected(true);
				for (int i = 0; i < 5; i++) {
					if (e.getSource() == buttons[i]) {
						Window.speeds[i].doClick();
					}
				}
			}
		};

		c.gridwidth = 1;
		c.weightx = 0.5;
		c.gridy = 0;
		buttons = new JToggleButton[5];
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < 5; i++) {
			c.gridx = i;
			buttons[i] = new JToggleButton(new ImageIcon(images[i]));
			buttons[i].setMargin(new Insets(0, 0, 0, 0));
			buttons[i].setBackground(new Color(238, 238, 238));
			buttons[i].setBorderPainted(false);
			buttons[i].setFocusPainted(false);
			buttons[i].addActionListener(buttonListener);
			group.add(buttons[i]);
			this.add(buttons[i], c);
		}
	}

	private static JLabel createLabel(String format, int header, int indent, Object... args) {
		return createLabel(format, header, indent, Color.WHITE, args);
	}

	private static JLabel createLabel(String format, int header, int indent, Color color, Object... args) {
		String str = String.format(Language.getCurrentLocale(), format, args);
		String c = String.format("#%06x", color.getRGB() & 0xFFFFFF);
		JLabel label = new JLabel("<html><body style=\"color:" + c + ";\">" + str + "</body></html>");
		label.setBorder(BorderFactory.createEmptyBorder(2, 2 + indent * 10, 2, 2));
		label.setForeground(Color.WHITE);

		int fsize = header > 0 ? (26 - 2 * header) : 14;
		FontMetrics fm;
		do {
			fm = label.getFontMetrics(new Font(Font.SANS_SERIF, header > 0 ? Font.BOLD : Font.PLAIN, fsize--));
		} while (fm.stringWidth(str.replaceAll("\\<.+?\\>", "")) >= 244 - indent * 10);
		label.setFont(fm.getFont());

		return label;
	}

	private static JLabel createUpgradeLabel(String format, int header, int indent, String chgFormat, Number change, boolean positive, Object... args) {
		String str = String.format(Language.getCurrentLocale(), format, args);
		String chg = Math.abs(change.doubleValue()) < 0.0001 ? "" : String.format(Language.getCurrentLocale(), chgFormat, change);
		JLabel label = new JLabel("<html><body>" + str + " <span style=\"color:" + (positive ? "lime" : "red") + ";\">" + chg + "</span></body></html>");
		label.setBorder(BorderFactory.createEmptyBorder(2, 2 + indent * 10, 2, 2));
		label.setForeground(Color.WHITE);

		int fsize = header > 0 ? (26 - 2 * header) : 14;
		FontMetrics fm;
		do {
			fm = label.getFontMetrics(new Font(Font.SANS_SERIF, header > 0 ? Font.BOLD : Font.PLAIN, fsize--));
		} while (fm.stringWidth(str + " " + chg) >= 244 - indent * 10);
		label.setFont(fm.getFont());

		return label;
	}

	private static JButton createSellButton(CollidableEntity entity) {
		JButton button = new JButton(String.format(Language.getCurrentLocale(), "%s     %s%d", Language.get(entity.getType().sellCost < 0 ? "remove" : "sell"), entity.getType().sellCost > 0 ? "+" : "", entity.getType().sellCost));

		button.setMaximumSize(new Dimension(248, button.getPreferredSize().height * 3 / 2));
		button.setMinimumSize(new Dimension(248, button.getPreferredSize().height * 3 / 2));
		button.setPreferredSize(new Dimension(248, button.getPreferredSize().height * 3 / 2));

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
			dps += 0.5 * p.rate * (p.maxDamage + p.minDamage);

		return dps;
	}

	private static double getTotalDamage(InstantType instant) {
		double damage = 0;
		for (int i = 0; i < instant.count; i++) {
			for (ProjectileType p : instant.projectiles) {
				damage += 0.5 * (p.maxDamage - p.minDamage);
			}
		}

		return damage;
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

	private static final Comparator<Identifiable> diffComp = new Comparator<Identifiable>() {

		@Override
		public int compare(Identifiable o1, Identifiable o2) {
			return Long.compare(o1.getUID(), o2.getUID());
		}

	};

	public static void updateMoney() {
		money.setText(String.format(Language.getCurrentLocale(), "%s: %d\n", Language.get("money"), GameLogic.getMoney()));
	}
	
	private static void addLabels(Object obj) {
		displayedObject = obj;

		updateMoney();

		panel.removeAll();
		bottom.removeAll();
		if (obj == null) return;

		panel.add(Box.createVerticalStrut(10));

		if (obj instanceof Upgrade) {
			if (obj instanceof TowerUpgrade && GameLogic.getSelectedEntity() instanceof Tower) {
				TowerUpgrade upg = (TowerUpgrade) obj;
				Tower tower = (Tower) GameLogic.getSelectedEntity();

				panel.add(createLabel("%s", 2, 0, Language.get(upg.id)));
				panel.add(createLabel("%s", 0, 0, new Color(255, 255, 128), Language.get(upg.mod.id)));

				panel.add(createUpgradeLabel("%s: %d", 0, 0, "%+d", upg.sellCost, upg.sellCost > 0, Language.get("sell-cost"), tower.getType().getSellCost()));

				TowerType upgraded = upg.upgradeTower(tower);

				double dpsChg = getDamagePerSecond(upgraded.projectiles) - getDamagePerSecond(tower.getType().projectiles);
				if (tower.getType().projectiles.length > 0 || upgraded.projectiles.length > 0) panel.add(createUpgradeLabel("%s: %.5g", 0, 0, "%+.5g", dpsChg, dpsChg > 0, Language.get("dps"), getDamagePerSecond(tower.getType().projectiles)));
				panel.add(createUpgradeLabel("%s: %.3g %s", 0, 0, "%+.3g", upg.range, upg.range > 0, Language.get("detect-range"), tower.getType().range, Language.get("tiles")));

				panel.add(new JSeparator()).setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

				panel.add(createLabel("%s", 5, 0, Language.get("projectiles")));
				if (tower.getType().projectiles.length <= 0 && upgraded.projectiles.length <= 0) {
					panel.add(createLabel("%s", 0, 1, Language.get("none")));
				} else {
					Set<ProjectileType> added = new TreeSet<>(diffComp);
					added.addAll(Arrays.asList(upgraded.projectiles));
					added.removeIf(p -> {
						for (ProjectileType pr : tower.getType().projectiles) {
							if (diffComp.compare(p, pr) == 0) return true;
						}

						return false;
					});
					List<ProjectileType> addList = new ArrayList<>(added);

					Set<ProjectileType> removed = new TreeSet<>(diffComp);
					removed.addAll(Arrays.asList(tower.getType().projectiles));
					removed.removeIf(p -> {
						for (ProjectileType pr : upgraded.projectiles) {
							if (diffComp.compare(p, pr) == 0) return true;
						}

						return false;
					});
					List<ProjectileType> delList = new ArrayList<>(removed);

					Set<ProjectileType> unique = new TreeSet<>(diffComp);
					unique.addAll(Arrays.asList(tower.getType().projectiles));
					unique.addAll(Arrays.asList(upgraded.projectiles));

					Set<ProjectileType> sorted = new TreeSet<>(projComp);
					sorted.addAll(unique);

					for (ProjectileType p : sorted) {
						if (addList.contains(p) || delList.contains(p)) {
							Color c = addList.contains(p) ? Color.GREEN : Color.RED;

							panel.add(createLabel("%s: %s", 0, 1, c, Language.get("delivery"), p.delivery.toString()));
							long count = unique.stream().filter(proj -> projComp.compare(proj, p) == 0).count();
							if (count > 1) panel.add(createLabel("%s: %d", 0, 1, c, Language.get("count"), count));
							panel.add(createLabel("%s: %.3g \u2013 %.3g", 0, 1, c, Language.get("damage"), p.maxDamage, p.minDamage));
							panel.add(createLabel("%s: %.4g %s", 0, 1, c, Language.get("rate"), p.rate * 60, Language.get("rpm")));
							panel.add(createLabel("%s: %s", 0, 1, c, Language.get("range"), p.maxDist > 1e100 ? "\u221E" : String.format(Language.getCurrentLocale(), "%.3g %s", p.maxDist, Language.get("tiles"))));
							if (p.explodeRadius > 0.000001 && p.delivery == DeliveryType.SINGLE_TARGET) panel.add(createLabel("%s: %.3g", 0, 1, c, Language.get("explode-radius"), p.explodeRadius));
							panel.add(createLabel("%s", 6, 1, c, Language.get("effects")));
							if (p.effects.length == 0) {
								panel.add(createLabel("%s", 0, 2, c, Language.get("none")));
							} else {
								for (EffectType e : p.effects)
									panel.add(createLabel(e.toString(), 0, 2, c));
							}
						} else {
							ProjectileType orig = Arrays.stream(tower.getType().projectiles).filter(proj -> proj.uid == p.uid).findAny().get();
							ProjectileType chgd = Arrays.stream(upgraded.projectiles).filter(proj -> proj.uid == p.uid).findAny().get();

							panel.add(createLabel("%s: %s", 0, 1, Language.get("delivery"), p.delivery.toString()));

							long oCount = Arrays.stream(tower.getType().projectiles).filter(proj -> projComp.compare(proj, orig) == 0).count();
							long nCount = Arrays.stream(upgraded.projectiles).filter(proj -> projComp.compare(proj, chgd) == 0).count();
							if (oCount > 1 || nCount > 1) panel.add(createUpgradeLabel("%s: %d", 0, 1, "%+d", nCount - oCount, nCount - oCount > 0, Language.get("count"), oCount));

							String max = orig.maxDamage - chgd.maxDamage == 0 ? "" : String.format(Language.getCurrentLocale(), " <span style=\"color:%s\">%+.3g</span>", orig.maxDamage - chgd.maxDamage < 0 ? "lime" : "red", chgd.maxDamage - orig.maxDamage);
							String min = orig.minDamage - chgd.minDamage == 0 ? "" : String.format(Language.getCurrentLocale(), " <span style=\"color:%s\">%+.3g</span>", orig.minDamage - chgd.minDamage < 0 ? "lime" : "red", chgd.minDamage - orig.minDamage);

							panel.add(createLabel("%s: %.3g%s \u2013 %.3g%s", 0, 1, Language.get("damage"), orig.maxDamage, max, orig.minDamage, min));
							panel.add(createUpgradeLabel("%s: %.4g %s", 0, 1, "%+.4g", (chgd.rate - orig.rate) * 60, chgd.rate - orig.rate > 0, Language.get("rate"), p.rate * 60, Language.get("rpm")));
							if (orig.maxDist > 1e100 && chgd.maxDist > 1e100) {
								panel.add(createLabel("%s: \u221E", 0, 1, Language.get("range")));
							} else if (orig.maxDist > 1e100 || chgd.maxDist > 1e100) {
								panel.add(createLabel("%s: %s <span style=\"color:" + (chgd.maxDist <= 1e100 ? "red" : "lime") + ";\">", 0, 1, Language.get("range"), orig.maxDist > 1e100 ? "\u221E" : String.format(Language.getCurrentLocale(), "%.3g %s", p.maxDist, Language.get("tiles")), chgd.maxDist > 1e100 ? "\u221E" : String.format(Language.getCurrentLocale(), "%.3g", chgd.maxDist)));
							} else {
								panel.add(createUpgradeLabel("%s: %.3g %s", 0, 1, "%+.3g", chgd.maxDist - orig.maxDist, chgd.maxDist > orig.maxDist, Language.get("range"), orig.maxDist, Language.get("tiles")));
							}
							if ((orig.explodeRadius > 0.000001 || chgd.explodeRadius > 0.000001) && p.delivery == DeliveryType.SINGLE_TARGET) panel.add(createUpgradeLabel("%s: %.3g", 0, 1, "%+.3g", chgd.explodeRadius - orig.explodeRadius, chgd.explodeRadius > orig.explodeRadius, Language.get("explode-radius"), p.explodeRadius));
							panel.add(createLabel("%s", 6, 1, Language.get("effects")));
							if (orig.effects.length == 0 && chgd.effects.length == 0) {
								panel.add(createLabel("%s", 0, 2, Language.get("none")));
							} else {
								Set<EffectType> ae = new TreeSet<>(diffComp);
								ae.addAll(Arrays.asList(chgd.effects));
								ae.removeIf(e -> {
									for (EffectType ef : orig.effects) {
										if (diffComp.compare(e, ef) == 0) return true;
									}

									return false;
								});
								List<EffectType> addEffects = new ArrayList<>(ae);

								Set<EffectType> re = new TreeSet<>(diffComp);
								re.addAll(Arrays.asList(orig.effects));
								re.removeIf(e -> {
									for (EffectType ef : chgd.effects) {
										if (diffComp.compare(e, ef) == 0) return true;
									}

									return false;
								});
								List<EffectType> delEffects = new ArrayList<>(re);

								Set<EffectType> uniqueEffects = new TreeSet<>(diffComp);
								uniqueEffects.addAll(Arrays.asList(orig.effects));
								uniqueEffects.addAll(Arrays.asList(chgd.effects));
								
								for (EffectType e : uniqueEffects) {
									if (addEffects.contains(e) || delEffects.contains(e)) {
										Color c = addEffects.contains(e) ? Color.GREEN : Color.RED;
										panel.add(createLabel(e.toString(), 0, 2, c));
									} else {
										EffectType origEffect = Arrays.stream(orig.effects).filter(eff -> eff.uid == e.uid).findAny().get();
										EffectType newEffect = Arrays.stream(chgd.effects).filter(eff -> eff.uid == e.uid).findAny().get();

										if (e.type.amplifiable) {
											String amp = origEffect.amplifier == newEffect.amplifier ? "" : String.format(Language.getCurrentLocale(), " <span style=\"color:%s\">%+.2f</span>", origEffect.amplifier < newEffect.amplifier ? "lime" : "red", newEffect.amplifier - origEffect.amplifier);
											String dur = origEffect.duration == newEffect.duration ? "" : String.format(Language.getCurrentLocale(), " <span style=\"color:%s\">%+.3g</span>", origEffect.duration < newEffect.duration ? "lime" : "red", newEffect.duration - origEffect.duration);

											panel.add(createLabel("%s %.2f%s, %.3gs%s", 0, 2, e.type, origEffect.amplifier, amp, origEffect.duration, dur));
										} else {
											panel.add(createUpgradeLabel(e.toString(), 0, 2, "%+.3g", newEffect.duration - origEffect.duration, newEffect.duration > origEffect.duration));
										}
									}
								}
							}
						}
						panel.add(new JSeparator()).setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
					}
				}
			}
		} else {
			IdType type = null;

			if (obj instanceof Entity) {
				type = ((Entity) obj).getType();
			} else if (obj instanceof IdType) {
				type = (IdType) obj;
			}

			if (type != null) {
				panel.add(createLabel("%s", 2, 0, Language.get(type.id)));
				if (type instanceof ModdedType) panel.add(createLabel("%s", 0, 0, new Color(255, 255, 128), Language.get(((ModdedType) type).mod.id)));
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
			if (obj instanceof Sellable) panel.add(createLabel("%s: %d", 0, 0, Language.get("sell-cost"), ((Sellable) obj).getSellCost()));
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
						for (StatusEffect e : effects)
							panel.add(createLabel(e.toString(), 0, 2));
					}
				}
			}

			if (type instanceof InstantType) {
				InstantType instant = (InstantType) type;

				panel.add(createLabel("%s: %s", 0, 0, Language.get("inst-target"), instant.target.toString()));
				panel.add(createLabel("%s: %.5g", 0, 0, Language.get("total-damage"), getTotalDamage(instant)));
				panel.add(createLabel("%s: %.3g %s", 0, 0, Language.get("detect-range"), instant.range, Language.get("tiles")));
				if (instant.count > 1) panel.add(createLabel("%s: %d", 0, 0, Language.get("count"), instant.count));

				panel.add(new JSeparator()).setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

				panel.add(createLabel("%s", 5, 0, Language.get("projectiles")));
				if (instant.projectiles.length == 0) {
					panel.add(createLabel("%s", 0, 1, Language.get("none")));
				} else {
					TreeSet<ProjectileType> sorted = new TreeSet<>(projComp);
					sorted.addAll(Arrays.asList(instant.projectiles));

					for (ProjectileType p : sorted) {
						panel.add(createLabel("%s: %s", 0, 1, Language.get("delivery"), p.delivery.toString()));
						if (sorted.size() != instant.projectiles.length) {
							long count = Arrays.stream(instant.projectiles).filter(proj -> projComp.compare(proj, p) == 0).count();
							if (count > 1) panel.add(createLabel("%s: %d", 0, 1, Language.get("count"), count));
						}
						panel.add(createLabel("%s: %.3g \u2013 %.3g", 0, 1, Language.get("damage"), p.maxDamage, p.minDamage));
						panel.add(createLabel("%s: %.4g %s", 0, 1, Language.get("rate"), p.rate * 60, Language.get("rpm")));
						panel.add(createLabel("%s: %s", 0, 1, Language.get("range"), p.maxDist > 1e100 ? "\u221E" : String.format(Language.getCurrentLocale(), "%.3g %s", p.maxDist, Language.get("tiles"))));
						if (p.explodeRadius > 0.000001 && p.delivery == DeliveryType.SINGLE_TARGET) panel.add(createLabel("%s: %.3g", 0, 1, Language.get("explode-radius"), p.explodeRadius));
						panel.add(createLabel("%s", 6, 1, Language.get("effects")));
						if (p.effects.length == 0) {
							panel.add(createLabel("%s", 0, 2, Language.get("none")));
						} else {
							for (EffectType e : p.effects)
								panel.add(createLabel(e.toString(), 0, 2));
						}
						panel.add(new JSeparator()).setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
					}
				}
			} else {
				panel.add(new JSeparator()).setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
			}

			if (projectiles != null) {
				panel.add(createLabel("%s", 5, 0, Language.get("projectiles")));
				if (projectiles.length == 0) {
					panel.add(createLabel("%s", 0, 1, Language.get("none")));
				} else {
					TreeSet<ProjectileType> sorted = new TreeSet<>(projComp);
					sorted.addAll(Arrays.asList(projectiles));

					for (ProjectileType p : sorted) {
						panel.add(createLabel("%s: %s", 0, 1, Language.get("delivery"), p.delivery.toString()));
						if (sorted.size() != projectiles.length) {
							long count = Arrays.stream(projectiles).filter(proj -> projComp.compare(proj, p) == 0).count();
							if (count > 1) panel.add(createLabel("%s: %d", 0, 1, Language.get("count"), count));
						}
						panel.add(createLabel("%s: %.3g \u2013 %.3g", 0, 1, Language.get("damage"), p.maxDamage, p.minDamage));
						panel.add(createLabel("%s: %.4g %s", 0, 1, Language.get("rate"), p.rate * 60, Language.get("rpm")));
						panel.add(createLabel("%s: %s", 0, 1, Language.get("range"), p.maxDist > 1e100 ? "\u221E" : String.format(Language.getCurrentLocale(), "%.3g %s", p.maxDist, Language.get("tiles"))));
						if (p.explodeRadius > 0.000001 && p.delivery == DeliveryType.SINGLE_TARGET) panel.add(createLabel("%s: %.3g", 0, 1, Language.get("explode-radius"), p.explodeRadius));
						panel.add(createLabel("%s", 6, 1, Language.get("effects")));
						if (p.effects.length == 0) {
							panel.add(createLabel("%s", 0, 2, Language.get("none")));
						} else {
							for (EffectType e : p.effects)
								panel.add(createLabel(e.toString(), 0, 2));
						}
						panel.add(new JSeparator()).setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
					}
				}
			}

			if (obj instanceof CollidableEntity && GameLogic.getState() == State.PLAYING) {
				bottom.add(createSellButton((CollidableEntity) obj));
			}
		}
	}

	private static final Comparator<ProjectileType> projComp = new Comparator<ProjectileType>() {

		@Override
		public int compare(ProjectileType o1, ProjectileType o2) {
			int i = Integer.compare(o1.delivery.hashCode(), o2.delivery.hashCode());
			if (i != 0) return i;

			i = Double.compare(o1.maxDamage, o2.maxDamage);
			if (i != 0) return i;

			i = Double.compare(o1.minDamage, o2.minDamage);
			if (i != 0) return i;

			i = Double.compare(o1.rate, o2.rate);
			if (i != 0) return i;

			i = Double.compare(o1.maxDist, o2.maxDist);
			if (i != 0) return i;

			i = Double.compare(o1.explodeRadius, o2.explodeRadius);
			if (i != 0) return i;

			i = Double.compare(o1.angle, o2.angle);
			if (i != 0) return i;

			i = Integer.compare(o1.effects.length, o2.effects.length);
			if (i != 0) return i;

			for (int n = 0; n < o1.effects.length; n++) {
				i = Integer.compare(o1.effects[n].hashCode(), o2.effects[n].hashCode());
				if (i != 0) return i;

				i = Double.compare(o1.effects[n].duration, o2.effects[n].duration);
				if (i != 0) return i;

				i = Double.compare(o1.effects[n].amplifier, o2.effects[n].amplifier);
				if (i != 0) return i;
			}
			return 0;
		}

	};

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

	public static void enableButtons(boolean enabled) {
		for (JToggleButton button : buttons) {
			button.setEnabled(enabled);
		}
	}

}
