package android.example.mas;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class User implements Parcelable {
    private String mail;
    private String username;
    private String id;
    private String status;
    private int delete;
    private String lastMessageDate;
    private String countNoRedMessage;
    private String isGroupChat;
    private String avatarPuth;
    private String lastMessage;

    public String getIsGroupChat() {
        return isGroupChat;
    }

    public void setIsGroupChat(String isGroupChat) {
        this.isGroupChat = isGroupChat;
    }

    public String getAvatarPuth() {
        return avatarPuth;
    }

    public void setAvatarPuth(String avatarPuth) {
        this.avatarPuth = avatarPuth;
    }
    public String getCountNoRedMessage() {
        return countNoRedMessage;
    }

    public void setCountNoRedMessage(String countNoRedMessage) {
        this.countNoRedMessage = countNoRedMessage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }



    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(String lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public int getDelete() {
        return delete;
    }

    public void setDelete(int delete) {
        this.delete = delete;
    }

    public User(Parcel in) {
        String[] data = new String[6];
        in.readStringArray(data);
        mail= data[0];
        username = data[1];
        id = data[2];
        status = data[3];
        avatarPuth = data[4];
        isGroupChat = data[5];
        delete = 0;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public User(String mail) {
        this.mail = mail;
    }



    public User(String mail, String username, String id) {
        this.mail = mail;
        this.username = username;
        this.id = id;
    }

    public User() {
    }

    public User(String mail, String username, String id, String status) {
        this.mail = mail;
        this.username = username;
        this.id = id;
        this.status = status;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {mail,username,id,status, avatarPuth, isGroupChat});
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
