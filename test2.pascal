{if all goes well, the program should output 'abc abc'}

program g05;
const
  STRLEN = 20;

type
  string = array [0 .. STRLEN] of char;
  book = record
           marks: array [0 .. 2] of integer;
           title: string
         end;
           
var
  nl: char;
  eos: char;

{ Put line to stdout }
procedure put_string (str: ^string);
var
  i: integer;

begin
  i := 0;
  while str^[i] <> eos do
  begin
    putch (str^[i]);
    i := i + 1
  end;
  putch (nl)
end;

procedure test ();
var
  x: array [0 .. 9] of ^book;
  y: ^book;
  z: ^string;
  t: char;

begin
  y := [book];
  y^.title[0] := 'a';
  y^.title[1] := 'b';
  y^.title[2] := 'c';
  y^.title[3] := eos;
  put_string (^(y^.title));

  x[3] := y;
  put_string (^(x[3]^.title))

{  t := y^.title[3];
  z := ^(y^.title);}
end;

begin
  nl := chr (10);
  eos := chr (0);

  test ()
end.
