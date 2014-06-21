{tests two different ways of calculating a factorial, should output '120 720'}

program HelloWorld;

var
	c: integer;
	i: integer;
	newLine: char;
 
 function factorialExit(r: integer):integer;
	begin
		if r = 1 then
			factorialExit := 1
		else
			factorialExit := r * factorialExit(r - 1)
	end;
 
 function factorial(z: integer):integer;
	var
		temp: integer;
		
	begin    
		temp := 1;
		for i := 1 to z do
			begin
				temp := temp * i
			end;
		factorial := temp
	end;
 
begin
	c := 5;
	i := factorial(c);
	putint(i);
	putch(' ');
	c := 6;
	i:= factorialExit(c);
	putint(i);
	newLine := chr(10);
	putch(newLine)
end.