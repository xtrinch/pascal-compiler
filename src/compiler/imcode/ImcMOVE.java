package compiler.imcode;

import java.io.*;

import compiler.abstree.tree.VisibilityType;

public class ImcMOVE extends ImcStmt {

	/** Ponor.  */
	public ImcExpr dst;

	/** Izvor.  */
	public ImcExpr src;

	
	public VisibilityType dstVisType;
	
	public ImcMOVE(ImcExpr dst, ImcExpr src, VisibilityType type) {
		if(dst == null)	System.out.println("null pointer");
		this.dst = dst;
		this.src = src;
		this.dstVisType = type;
	}

	@Override
	public void toXML(PrintStream xml) {
		xml.print("<imcnode kind=\"MOVE\">\n");
		dst.toXML(xml);
		src.toXML(xml);
		xml.print("</imcnode>\n");
	}

	@Override
	public ImcSEQ linear() {
		ImcSEQ lin = new ImcSEQ();
		ImcESEQ dst = this.dst.linear();
		ImcESEQ src = this.src.linear();
		lin.stmts.addAll(((ImcSEQ)dst.stmt).stmts);
		lin.stmts.addAll(((ImcSEQ)src.stmt).stmts);
		lin.stmts.add(new ImcMOVE(dst.expr, src.expr, this.dstVisType));
		return lin;
	}

}
