package compilador.lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Lexico {
    private String nomeArquivo;
    private BufferedReader br;
    private char caractere;
    private static final List<String> palavrasReservadas = Arrays.asList("const", "type", "var",
            "begin", "end", "while", "do", "for", "downto", "if", "then", "else", "case", "of",
            "array", "function", "procedure", "label", "record", "exit", "break", "continue", "and",
            "or", "not", "integer", "program", "write", "writeln", "read", "repeat", "until", "to");
    private int linha = 1;
    private int coluna = 1;

    private static final char EOF_CHAR = (char) 65535;

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

    // Lê o próximo caractere e incrementa a coluna
    private void avanca() throws IOException {
        caractere = (char) br.read();
        coluna++;
    }

    // Lê o próximo caractere tratando quebras de linha (atualiza linha e coluna)
    private void avancaComNewline() throws IOException {
        if (caractere == '\n') {
            linha++;
            coluna = 1;
            caractere = (char) br.read();
        } else {
            avanca();
        }
    }

    // Método de análise léxica: retorna o próximo token do arquivo
    public Token getNexToken() {
        StringBuilder lexema;
        Token token;

        try {
            while (caractere != EOF_CHAR) {
                lexema = new StringBuilder();
                token = new Token(linha, coluna);

                if (Character.isDigit(caractere)) {
                    // Reconhece literais inteiros
                    while (Character.isDigit(caractere)) {
                        lexema.append(caractere);
                        avanca();
                    }
                    token.setClasse(ClasseToken.Inteiro);
                    token.setValor(new ValorToken(Integer.parseInt(lexema.toString())));
                    return token;

                } else if (Character.isAlphabetic(caractere)) {
                    // Reconhece identificadores e palavras reservadas
                    while (Character.isAlphabetic(caractere) || Character.isDigit(caractere)) {
                        lexema.append(caractere);
                        avanca();
                    }
                    String lexStr = lexema.toString().toLowerCase();
                    if (palavrasReservadas.contains(lexStr)) {
                        token.setClasse(ClasseToken.PalavraReservada);
                    } else {
                        token.setClasse(ClasseToken.Identificador);
                    }
                    token.setValor(new ValorToken(lexStr));
                    return token;

                } else if (caractere == ' ' || caractere == '\t' || caractere == '\r') {
                    // Ignora espaços em branco, tabulações e carriage return
                    avanca();

                } else if (caractere == '\n') {
                    // Ignora quebras de linha e atualiza contadores de posição
                    linha++;
                    coluna = 1;
                    caractere = (char) br.read();

                } else if (caractere == '\'') {
                    // Reconhece strings delimitadas por aspas simples: '...'
                    // Erro léxico se encontrar \n ou EOF antes de fechar a aspa
                    int linhaInicio = linha;
                    int colunaInicio = coluna;
                    avanca(); // consome a aspa de abertura
                    while (caractere != '\'') {
                        if (caractere == '\n') {
                            throw new RuntimeException(
                                "Erro léxico [" + linhaInicio + ":" + colunaInicio + "]: " +
                                "string não fechada (quebra de linha na linha " + linha + ")");
                        }
                        if (caractere == EOF_CHAR) {
                            throw new RuntimeException(
                                "Erro léxico [" + linhaInicio + ":" + colunaInicio + "]: " +
                                "string não fechada (fim de arquivo)");
                        }
                        lexema.append(caractere);
                        avanca();
                    }
                    avanca(); // consome a aspa de fechamento
                    token.setClasse(ClasseToken.String);
                    token.setValor(new ValorToken(lexema.toString()));
                    return token;

                } else if (caractere == '/') {
                    // Pode ser divisão (/) ou comentário de linha (//)
                    avanca();
                    if (caractere == '/') {
                        // Comentário de linha: ignora tudo até o fim da linha
                        while (caractere != '\n' && caractere != EOF_CHAR) {
                            avanca();
                        }
                    } else {
                        // Operador de divisão
                        token.setClasse(ClasseToken.Divisao);
                        return token;
                    }

                } else if (caractere == '{') {
                    // Comentário em bloco estilo Pascal: { ... }
                    // Erro léxico se o arquivo terminar sem fechar o comentário
                    int linhaInicio = linha;
                    int colunaInicio = coluna;
                    avanca(); // consome o '{'
                    while (caractere != '}') {
                        if (caractere == EOF_CHAR) {
                            throw new RuntimeException(
                                "Erro léxico [" + linhaInicio + ":" + colunaInicio + "]: " +
                                "comentário em bloco não fechado (fim de arquivo)");
                        }
                        avancaComNewline();
                    }
                    avanca(); // consome o '}'

                } else if (caractere == '(') {
                    // Pode ser abre parênteses ou início de comentário em bloco (* ... *)
                    avanca();
                    if (caractere == '*') {
                        // Comentário em bloco estilo Pascal: (* ... *)
                        // Erro léxico se o arquivo terminar sem fechar o comentário
                        int linhaInicio = linha;
                        int colunaInicio = coluna - 1; // aponta para o '(' inicial
                        avanca(); // consome o '*' de abertura
                        while (true) {
                            if (caractere == EOF_CHAR) {
                                throw new RuntimeException(
                                    "Erro léxico [" + linhaInicio + ":" + colunaInicio + "]: " +
                                    "comentário em bloco não fechado (fim de arquivo)");
                            }
                            if (caractere == '*') {
                                avanca(); // consome o '*' candidato ao fechamento
                                if (caractere == ')') {
                                    avanca(); // consome o ')' e encerra o comentário
                                    break;
                                }
                                // '*' não seguido de ')': continua sem avançar novamente
                            } else {
                                avancaComNewline();
                            }
                        }
                    } else {
                        // Abre parênteses simples
                        token.setClasse(ClasseToken.AbreParenteses);
                        return token;
                    }

                } else if (caractere == '+') {
                    avanca();
                    token.setClasse(ClasseToken.Mais);
                    return token;

                } else if (caractere == '-') {
                    avanca();
                    token.setClasse(ClasseToken.Menos);
                    return token;

                } else if (caractere == '*') {
                    avanca();
                    token.setClasse(ClasseToken.Multiplicacao);
                    return token;

                } else if (caractere == '=') {
                    avanca();
                    token.setClasse(ClasseToken.Igualdade);
                    return token;

                } else if (caractere == ':') {
                    // Pode ser dois pontos (:) ou atribuição (:=)
                    avanca();
                    if (caractere == '=') {
                        avanca();
                        token.setClasse(ClasseToken.Atribuicao);
                    } else {
                        token.setClasse(ClasseToken.DoisPontos);
                    }
                    return token;

                } else if (caractere == '>') {
                    // Pode ser maior (>) ou maior ou igual (>=)
                    avanca();
                    if (caractere == '=') {
                        avanca();
                        token.setClasse(ClasseToken.MaiorIgual);
                    } else {
                        token.setClasse(ClasseToken.Maior);
                    }
                    return token;

                } else if (caractere == '<') {
                    // Pode ser menor (<), menor ou igual (<=) ou diferente (<>)
                    avanca();
                    if (caractere == '=') {
                        avanca();
                        token.setClasse(ClasseToken.MenorIgual);
                    } else if (caractere == '>') {
                        avanca();
                        token.setClasse(ClasseToken.Diferente);
                    } else {
                        token.setClasse(ClasseToken.Menor);
                    }
                    return token;

                } else if (caractere == ';') {
                    avanca();
                    token.setClasse(ClasseToken.PontoVirgula);
                    return token;

                } else if (caractere == ',') {
                    avanca();
                    token.setClasse(ClasseToken.Virgula);
                    return token;

                } else if (caractere == ')') {
                    avanca();
                    token.setClasse(ClasseToken.FechaParenteses);
                    return token;

                } else if (caractere == '.') {
                    avanca();
                    token.setClasse(ClasseToken.Ponto);
                    return token;

                } else {
                    // Caractere não reconhecido: erro léxico
                    throw new RuntimeException(
                        "Erro léxico [" + linha + ":" + coluna + "]: " +
                        "caractere inválido '" + caractere + "'");
                }
            }

            // Fim de arquivo
            token = new Token(linha, coluna);
            token.setClasse(ClasseToken.EOF);
            return token;

        } catch (IOException e) {
            System.err.println("Não foi possível ler o arquivo: " + nomeArquivo);
        }
        return null;
    }
}
