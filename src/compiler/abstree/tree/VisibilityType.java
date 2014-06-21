package compiler.abstree.tree;

public class VisibilityType {
	
	public final static int PUBLIC = 0;
	public final static int PRIVATE = 1;
	public final static int SINGLE = 2;
	
	/** Tip konstante. */
	public int type;
	
	public VisibilityType(int type) {
		this.type = type;
	}
}
