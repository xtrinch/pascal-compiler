program StudentDB;

type
    studInfo = record
        name_f:   array[1 .. 1024] of char;
        name_l:   array[1 .. 1024] of char;
        address:  array[1 .. 1024] of char;
        comments: array[1 .. 1024] of char;
        ID:       integer;
        positive: boolean
    end;

    epic = record
        stu: studInfo;
        ID:  integer
    end;

var 
    studentName_f : array[1 .. 1024] of char;
    studentName_l : array[1 .. 1024] of char;
    address:        array[1 .. 1024] of char;
    comments:       array[1 .. 1024] of char;
    ID:             integer;        
    studetnStruct: studInfo;
    e:              epic;
procedure SetName(c: array[1 .. 1024] of char);
const
    lower = 1;
    upper = 1024;
var
    i:integer;
    b:boolean;
begin
    while lower < upper do
    begin
        studetnStruct.ID := i;
        e.stu.ID := i;
        studetnStruct.positive := true
        {{lower := lower + 1}}
    end
end;

begin
    SetName(studentName_f)
end.