package compilador.lexico;

public class ValorToken {

    private int inteiro;
    private String texto;

    public ValorToken(int inteiro) {
        this.inteiro = inteiro;
    }

    public ValorToken(String texto) {
        this.texto = texto;
    }

    public int getInteiro() {
        return inteiro;
    }

    public void setInteiro(int inteiro) {
        this.inteiro = inteiro;
    }

    public String getTexto() {
        return texto;
    }
    
    public void setTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return "ValorToken [" + ((texto != null) ? "texto=" + texto : "inteiro=" + inteiro) + "]";
    }
    
}
