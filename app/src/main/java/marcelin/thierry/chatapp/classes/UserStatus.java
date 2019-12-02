package marcelin.thierry.chatapp.classes;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class UserStatus implements Serializable {

    private String phoneNumber;
    private List<Status> statusList;
    private String nameStoredInPhone;
    private long timestamp;
    private String content;
    private boolean allSeen;

    public UserStatus(){ }

    public UserStatus(String phoneNumber, List<Status> statusList){
        this.phoneNumber = phoneNumber;
        this.statusList = statusList;
    }

    public UserStatus(String phoneNumber){
        this.phoneNumber = phoneNumber;
        statusList = new ArrayList<>();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Status> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<Status> statusList) {
        this.statusList = statusList;
    }

    public String getNameStoredInPhone() {
        return nameStoredInPhone;
    }

    public void setNameStoredInPhone(String nameStoredInPhone) {
        this.nameStoredInPhone = nameStoredInPhone;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean areAllSeen() {
        return allSeen;
    }

    public void setAllSeen(boolean allSeen) {
        this.allSeen = allSeen;
    }

    @Override
    public int hashCode() {
        return this.phoneNumber.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof UserStatus)) { return false; }
        return ((UserStatus) obj).phoneNumber.equals(this.phoneNumber);
    }


}