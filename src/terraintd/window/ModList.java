package terraintd.window;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSplitPane;

import terraintd.types.Mod;


public class ModList extends JDialog {

	private static final long serialVersionUID = -4204900324206949102L;
	
	private JList<Mod> mods;
	
	private ModList() {
		mods = new JList<>(Mod.values());
		mods.setCellRenderer(new ModCellRenderer());
		
		this.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mods, null));
	}
	
	private static final class ModCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 15921663840946983L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if (!(c instanceof JLabel)) return c;
			
			// TODO
			return c;
		}
		
		
	}

}
