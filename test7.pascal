program ArraysAnd2DArrays;
{15 17 19 0 0 0 0 0 0 0   1 2 2 4 :-) Done.}

var
    i: integer;
    j: integer;
    a: array[1..10] of integer;
    b: array[1..2] of array[1..2] of integer;

procedure printInt(i:integer);
begin
    putint(i);
    putch(' ')
end;

begin
    a[1] := 15;
    a[2] := 17;
    a[3] := 19;

    for i:=1 to 10 do
        printInt(a[i]);
    putch(' '); putch(' ');
    
    for i:=1 to 2 do
        for j:=1 to 2 do
            b[i][j] := i*j;

    for i:=1 to 2 do
        for j:=1 to 2 do
            printInt(b[i][j])
end. 
