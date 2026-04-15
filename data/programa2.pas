{ Programa que nao faz nada }
Program MuitoDoido;
Var TempoEmAnos, ValorSalario, x, y, z : Integer;
a, b, c : Integer; 
Begin 
    If (TempoEmAnos > 10) Then
    Begin
        ValorSalario := 100; 
    End
    Else Begin
        ValorSalario := ValorSalario * 2;
    End;
    Write(ValorSalario);
    x := 10;
    write(x);
    x := 2 + 5 * 3;
    write('Valor: ');
    write(x);
    read(x);
    read(y);
    z := x * y;
    write('Resultado: ');
    write(z);
    read(x);
    read(y);
    if (x >= 10) then
    begin
        write(x);
    end
    else
    begin
        write(y);
    end;
    c := 1;
    while (c <= 10) do
    begin
        write(c);
        c := c + 1;
    end;
    c := 1;
    repeat
        write(c);
        c := c + 1;
    until (c > 10);
    for c := 1 to 10 do
    begin
        write(c);
    end;
    write('Informe a: ');
    read(a);
    write('Informe b: ');
    read(b);
    if (a > 0 and b > 0) then  // para que isso
    begin
        writeln('Positivos');
    end
    else
    begin
        writeln('Um dos valores não é positivo');
    end;
end.