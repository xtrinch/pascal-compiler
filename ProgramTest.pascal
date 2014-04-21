program HelloWorld;
const
  a = 3;
  b = a + 2;

type
  ta = integer;
  tb = ta;
  tc = ^ta;
  td = array [a .. b] of tc;
  te = record
         el1: char;
         el2: boolean;
         el3: td
       end;
  tf = array [0 .. 4] of record el1: boolean; el2: ta end;
  tg = (^ta);

begin
end. 
