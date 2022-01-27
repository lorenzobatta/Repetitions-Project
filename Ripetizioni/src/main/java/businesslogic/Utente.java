package businesslogic;

public class Utente {

    private final int id;
    private final String account;
    private final String password;
    private final String role;

    public Utente(int id, String acc, String pass, String r){
        this.id=id;
        account=acc;
        password=pass;
        role=r;
    }

    public void printUtente(){
        System.out.println("ID=" + id + " Account=" + account + " Password=" + password + " Ruolo="+ role);
    }

    public String toString(){
        return "ID: "+ id +"  Username: " + account + "  Password: "+password + "  Ruolo: "+role;
    }

    public int getId(){
        return id;
    }

    public String getRole(){
        return role;
    }

    public String getAccount(){
        return account;
    }

}
