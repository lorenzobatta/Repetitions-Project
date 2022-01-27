package persistence;

import businesslogic.*;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class DAO {

    private static final String url = "jdbc:mysql://localhost:3306/ripetizioni?serverTimezone=UTC";
    private static final String user = "root";
    private static final String password = "";

    private static int lastId;

    public static void registerDriver() {
        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            //System.out.println("Driver correttamente registrato");
        } catch (SQLException e) {
            System.out.println("Errore: " + e.getMessage());
        }
    }

    public static ArrayList<Utente> getAllUtenti() {
        Connection conn1 = null;
        ArrayList<Utente> out = new ArrayList<>();
        try {
            conn1 = DriverManager.getConnection(url, user, password);
            //if (conn1 != null) {System.out.println("Connected to the database");}
            Statement st = conn1.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM UTENTI");
            while (rs.next()) {
                Utente p = new Utente(rs.getInt("id"),rs.getString("account"),
                        rs.getString("password"), rs.getString("ruolo"));
                out.add(p);}
        } catch (SQLException e) {System.out.println(e.getMessage());}
        return out;
    }

    public static Utente queryUtente(int id){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM UTENTI WHERE ID = "+ id);
            if(rs.next()) {
                Utente u = new Utente(rs.getInt("id"), rs.getString("account"), rs.getString("password"), rs.getString("ruolo"));
                st.close();
                conn.close();
                return u;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static Utente queryLoginUtente(String acc, String pass){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM UTENTI WHERE ACCOUNT = '"+ acc + "' AND PASSWORD = '"+ pass+"'");
            if(rs.next()) {
                Utente u = new Utente(rs.getInt("id"), rs.getString("account"), rs.getString("password"), rs.getString("ruolo"));
                st.close();
                conn.close();
                return u;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Corso> queryActiveCorsiForUtente(int idUtente){
        Connection conn = null;
        ArrayList<Integer> idCorsi = new ArrayList<>();
        ArrayList<Corso> corsi = new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI WHERE IDUTENTE = "+ idUtente);
            while(rs.next()) {
                if(!idCorsi.contains(rs.getInt("idCorso"))) {
                    idCorsi.add(rs.getInt("idCorso"));
                }

            }
            for(Integer i : idCorsi)
                corsi.add(DAO.queryCorso(i));
            st.close();
            conn.close();
            return corsi;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return corsi;

    }

    public static ArrayList<Docente> queryActiveDocentiForUtente(int idUtente,String corso){
        Connection conn = null;
        ArrayList<Integer> idDocenti = new ArrayList<>();
        ArrayList<Docente> docenti = new ArrayList<>();
        int idCorso = queryIdForCorso(corso);

        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI WHERE IDUTENTE = "+ idUtente);
            while(rs.next()) {
                if(!idDocenti.contains(rs.getInt("idDocente"))) {
                    idDocenti.add(rs.getInt("idDocente"));
                }

            }
            for(Integer i : idDocenti) {
                Docente d = DAO.queryDocentiInsegnaCorsoId(i, idCorso);
                if(d != null)
                    docenti.add(d);
            }
            st.close();
            conn.close();
            return docenti;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return docenti;

    }

    public static Docente queryDocentiInsegnaCorsoId(int idDocente,int idCorso){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM INSEGNA WHERE IDCORSO = "+ idCorso + " AND IDDOCENTE = "+idDocente);
            if(rs.next()) {
                return queryDocente(rs.getInt("idDocente"));

            }

            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Docente> queryAllDocenti() {
        Connection conn = null;
        ArrayList<Docente> doc = new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM DOCENTE ");
            while(rs.next()) {
                Docente d = new Docente(rs.getInt("id"),rs.getString("nome"),rs.getString("cognome"));
                doc.add(d);

            }

            st.close();
            conn.close();
            return doc;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static void executeQuery(String query, ResultHandler handler) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                handler.handle(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static int[] executeBatchUpdate(String parametrizedQuery, int itemNumber, BatchUpdateHandler handler) {
        int[] result = new int[0];
        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement ps = conn.prepareStatement(parametrizedQuery, Statement.RETURN_GENERATED_KEYS);
        ) {
            for (int i = 0; i < itemNumber; i++) {
                handler.handleBatchItem(ps, i);
                ps.addBatch();
            }
            result = ps.executeBatch();
            ResultSet keys = ps.getGeneratedKeys();
            int count = 0;
            while (keys.next()) {
                handler.handleGeneratedIds(keys, count);
                count++;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static int executeUpdate(String update) {
        int result = 0;
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(update, Statement.RETURN_GENERATED_KEYS)) {
            result = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                lastId = rs.getInt(1);
            } else {
                lastId = 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static int getLastId() {
        return lastId;
    }

    public static boolean insertCorso(String titolo){

        /**  IMPLEMENTARE CONTROLLO UTENTE COLLEGATO = ADMIN    */

        Connection conn1 = null;
        try{
            conn1 = DriverManager.getConnection(url, user, password);
            //if (conn1 != null) {System.out.println("Connected to the database");}
            Statement st = conn1.createStatement();
            st.executeUpdate("INSERT INTO CORSI (titolo) VALUES ('"+titolo+"')");
            st.close();
            conn1.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static boolean deleteCorso(int idCorso){
        Corso c = DAO.queryCorso(idCorso);

        Connection conn1 = null;
        try{
            conn1 = DriverManager.getConnection(url, user, password);
            //if (conn1 != null) {System.out.println("Connected to the database");}
            Statement st = conn1.createStatement();
            try {
                st.executeUpdate("DELETE FROM INSEGNA WHERE idCorso =" + idCorso);
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
            try{
                ArrayList<Prenotazione> prenotazioni = DAO.queryAllPrenotazioniForCorso(idCorso);
                for(Prenotazione p : prenotazioni) {
                    st.executeUpdate("DELETE FROM PRENOTAZIONI WHERE idCorso =" + idCorso);
                    st.executeUpdate("INSERT INTO STORICOPRENOTAZIONI (idCorso,idDocente,idUtente,giorno,slot,stato) VALUES('" + idCorso +"', ' "+p.getIdDocente()+"', '"+p.getIdUtente() + "' , '" + p.getGiorno()+ "' , '" + p.getSlot() + "' , '" +"Disdetta" + "') ");
                }
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
            st.executeUpdate("DELETE FROM CORSI WHERE id = " + c.getId());
            st.executeUpdate("INSERT INTO STORICOCORSI (id,titolo) VALUES ('"+c.getId()+"',' "+ c.getTitle() + "' )");
            st.close();
            conn1.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static ArrayList<Prenotazione> queryAllPrenotazioniForCorso(int idCorso){

        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI WHERE IDCORSO = " + idCorso);
            while (rs.next()){
                Prenotazione prenot = new Prenotazione(DAO.queryCorso(idCorso),DAO.queryDocente(rs.getInt("idDocente")),DAO.queryUtente(rs.getInt("idUtente")),rs.getString("giorno"),rs.getString("slot"));
                p.add(prenot);
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }

    public static Corso queryCorso(int id){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM CORSI WHERE ID = "+ id);
            if(rs.next()) {
                Corso c = new Corso(rs.getInt("id"), rs.getString("titolo"));
                st.close();
                conn.close();
                return c;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static Corso queryAllCorsii(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM CORSI");
            if(rs.next()) {
                Corso c = new Corso(rs.getInt("id"), rs.getString("titolo"));
                st.close();
                conn.close();
                return c;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Corso> queryAllStoricoCorsi(){
        Connection conn = null;
        ArrayList<Corso> corsi = new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM STORICOCORSI");
            if(rs.next()) {
                Corso c = new Corso(rs.getInt("id"), rs.getString("titolo"));
                st.close();
                conn.close();
                corsi.add(c);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return corsi;
    }

    public static boolean insertDocente(String nome, String cognome){
        Connection conn1 = null;
        try{
            conn1 = DriverManager.getConnection(url, user, password);
            //if (conn1 != null) {System.out.println("Connected to the database");}
            Statement st = conn1.createStatement();
            st.executeUpdate("INSERT INTO DOCENTE (nome,cognome) VALUES ('" + nome +"', '"+cognome + "' )");
            st.close();
            conn1.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static boolean deleteDocente(int idDocente){
        Connection conn1 = null;
        Docente d = DAO.queryDocente(idDocente);
        try{
            conn1 = DriverManager.getConnection(url, user, password);
            //if (conn1 != null) {System.out.println("Connected to the database");}
            Statement st = conn1.createStatement();
            try {
                st.executeUpdate("DELETE FROM INSEGNA WHERE idDocente =" + d.getId());
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
            try{
                ArrayList<Prenotazione> prenotazioni = DAO.queryAllPrenotazioniForDocente(idDocente);
                for(Prenotazione p : prenotazioni) {
                    st.executeUpdate("DELETE FROM PRENOTAZIONI WHERE idDocente =" + idDocente);
                    st.executeUpdate("INSERT INTO STORICOPRENOTAZIONI (idCorso,idDocente,idUtente,giorno,slot,stato) VALUES('" + idDocente +"', ' "+p.getIdDocente()+"', '"+p.getIdUtente() + "' , '" + p.getGiorno()+ "' , '" + p.getSlot() + "' , '" +"Disdetta" + "') ");
                }
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
            st.executeUpdate("DELETE FROM DOCENTE WHERE id = " + d.getId());

            st.executeUpdate("INSERT INTO STORICODOCENTE (id,nome,cognome) VALUES ('"+d.getId()+"',' "+ d.getName() +"', '"+d.getSurname() + "' )");

            st.close();
            conn1.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static ArrayList<Prenotazione> queryAllPrenotazioniForDocente(int idDocente){

        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI WHERE IDDOCENTE = " + idDocente);
            while (rs.next()){
                Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")),DAO.queryDocente(idDocente),DAO.queryUtente(rs.getInt("idUtente")),rs.getString("giorno"),rs.getString("slot"));
                p.add(prenot);
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }

    public static Docente queryDocente(int id){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM DOCENTE WHERE ID = "+ id);
            if(rs.next()) {
                Docente d = new Docente(rs.getInt("id"), rs.getString("nome"), rs.getString("cognome"));
                st.close();
                conn.close();
                return d;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static Docente queryDocenteNelloStorico(int id){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM STORICODOCENTE WHERE ID = "+ id);
            if(rs.next()) {
                Docente d = new Docente(rs.getInt("id"), rs.getString("nome"), rs.getString("cognome"));
                st.close();
                conn.close();
                return d;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Docente> queryAllStoricoDocente(){
        Connection conn = null;
        ArrayList<Docente> docenti= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            //if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM STORICODOCENTE  ");
            while (rs.next()) {
                Docente d = new Docente(rs.getInt("id"), rs.getString("nome"), rs.getString("cognome"));
                st.close();
                conn.close();
                docenti.add(d);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return docenti;
    }

    public static boolean assignDocenteCorso(int idDocente, int idCorso){
        Connection conn1 = null;
        Corso c = queryCorso(idCorso);
        try{
            conn1 = DriverManager.getConnection(url, user, password);
            //if (conn1 != null) {System.out.println("Connected to the database");}
            Statement st = conn1.createStatement();
            st.executeUpdate("INSERT INTO INSEGNA (idCorso,titoloCorso,idDocente) VALUES('" + c.getId() +"', '"+c.getTitle()+"', '"+idDocente + "' )");
            st.close();
            conn1.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static ArrayList<Docente> queryDocentiNotAssignedAtCorso(int idCorso){
        Corso c = DAO.queryCorso(idCorso);
        Connection conn = null;
        ArrayList<Docente> doc = new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url, user, password);
            //if (conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM INSEGNA WHERE IDCORSO = " + idCorso );
            while(rs.next()){
                Docente d = DAO.queryDocente(rs.getInt("idDocente"));
                doc.add(d);
            }
            return docentiNotAssigned(doc);
        }catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return doc;
    }

    private static ArrayList<Docente> docentiNotAssigned(ArrayList<Docente> doc){
        ArrayList<Docente> allDocenti = DAO.queryAllDocenti();
        boolean flag = false;
        ArrayList<Docente> result = new ArrayList<>();
        for(Docente d : allDocenti){
            for(Docente d2 : doc){
                if(d.getId() == d2.getId())
                    flag = true;
            }
            if(!flag)
                result.add(d);
            flag = false;
        }
        return result;
    }

    public static boolean deleteAssignmentDocenteCorso(int idDocente, int idCorso){
        Connection conn1 = null;
        try{
            conn1 = DriverManager.getConnection(url, user, password);
            //if (conn1 != null) {System.out.println("Connected to the database");}
            Statement st = conn1.createStatement();
            String s="DELETE FROM INSEGNA WHERE idDocente = " + idDocente + " AND idCorso = " + idCorso;
            st.executeUpdate(s);
            st.close();
            conn1.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static ArrayList<Insegna> queryAllDocentiForCorso(){

        ArrayList<Insegna> associazione = new ArrayList<>();
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url, user, password);
            //if (conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM INSEGNA ");
            while(rs.next()){
                Insegna i = new Insegna(DAO.queryDocente(rs.getInt("idDocente")),DAO.queryCorso(rs.getInt("idCorso")));
                associazione.add(i);
                System.out.println(i.getCorso().getTitle());
            }
            st.close();
            conn.close();
            return associazione;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return associazione;
    }

    public static void queryAllInsegnamenti(){
        Connection conn1 = null;
        //ArrayList<Corso> corsi = new ArrayList<>();
        try {
            conn1 = DriverManager.getConnection(url, user, password);
            //if (conn1 != null) {System.out.println("Connected to the database");}
            Statement st = conn1.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM CORSI");
            while (rs.next()) {
                Corso c = new Corso(rs.getInt("id"),rs.getString("titolo"));
                //corsi.add(c);
                System.out.println(c.toString());
                Statement st2 = conn1.createStatement();
                ResultSet r = st2.executeQuery("SELECT * FROM INSEGNA WHERE idCorso = "+ c.getId());
                while(r.next()){
                    int idDocente=r.getInt("idDocente");
                    Statement st3 = conn1.createStatement();
                    ResultSet res= st3.executeQuery("SELECT * FROM DOCENTE WHERE id = "+idDocente);
                    while (res.next()){
                        Docente d= new Docente(res.getInt("id"),res.getString("nome"),res.getString("cognome"));
                        System.out.println(d.toString() + ". Insegno : " + c.getTitle());
                    }
                }
            }
            conn1.close();
        } catch (SQLException e) {System.out.println(e.getMessage());}
    }

    public static void insertPrenotazione(int cId, int dId, int uId , String day, String slot){

        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            st.executeUpdate("INSERT INTO PRENOTAZIONI (idCorso,idDocente,idUtente,giorno,slot) VALUES('" + cId +"', ' "+dId+"', '"+uId + "' , '" + day + "' , '" + slot+ "' ) ");
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public static void deletePrenotaizone(int corsoId, int docenteId, int utenteId, String day, String slot,String stato){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            /*
            System.out.println("DELETE FROM PRENOTAZIONI WHERE IDCORSO = "+corsoId +
                    " AND IDDOCENTE = "+docenteId + " AND IDUTENTE = "+ utenteId +
                    " AND GIORNO = '"+ day+"' AND SLOT =" +slot);

             */
            st.executeUpdate("DELETE FROM PRENOTAZIONI WHERE IDCORSO = "+corsoId +
                    " AND IDDOCENTE = "+docenteId + " AND IDUTENTE = "+ utenteId +
                    " AND GIORNO = '"+ day+"' AND SLOT = '" +slot + "'");

            st.executeUpdate("INSERT INTO STORICOPRENOTAZIONI (idCorso,idDocente,idUtente,giorno,slot,stato) VALUES('" + corsoId +"', ' "+docenteId+"', '"+utenteId + "' , '" + day + "' , '" + slot+ "' , '" +stato + "') ");
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static Prenotazione queryPrenotazione(Corso c, Docente d, Utente u , String day, String slot){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs=st.executeQuery("SELECT * FROM PRENOTAZIONI WHERE IDCORSO = "+ c.getId() + " AND idDocente = "+ d.getId() +" AND idUtente = " + u.getId() +" AND giorno = '" + day + "' AND slot = "+slot);
            while (rs.next()){
                //Prenotazione p= new Prenotazione(DAO.queryCorso(rs.getInt("")))
            }
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Prenotazione> queryAllPrenotazioni(){
        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI ");
            while (rs.next()){
                Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")),DAO.queryDocente(rs.getInt("idDocente")),rs.getString("giorno"),rs.getString("slot"));
                p.add(prenot);
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }

    public static ArrayList<Prenotazione> queryAllPrenotazioniForAdmin(){
        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI ");
            while (rs.next()){
                Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")),DAO.queryDocente(rs.getInt("idDocente")),DAO.queryUtente(rs.getInt("idUtente")),rs.getString("giorno"),rs.getString("slot"));
                p.add(prenot);
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }

    public static ArrayList<Prenotazione> queryAllPrenotazioniEffettuateForAdmin(){
        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM STORICOPRENOTAZIONI WHERE STATO = '" + "Effettuata" +"'");
            Docente d;
            while (rs.next()){
                d = DAO.queryDocente(rs.getInt("idDocente"));
                if(d!= null) {
                    Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")), d, DAO.queryUtente(rs.getInt("idUtente")), rs.getString("giorno"), rs.getString("slot"), rs.getString("stato"));
                    p.add(prenot);
                }
                else{
                    d = DAO.queryDocenteNelloStorico(rs.getInt("idDocente"));
                    Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")), d, DAO.queryUtente(rs.getInt("idUtente")), rs.getString("giorno"), rs.getString("slot"), rs.getString("stato"));
                    p.add(prenot);
                }
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }

    public static ArrayList<Prenotazione> queryAllPrenotazioniDisdetteForAdmin(){
        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM STORICOPRENOTAZIONI WHERE STATO = '" + "Disdetta" +"'");
            Docente d;
            while (rs.next()){
                d = DAO.queryDocente(rs.getInt("idDocente"));
                if(d!= null) {
                    Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")), d, DAO.queryUtente(rs.getInt("idUtente")), rs.getString("giorno"), rs.getString("slot"), rs.getString("stato"));
                    p.add(prenot);
                }
                else{
                    d = DAO.queryDocenteNelloStorico(rs.getInt("idDocente"));
                    Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")), d, DAO.queryUtente(rs.getInt("idUtente")), rs.getString("giorno"), rs.getString("slot"), rs.getString("stato"));
                    p.add(prenot);
                }
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }

    public static ArrayList<Prenotazione> queryAllPrenotazioniForUser(int userId){

        Utente u = DAO.queryUtente(userId);
        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI WHERE IDUTENTE = " + userId);
            while (rs.next()){
                Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")),DAO.queryDocente(rs.getInt("idDocente")),u,rs.getString("giorno"),rs.getString("slot"));
                p.add(prenot);
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }

    public static ArrayList<Prenotazione> queryAllStoricoPrenotazioneForUser(int userId){
        Utente u = DAO.queryUtente(userId);
        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM STORICOPRENOTAZIONI WHERE IDUTENTE = " + userId);
            while (rs.next()){
                Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")),DAO.queryDocente(rs.getInt("idDocente")),u,rs.getString("giorno"),rs.getString("slot"),rs.getString("stato"));
                p.add(prenot);
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }
    public static ArrayList<Prenotazione> queryPrenotazioniEffettuateForUser(int userId){
        Utente u = DAO.queryUtente(userId);
        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM STORICOPRENOTAZIONI WHERE IDUTENTE = " + userId  + " AND STATO = '"+"Effettuata"+"'");
            Docente d;
            while (rs.next()){
                d = DAO.queryDocente(rs.getInt("idDocente"));
                if(d!= null) {
                    Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")), d, DAO.queryUtente(rs.getInt("idUtente")), rs.getString("giorno"), rs.getString("slot"), rs.getString("stato"));
                    p.add(prenot);
                }
                else{
                    d = DAO.queryDocenteNelloStorico(rs.getInt("idDocente"));
                    Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")), d, DAO.queryUtente(rs.getInt("idUtente")), rs.getString("giorno"), rs.getString("slot"), rs.getString("stato"));
                    p.add(prenot);
                }
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }
    public static ArrayList<Prenotazione> queryPrenotazioniCancellateForUser(int userId){
        Utente u = DAO.queryUtente(userId);
        Connection conn = null;
        ArrayList<Prenotazione> p= new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM STORICOPRENOTAZIONI WHERE IDUTENTE = " + userId  + " AND STATO = '"+"Disdetta"+"'");
            Docente d;
            while (rs.next()){
                d = DAO.queryDocente(rs.getInt("idDocente"));
                if(d!= null) {
                    Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")), d, DAO.queryUtente(rs.getInt("idUtente")), rs.getString("giorno"), rs.getString("slot"), rs.getString("stato"));
                    p.add(prenot);
                }
                else{
                    d = DAO.queryDocenteNelloStorico(rs.getInt("idDocente"));
                    Prenotazione prenot = new Prenotazione(DAO.queryCorso(rs.getInt("idCorso")), d, DAO.queryUtente(rs.getInt("idUtente")), rs.getString("giorno"), rs.getString("slot"), rs.getString("stato"));
                    p.add(prenot);
                }
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return p;
    }

    public static ArrayList<Prenotazione> queryAllRipetizioniForDocenteForCorso(Corso c, Docente d){
        Connection conn = null;
        ArrayList<Prenotazione> prenotazioni = new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI WHERE idDocente = " +d.getId());
            while(rs.next()){
                Prenotazione p = new Prenotazione(c,d,rs.getString("giorno"),rs.getString("slot"));
                prenotazioni.add(p);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return freeSlot(c,d,prenotazioni);
    }

    private static ArrayList<Prenotazione> freeSlot(Corso c, Docente d, ArrayList<Prenotazione> p){
        ArrayList<Prenotazione> prenot = new ArrayList<>();
        boolean flag = false;
        String[] g = {"Lunedi","Martedi","Mercoledi","Giovedi","Venerdi"};
        String[] slots = {"15:00-16:00","16:00-17:00","17:00-18:00","18:00-19:00"};
        for(String s : g){
            for(String slot : slots){
                for(Prenotazione temp : p) {
                    if (s.equals(temp.getGiorno()) && slot.equals(temp.getSlot())) {
                        flag = true;
                    }
                }
                if(!flag)
                    prenot.add(new Prenotazione(c, d, s, slot));
                else
                    flag = false;

            }
        }
        return prenot;
    }


    /**  Query usando il JSON **/

    /*
    public static Json queryAllCorsi(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,password);
            if(conn != null) {System.out.println("Connected to the database");}
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM CORSI");
            ArrayList<Corso> listCorsi = new ArrayList<>();
            while(rs.next()) {
                listCorsi.add(new Corso(rs.getInt("id"),  rs.getString("titolo")));
            }
            st.close();
            conn.close();
            return new Json(LocalDateTime.now().toString(), 200, "OK", listCorsi);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

      */

    public static ArrayList<Corso> queryAllCorsi(){

        ArrayList<Corso> corsoArrayList = new ArrayList<>();
        String query = "SELECT * FROM corsi";
        executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Corso corso = new Corso(rs.getInt("id"),  rs.getString("titolo"));
                corsoArrayList.add(corso);
            }
        });
        return corsoArrayList;
    }

    public static int queryIdForCorso(String nome_corso){

        final int[] id = new int[1];
        String query = "SELECT id FROM corsi WHERE titolo = '" + nome_corso +"'";

        executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                id[0] = rs.getInt("id");
            }
        });
        return id[0];
    }

    public static ArrayList<Prenotazione> queryAllRipetizioniForDocenteForCorso(int idCorso, int idDocente){
        Connection conn = null;
        ArrayList<Prenotazione> prenotazioni = new ArrayList<>();
        Docente d = DAO.queryDocente(idDocente);
        Corso c = DAO.queryCorso(idCorso);
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI WHERE idDocente = " +idDocente);
            while(rs.next()){
                Prenotazione p = new Prenotazione(c,d,rs.getString("giorno"),rs.getString("slot"));
                prenotazioni.add(p);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return freeSlot(c,d,prenotazioni);
    }

    public static Json queryAllDocentiByCorsi(String nome_corso) {

        int id = queryIdForCorso(nome_corso);

        String query = "SELECT idDocente FROM insegna WHERE idCorso=" +id;

        ArrayList<Integer> idDocentiList = new ArrayList<>();
        ArrayList<Docente> docentiList = new ArrayList<>();

        executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                int idDocente = rs.getInt("idDocente");
                idDocentiList.add(idDocente);
            }
        });

        for (int idDocente : idDocentiList) {
            Docente docente = queryDocente(idDocente);
            docentiList.add(docente);
        }

        if(!docentiList.isEmpty())
            return new Json(LocalDateTime.now().toString(), 200, "OK", docentiList);
        else
            return new Json(LocalDateTime.now().toString(), 404, "NOT FOUND", docentiList);
    }

    public static ArrayList<Prenotazione> queryAllRipetizioniForAndroid(String corso,String docente){
        int idCorso= DAO.queryIdForCorso(corso);
        Corso c = DAO.queryCorso(idCorso);
        Docente d = DAO.queryDocente(DAO.queryIdForDocente(docente));

        Connection conn = null;
        ArrayList<Prenotazione> prenotazioni = new ArrayList<>();
        try{
            conn = DriverManager.getConnection(url,user,password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM PRENOTAZIONI WHERE idDocente = " +d.getId());
            while(rs.next()){
                Prenotazione p = new Prenotazione(c,d,rs.getString("giorno"),rs.getString("slot"));
                prenotazioni.add(p);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return freeSlot(c,d,prenotazioni);
    }

    public static int queryIdForDocente(String docente) {

        final int[] id = new int[1];
        String query = "SELECT id FROM docente WHERE cognome = '" + docente +"'";

        executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                id[0] = rs.getInt("id");
            }
        });
        return id[0];
    }
}


