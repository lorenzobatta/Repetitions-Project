package businesslogic;

public class Insegna {
    private final Docente docente;
    private final Corso corso;

    public Insegna(Docente d, Corso c){
        docente = d;
        corso = c;
    }

    public Docente getDocente() {
        return docente;
    }

    public Corso getCorso() {
        return corso;
    }
}
