package compilador;

import java.io.IOException;
import java.nio.file.Paths;
import compilador.sintatico.Sintatico;

public class App {
    public static void main(String[] args) throws Exception {

        String arquivo = (args.length > 0) ? args[0] : "data/programa5.pas";
        Sintatico sintatico = new Sintatico(arquivo);
        sintatico.analisar();
        System.out.println("Compilação concluída. Arquivo gerado: queronemver.asm");

        String caminhoArquivo = Paths.get("queronemver.asm").toAbsolutePath().toString();
        String caminho = caminhoArquivo.substring(0, caminhoArquivo.indexOf("queronemver.asm"));
        ProcessBuilder processBuilder;
        Process process;
        int exitCode;

        try {
            processBuilder = new ProcessBuilder("/usr/bin/nasm", "-f elf",
                    caminho + "queronemver.asm", "-o" + caminho + "queronemver.o");
            process = processBuilder.start();
            exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
            processBuilder = new ProcessBuilder("gcc", "-m32", "-z", "noexecstack", "-no-pie", "-o",
                    caminho + "queronemver", caminho + "queronemver.o");
            process = processBuilder.start();
            exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
            processBuilder = new ProcessBuilder("./queronemver");
            processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            process = processBuilder.start();
            exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
