package compiler.semanal;
import compiler.abstree.*;
import compiler.report.*;
import compiler.semanal.type.SemAtomType;
import compiler.semanal.type.SemPointerType;
import compiler.semanal.type.SemType;
import compiler.semanal.type.SemTypeError;
import compiler.abstree.tree.AbsAlloc;
import compiler.abstree.tree.AbsArrayType;
import compiler.abstree.tree.AbsAssignStmt;
import compiler.abstree.tree.AbsAtomConst;
import compiler.abstree.tree.AbsAtomType;
import compiler.abstree.tree.AbsBinExpr;
import compiler.abstree.tree.AbsBlockStmt;
import compiler.abstree.tree.AbsCallExpr;
import compiler.abstree.tree.AbsConstDecl;
import compiler.abstree.tree.AbsDeclName;
import compiler.abstree.tree.AbsDecls;
import compiler.abstree.tree.AbsExprStmt;
import compiler.abstree.tree.AbsForStmt;
import compiler.abstree.tree.AbsFunDecl;
import compiler.abstree.tree.AbsIfStmt;
import compiler.abstree.tree.AbsNilConst;
import compiler.abstree.tree.AbsPointerType;
import compiler.abstree.tree.AbsProcDecl;
import compiler.abstree.tree.AbsProgram;
import compiler.abstree.tree.AbsRecordType;
import compiler.abstree.tree.AbsStmts;
import compiler.abstree.tree.AbsTypeDecl;
import compiler.abstree.tree.AbsTypeName;
import compiler.abstree.tree.AbsUnExpr;
import compiler.abstree.tree.AbsValExprs;
import compiler.abstree.tree.AbsValName;
import compiler.abstree.tree.AbsVarDecl;
import compiler.abstree.tree.AbsWhileStmt;

public class SemTypeChecker implements AbsVisitor {

	public boolean error;

	@Override
	public void visit(AbsAlloc acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsArrayType acceptor) {
		
	}

	@Override
	public void visit(AbsAssignStmt acceptor) {
		acceptor.dstExpr.accept(this);
		acceptor.srcExpr.accept(this);
		
		SemType src = SemDesc.getActualType(acceptor.srcExpr);
		SemType dst = SemDesc.getActualType(acceptor.dstExpr);
		
		if(src != null && dst != null) {
			// lahko prilagodimo tipa med sabo ?
			if(src.coercesTo(dst)) {
				// razen atomarnih tipov in kazalcev ne moremo prirejati med sabo
				if(src instanceof SemAtomType || src instanceof SemPointerType) {
					SemDesc.setActualType(acceptor, src);
				} else {
					Report.error("Expected integer or pointer type!", acceptor.begLine, acceptor.begColumn, acceptor.endLine, acceptor.endColumn, 1);
				}
				
			} else {
				Report.error("Types do not match!", acceptor.begLine, acceptor.begColumn, acceptor.endLine, acceptor.endColumn, 1);
			}
			
		}
	}

	@Override
	public void visit(AbsAtomConst acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsAtomType acceptor) {
		SemAtomType type = new SemAtomType(acceptor.type);
		SemDesc.setActualType(acceptor, type);
		
	}

	@Override
	public void visit(AbsBinExpr acceptor) {
		acceptor.fstExpr.accept(this);
		acceptor.sndExpr.accept(this);
		
	}

	@Override
	public void visit(AbsBlockStmt acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsCallExpr acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsConstDecl acceptor) {
	}

	@Override
	public void visit(AbsDeclName acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsDecls acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsExprStmt acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsForStmt acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsFunDecl acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsIfStmt acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsNilConst acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsPointerType acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsProcDecl acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsProgram acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsRecordType acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsStmts acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsTypeDecl acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsTypeName acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsUnExpr acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsValExprs acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsValName acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsVarDecl acceptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AbsWhileStmt acceptor) {
		// TODO Auto-generated method stub
		
	}

}
