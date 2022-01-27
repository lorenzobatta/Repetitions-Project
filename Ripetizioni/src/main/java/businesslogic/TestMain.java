package businesslogic;

import persistence.DAO;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TestMain {

    public static void main(String[] args){
        DAO.registerDriver();
        //ArrayList<Prenotazione> provaPreno = DAO.queryAllPrenotazioniForUser(2);
       /* ArrayList<Docente> doc = DAO.queryDocentiNotAssignedAtCorso(7);
        for(Docente d : doc)
            System.out.println(d.getName() + "  " + d.getSurname());*/
        //DAO.queryAllRipetizioniForAndroid();


        /*int idCorso = 6;
        int idDocente = 3;
        Utente u = DAO.queryUtente(2);
        Docente d = DAO.queryDocente(3);
        Corso c = DAO.queryCorso(6);

        Prenotazione pren = new Prenotazione(c,d,u,"Martedi","17:00-18:00");
        //DAO.insertPrenotazione(c,d,u,"Martedi","17:00-18:00");

        ArrayList<Prenotazione> p = DAO.queryAllRipetizioniForDocenteForCorso(idCorso,idDocente);
        for (Prenotazione pre : p)
            pre.printPrenotazioneSenzaUtente();*/
    }
}
