package compilador.sintatico;

import java.util.LinkedHashMap;
import java.util.Map;

public class TabelaSimbolos {

    private Map<String, Registro> tabela = new LinkedHashMap<>();
    public TabelaSimbolos tabelaPai;

    public TabelaSimbolos() {
        this.tabelaPai = null;
    }

    public TabelaSimbolos(TabelaSimbolos pai) {
        this.tabelaPai = pai;
    }

    public Registro add(String nome) {
        Registro r = new Registro(nome);
        tabela.put(nome.toLowerCase(), r);
        return r;
    }

    public Registro get(String nome) {
        Registro r = tabela.get(nome.toLowerCase());
        if (r != null) return r;
        if (tabelaPai != null) return tabelaPai.get(nome);
        return null;
    }

    public boolean isPresent(String nome) {
        if (tabela.containsKey(nome.toLowerCase())) return true;
        if (tabelaPai != null) return tabelaPai.isPresent(nome);
        return false;
    }

    public boolean isPresentLocal(String nome) {
        return tabela.containsKey(nome.toLowerCase());
    }
}
