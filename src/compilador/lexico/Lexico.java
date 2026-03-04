package compilador.lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class Lexico {
    private String nomeArquivo;
    private BufferedReader br;
    private char caractere;
    // private List<String> palavrasReservadas;
    private int linha;
    private int coluna;

    public Lexico(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
        try {
            br = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            caractere = (char) br.read();
        } catch (IOException ex) {
            System.out.println("Erro abrindo o arquivo " + nomeArquivo);
            System.out.println("Caminho do arquivo: " + caminhoArquivo);
        }
    }

    // metodo de analise
    public Token getNexToken() {
        StringBuilder lexema;
        Token token;

        try {
            while (caractere != 65535) { // 665535 = EOF (final do arquivo)
                lexema = new StringBuilder();
                token = new Token(linha, coluna);

                if (Character.isDigit(caractere)) {
                    while (Character.isDigit(caractere)) {
                        lexema.append(caractere);
                        caractere = (char) br.read();
                    }
                    token.setClasse(ClasseToken.Inteiro);
                    token.setValor(new ValorToken(Integer.parseInt(lexema.toString())));
                    return token;
                } else if (Character.isAlphabetic(caractere)) {
                    while (Character.isAlphabetic(caractere) || Character.isDigit(caractere)) {
                        lexema.append(caractere);
                        caractere = (char) br.read();
                    }
                    token.setClasse(ClasseToken.Identificador);
                    token.setValor(new ValorToken(lexema.toString()));
                    return token;
                } else if (caractere == ' ' || caractere == '\t') {
                    caractere = (char) br.read();
                }
            }
            token = new Token(linha, coluna);
            token.setClasse(ClasseToken.EOF);
            return token;
        } catch (IOException e) {
            System.err.println("Não foi possível ler o arquivo: " + nomeArquivo);
        }
        return null;
    }
}
