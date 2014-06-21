package compiler.imcode;

import java.util.LinkedList;

import compiler.abstree.AbsVisitor;
import compiler.abstree.tree.AbsAlloc;
import compiler.abstree.tree.AbsArrayType;
import compiler.abstree.tree.AbsAssignStmt;
import compiler.abstree.tree.AbsAtomConst;
import compiler.abstree.tree.AbsAtomType;
import compiler.abstree.tree.AbsBinExpr;
import compiler.abstree.tree.AbsBlockStmt;
import compiler.abstree.tree.AbsCallExpr;
import compiler.abstree.tree.AbsConstDecl;
import compiler.abstree.tree.AbsConstExpr;
import compiler.abstree.tree.AbsDecl;
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
import compiler.abstree.tree.AbsStmt;
import compiler.abstree.tree.AbsStmts;
import compiler.abstree.tree.AbsTypeDecl;
import compiler.abstree.tree.AbsTypeName;
import compiler.abstree.tree.AbsUnExpr;
import compiler.abstree.tree.AbsValExpr;
import compiler.abstree.tree.AbsValExprs;
import compiler.abstree.tree.AbsValName;
import compiler.abstree.tree.AbsVarDecl;
import compiler.abstree.tree.AbsWhileStmt;
import compiler.abstree.tree.QMarkStmt;
import compiler.abstree.tree.VisibilityType;
import compiler.frames.FrmAccess;
import compiler.frames.FrmArgAccess;
import compiler.frames.FrmDesc;
import compiler.frames.FrmFrame;
import compiler.frames.FrmLabel;
import compiler.frames.FrmLocAccess;
import compiler.frames.FrmVarAccess;
import compiler.report.Report;
import compiler.semanal.SemDesc;
import compiler.semanal.type.SemArrayType;
import compiler.semanal.type.SemRecordType;
import compiler.semanal.type.SemType;

public class IMCodeGenerator implements AbsVisitor {

	public LinkedList<ImcChunk> chunks = new LinkedList<ImcChunk>();
	public ImcCode code;
	public FrmFrame currFrame;
	
	@Override
	public void visit(AbsProgram acceptor) {
		
		// dodamo globalne spremenljivke kot ImcDataChunk
		for(AbsDecl decl : acceptor.decls.decls) {
			if(decl instanceof AbsVarDecl) {
				FrmVarAccess access = (FrmVarAccess) FrmDesc.getAccess((AbsVarDecl)decl);
				SemType type = SemDesc.getActualType(decl);
				ImcDataChunk dataChunk = new ImcDataChunk(access.label, type.size());
				this.chunks.add(dataChunk);
			}
		}
		acceptor.decls.accept(this);
		
		
		FrmFrame frame = FrmDesc.getFrame(acceptor);
	    this.currFrame = frame;
	    
	    acceptor.stmt.accept(this);
	    
		ImcCodeChunk chunk = new ImcCodeChunk(frame, (ImcStmt) code);
		this.chunks.add(chunk);
	}
	
	@Override
	public void visit(AbsBlockStmt acceptor) {
		acceptor.stmts.accept(this);
	}
	
	@Override
	public void visit(AbsStmts acceptor) {	
		/**
		 * Stavki.
		 * 
		 * @see AbsAssignStmt
		 * @see AbsBeginStmt
		 * @see AbsExprStmt
		 * @see AbsForStmt
		 * @see AbsIfStmt
		 * @see AbsWhileStmt
		 */
		
		ImcSEQ sts = new ImcSEQ();
		for(AbsStmt s : acceptor.stmts) {
			s.accept(this);
			sts.stmts.add((ImcStmt) code);
		}
		code = sts;
	}

	@Override
	public void visit(AbsAssignStmt acceptor) {
		// AbsValExpr
		acceptor.dstExpr.accept(this);
		ImcExpr e1 = null;
		
		// v primeru da prirejamo return value funkciji
		if(SemDesc.getNameDecl(acceptor.dstExpr) instanceof AbsFunDecl) {
			if(this.currFrame.RV == null)	System.out.println("this.currframe.rv is null" +this.currFrame.label.name());
			if(this.currFrame == null)	System.out.println("entire frame is null");
			e1 = new ImcTEMP(this.currFrame.RV);
			
		} else {
			e1 = (ImcExpr) code;
		}
		acceptor.srcExpr.accept(this);
		ImcExpr e2 = (ImcExpr) code;

		/*if(FrmDesc.getAccess(acceptor.srcExpr)) {
			
			
		}*/
		
		ImcMOVE s = null;
		
		if(SemDesc.getNameDecl(acceptor.dstExpr) instanceof AbsFunDecl) {
			s = new ImcMOVE(e1, e2, new VisibilityType(0));
		} else if(SemDesc.getNameDecl(acceptor.dstExpr) instanceof AbsVarDecl) {
			s = new ImcMOVE(e1, e2, ((AbsVarDecl)SemDesc.getNameDecl(acceptor.dstExpr)).visType);
		} else {
			// records i suppose
			s = new ImcMOVE(e1, e2, new VisibilityType(0));
			
		}
		
		code = s;
	}
	
	@Override
	public void visit(AbsValExprs acceptor) {
		/**
		 * Izrazi za opis vrednosti.
		 * 
		 * @see AbsConstExpr (AbsAtomConst, AbsNilConst)
		 * @see AbsUnExpr
		 * @see AbsBinExpr
		 * @see AbsValName
		 * @see AbsCallExpr
		 * @see AbsAlloc
		 */
		for (AbsValExpr expr : acceptor.exprs) {
			expr.accept(this);
		}
	}
	
	@Override
	public void visit(AbsAtomConst acceptor) {
		switch(acceptor.type) {
		case AbsAtomConst.INT:
			code = new ImcCONST(Integer.parseInt(acceptor.value));
			break;
		case AbsAtomConst.CHAR:
			code = new ImcCONST((int)acceptor.value.charAt(1));
			break;
		case AbsAtomConst.BOOL:
			code = new ImcCONST(acceptor.value.equals("true") ? 1 : 0);
			break;
		}
	}
	
	@Override
	public void visit(AbsValName acceptor) {
		AbsDecl decl = SemDesc.getNameDecl(acceptor);
		FrmAccess access = FrmDesc.getAccess(decl);
		FrmLabel label;
		
		if(access instanceof FrmVarAccess) {
			// globalne
			label = ((FrmVarAccess)access).label;
			code = new ImcMEM(new ImcNAME(label));
		} else if(access == null) {
			Integer val = SemDesc.getActualConst(decl);
			if(val != null)
				code = new ImcCONST(val);
		} else if(access instanceof FrmLocAccess) {
			FrmLocAccess loc = (FrmLocAccess) access;
			
			if(decl instanceof AbsVarDecl) {
				decl = (AbsVarDecl) decl;
				// attempt at PRIVATE DECLARATION
				if (((AbsVarDecl) decl).visType.type == VisibilityType.PRIVATE && loc.frame.level != currFrame.level) {
					Report.error("Fatal error: you tried accessing \""+((AbsVarDecl)decl).name.name+"\", declared as private, outside its visibility scope! \n(Private variables are not visible in nested functions and procedures)", acceptor.begLine, acceptor.endColumn, 1);
				}
			}
			
			ImcExpr ex = new ImcTEMP(currFrame.FP);
			for(int i = currFrame.level - loc.frame.level; i > 0; i--) {	
				ex = new ImcMEM(ex);
			}
			ex = new ImcBINOP(ImcBINOP.ADD, ex, new ImcCONST(loc.offset));
			code = new ImcMEM(ex);
			
		} else if(access instanceof FrmArgAccess) {
			/// AAA
			FrmArgAccess acc = (FrmArgAccess) access;
			ImcExpr e = new ImcTEMP(currFrame.FP);
			
			for(int i = currFrame.level - acc.frame.level; i > 0; i--) {	
				e = new ImcMEM(e);
			}
			
			// naslov
			e = new ImcBINOP(ImcBINOP.ADD, e,new ImcCONST(acc.offset));
			e = new ImcMEM(e);
			code = e;
		}
	}
	
	@Override
	public void visit(AbsWhileStmt acceptor) {
		ImcSEQ seq = new ImcSEQ();
		ImcLABEL trueLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL falseLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL startWhile = new ImcLABEL(FrmLabel.newLabel());
		
		acceptor.cond.accept(this);
		seq.stmts.add(startWhile);
		code = new ImcCJUMP((ImcExpr) code, trueLabel.label, falseLabel.label);
		seq.stmts.add((ImcStmt) code);
		seq.stmts.add(trueLabel);
		
		acceptor.stmt.accept(this);
		seq.stmts.add((ImcStmt) code);
		
		seq.stmts.add(new ImcJUMP(startWhile.label));
		seq.stmts.add(falseLabel);
		
		code = seq;
	}
	
	
	@Override
	public void visit(AbsForStmt acceptor) {
		ImcSEQ seq = new ImcSEQ();
		ImcLABEL trueLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL falseLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL startFor = new ImcLABEL(FrmLabel.newLabel());
		
		// assign the iterator variable to its starting value
		acceptor.name.accept(this);
		ImcExpr name = (ImcExpr) code;
		acceptor.loBound.accept(this);
		ImcExpr loBound = (ImcExpr) code;
		seq.stmts.add(new ImcMOVE(name, loBound, new VisibilityType(0)));
		
		acceptor.hiBound.accept(this);
		ImcExpr hiBound = (ImcExpr) code;
		
		// add label for the start of the entire for statement
		seq.stmts.add(startFor);
		
		// check the condition
		code = new ImcCJUMP((ImcExpr) new ImcBINOP(ImcBINOP.LEQ, name, hiBound), trueLabel.label, falseLabel.label);
		seq.stmts.add((ImcStmt)code);
		seq.stmts.add(trueLabel);
		
		acceptor.stmt.accept(this);
		seq.stmts.add((ImcStmt)code);
		
		// increment iterator variable
		code = new ImcMOVE(name, new ImcBINOP(ImcBINOP.ADD, name, new ImcCONST(1)), new VisibilityType(0));
		seq.stmts.add((ImcStmt)code);
		
		// jump to the start of the for statement
		seq.stmts.add(new ImcJUMP(startFor.label));
		
		// false label, skip the statement
		seq.stmts.add(falseLabel);
		
		code = seq;
	}
	
	@Override
	public void visit(AbsBinExpr acceptor) {
		
		acceptor.fstExpr.accept(this);
		ImcExpr e1 = (ImcExpr) code;
		acceptor.sndExpr.accept(this);
		ImcExpr e2 = null;
		if(code instanceof ImcExpr)
			e2 = (ImcExpr) code;
		
		if(acceptor.oper == AbsBinExpr.ARRACCESS) {
			SemArrayType type = (SemArrayType) SemDesc.getActualType(acceptor.fstExpr);
			int size = type.type.size();
			// get actual index
			code = new ImcBINOP(ImcBINOP.SUB, e2, new ImcCONST(type.loBound));
			// get actual offset
			code = new ImcBINOP(ImcBINOP.MUL, (ImcExpr)code, new ImcCONST(size));
			// get actual memory address (e1 imcMem iz absvalname) ?
			code = new ImcBINOP(ImcBINOP.ADD, (ImcExpr)code, ((ImcMEM)e1).expr);
			code = new ImcMEM((ImcExpr)code);
		} else if(acceptor.oper == AbsBinExpr.RECACCESS) {
			SemRecordType rec = (SemRecordType) SemDesc.getActualType(acceptor.fstExpr);
			String name = ((AbsValName)acceptor.sndExpr).name;
			int offset = 0;
			for(int i=0; i < rec.getNumFields(); i++) {
				if(rec.getFieldName(i).name.equals(name)) {
					
					// field address
					code = new ImcBINOP(ImcBINOP.ADD, ((ImcMEM)e1).expr, new ImcCONST(offset));
					break;
				}
				offset += rec.getFieldType(i).size();
			}
			code = new ImcMEM((ImcExpr)code);
			
		} else {
			code = new ImcBINOP(acceptor.oper, e1, e2);
		}
	}
	
	@Override
	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);
		
		switch(acceptor.oper) {
		case AbsUnExpr.ADD:
			code = new ImcBINOP(ImcBINOP.ADD, new ImcCONST(0), (ImcExpr) code);
			break;
		case AbsUnExpr.MEM:
			code = ((ImcMEM)code).expr;
			break;
		case AbsUnExpr.NOT:
			code = new ImcBINOP(ImcBINOP.EQU, new ImcCONST(0), (ImcExpr) code);
			break;
		case AbsUnExpr.SUB:
			code = new ImcBINOP(ImcBINOP.SUB, new ImcCONST(0), (ImcExpr) code);
			break;
		case AbsUnExpr.VAL:
			code = new ImcMEM((ImcExpr)code);
			break;		
		}
	}
	
	
	@Override
	public void visit(AbsIfStmt acceptor) {
		ImcSEQ seq = new ImcSEQ();
		
		ImcLABEL thenLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL elseLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL endLabel = new ImcLABEL(FrmLabel.newLabel());
		acceptor.cond.accept(this);
		// NEED ABS BIN EXPR
		ImcExpr condExpr = (ImcExpr)code;
		
		code = new ImcCJUMP(condExpr, thenLabel.label, elseLabel.label);
		seq.stmts.add((ImcStmt)code);
		
		seq.stmts.add(thenLabel);
		acceptor.thenStmt.accept(this);
		seq.stmts.add((ImcStmt)code);
		seq.stmts.add(new ImcJUMP(endLabel.label));
		
		seq.stmts.add(elseLabel);
		acceptor.elseStmt.accept(this);
		seq.stmts.add((ImcStmt)code);
		seq.stmts.add(endLabel);
		
		code = seq;
	}
	
	@Override
	public void visit(AbsDecls acceptor) {
		for(AbsDecl decl : acceptor.decls) {
			if(decl instanceof AbsProcDecl || decl instanceof AbsFunDecl) {
				decl.accept(this);
			}
		}
	}
	
	@Override
	public void visit(AbsProcDecl acceptor) {
		
		acceptor.decls.accept(this);
		
		FrmFrame frame = FrmDesc.getFrame(acceptor);
		this.currFrame = frame;
		//PUT DECLS BEFORE CURR FRAME
		
		
		acceptor.stmt.accept(this);
		this.chunks.add(new ImcCodeChunk(frame, (ImcStmt)code));
		
	}
	
	@Override
	public void visit(AbsFunDecl acceptor) {

		FrmFrame frame = FrmDesc.getFrame(acceptor);
		this.currFrame = frame;
		acceptor.stmt.accept(this);
		acceptor.decls.accept(this);
		
		// dodamo kodo procedure v kose kode
		this.chunks.add(new ImcCodeChunk(frame, (ImcStmt)code));
	}
	
	@Override
	public void visit(AbsExprStmt acceptor) {
		acceptor.expr.accept(this);
		code = new ImcEXP((ImcExpr)code);
	}
	
	@Override
	public void visit(AbsCallExpr acceptor) {
		FrmFrame declFrame = FrmDesc.getFrame(SemDesc.getNameDecl(acceptor.name));
		ImcCALL call = new ImcCALL(declFrame.label);
		// into argument space we add the static link and the arguments
		ImcExpr staticLink = new ImcTEMP(currFrame.FP);
		for(int i = currFrame.level; i >= declFrame.level; i--) {
			staticLink = new ImcMEM(staticLink);
		}
		
		call.args.add(staticLink);
		call.size.add(4);
		for(AbsValExpr e: acceptor.args.exprs) {
			e.accept(this);
			call.args.add((ImcExpr) code);
			// naceloma 4
			call.size.add(SemDesc.getActualType(e).size());
		}
		
		code = call;
	}
	
	@Override
	public void visit(AbsAlloc acceptor) {
		// dodeljevanje pomnilnika
		// Dodeljevanje pomnilnika z izrazom oblike [<type>] prevedete v klic funkcije malloc(<typesize>), 
		// kjer je <typesize>velikost posameznega podatka tipa <type> (v bytih)
		SemType type = SemDesc.getActualType(acceptor);
		int size = type.size();
		
		ImcCALL call = new ImcCALL(FrmLabel.newLabel("malloc"));
		// static link
		call.args.add(new ImcCONST(0)); // null
		call.args.add(new ImcCONST(size));
		
		call.size.add(4);
		call.size.add(4);
		code = call;
		
	}
	
	@Override
	public void visit(AbsNilConst acceptor) {
		code = new ImcCONST(0);
	}

	@Override
	public void visit(AbsArrayType acceptor) {
		// empty
	}

	@Override
	public void visit(AbsAtomType acceptor) {
		// empty
	}
	
	@Override
	public void visit(AbsTypeDecl acceptor) {
		// empty
	}

	@Override
	public void visit(AbsConstDecl acceptor) {
		// empty
	}

	@Override
	public void visit(AbsDeclName acceptor) {
		// empty
	}

	@Override
	public void visit(AbsPointerType acceptor) {
		// empty
	}
	
	@Override
	public void visit(AbsVarDecl acceptor) {
		// empty
	}

	@Override
	public void visit(AbsRecordType acceptor) {
		// empty
	}

	@Override
	public void visit(AbsTypeName acceptor) {
		// empty
	}

	@Override
	public void visit(QMarkStmt acceptor) {
		ImcSEQ seq = new ImcSEQ();
		
		ImcLABEL thenLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL elseLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL endLabel = new ImcLABEL(FrmLabel.newLabel());
		acceptor.cond.accept(this);
		// NEED ABS BIN EXPR
		ImcExpr condExpr = (ImcExpr)code;
		
		code = new ImcCJUMP(condExpr, thenLabel.label, elseLabel.label);
		seq.stmts.add((ImcStmt)code);
		
		seq.stmts.add(thenLabel);
		acceptor.stmt1.accept(this);
		seq.stmts.add((ImcStmt)code);
		seq.stmts.add(new ImcJUMP(endLabel.label));
		
		seq.stmts.add(elseLabel);
		acceptor.stmt2.accept(this);
		seq.stmts.add((ImcStmt)code);
		seq.stmts.add(endLabel);
		
		code = seq;
	}

}
