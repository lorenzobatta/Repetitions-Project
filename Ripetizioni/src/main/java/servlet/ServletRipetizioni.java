package servlet;

import businesslogic.*;
import persistence.DAO;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.google.gson.Gson;

@WebServlet(name = "ServletRipetizioni", urlPatterns = {"/ServletRipetizioni"})
public class ServletRipetizioni extends HttpServlet {

    public void init(ServletConfig conf) throws ServletException{
        DAO.registerDriver();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        System.out.println("Prima di action. Action =" + request.getParameter("action"));
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession s = request.getSession();
        String jsessionID = s.getId();


        switch (request.getParameter("action")) {

            case "checkLogged":
                if(!s.isNew()){

                    Utente u = DAO.queryUtente((int) s.getAttribute("userId"));
                    LoginControl control = new LoginControl(true, u,DAO.queryAllPrenotazioniForUser(u.getId()),jsessionID);
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", control)));
                }
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "OK")));
                break;

            case "login":
                String username = request.getParameter("username");
                String password = request.getParameter("password");

                request.setAttribute("user", username);
                request.setAttribute("pass", password);

                Utente u = DAO.queryLoginUtente(username, password);
                if (u == null) {
                    System.out.println("Utente non esistente");
                    LoginControl loginfailed = new LoginControl(false, null,null,jsessionID);
                    Json j = new Json(LocalDateTime.now().toString(), 200, "OK", loginfailed);
                    out.print(new Gson().toJson(j));
                    break;
                }
                else {
                    u.printUtente();


                    System.out.println("JSessionID:" + jsessionID);
                    s.setAttribute("account", u.getAccount());
                    s.setAttribute("userId", u.getId());
                    s.setAttribute("role",u.getRole());


                    s.setMaxInactiveInterval(5 * 60);
                    LoginControl control = new LoginControl(true, u,DAO.queryAllPrenotazioniForUser(u.getId()),jsessionID);

                    Json j = new Json(LocalDateTime.now().toString(), 200, "OK", control);
                    out.print(new Gson().toJson(j));
                }
                break;

            case "logout" :
                s.invalidate();
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                break;

            case "prenotaRipetizione" :
                System.out.println("dentro prenota ripetizione");
                System.out.println(request.getParameter("corso"));
                System.out.println(request.getParameter("docente"));
                System.out.println(request.getParameter("giorno"));
                System.out.println(request.getParameter("slot"));
                int idCorso = Integer.parseInt(request.getParameter("corso"));
                int idDoc = Integer.parseInt(request.getParameter("docente"));
                int idUtente = (int) s.getAttribute("userId");
                //int idUtente = Integer.parseInt(request.getParameter("userId"));

                System.out.println(idUtente);
                DAO.insertPrenotazione(idCorso,idDoc,idUtente,request.getParameter("giorno"),request.getParameter("slot"));

                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                break;

            case "viewMyPrenotationCorsi" :
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK",DAO.queryActiveCorsiForUtente((int) s.getAttribute("userId")))));
                break;

            case "getMyActivePrenotation" :

                int uId =(int) s.getAttribute("userId");
                System.out.println("uId" + uId);
                ArrayList<Prenotazione> userPrenotation = DAO.queryAllPrenotazioniForUser(uId);
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", userPrenotation)));
                break;

            case "getMyStoricoPrenotation" :
                int userId = (int)s.getAttribute("userId");
                ArrayList<Prenotazione> userStoricoPrenotazioni = DAO.queryAllStoricoPrenotazioneForUser(userId);
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", userStoricoPrenotazioni)));
                break;

            case "getMyPrenotazioniEffettuate" :
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", DAO.queryPrenotazioniEffettuateForUser((int)s.getAttribute("userId")))));
                break;

            case "getMyPrenotazioniDisdette" :
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", DAO.queryPrenotazioniCancellateForUser((int)s.getAttribute("userId")))));
                break;

            case "disdiciPrenotazione" :
                DAO.deletePrenotaizone(Integer.parseInt(request.getParameter("corso")),Integer.parseInt(request.getParameter("docente")),
                        (int) s.getAttribute("userId"),request.getParameter("giorno"),request.getParameter("slot"),"Disdetta");
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                break;
            case "segnaPrenotazioneEffettuata" :
                DAO.deletePrenotaizone(Integer.parseInt(request.getParameter("corso")),Integer.parseInt(request.getParameter("docente")),
                        (int) s.getAttribute("userId"),request.getParameter("giorno"),request.getParameter("slot"),"Effettuata");
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                break;

            case "getAllActivePrenotationForAdmin" :
                if(s.getAttribute("role").equals("admin")){
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", DAO.queryAllPrenotazioniForAdmin())));

                }else{
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                }
                break;

            case "getStoricoPrenotationEffettuateForAdmin" :
                if(s.getAttribute("role").equals("admin"))
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", DAO.queryAllPrenotazioniEffettuateForAdmin())));

                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;

            case "getStoricoPrenotationDisdetteForAdmin" :
                if(s.getAttribute("role").equals("admin"))
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", DAO.queryAllPrenotazioniDisdetteForAdmin())));

                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;


            case "corsi":
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", DAO.queryAllCorsi())));
                break;

            case "getInsegnantiPrenotazioniEffettuate":
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK",DAO.queryActiveDocentiForUtente((int) s.getAttribute("userId"),request.getParameter("corso")))));
                break;

            case "insegnanti":

                out.print(new Gson().toJson(DAO.queryAllDocentiByCorsi(request.getParameter("corso"))));
                break;

            case "getRipetizioniForDocente":
                System.out.println("dentro ripetizioni for docente");
                String d = request.getParameter("docente");
                String c = request.getParameter("corso");

                int idDocente = Integer.parseInt(d);
                int idCor = Integer.parseInt(c);

                ArrayList<Prenotazione> p = DAO.queryAllRipetizioniForDocenteForCorso(idCor,idDocente);
                Json j = new Json(LocalDateTime.now().toString(), 200, "OK", p);
                out.print(new Gson().toJson(j));

                break;

            case "queryAllDocenti":
                 out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK",DAO.queryAllDocenti())));
                 break;


            case "addDocente" :
                if(s.getAttribute("role").equals("admin")) {
                    if (DAO.insertDocente(request.getParameter("nome"), request.getParameter("cognome"))) {
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                    }
                    else
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "ERRORE IMPREVISTO")));
                }
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;


            case "deleteDocente" :
                if(s.getAttribute("role").equals("admin")) {
                    if (DAO.deleteDocente(Integer.parseInt(request.getParameter("idDocente")))) {
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                    }
                    else
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "ERRORE IMPREVISTO")));
                }
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;


            case "addCorso":
                if(s.getAttribute("role").equals("admin")) {
                    if (DAO.insertCorso(request.getParameter("titolo"))) {
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                    }
                    else
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "ERRORE IMPREVISTO")));
                }
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;

            case "deleteCorso" :
                if(s.getAttribute("role").equals("admin")) {
                    if (DAO.deleteCorso(Integer.parseInt(request.getParameter("idCorso")))) {
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                    }
                    else
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "ERRORE IMPREVISTO")));
                }
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;

            case "queryDocentiNotAssignedAtCorso" :
                System.out.println(Integer.parseInt(request.getParameter("idCorso")));
                if(s.getAttribute("role").equals("admin"))
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", DAO.queryDocentiNotAssignedAtCorso(Integer.parseInt(request.getParameter("idCorso"))))));
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;


            case "queryAssignmentCorsoDocente" :
                if(s.getAttribute("role").equals("admin"))
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK",DAO.queryAllDocentiForCorso())));
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;

            case "assignCorsoDocente" :

                if(s.getAttribute("role").equals("admin")) {
                    System.out.println("idCOrso = " + request.getParameter("idCorso") + " idDocente = "+ request.getParameter("idDocente") );
                    if (DAO.assignDocenteCorso(Integer.parseInt(request.getParameter("idDocente")),Integer.parseInt(request.getParameter("idCorso")))) {
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                    }
                    else
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "ERRORE IMPREVISTO")));
                }
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;


            case "deleteAssignmentCorsoDocente" :
                if(s.getAttribute("role").equals("admin")) {
                    if (DAO.deleteAssignmentDocenteCorso(Integer.parseInt(request.getParameter("idDocente")),Integer.parseInt(request.getParameter("idCorso")))) {
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                    }
                    else
                        out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "ERRORE IMPREVISTO")));
                }
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "Errore: non hai i permessi per accedere a questa funzionalita")));
                break;

                // PRENOTAZIONI PER ANDROID

            case "getRipetizioniDisponibili":
                ArrayList<Prenotazione> prenotAndroid = DAO.queryAllRipetizioniForAndroid(request.getParameter("corso"),request.getParameter("docente"));
                if(prenotAndroid.isEmpty())
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 404, "OK", prenotAndroid)));
                else
                    out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", prenotAndroid)));
                break;

            case "prenotaRipetizioneForAndroid" :
                System.out.println("dentro prenota ripetizioneForAndroid");
                System.out.println(request.getParameter("corso"));
                System.out.println(request.getParameter("docente"));
                System.out.println(request.getParameter("giorno"));
                System.out.println(request.getParameter("slot"));

                DAO.insertPrenotazione(Integer.parseInt(request.getParameter("corso")),Integer.parseInt(request.getParameter("docente")),Integer.parseInt(request.getParameter("utente")),request.getParameter("giorno"),request.getParameter("slot"));

                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK",new ArrayList<>())));
                break;

            case "getMyActivePrenotationForAndroid" :

                ArrayList<Prenotazione> userPrenotationAndroid = DAO.queryAllPrenotazioniForUser(Integer.parseInt(request.getParameter("userId")));
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", userPrenotationAndroid)));
                break;

            case "segnaPrenotazioneEffettuataForAndroid" :
                DAO.deletePrenotaizone(Integer.parseInt(request.getParameter("corso")),Integer.parseInt(request.getParameter("docente")),
                        Integer.parseInt(request.getParameter("utente")),request.getParameter("giorno"),request.getParameter("slot"),"Effettuata");
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                break;

            case "disdiciPrenotazioneForAndroid" :
                DAO.deletePrenotaizone(Integer.parseInt(request.getParameter("corso")),Integer.parseInt(request.getParameter("docente")),
                        Integer.parseInt(request.getParameter("utente")),request.getParameter("giorno"),request.getParameter("slot"),"Disdetta");
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK")));
                break;

            case "getMyPrenotazioniDisdetteForAndroid" :

                ArrayList<Prenotazione> userPrenotationDisdetteAndroid = DAO.queryPrenotazioniCancellateForUser(Integer.parseInt(request.getParameter("userId")));
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", userPrenotationDisdetteAndroid)));
                break;

            case "getMyPrenotazioniEffettuateForAndroid" :
                out.print(new Gson().toJson(new Json(LocalDateTime.now().toString(), 200, "OK", DAO.queryPrenotazioniEffettuateForUser(Integer.parseInt(request.getParameter("userId"))))));
                break;


            default:
                break;
        }
    }
}
