package terraintd.window;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.HashMap;

public class SquareLayout implements LayoutManager2 {

	private HashMap<Component, Integer> columns = new HashMap<>();
	
	private int numColumns;
	
	public SquareLayout() {
		this(1);
	}
	
	public SquareLayout(int columns) {
		this.numColumns = columns;
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		columns.put(comp, 0);
	}

	@Override
	public void layoutContainer(Container parent) {
		if (!sizesSet) setSizes(parent);
		
		int n = parent.getComponentCount();
		for (int i = 0; i < n; i++) {
			parent.getComponent(i).setBounds(s * (i / r), s * (i % r), s, s);
		}
	}

	private int s, r;

	private void setSizes(Container parent) {
		int n = parent.getComponentCount();

		if (n == 0) return;

		Insets insets = parent.getInsets();
		int w = parent.getWidth() - (insets.left + insets.right);
		int h = parent.getHeight() - (insets.top + insets.bottom);

		int nh = (int) Math.ceil(Math.sqrt((double) n * (double) h / (double) w));
		int nw = (int) Math.ceil(Math.sqrt((double) n * (double) w / (double) h));


		int sh = nh == 0 ? 0 : h / nh;
		int sw = nw == 0 ? 0 : w / nw;

//		System.out.println(n + ", " + h + ", " + w + ", " + nh + ", " + nw + ", " + sh + ", " + sw);
//		System.out.println((sh * nw) + ", " + (sw * nh));
		
		if (sh * (int) Math.ceil((double) n / (double) nh) > w)
			sh = 0;
		else if (sw * (int) Math.ceil((double) n / (double) nw) > h)
			sw = 0;
		
//		System.out.println(sh + ", " + sw);
		
		if (sh > sw) {
			r = nh;//(int) Math.ceil((double) n / (double) nh);
			s = sh;
		} else {
			r = (int) Math.ceil((double) n / (double) nw);//nw;
			s = sw;
		}

		sizesSet = true;
	}

	private boolean sizesSet = false;

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(1, 1);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		setSizes(parent);

		return new Dimension(s * (int) Math.ceil((double) parent.getComponentCount() / r), s * r);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		columns.remove(comp);
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		columns.put(comp, constraints instanceof Number ? ((Number) constraints).intValue() : 0);
	}

	@Override
	public float getLayoutAlignmentX(Container arg0) {
		return 0.5F;
	}

	@Override
	public float getLayoutAlignmentY(Container arg0) {
		return 0.5F;
	}

	@Override
	public void invalidateLayout(Container arg0) {
		this.sizesSet = false;
	}

	@Override
	public Dimension maximumLayoutSize(Container arg0) {
		return new Dimension(32767, 32767);
	}

	
	public int getNumColumns() {
		return numColumns;
	}

	
	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

}
