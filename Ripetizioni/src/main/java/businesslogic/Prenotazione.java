package businesslogic;

public class Prenotazione {

    private Corso corso;
    private Docente docente;
    private Utente utente;
    private String giorno;
    private String slot;
    private String stato;



    public Prenotazione(Corso c,Docente d, Utente u, String day,String slot){
        corso = c;
        docente = d;
        utente = u;
        giorno = day;
        this.slot = slot;
    }
    public Prenotazione(Corso c,Docente d, String day,String slot){
        corso = c;
        docente = d;
        giorno = day;
        this.slot = slot;
    }

    public Prenotazione(Corso idCorso, Docente idDocente, Utente u, String giorno, String slot, String stato) {
        corso = idCorso;
        docente = idDocente;
        utente = u;
        this.giorno = giorno;
        this.slot = slot;
        this.stato = stato;
    }

    public void printPrenotazione(){
        System.out.println("PRENOTAZIONE /******* \n" + corso.toString() + "\n" + docente.toString() + "\n" + utente.toString() + "\n" + giorno + " " + slot + "\nFINE PRENOTAZIONE ******/");
    }

    public void printPrenotazioneSenzaUtente(){
        System.out.println("PRENOTAZIONE /******* \n" + corso.toString() + "\n" + docente.toString() + "\n" + giorno + " " + slot + "\nFINE PRENOTAZIONE ******/");
    }

    public String getCorsoTitle(){
        return corso.getTitle();
    }

    public String getCognomeDocente(){
        return docente.getSurname();
    }

    public String getNomeDocente(){
        return docente.getName();
    }

    public int getIdDocente() {return docente.getId();}

    public int getIdUtente() { return utente.getId(); }

    public int getIdCorso(){ return corso.getId(); }

    public String getGiorno(){
        return giorno;
    }

    public String getSlot(){
        return slot;
    }
}
