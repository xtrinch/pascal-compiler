program Loops;
{0 1 2 3 4 5 6 7 8 9 20 21 22 :-) Done.}

var
    i:integer;

procedure printInt(i:integer);
begin
    putint(i);
    putch(' ')
end;

begin
    i := 0;
    while i < 10 do
    begin
        printInt(i);
        i := i+1
    end;
    for i := 20 to 22 do
        printInt(i)
end. 
