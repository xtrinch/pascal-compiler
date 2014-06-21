program Fibonacci;
{1 1 2 3 5 8 13 :-) Done.}

procedure printInt(i:integer);
begin
    putint(i);
    putch(' ')
end;

procedure fib(a:integer; b:integer; n:integer);
begin
    if n > 0 then
    begin
        printInt(a+b);
        fib(b, a+b, n-1)
    end
end;

begin
    printInt(1);
    printInt(1);
    fib(1,1,5)
end. 
