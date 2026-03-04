package compilador;

import compilador.lexico.ClasseToken;
import compilador.lexico.Lexico;
import compilador.lexico.Token;

public class App {
    public static void main(String[] args) throws Exception {
        Lexico lexico = new Lexico("teste.pas");
        Token token = lexico.getNexToken();

        while (token.getClasse() != ClasseToken.EOF) {
            System.out.println(token);
            token = lexico.getNexToken();
        }
        System.out.println(token);
    }
}
