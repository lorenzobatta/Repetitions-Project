package businesslogic;

public class Docente {

    private final int id;
    private final String name;
    private final String surname;

    public Docente(int id, String name, String surname){
        this.id=id;
        this.name=name;
        this.surname=surname;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getSurname(){
        return surname;
    }

    public String toString(){
        return "Sono il professore "+name + " " + surname + ". Ho id = "+id;
    }
}
