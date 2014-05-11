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
import compiler.frames.FrmAccess;
import compiler.frames.FrmDesc;
import compiler.frames.FrmFrame;
import compiler.frames.FrmLabel;
import compiler.frames.FrmVarAccess;
import compiler.semanal.SemDesc;
import compiler.semanal.type.SemType;

public class IMCodeGenerator implements AbsVisitor {

	public LinkedList<ImcChunk> chunks = new LinkedList<ImcChunk>();
	public ImcCode code;
	public FrmFrame currFrame;
	
	@Override
	public void visit(AbsProgram acceptor) {
		FrmFrame frame = FrmDesc.getFrame(acceptor);
		
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
		acceptor.dstExpr.accept(this);
		ImcExpr e1 = (ImcExpr) code;
		acceptor.srcExpr.accept(this);
		ImcExpr e2 = (ImcExpr) code;
		ImcMOVE s = new ImcMOVE(e1, e2);
		
		code = s;
	}
	
	@Override
	public void visit(AbsValExprs acceptor) {
		System.out.println("AbsValExprs");
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
		
		System.out.println("AbsAtomConst");
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
		System.out.println("AbsValName");
		
		if(access instanceof FrmVarAccess) {
			// globalne
			label = ((FrmVarAccess)access).label;
			code = new ImcMEM(new ImcNAME(label));
		} else if(access == null) {
			Integer val = SemDesc.getActualConst(decl);
			code = new ImcCONST(val);
		} // TODO else if
	}
	
	@Override
	public void visit(AbsWhileStmt acceptor) {
		System.out.println("AbsWhileStmt");
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
		System.out.println("AbsForStmt");
		ImcSEQ seq = new ImcSEQ();
		ImcLABEL trueLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL falseLabel = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL startFor = new ImcLABEL(FrmLabel.newLabel());
		
		// assign the iterator variable to its starting value
		acceptor.name.accept(this);
		ImcExpr name = (ImcExpr) code;
		acceptor.loBound.accept(this);
		ImcExpr loBound = (ImcExpr) code;
		seq.stmts.add(new ImcMOVE(name, loBound));
		
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
		code = new ImcMOVE(name, new ImcBINOP(ImcBINOP.ADD, name, new ImcCONST(1)));
		seq.stmts.add((ImcStmt)code);
		
		// jump to the start of the for statement
		seq.stmts.add(new ImcJUMP(startFor.label));
		
		// false label, skip the statement
		seq.stmts.add(falseLabel);
		
		code = seq;
	}
	
	@Override
	public void visit(AbsBinExpr acceptor) {
		
		// TODO ARRACCES N SHIT
		
		acceptor.fstExpr.accept(this);
		ImcExpr e1 = (ImcExpr) code;
		acceptor.sndExpr.accept(this);
		ImcExpr e2 = (ImcExpr) code;
		code = new ImcBINOP(acceptor.oper, e1, e2);
	}
	
	@Override
	public void visit(AbsUnExpr acceptor) {
		System.out.println("ABsUnExpr");
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
		System.out.println("AbsProcDecl");
		FrmFrame frame = FrmDesc.getFrame(acceptor);
		this.currFrame = frame;
		acceptor.decls.accept(this);
		acceptor.stmt.accept(this);
		// dodamo kodo procedure v kose kode
		this.chunks.add(new ImcCodeChunk(currFrame, (ImcStmt)code));
	}
	
	@Override
	public void visit(AbsFunDecl acceptor) {
		System.out.println("AbsFunDecl");
		FrmFrame frame = FrmDesc.getFrame(acceptor);
		this.currFrame = frame;
		acceptor.decls.accept(this);
		acceptor.stmt.accept(this);
		// dodamo kodo procedure v kose kode
		this.chunks.add(new ImcCodeChunk(currFrame, (ImcStmt)code));
	}
	
	@Override
	public void visit(AbsExprStmt acceptor) {
		System.out.println("AbsExprStmt");
		acceptor.expr.accept(this);
		code = new ImcEXP((ImcExpr)code);
	}
	
	@Override
	public void visit(AbsCallExpr acceptor) {
		System.out.println("AbsCallExpr");
		FrmFrame callFrame = FrmDesc.getFrame(SemDesc.getNameDecl(acceptor.name));
		ImcCALL call = new ImcCALL(callFrame.label);
		// into argument space we add the static link and the arguments
		ImcExpr staticLink = new ImcTEMP(currFrame.FP);
		for(int i = currFrame.level; i >= callFrame.level; i--) {
			staticLink = new ImcMEM(staticLink);
		}
		
		call.args.add(staticLink);
		
		for(AbsValExpr e: acceptor.args.exprs) {
			e.accept(this);
			call.args.add((ImcExpr) code);
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

}
