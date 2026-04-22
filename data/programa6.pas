Program TesteRepeat;
Var x : Integer;
Begin
x := 1;
repeat
writeln(x);
x := x + 1;
until (x > 10);
End.