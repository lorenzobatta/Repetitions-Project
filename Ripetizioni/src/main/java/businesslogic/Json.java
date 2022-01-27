package businesslogic;

import java.util.ArrayList;

public class Json {

    private final String timestamp;
    private final int status;
    private final String text;
    private ArrayList<?> data;
    private LoginControl loginControl;

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }

    public ArrayList<?> getData() {
        return data;
    }

    public LoginControl getLoginControl(){
        return loginControl;
    }

    public Json(String timestamp, int status, String text, ArrayList<?> data) {
        this.timestamp = timestamp;
        this.status = status;
        this.text = text;
        this.data = data;
    }
    public Json(String timestamp, int status, String text, LoginControl data) {
        this.timestamp = timestamp;
        this.status = status;
        this.text = text;
        this.loginControl = data;
    }

    public Json(String timestamp, int status, String text){
        this.timestamp = timestamp;
        this.status = status;
        this.text = text;
    }

}
