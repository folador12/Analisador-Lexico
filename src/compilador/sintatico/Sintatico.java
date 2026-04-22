package compilador.sintatico;

import compilador.lexico.ClasseToken;
import compilador.lexico.Lexico;
import compilador.lexico.Token;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Sintatico {

    private Lexico lexico;
    private Token token;

    private TabelaSimbolos tabela = new TabelaSimbolos();
    private String rotulo = "";
    private int contRotulo = 1;
    private int offsetVariavel = 0;
    private String nomeArquivoSaida;
    private String caminhoArquivoSaida;
    private BufferedWriter bw;
    private FileWriter fw;
    private static final int TAMANHO_INTEIRO = 4;
    private List<String> variaveis = new ArrayList<>();
    private List<String> sectionData = new ArrayList<>();
    private Registro registro;
    private String rotuloElse;

    // --- campos para suporte a procedimentos/funções (partes amarelas) ---
    private int nivel = 0;
    private int maxNivel = 0;
    private int tamanhoLocais = 0;
    private List<String> parametrosAtual = new ArrayList<>();
    private Registro registroCorpo = null;

    public Sintatico(String nomeArquivo) {
        lexico = new Lexico(nomeArquivo);
        nomeArquivoSaida = "queronemver.asm";
        caminhoArquivoSaida = Paths.get(nomeArquivoSaida).toAbsolutePath().toString();
        bw = null;
        fw = null;
        try {
            fw = new FileWriter(caminhoArquivoSaida, Charset.forName("UTF-8"));
            bw = new BufferedWriter(fw);
        } catch (Exception e) {
            System.err.println("Erro ao criar arquivo de saída");
        }
    }

    // -----------------------------------------------------------------------
    // Infraestrutura
    // -----------------------------------------------------------------------

    private void escreverCodigo(String instrucoes) {
        try {
            if (rotulo.isEmpty()) {
                bw.write(instrucoes + "\n");
            } else {
                bw.write(rotulo + ": " + instrucoes + "\n");
                rotulo = "";
            }
        } catch (IOException e) {
            System.err.println("Erro escrevendo no arquivo de saída");
        }
    }

    private String criarRotulo(String texto) {
        String retorno = "rotulo" + texto + contRotulo;
        contRotulo++;
        return retorno;
    }

    /**
     * Retorna o endereço de memória de um registro.
     * Variáveis locais: [ebp - offset] (offset positivo, abaixo do EBP).
     * Parâmetros e valor de retorno de função: [ebp + offset] (offset positivo, acima do EBP).
     */
    private String enderecoVariavel(Registro reg) {
        if (reg.getCategoria() == Categoria.PARAMETRO || reg.getCategoria() == Categoria.FUNCAO) {
            return "[ebp + " + reg.getOffset() + "]";
        }
        return "[ebp - " + reg.getOffset() + "]";
    }

    private void avanca() {
        token = lexico.getNexToken();
    }

    private void erro(String mensagem) {
        System.err.println("Erro sintático [linha " + token.getLinha() + ", coluna " + token.getColuna() + "]: " + mensagem);
        System.exit(-1);
    }

    private boolean isKeyword(String palavra) {
        return token.getClasse() == ClasseToken.PalavraReservada
                && token.getValor().getTexto().equalsIgnoreCase(palavra);
    }

    private void consomeKeyword(String palavra) {
        if (!isKeyword(palavra)) {
            erro("Esperado '" + palavra + "', encontrado '" + tokenTexto() + "'");
        }
        avanca();
    }

    private void consome(ClasseToken classe) {
        if (token.getClasse() != classe) {
            erro("Esperado token " + classe + ", encontrado " + token.getClasse() + " ('" + tokenTexto() + "')");
        }
        avanca();
    }

    private String tokenTexto() {
        if (token.getValor() == null) return token.getClasse().toString();
        if (token.getValor().getTexto() != null) return token.getValor().getTexto();
        return String.valueOf(token.getValor().getInteiro());
    }

    // -----------------------------------------------------------------------
    // Ponto de entrada
    // -----------------------------------------------------------------------

    public void analisar() {
        avanca();
        programa();
    }

    // -----------------------------------------------------------------------
    // Regras gramaticais
    // -----------------------------------------------------------------------

    // <programa> ::= program id {A01} ; <corpo> . {A45}
    private void programa() {
        consomeKeyword("program");

        if (token.getClasse() != ClasseToken.Identificador) {
            erro("Esperado identificador após 'program'");
        }

        // {A01}
        Registro reg = tabela.add(token.getValor().getTexto());
        offsetVariavel = 0;
        reg.setCategoria(Categoria.PROGRAMA_PRINCIPAL);
        escreverCodigo("global main");
        escreverCodigo("extern printf");
        escreverCodigo("extern scanf\n");
        escreverCodigo("section .text");
        rotulo = "main";
        escreverCodigo("\t; Entrada do programa");
        escreverCodigo("\tpush ebp");
        escreverCodigo("\tmov ebp, esp");

        avanca(); // consume id

        consome(ClasseToken.PontoVirgula);
        corpo();
        consome(ClasseToken.Ponto);

        // {A45}
        escreverCodigo("\tleave");
        escreverCodigo("\tret");
        if (maxNivel > 0) {
            sectionData.add("display: times " + maxNivel + " dd 0");
        }
        if (!sectionData.isEmpty()) {
            escreverCodigo("\nsection .data\n");
            for (String mensagem : sectionData) {
                escreverCodigo(mensagem);
            }
        }
        try {
            bw.close();
            fw.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar arquivo de saída");
        }
    }

    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    private void corpo() {
        tamanhoLocais = 0;   // zera para acumular locais desta scope
        declara();

        // Para o programa principal (nivel == 0), emite um JMP para pular as definições
        // de procedimentos/funções que ficam entre o prólogo e o corpo principal.
        String rotuloCorpoInicio = null;
        if (nivel == 0) {
            rotuloCorpoInicio = criarRotulo("InicioCorpo");
            escreverCodigo("\tjmp " + rotuloCorpoInicio);
        }

        rotina();

        // {A44} — somente para procedimentos/funções (nivel > 0)
        if (nivel > 0 && registroCorpo != null) {
            rotulo = registroCorpo.getRotulo();
            escreverCodigo("\tpush ebp");
            escreverCodigo("\tpush dword[display + " + ((nivel - 1) * TAMANHO_INTEIRO) + "]");
            escreverCodigo("\tmov ebp, esp");
            escreverCodigo("\tmov dword[display + " + ((nivel - 1) * TAMANHO_INTEIRO) + "], ebp");
            if (tamanhoLocais > 0) {
                escreverCodigo("\tsub esp, " + tamanhoLocais);
            }
        }

        // Aplica o rótulo de início do corpo principal (pula procedimentos/funções)
        if (rotuloCorpoInicio != null) {
            rotulo = rotuloCorpoInicio;
        }

        consomeKeyword("begin");
        sentencas();
        consomeKeyword("end");

        // {A46} — somente para procedimentos/funções (nivel > 0)
        if (nivel > 0) {
            if (tamanhoLocais > 0) {
                escreverCodigo("\tadd esp, " + tamanhoLocais);
            }
            escreverCodigo("\tpop dword[display + " + ((nivel - 1) * TAMANHO_INTEIRO) + "]");
            escreverCodigo("\tpop ebp");
            escreverCodigo("\tret");
        }
    }

    // <declara> ::= var <dvar> <mais_dc> | ε
    private void declara() {
        if (isKeyword("var")) {
            avanca();
            dvar();
            maisDc();
        }
    }

    // <mais_dc> ::= ; <cont_dc>
    private void maisDc() {
        if (token.getClasse() == ClasseToken.PontoVirgula) {
            avanca();
            contDc();
        }
    }

    // <cont_dc> ::= <dvar> <mais_dc> | ε
    private void contDc() {
        if (token.getClasse() == ClasseToken.Identificador) {
            dvar();
            maisDc();
        }
    }

    // <dvar> ::= <variaveis> : <tipo_var> {A02}
    private void dvar() {
        variaveis_regra();
        consome(ClasseToken.DoisPontos);
        tipoVar();

        // {A02}
        int tamanho = 0;
        for (String var : variaveis) {
            tabela.get(var).setTipo(Tipo.INTEGER);
            tamanho += TAMANHO_INTEIRO;
        }
        tamanhoLocais += tamanho;
        if (nivel == 0) {
            // programa principal: cabeçalho já foi gerado em A01, apenas emite sub esp
            escreverCodigo("\tsub esp, " + tamanho);
        }
        // para procedimentos/funções (nivel > 0), A44 gerará o sub esp com tamanhoLocais
        variaveis.clear();
    }

    // <tipo_var> ::= integer
    private void tipoVar() {
        consomeKeyword("integer");
    }

    // <variaveis> ::= id {A03} <mais_var>
    private void variaveis_regra() {
        if (token.getClasse() != ClasseToken.Identificador) {
            erro("Esperado identificador na declaração de variável");
        }

        // {A03}
        String variavel = token.getValor().getTexto();
        if (tabela.isPresentLocal(variavel)) {
            System.err.println("Variável '" + variavel + "' já foi declarada anteriormente");
            System.exit(-1);
        }
        tabela.add(variavel);
        tabela.get(variavel).setCategoria(Categoria.VARIAVEL);
        offsetVariavel += TAMANHO_INTEIRO;
        tabela.get(variavel).setOffset(offsetVariavel);
        variaveis.add(variavel);

        avanca();
        maisVar();
    }

    // <mais_var> ::= , <variaveis> | ε
    private void maisVar() {
        if (token.getClasse() == ClasseToken.Virgula) {
            avanca();
            variaveis_regra();
        }
    }

    // -----------------------------------------------------------------------
    // Rotina (partes amarelas: procedimentos e funções)
    // -----------------------------------------------------------------------

    // <rotina> ::= <procedimento> | <funcao> | ε
    private void rotina() {
        if (isKeyword("procedure")) {
            procedimento();
        } else if (isKeyword("function")) {
            funcao();
        }
    }

    // <procedimento> ::= procedure id {A04} <parametros> {A48} ; <corpo> {A56} ; <rotina>
    private void procedimento() {
        consomeKeyword("procedure");

        if (token.getClasse() != ClasseToken.Identificador) {
            erro("Esperado identificador após 'procedure'");
        }

        // {A04}
        String nomeProcedimento = token.getValor().getTexto();
        if (tabela.isPresent(nomeProcedimento)) {
            System.err.println("Identificador '" + nomeProcedimento + "' já foi declarado anteriormente");
            System.exit(-1);
        }
        nivel++;
        if (nivel > maxNivel) maxNivel = nivel;
        Registro regProc = tabela.add(nomeProcedimento);
        regProc.setCategoria(Categoria.PROCEDIMENTO);
        regProc.setNivel(nivel);
        regProc.setRotulo(nomeProcedimento);
        avanca();

        // nova tabela de símbolos para o escopo do procedimento
        TabelaSimbolos tabelaAnterior = tabela;
        tabela = new TabelaSimbolos(tabelaAnterior);
        int offsetAnterior = offsetVariavel;
        offsetVariavel = 0;
        int tamanhoLocaisAnterior = tamanhoLocais;
        parametrosAtual.clear();

        parametros();

        // {A48}
        int numParams = parametrosAtual.size();
        regProc.setNumeroParametros(numParams);
        for (int i = 0; i < numParams; i++) {
            // offset = 12 + ((numParams - (i+1)) * TAMANHO_INTEIRO)
            // parâmetros ficam acima do EBP: [ebp+12] = último param, [ebp+16] = penúltimo, etc.
            int offset = 12 + ((numParams - (i + 1)) * TAMANHO_INTEIRO);
            tabela.get(parametrosAtual.get(i)).setOffset(offset);
        }

        consome(ClasseToken.PontoVirgula);

        Registro registroCorpoAnterior = registroCorpo;
        registroCorpo = regProc;
        corpo();
        registroCorpo = registroCorpoAnterior;

        // {A56}
        nivel--;
        tabela = tabelaAnterior;
        offsetVariavel = offsetAnterior;
        tamanhoLocais = tamanhoLocaisAnterior;

        consome(ClasseToken.PontoVirgula);
        rotina();
    }

    // <funcao> ::= function id {A05} <parametros> {A48} : <tipo_funcao> {A47} ; <corpo> {A56} ; <rotina>
    private void funcao() {
        consomeKeyword("function");

        if (token.getClasse() != ClasseToken.Identificador) {
            erro("Esperado identificador após 'function'");
        }

        // {A05}
        String nomeFuncao = token.getValor().getTexto();
        if (tabela.isPresent(nomeFuncao)) {
            System.err.println("Identificador '" + nomeFuncao + "' já foi declarado anteriormente");
            System.exit(-1);
        }
        nivel++;
        if (nivel > maxNivel) maxNivel = nivel;
        Registro regFunc = tabela.add(nomeFuncao);
        regFunc.setCategoria(Categoria.FUNCAO);
        regFunc.setNivel(nivel);
        regFunc.setRotulo(nomeFuncao);
        avanca();

        // nova tabela de símbolos; insere a própria função para permitir atribuição do retorno
        TabelaSimbolos tabelaAnterior = tabela;
        tabela = new TabelaSimbolos(tabelaAnterior);
        Registro regFuncLocal = tabela.add(nomeFuncao);
        regFuncLocal.setCategoria(Categoria.FUNCAO);
        regFuncLocal.setNivel(nivel);
        regFuncLocal.setRotulo(nomeFuncao);

        int offsetAnterior = offsetVariavel;
        offsetVariavel = 0;
        int tamanhoLocaisAnterior = tamanhoLocais;
        parametrosAtual.clear();

        parametros();

        // {A48}
        int numParams = parametrosAtual.size();
        regFunc.setNumeroParametros(numParams);
        regFuncLocal.setNumeroParametros(numParams);
        for (int i = 0; i < numParams; i++) {
            int offset = 12 + ((numParams - (i + 1)) * TAMANHO_INTEIRO);
            tabela.get(parametrosAtual.get(i)).setOffset(offset);
        }

        consome(ClasseToken.DoisPontos);
        tipoFuncao();

        // {A47} — tipo é sempre integer; define offset do retorno
        int offsetRetorno = 12 + numParams * TAMANHO_INTEIRO;
        regFunc.setOffset(offsetRetorno);
        regFuncLocal.setOffset(offsetRetorno);

        consome(ClasseToken.PontoVirgula);

        Registro registroCorpoAnterior = registroCorpo;
        registroCorpo = regFunc;
        corpo();
        registroCorpo = registroCorpoAnterior;

        // {A56}
        nivel--;
        tabela = tabelaAnterior;
        offsetVariavel = offsetAnterior;
        tamanhoLocais = tamanhoLocaisAnterior;

        consome(ClasseToken.PontoVirgula);
        rotina();
    }

    // <tipo_funcao> ::= integer
    private void tipoFuncao() {
        consomeKeyword("integer");
    }

    // <parametros> ::= ( <lista_parametros> ) | ε
    private void parametros() {
        if (token.getClasse() == ClasseToken.AbreParenteses) {
            avanca();
            listaParametros();
            consome(ClasseToken.FechaParenteses);
        }
    }

    // <lista_parametros> ::= <lista_id> : <tipo_var> {A06} <cont_lista_par>
    private void listaParametros() {
        listaId();
        consome(ClasseToken.DoisPontos);
        tipoVar();
        // {A06} — tipo sempre integer, pode ser desprezado
        contListaPar();
    }

    // <cont_lista_par> ::= ; <lista_parametros> | ε
    private void contListaPar() {
        if (token.getClasse() == ClasseToken.PontoVirgula) {
            avanca();
            listaParametros();
        }
    }

    // <lista_id> ::= id {A07} <cont_lista_id>
    private void listaId() {
        if (token.getClasse() != ClasseToken.Identificador) {
            erro("Esperado identificador na lista de parâmetros");
        }

        // {A07}
        String nomeParam = token.getValor().getTexto();
        if (tabela.isPresentLocal(nomeParam)) {
            System.err.println("Parâmetro '" + nomeParam + "' já foi declarado anteriormente");
            System.exit(-1);
        }
        Registro regParam = tabela.add(nomeParam);
        regParam.setCategoria(Categoria.PARAMETRO);
        regParam.setNivel(nivel);
        parametrosAtual.add(nomeParam);

        avanca();
        contListaId();
    }

    // <cont_lista_id> ::= , <lista_id> | ε
    private void contListaId() {
        if (token.getClasse() == ClasseToken.Virgula) {
            avanca();
            listaId();
        }
    }

    // -----------------------------------------------------------------------
    // Sentencas e comandos
    // -----------------------------------------------------------------------

    // <sentencas> ::= <comando> <mais_sentencas>
    private void sentencas() {
        comando();
        maisSentencas();
    }

    // <mais_sentencas> ::= ; <cont_sentencas>
    private void maisSentencas() {
        if (token.getClasse() == ClasseToken.PontoVirgula) {
            avanca();
            contSentencas();
        }
    }

    // <cont_sentencas> ::= <sentencas> | ε
    private void contSentencas() {
        if (isInicioComando()) {
            sentencas();
        }
    }

    private boolean isInicioComando() {
        if (token.getClasse() == ClasseToken.Identificador) return true;
        if (token.getClasse() != ClasseToken.PalavraReservada) return false;
        String kw = token.getValor().getTexto().toLowerCase();
        switch (kw) {
            case "read": case "write": case "writeln":
            case "for": case "repeat": case "while": case "if":
                return true;
            default:
                return false;
        }
    }

    // <comando> ::= read(...) | write(...) | writeln(...) | for | repeat | while | if | id := | chamada
    private void comando() {
        if (isKeyword("read")) {
            avanca();
            consome(ClasseToken.AbreParenteses);
            varRead();
            consome(ClasseToken.FechaParenteses);

        } else if (isKeyword("write")) {
            avanca();
            consome(ClasseToken.AbreParenteses);
            expWrite(false);
            consome(ClasseToken.FechaParenteses);

        } else if (isKeyword("writeln")) {
            avanca();
            consome(ClasseToken.AbreParenteses);
            expWrite(false);
            consome(ClasseToken.FechaParenteses);
            // {A61}
            String novaLinha = "rotuloStringLN: db '',10,0";
            if (!sectionData.contains(novaLinha)) {
                sectionData.add(novaLinha);
            }
            escreverCodigo("\tpush rotuloStringLN");
            escreverCodigo("\tcall printf");
            escreverCodigo("\tadd esp, 4");

        } else if (isKeyword("for")) {
            comandoFor();

        } else if (isKeyword("repeat")) {
            comandoRepeat();

        } else if (isKeyword("while")) {
            comandoWhile();

        } else if (isKeyword("if")) {
            comandoIf();

        } else if (token.getClasse() == ClasseToken.Identificador) {
            String nome = token.getValor().getTexto();
            avanca();

            if (token.getClasse() == ClasseToken.Atribuicao) {
                // {A49} id := expressao {A22}
                if (!tabela.isPresent(nome)) {
                    System.err.println("Variável '" + nome + "' não foi declarada");
                    System.exit(-1);
                }
                registro = tabela.get(nome);
                boolean ehFuncaoCorrente = registro.getCategoria() == Categoria.FUNCAO
                        && registro.getNivel() == nivel;
                if (registro.getCategoria() != Categoria.VARIAVEL
                        && registro.getCategoria() != Categoria.PARAMETRO
                        && !ehFuncaoCorrente) {
                    System.err.println("O identificador '" + nome + "' não é uma variável. A49");
                    System.exit(-1);
                }
                avanca(); // consume :=
                expressao();
                // {A22}
                escreverCodigo("\tpop eax");
                escreverCodigo("\tmov dword" + enderecoVariavel(registro) + ", eax");

            } else {
                // {A50} chamada de procedimento <argumentos> {A23}
                if (!tabela.isPresent(nome)) {
                    System.err.println("Identificador '" + nome + "' não foi declarado");
                    System.exit(-1);
                }
                Registro regProc = tabela.get(nome);
                if (regProc.getCategoria() != Categoria.PROCEDIMENTO) {
                    System.err.println("Identificador '" + nome + "' não é um procedimento. A50");
                    System.exit(-1);
                }
                int numArgs = argumentos();
                // {A23}
                if (numArgs != regProc.getNumeroParametros()) {
                    System.err.println("Número de argumentos inválido para '" + nome + "'");
                    System.exit(-1);
                }
                escreverCodigo("\tcall " + regProc.getRotulo());
                escreverCodigo("\tadd esp, " + (numArgs * TAMANHO_INTEIRO));
            }
        } else {
            erro("Comando inválido: '" + tokenTexto() + "'");
        }
    }

    // <var_read> ::= id {A08} <mais_var_read>
    private void varRead() {
        if (token.getClasse() != ClasseToken.Identificador) {
            erro("Esperado identificador em read");
        }

        // {A08}
        String variavel = token.getValor().getTexto();
        if (!tabela.isPresent(variavel)) {
            System.err.println("Variável '" + variavel + "' não foi declarada");
            System.exit(-1);
        }
        Registro reg = tabela.get(variavel);
        if (reg.getCategoria() != Categoria.VARIAVEL && reg.getCategoria() != Categoria.PARAMETRO) {
            System.err.println("Identificador '" + variavel + "' não é uma variável");
            System.exit(-1);
        }
        escreverCodigo("\tlea eax, " + enderecoVariavel(reg));
        escreverCodigo("\tpush eax");
        escreverCodigo("\tpush @Integer");
        escreverCodigo("\tcall scanf");
        escreverCodigo("\tadd esp, 8");
        if (!sectionData.contains("@Integer: db '%d',0")) {
            sectionData.add("@Integer: db '%d',0");
        }

        avanca();
        maisVarRead();
    }

    // <mais_var_read> ::= , <var_read> | ε
    private void maisVarRead() {
        if (token.getClasse() == ClasseToken.Virgula) {
            avanca();
            varRead();
        }
    }

    // <exp_write> ::= id {A09} | string {A59} | intnum {A43}   + mais_exp_write
    private void expWrite(boolean isWriteln) {
        if (token.getClasse() == ClasseToken.Identificador) {
            // {A09}
            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável '" + variavel + "' não foi declarada");
                System.exit(-1);
            }
            Registro reg = tabela.get(variavel);
            if (reg.getCategoria() != Categoria.VARIAVEL && reg.getCategoria() != Categoria.PARAMETRO) {
                System.err.println("Identificador '" + variavel + "' não é uma variável");
                System.exit(-1);
            }
            escreverCodigo("\tpush dword" + enderecoVariavel(reg));
            escreverCodigo("\tpush @Integer");
            escreverCodigo("\tcall printf");
            escreverCodigo("\tadd esp, 8");
            if (!sectionData.contains("@Integer: db '%d',0")) {
                sectionData.add("@Integer: db '%d',0");
            }
            avanca();

        } else if (token.getClasse() == ClasseToken.String) {
            // {A59}
            String string = token.getValor().getTexto();
            String rot = criarRotulo("String");
            sectionData.add(rot + ": db '" + string + "',0");
            escreverCodigo("\tpush " + rot);
            escreverCodigo("\tcall printf");
            escreverCodigo("\tadd esp, 4");
            avanca();

        } else if (token.getClasse() == ClasseToken.Inteiro) {
            // {A43}
            escreverCodigo("\tpush " + token.getValor().getInteiro());
            escreverCodigo("\tpush @Integer");
            escreverCodigo("\tcall printf");
            escreverCodigo("\tadd esp, 8");
            if (!sectionData.contains("@Integer: db '%d',0")) {
                sectionData.add("@Integer: db '%d',0");
            }
            avanca();

        } else {
            erro("Esperado expressão em write/writeln");
        }

        maisExpWrite(isWriteln);
    }

    // <mais_exp_write> ::= , <exp_write> | ε
    private void maisExpWrite(boolean isWriteln) {
        if (token.getClasse() == ClasseToken.Virgula) {
            avanca();
            expWrite(isWriteln);
        }
    }

    // for id {A57} := expressao {A11} to expressao {A12} do begin sentencas end {A13}
    private void comandoFor() {
        consomeKeyword("for");

        if (token.getClasse() != ClasseToken.Identificador) {
            erro("Esperado identificador após 'for'");
        }

        // {A57}
        String variavel = token.getValor().getTexto();
        if (!tabela.isPresent(variavel)) {
            System.err.println("Variável '" + variavel + "' não foi declarada");
            System.exit(-1);
        }
        Registro regFor = tabela.get(variavel);
        if (regFor.getCategoria() != Categoria.VARIAVEL && regFor.getCategoria() != Categoria.PARAMETRO) {
            System.err.println("O identificador '" + variavel + "' não é uma variável. A57");
            System.exit(-1);
        }
        avanca();

        consome(ClasseToken.Atribuicao);
        expressao();

        // {A11}
        escreverCodigo("\tpop dword" + enderecoVariavel(regFor));
        String rotuloEntrada = criarRotulo("FOR");
        String rotuloSaida = criarRotulo("FIMFOR");

        consomeKeyword("to");
        expressao(); // empilha o limite "to"

        // {A12}
        rotulo = rotuloEntrada;
        escreverCodigo("\tpush ecx\n"
                + "\tmov ecx, dword" + enderecoVariavel(regFor) + "\n"
                + "\tcmp ecx, dword[esp+4]\n"
                + "\tjg " + rotuloSaida + "\n"
                + "\tpop ecx");

        consomeKeyword("do");
        consomeKeyword("begin");
        sentencas();
        consomeKeyword("end");

        // {A13}
        escreverCodigo("\tadd dword" + enderecoVariavel(regFor) + ", 1");
        escreverCodigo("\tjmp " + rotuloEntrada);
        rotulo = rotuloSaida;
        escreverCodigo("\tadd esp, 8");
    }

    // repeat {A14} sentencas until ( expressao_logica ) {A15}
    private void comandoRepeat() {
        consomeKeyword("repeat");

        // {A14}
        String rotRepeat = criarRotulo("Repeat");
        rotulo = rotRepeat;

        sentencas();

        consomeKeyword("until");
        consome(ClasseToken.AbreParenteses);
        expressaoLogica();
        consome(ClasseToken.FechaParenteses);

        // {A15}
        escreverCodigo("\tcmp dword[esp], 0");
        escreverCodigo("\tje " + rotRepeat);
        escreverCodigo("\tadd esp, 4");
    }

    // while {A16} ( expressao_logica ) {A17} do begin sentencas end {A18}
    private void comandoWhile() {
        consomeKeyword("while");

        // {A16}
        String rotuloWhile = criarRotulo("While");
        String rotuloFim = criarRotulo("FimWhile");
        rotulo = rotuloWhile;

        consome(ClasseToken.AbreParenteses);
        expressaoLogica();
        consome(ClasseToken.FechaParenteses);

        // {A17}
        escreverCodigo("\tcmp dword[esp], 0");
        escreverCodigo("\tje " + rotuloFim);
        escreverCodigo("\tadd esp, 4");

        consomeKeyword("do");
        consomeKeyword("begin");
        sentencas();
        consomeKeyword("end");

        // {A18}
        escreverCodigo("\tjmp " + rotuloWhile);
        rotulo = rotuloFim;
        escreverCodigo("\tadd esp, 4");
    }

    // if ( expressao_logica ) {A19} then begin sentencas end {A20} pfalsa {A21}
    private void comandoIf() {
        consomeKeyword("if");
        consome(ClasseToken.AbreParenteses);
        expressaoLogica();
        consome(ClasseToken.FechaParenteses);

        // {A19}
        rotuloElse = criarRotulo("Else");
        String rotuloFim = criarRotulo("FimIf");
        escreverCodigo("\tcmp dword[esp], 0");
        escreverCodigo("\tje " + rotuloElse);
        escreverCodigo("\tadd esp, 4");

        consomeKeyword("then");
        consomeKeyword("begin");
        sentencas();
        consomeKeyword("end");

        // {A20}
        escreverCodigo("\tjmp " + rotuloFim);

        // pfalsa with {A25}
        pfalsa(rotuloFim);

        // {A21}
        rotulo = rotuloFim;
    }

    // <pfalsa> ::= {A25} else begin sentencas end | ε
    private void pfalsa(String rotuloFim) {
        // {A25} — emite o rótulo do else e sempre faz pop do resultado lógico
        escreverCodigo(rotuloElse + ":");
        escreverCodigo("\tadd esp, 4"); // pop do resultado da expressao_logica (caminho falso)
        if (isKeyword("else")) {
            avanca();
            consomeKeyword("begin");
            sentencas();
            consomeKeyword("end");
        }
    }

    // <argumentos> ::= ( <lista_arg> ) | ε   — retorna número de args empilhados
    private int argumentos() {
        if (token.getClasse() == ClasseToken.AbreParenteses) {
            avanca();
            int count = listaArg();
            consome(ClasseToken.FechaParenteses);
            return count;
        }
        return 0;
    }

    // <lista_arg> ::= <expressao> <cont_lista_arg>
    private int listaArg() {
        expressao();
        int count = 1 + contListaArg();
        return count;
    }

    // <cont_lista_arg> ::= , <lista_arg> | ε
    private int contListaArg() {
        if (token.getClasse() == ClasseToken.Virgula) {
            avanca();
            return listaArg();
        }
        return 0;
    }

    // -----------------------------------------------------------------------
    // Expressões lógicas
    // -----------------------------------------------------------------------

    // <expressao_logica> ::= <termo_logico> <mais_expr_logica>
    private void expressaoLogica() {
        termoLogico();
        maisExprLogica();
    }

    // <mais_expr_logica> ::= or <termo_logico> {A26} <mais_expr_logica> | ε
    private void maisExprLogica() {
        if (isKeyword("or")) {
            avanca();
            termoLogico();
            // {A26}
            String rotSaida = criarRotulo("SaidaMEL");
            String rotVerdade = criarRotulo("VerdadeMEL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tje " + rotVerdade);
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tje " + rotVerdade);
            escreverCodigo("\tmov dword [ESP + 4], 0");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotVerdade;
            escreverCodigo("\tmov dword [ESP + 4], 1");
            rotulo = rotSaida;
            escreverCodigo("\tadd esp, 4");
            maisExprLogica();
        }
    }

    // <termo_logico> ::= <fator_logico> <mais_termo_logico>
    private void termoLogico() {
        fatorLogico();
        maisTermoLogico();
    }

    // <mais_termo_logico> ::= and <fator_logico> {A27} <mais_termo_logico> | ε
    private void maisTermoLogico() {
        if (isKeyword("and")) {
            avanca();
            fatorLogico();
            // {A27}
            String rotSaida = criarRotulo("SaidaMTL");
            String rotFalso = criarRotulo("FalsoMTL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
            maisTermoLogico();
        }
    }

    // <fator_logico> ::= <relacional> | ( <expressao_logica> ) | not <fator_logico> | true | false
    private void fatorLogico() {
        if (isKeyword("not")) {
            avanca();
            fatorLogico();
            // {A28}
            String rotFalso = criarRotulo("FalsoFL");
            String rotSaida = criarRotulo("SaidaFL");
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 0");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 1");
            rotulo = rotSaida;

        } else if (isKeyword("true")) {
            avanca();
            escreverCodigo("\tpush 1"); // {A29}

        } else if (isKeyword("false")) {
            avanca();
            escreverCodigo("\tpush 0"); // {A30}

        } else if (token.getClasse() == ClasseToken.AbreParenteses) {
            avanca();
            expressaoLogica();
            consome(ClasseToken.FechaParenteses);

        } else {
            relacional();
        }
    }

    // <relacional> ::= <expressao> (= | > | >= | < | <= | <>) <expressao> {A31-A36}
    private void relacional() {
        expressao();

        if (token.getClasse() == ClasseToken.Igualdade) {
            avanca();
            expressao();
            geraRelacional("jne"); // {A31}

        } else if (token.getClasse() == ClasseToken.Maior) {
            avanca();
            expressao();
            geraRelacional("jle"); // {A32}

        } else if (token.getClasse() == ClasseToken.MaiorIgual) {
            avanca();
            expressao();
            geraRelacional("jl"); // {A33}

        } else if (token.getClasse() == ClasseToken.Menor) {
            avanca();
            expressao();
            geraRelacional("jge"); // {A34}

        } else if (token.getClasse() == ClasseToken.MenorIgual) {
            avanca();
            expressao();
            geraRelacional("jg"); // {A35}

        } else if (token.getClasse() == ClasseToken.Diferente) {
            avanca();
            expressao();
            geraRelacional("je"); // {A36}

        } else {
            erro("Operador relacional esperado");
        }
    }

    private void geraRelacional(String jumpFalso) {
        String rotFalso = criarRotulo("FalsoREL");
        String rotSaida = criarRotulo("SaidaREL");
        escreverCodigo("\tpop eax");
        escreverCodigo("\tcmp dword [ESP], eax");
        escreverCodigo("\t" + jumpFalso + " " + rotFalso);
        escreverCodigo("\tmov dword [ESP], 1");
        escreverCodigo("\tjmp " + rotSaida);
        rotulo = rotFalso;
        escreverCodigo("\tmov dword [ESP], 0");
        rotulo = rotSaida;
    }

    // -----------------------------------------------------------------------
    // Expressões aritméticas
    // -----------------------------------------------------------------------

    // <expressao> ::= <termo> <mais_expressao>
    private void expressao() {
        termo();
        maisExpressao();
    }

    // <mais_expressao> ::= + <termo> {A37} | - <termo> {A38} | ε
    private void maisExpressao() {
        if (token.getClasse() == ClasseToken.Mais) {
            avanca();
            termo();
            escreverCodigo("\tpop eax");
            escreverCodigo("\tadd dword[ESP], eax"); // {A37}
            maisExpressao();

        } else if (token.getClasse() == ClasseToken.Menos) {
            avanca();
            termo();
            escreverCodigo("\tpop eax");
            escreverCodigo("\tsub dword[ESP], eax"); // {A38}
            maisExpressao();
        }
    }

    // <termo> ::= <fator> <mais_termo>
    private void termo() {
        fator();
        maisTermo();
    }

    // <mais_termo> ::= * <fator> {A39} | / <fator> {A40} | ε
    private void maisTermo() {
        if (token.getClasse() == ClasseToken.Multiplicacao) {
            avanca();
            fator();
            escreverCodigo("\tpop eax");
            escreverCodigo("\timul eax, dword [ESP]");
            escreverCodigo("\tmov dword [ESP], eax"); // {A39}
            maisTermo();

        } else if (token.getClasse() == ClasseToken.Divisao) {
            avanca();
            fator();
            escreverCodigo("\tpop ecx");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcdq");
            escreverCodigo("\tidiv ecx");
            escreverCodigo("\tpush eax"); // {A40}
            maisTermo();
        }
    }

    // <fator> ::= id {A55} | intnum {A41} | ( <expressao> ) | id {A60} <argumentos> {A42}
    private void fator() {
        if (token.getClasse() == ClasseToken.Identificador) {
            String nome = token.getValor().getTexto();
            avanca();

            if (token.getClasse() == ClasseToken.AbreParenteses) {
                // id {A60} <argumentos> {A42} — chamada de função
                if (!tabela.isPresent(nome)) {
                    System.err.println("Identificador '" + nome + "' não foi declarado");
                    System.exit(-1);
                }
                Registro regFunc = tabela.get(nome);
                if (regFunc.getCategoria() != Categoria.FUNCAO) {
                    System.err.println("Identificador '" + nome + "' não é uma função. A42");
                    System.exit(-1);
                }
                escreverCodigo("\tsub esp, 4"); // {A60} — espaço para valor de retorno
                int numArgs = argumentos();
                // {A42}
                if (numArgs != regFunc.getNumeroParametros()) {
                    System.err.println("Número de argumentos insuficiente para '" + nome + "'");
                    System.exit(-1);
                }
                escreverCodigo("\tcall " + regFunc.getRotulo());
                escreverCodigo("\tadd esp, " + (numArgs * TAMANHO_INTEIRO));
                // valor de retorno permanece no topo da pilha (o slot alocado com sub esp, 4)

            } else {
                // id {A55} — variável ou parâmetro
                if (!tabela.isPresent(nome)) {
                    System.err.println("Variável '" + nome + "' não foi declarada");
                    System.exit(-1);
                }
                Registro reg = tabela.get(nome);
                if (reg.getCategoria() != Categoria.VARIAVEL && reg.getCategoria() != Categoria.PARAMETRO) {
                    System.err.println("O identificador '" + nome + "' não é uma variável. A55");
                    System.exit(-1);
                }
                escreverCodigo("\tpush dword" + enderecoVariavel(reg));
            }

        } else if (token.getClasse() == ClasseToken.Inteiro) {
            escreverCodigo("\tpush " + token.getValor().getInteiro()); // {A41}
            avanca();

        } else if (token.getClasse() == ClasseToken.AbreParenteses) {
            avanca();
            expressao();
            consome(ClasseToken.FechaParenteses);

        } else {
            erro("Fator inválido: '" + tokenTexto() + "'");
        }
    }
}
