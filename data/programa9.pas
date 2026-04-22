Program Fibonacci;
Var termo1, termo2, aux, cont, quantos : Integer;
Begin
writeln('----------FIBONACCI------------');
termo1 := 1;
termo2 := 1;
quantos := 0;
aux := 0;
write('Informe a quantidade de numeros que deseja ver da sequencia fibonacci: ');
read(quantos);
writeln(termo1);
writeln(termo2);
cont := 2;
while (cont < quantos) do
begin
aux := termo1 + termo2;
writeln(aux);
termo1 := termo2;
termo2 := aux;
cont := cont + 1;
end;
End.