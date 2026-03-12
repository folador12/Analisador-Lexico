{ Comentario em bloco com chaves - linha unica }

{ Comentario em bloco com chaves
  ocupando multiplas linhas }

(* Comentario em bloco com parenteses e asterisco - linha unica *)

(* Comentario em bloco com parenteses e asterisco
   ocupando multiplas linhas *)

// Comentario de linha - ignorado ate o fim da linha

Program TesteLexico;

Var
    a, b, resultado : Integer;

Begin

    { Teste: atribuicao := e literais inteiros }
    a := 42;
    b := 100;

    { Teste: operadores aritmeticos + - * / }
    resultado := a + b;
    resultado := a - b;
    resultado := a * b;
    resultado := a / b;

    { Teste: operadores relacionais }
    If a = b Then resultado := 0;    { igualdade = }
    If a > b Then resultado := 0;    { maior > }
    If a >= b Then resultado := 0;   { maior ou igual >= }
    If a < b Then resultado := 0;    { menor < }
    If a <= b Then resultado := 0;   { menor ou igual <= }
    If a <> b Then resultado := 0;   { diferente <> }

    { Teste: dois pontos sozinho na declaracao (coberto no Var acima) }

    { Teste: string com aspas simples }
    Write('Hello, World!');
    Write('Outra string');
    Write('String com   espacos e 123 numeros');

    { Teste: palavras reservadas - while do }
    a := 1;
    While (a <= 5) Do
    Begin
        Write(a);
        a := a + 1;
    End;

    { Teste: palavras reservadas - repeat until }
    b := 1;
    Repeat
        Write(b);
        b := b + 1;
    Until (b > 5);

    { Teste: palavras reservadas - for to downto do }
    For a := 1 To 10 Do
        Write(a);

    For a := 10 Downto 1 Do
        Write(a);

    { Teste: palavras reservadas - if then else }
    If (a > 0) Then
    Begin
        Write(a);
    End
    Else
    Begin
        Write(b);
    End;

    { Teste: operadores logicos and or not }
    If (a > 0) And (b > 0) Then resultado := 1;
    If (a > 0) Or (b > 0) Then resultado := 2;
    If Not (a > 0) Then resultado := 3;

    { Teste: virgula , ponto-e-virgula ; parenteses ( ) }
    Read(a);
    Read(b);
    Writeln(a);

    { Teste: comentario de linha no meio do codigo }
    resultado := a + b; // esta parte e ignorada pelo lexico

    (* Teste: comentario em bloco multilinhas
       que ocupa varias linhas
       e so termina aqui *)

    { Teste: case of - palavras reservadas }
    Case a Of
        1 : Write(a);
        2 : Write(b);
    End;

    { Teste: const type array function procedure label record }
    { Teste: exit break continue }
    { (apenas para garantir reconhecimento como PalavraReservada) }

End.
