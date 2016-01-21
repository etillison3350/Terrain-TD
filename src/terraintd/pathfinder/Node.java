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
	private boolean explored;

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
	
	public boolean isExplored() {
		return explored;
	}

	void close() {
		this.closed = true;
	}
	
	void explore() {
		close();
		this.explored = true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (top ? 1231 : 1237);
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Node other = (Node) obj;
		if (top != other.top) return false;
		if (x != other.x) return false;
		if (y != other.y) return false;
		return true;
	}
}
