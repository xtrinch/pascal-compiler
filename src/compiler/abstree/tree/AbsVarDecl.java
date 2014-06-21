package compiler.abstree.tree;

import compiler.abstree.AbsVisitor;

/**
 * Deklaracija spremenljivke.
 */
public class AbsVarDecl extends AbsDecl {

	/** Ime spremenljivke. */
	public AbsDeclName name;
	
	/** Tip spremenljivke. */
	public AbsTypeExpr type;
	
	/** Tip vidnosti. */
	public VisibilityType visType;
	
	public AbsVarDecl(AbsDeclName name, AbsTypeExpr type, VisibilityType visType) {
		this.name = name;
		this.type = type;
		this.visType = visType;
	}

	public void accept(AbsVisitor visitor) {
		visitor.visit(this);
	}

}
