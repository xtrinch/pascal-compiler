package compiler.lexanal;

import java.io.*;

import compiler.report.*;
import compiler.synanal.*;

%%

%class      PascalLex
%public

%line
%column




/* Vzpostavimo zdruzljivost z orodjem Java Cup.
 * To bi lahko naredili tudi z ukazom %cup,
 * a v tem primeru ne bi mogli uporabiti razreda compiler.lexanal.PascalSym
 * namesto razreda java_cup.runtime.Symbol za opis osnovnih simbolov. */
%cupsym     compiler.synanal.PascalTok
%implements java_cup.runtime.Scanner
%function   next_token
%type       PascalSym
%eofval{
	if(bracketCount == 0)
    	return new PascalSym(PascalTok.EOF);
    else {
    	compiler.report.Report.error("You forgot to close some of your { brackets! Compiler error at line "+yyline+", column "+yycolumn+".", yyline, yycolumn, -1);
    }
%eofval}
%eofclose

%{
    private PascalSym sym(int type) {
    	String sequence = yytext();
    	/*if(type == PascalTok.CHAR_CONST) {
    		sequence = sequence.substring(1, sequence.length()-1);
    	}*/
    
    	if(sequence.equals("''''")) {
    		return new PascalSym(type, yyline + 1, yycolumn + 1, "'");
    	}
        return new PascalSym(type, yyline + 1, yycolumn + 1, sequence);
    }
%}



%{
int bracketCount = 0;
%}

%eof{
%eof}

%state COMMENT

%%


<COMMENT> {
	"}"								{ bracketCount --; 
									  if(bracketCount == 0) 
										yybegin(YYINITIAL); }	
	"{"								{ bracketCount++; }
	.								{}
	
	// whitespaces
	[ \n\t\r]+						{ }
	
}

<YYINITIAL> {


	"{"								{ bracketCount++; yybegin(COMMENT);  }
	"}"								{ compiler.report.Report.error("At least one too many } in your code!", yyline+1, yycolumn+1, -1); }
	
	// whitespaces
	[ \n\t\r]+						{ }
	
	// keywords
	"and"							{ return sym(PascalTok.AND); }
	"array"							{ return sym(PascalTok.ARRAY); }
	"begin"							{ return sym(PascalTok.BEGIN); }
	"const"							{ return sym(PascalTok.CONST); }
	"div"							{ return sym(PascalTok.DIV); }
	"do"							{ return sym(PascalTok.DO); }
	"end"							{ return sym(PascalTok.END); }
	"else"							{ return sym(PascalTok.ELSE); }
	"for"							{ return sym(PascalTok.FOR); }
	"function"						{ return sym(PascalTok.FUNCTION); }
	"if"							{ return sym(PascalTok.IF); }
	"nil"							{ return sym(PascalTok.NIL); }
	"not"							{ return sym(PascalTok.NOT); }
	"of"							{ return sym(PascalTok.OF); }
	"or"							{ return sym(PascalTok.OR); }
	"procedure"						{ return sym(PascalTok.PROCEDURE); }
	"program"						{ return sym(PascalTok.PROGRAM); }
	"record"						{ return sym(PascalTok.RECORD); }
	"then"							{ return sym(PascalTok.THEN); }
	"to"							{ return sym(PascalTok.TO); }
	"type"							{ return sym(PascalTok.TYPE); }
	"var"							{ return sym(PascalTok.VAR); }
	"while"							{ return sym(PascalTok.WHILE); }
	
	// names of atomic data structures
	"boolean"						{ return sym(PascalTok.BOOL); }
	"char"							{ return sym(PascalTok.CHAR); }
	"integer"						{ return sym(PascalTok.INT); }
	

		
	// constants of atomic data structures	
	"true"|"false"					{ return sym(PascalTok.BOOL_CONST); }
	"''''"							{ return sym(PascalTok.CHAR_CONST); }
	"'''"							{ compiler.report.Report.error("Invalid character. For ', use a sequence of four characters '.", yyline+1, yycolumn+1, -1); }
	['][\x20-\x26\x28-\x7E]?[']		{ return sym(PascalTok.CHAR_CONST); }
	['][^']*[']						{ compiler.report.Report.error("Invalid character definition.", yyline+1, yycolumn+1, -1); }
	[1-9][0-9]*						{ return sym(PascalTok.INT_CONST); }
	"0"								{ return sym(PascalTok.INT_CONST); }
	
	// names of programs, constants, types, variables and subprograms
	[A-Za-z_][A-Za-z_0-9]*			{ return sym(PascalTok.IDENTIFIER); }
	
	// other symbols
	":="							{ return sym(PascalTok.ASSIGN); }
	":"								{ return sym(PascalTok.COLON); }
	","								{ return sym(PascalTok.COMMA); }
	"."								{ return sym(PascalTok.DOT); }
	".."							{ return sym(PascalTok.DOTS); }
	"["								{ return sym(PascalTok.LBRACKET); }
	"("								{ return sym(PascalTok.LPARENTHESIS); }
	"]"								{ return sym(PascalTok.RBRACKET); }
	")"								{ return sym(PascalTok.RPARENTHESIS); }
	";"								{ return sym(PascalTok.SEMIC); }
	"+"								{ return sym(PascalTok.ADD); }
	"="								{ return sym(PascalTok.EQU); }
	">="							{ return sym(PascalTok.GEQ); }
	">"								{ return sym(PascalTok.GTH); }
	"<"								{ return sym(PascalTok.LTH); }
	"<="							{ return sym(PascalTok.LEQ); }
	"*"								{ return sym(PascalTok.MUL); }
	"<>"							{ return sym(PascalTok.NEQ); }
	"^"								{ return sym(PascalTok.PTR); }
	"-"								{ return sym(PascalTok.SUB); }

	.								{ compiler.report.Report.error("Character not recognizable as valid Pascal keyword, symbol, name, identifier or constant.", yyline+1, yycolumn+1, -1); }

}