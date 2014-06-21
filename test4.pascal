{ outputs 5 }
program HelloWorld;

var
    i:integer;

procedure p(i:integer);
begin
    putint(i)
end;

function f(i: integer):integer;
begin
    f := i+1
end;

begin
    i := 2+2; 
    p(f(i))
end. 
