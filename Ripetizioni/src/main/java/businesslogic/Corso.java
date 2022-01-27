package businesslogic;

public class Corso {
    private final int id;
    private final String title;

    public Corso(int id, String titolo ){
        this.id = id;
        this.title = titolo; }

    public String getTitle(){
        return title;
    }

    public int getId(){
        return id;
    }

    public String toString(){
        return "CORSO: "+ title + ". ID = "+ id ;
    }

}
