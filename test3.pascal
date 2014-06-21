program StaticLinks;
{3 :-) Done.}

procedure printInt(i:integer);
begin
    putint(i);
    putch(' ')
end;

procedure proc1(a: integer);
    procedure proc2(b: integer);
        procedure proc3();
        begin
            printInt(a+b)
        end;
    begin
        proc3()
    end;
begin
    proc2(2)
end;

begin
    proc1(1)
end. 
