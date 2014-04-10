{ Test program }
program TestProgram;

{ Main function }
begin
  v3 := [mystruct].mem4[2];
  v5 := [array [3..6] of ^mystruct];
  v4 := [array [3..6] of ^mystruct][3 * 1]
end.
