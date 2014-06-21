package compiler.frames;

import compiler.abstree.*;
import compiler.abstree.tree.*;
import compiler.report.Report;
import compiler.semanal.*;
import compiler.semanal.type.SemType;

public class FrmEvaluator implements AbsVisitor {
	
	public int sArg;
	/*
		- Vsak klicni zapis naj bo predstavljen z objektom razreda FrmFrame, 
		sestavljen pa naj bo tako, kot smo se dogovorili na predavanjih.
		
		- Vsak dostop do spremenljivke naj bo predstavljen z objektom razreda FrmAccess 
		(FrmVarAccess za globalne spremenljivke, FrmLocAccess za lokalne spremenljivke, 
		FrmArgAccess za argumente funkcij, FrmCmpAccess za komponente struktur).
	 */
	
	public void addFunFrame(String name) {
		AbsDecl decl = SemTable.fnd(name);
		AbsFunDecl a = null;
		AbsProcDecl b = null;
		if(decl instanceof AbsFunDecl) {
			a = (AbsFunDecl)decl;
			FrmDesc.setFrame(a, new FrmFrame(a, 1));
		} else if(decl instanceof AbsProcDecl) {
			b = (AbsProcDecl)decl;
			FrmDesc.setFrame(b, new FrmFrame(b, 1));
		}
	}
	
	public FrmEvaluator() {
		addFunFrame("putint");
		addFunFrame("putch");
		addFunFrame("getch");
		addFunFrame("getint");
		addFunFrame("ord");
		addFunFrame("chr");
		addFunFrame("free");
	}
	
	public void visit(AbsProgram acceptor) {
		FrmFrame frame = new FrmFrame(acceptor,0);
		
		for (AbsDecl decl : acceptor.decls.decls) {
			if (decl instanceof AbsVarDecl) {
				AbsVarDecl varDecl = (AbsVarDecl)decl;
				FrmVarAccess access = new FrmVarAccess(varDecl);
				FrmDesc.setAccess(varDecl, access);
			}
			decl.accept(this);
		}
		
		sArg = 0;
		acceptor.stmt.accept(this);
		frame.sizeArgs = sArg;
		FrmDesc.setFrame(acceptor, frame);
	}
	
	
	public void visit(AbsAtomType acceptor) {
	}
	
	public void visit(AbsConstDecl acceptor) {
	}
	
	public void visit(AbsFunDecl acceptor) {
		FrmFrame frame = new FrmFrame(acceptor, SemDesc.getScope(acceptor));
		
		for (AbsDecl decl : acceptor.pars.decls) {
			if (decl instanceof AbsVarDecl) {
				AbsVarDecl varDecl = (AbsVarDecl)decl;
				FrmArgAccess access = new FrmArgAccess(varDecl, frame);
				FrmDesc.setAccess(varDecl, access);
			} 	
			decl.accept(this);
		}
		
		for (AbsDecl decl : acceptor.decls.decls) {
			if (decl instanceof AbsVarDecl) {
				AbsVarDecl varDecl = (AbsVarDecl)decl;
				FrmLocAccess access = new FrmLocAccess(varDecl, frame);
				// add local variables to frame
				frame.locVars.add(access);
				FrmDesc.setAccess(varDecl, access);
			}
			decl.accept(this);
		}
		
		sArg = 0;
		acceptor.stmt.accept(this);
		frame.sizeArgs = sArg;	
		FrmDesc.setFrame(acceptor, frame);
	}
	
	public void visit(AbsProcDecl acceptor) {
		FrmFrame frame = new FrmFrame(acceptor, SemDesc.getScope(acceptor));
		for (AbsDecl decl : acceptor.pars.decls) {
			if (decl instanceof AbsVarDecl) {
				AbsVarDecl varDecl = (AbsVarDecl)decl;
				FrmArgAccess access = new FrmArgAccess(varDecl, frame);
				FrmDesc.setAccess(varDecl, access);
			}
		}
		

		for (AbsDecl decl : acceptor.decls.decls) {
			if (decl instanceof AbsVarDecl) {
				AbsVarDecl varDecl = (AbsVarDecl)decl;
				if(SemDesc.getActualType(varDecl) == null)
					Report.warning("No type");
				FrmLocAccess access = new FrmLocAccess(varDecl, frame);
				// add local variables into frame
				frame.locVars.add(access);
				FrmDesc.setAccess(varDecl, access);
			}
			decl.accept(this);
		}
		
		sArg = 0;
		acceptor.stmt.accept(this);
		frame.sizeArgs = sArg;
		FrmDesc.setFrame(acceptor, frame);
		
	}

	public void visit(AbsRecordType acceptor) {
		int offset = 0;
		for (AbsDecl decl : acceptor.fields.decls)
			if (decl instanceof AbsVarDecl) {
				AbsVarDecl varDecl = (AbsVarDecl)decl;
				FrmCmpAccess access = new FrmCmpAccess(varDecl, offset);
				FrmDesc.setAccess(varDecl, access);
				offset = offset + SemDesc.getActualType(varDecl.type).size();
			}
	}
	
	public void visit(AbsTypeDecl acceptor) {
		acceptor.type.accept(this);
	}
	
	public void visit(AbsBlockStmt acceptor) {
		acceptor.stmts.accept(this);
	}
	
	public void visit(AbsStmts acceptor) {
		for(AbsStmt s: acceptor.stmts) {
			s.accept(this);
		}
	}
	public void visit(AbsExprStmt acceptor) {
		acceptor.expr.accept(this);
	}
	public void visit(AbsForStmt acceptor) {
		acceptor.hiBound.accept(this);
		acceptor.loBound.accept(this);
		acceptor.stmt.accept(this);
	}
	public void visit(AbsIfStmt acceptor) {
		acceptor.cond.accept(this);
		acceptor.thenStmt.accept(this);
		acceptor.elseStmt.accept(this);
	}
	public void visit(AbsWhileStmt acceptor) {
		acceptor.cond.accept(this);
		acceptor.stmt.accept(this);
	}
	public void visit(AbsVarDecl acceptor) {
		acceptor.type.accept(this);
	}
	
	public void visit (AbsValExprs acceptor) {
		for (AbsValExpr expr : acceptor.exprs) {
			expr.accept(this);
		}
	}
	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);
	}
	public void visit(AbsValName acceptor) {
		
	}
	public void visit(AbsBinExpr acceptor) {
		acceptor.fstExpr.accept(this);
		acceptor.sndExpr.accept(this);
	}
	public void visit(AbsAssignStmt acceptor) {
		acceptor.dstExpr.accept(this);
		acceptor.srcExpr.accept(this);
	}
	public void visit(AbsAlloc acceptor) {
		acceptor.type.accept(this);
	}
	public void visit(AbsPointerType acceptor) {
		acceptor.type.accept(this);
	}
	public void visit(AbsArrayType acceptor) {
		acceptor.type.accept(this);
	}
	public void visit(AbsAtomConst acceptor) {
	}

	public void visit(AbsCallExpr acceptor) {
		int size = 0;
		for (AbsValExpr e : acceptor.args.exprs) {
			SemType st = SemDesc.getActualType(e);
			size += st.size();
		}
		size = size + 4;
		if (size > sArg) {
			sArg = size;
		}
		
	}

	public void visit(AbsDeclName acceptor) {
	}

	public void visit(AbsDecls acceptor) {
		return;
		
		/*
		for(AbsDecl d : acceptor.decls) {
			d.accept(this);
		}*/
	}

	public void visit(AbsNilConst acceptor) {
	}

	public void visit(AbsTypeName acceptor) {
	}

	@Override
	public void visit(QMarkStmt qMarkStmt) {
		// TODO Auto-generated method stub
		
	}
}
