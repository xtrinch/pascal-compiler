package compiler.abstree.tree;

import compiler.abstree.AbsVisitor;

public class QMarkStmt extends AbsStmt {
	
	public AbsValExpr cond;
	
	public AbsStmt stmt1;
	
	public AbsStmt stmt2;
	
	public QMarkStmt(AbsValExpr cond, AbsStmt stmt1, AbsStmt stmt2) {
		this.cond = cond;
		this.stmt1 = stmt1;
		this.stmt2 = stmt2;
	}

	@Override
	public void accept(AbsVisitor visitor) {
		visitor.visit(this);
	}
}
