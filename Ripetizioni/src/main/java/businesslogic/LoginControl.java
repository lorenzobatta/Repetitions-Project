package businesslogic;

import java.util.ArrayList;

public class LoginControl {
    private final boolean flag;
    private Utente utente;
    private final ArrayList<Prenotazione> prenotazioni;
    private final String sessionId;

    public LoginControl(boolean flag,Utente utente,ArrayList<Prenotazione> prenotazioni,String sessionId){
        this.flag = flag;
        this.utente = utente;
        this.prenotazioni = prenotazioni;
        this. sessionId = sessionId;
    }

    public boolean getFlag(){
        return flag;
    }

    public Utente getUtente(){
        return utente;
    }

    public String getSessionId(){
        return sessionId;
    }

    public ArrayList<Prenotazione> getPrenotazioni(){
        return prenotazioni;
    }


}
