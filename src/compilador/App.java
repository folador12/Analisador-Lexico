package compilador;

import compilador.sintatico.Sintatico;

public class App {
    public static void main(String[] args) throws Exception {
        String arquivo = (args.length > 0) ? args[0] : "data/programa5.pas";
        Sintatico sintatico = new Sintatico(arquivo);
        sintatico.analisar();
        System.out.println("Compilação concluída. Arquivo gerado: queronemver.asm");
    }
}
