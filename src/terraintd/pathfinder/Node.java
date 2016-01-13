package terraintd.pathfinder;

public class Node {

	public final int x, y;

	/**
	 * <ul>
	 * <li><b><i>top</i></b><br>
	 * <br>
	 * {@code public final int top}<br>
	 * <br>
	 * <code>true</code> if this Node is on top of its tile, <code>false</code> if on the left side.<br>
	 * </ul>
	 */
	public final boolean top;

	private Node next;
	private double cost;
	private boolean closed;

	public Node(int x, int y, boolean top) {
		this.x = x;
		this.y = y;
		this.top = top;
	}
	
	public double getAbsX() {
		return this.x + (this.top ? 0.5 : 0);
	}
	
	public double getAbsY() {
		return this.y + (this.top ? 0 : 0.5);
	}

	public Node getNextNode() {
		return next;
	}

	void setNext(Node next) {
		this.next = next;
	}

	public double getCost() {
		return cost;
	}

	void setCost(double cost) {
		this.cost = cost;
	}

	boolean isClosed() {
		return closed;
	}

	void setClosed(boolean closed) {
		this.closed = closed;
	}
}
