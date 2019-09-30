package android.example.mas;

import android.support.constraint.ConstraintLayout;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


// FLAG =   0  -  ТЕКСТОВОЕ СООБЩЕНИЕ    ||  1 - ГОЛОСОВОЕ СООБЩЕНИЕ   ||  2 - ИЗОБРАЖЕНИЕ
// READ =   0  -  СООБЩЕНИЕ НЕ ПРОЧИТАНО ||  1 - СООБЩЕНИЕ ПРОЧИТАНО



public class Message {
   private String message;
   private String time;
   private int flag = 0;
   private String read;
    private String id;
    private String msgUsername;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMsgUsername() {
        return msgUsername;
    }

    public void setMsgUsername(String msgUsername) {
        this.msgUsername = msgUsername;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public Message() {
    }

}
