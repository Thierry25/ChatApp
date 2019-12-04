package marcelin.thierry.chatapp.classes;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Map;

public class Status implements Serializable, Comparable<Status> {
    private boolean isSeen;
    private Map<String, Object> seenBy;
    private String content;
    private long timestamp;
    private String phoneNumber;
    private String nameStoredInPhone;
    private boolean allSeen;
    private String type;
    private String id;
    private String from;
    private String textEntered;

    public Status(){}

    public Status(boolean isSeen) {
        this.isSeen = isSeen;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    @Exclude
    public String getNameStoredInPhone() {
        return nameStoredInPhone;
    }

    @Exclude
    public void setNameStoredInPhone(String nameStoredInPhone) {
        this.nameStoredInPhone = nameStoredInPhone;
    }

    @Exclude
    public boolean areAllSeen() {
        return allSeen;
    }

    @Exclude
    public void setAllSeen(boolean allSeen) {
        this.allSeen = allSeen;
    }

    public Map<String, Object> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(Map<String, Object> seenBy) {
        this.seenBy = seenBy;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTextEntered() {
        return textEntered;
    }

    public void setTextEntered(String textEntered) {
        this.textEntered = textEntered;
    }

    @Exclude
    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Exclude
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Status)) { return false; }
        Status s = (Status) obj;
        return s.getId().equals(this.getId());
    }

    @Exclude
    @Override
    public int compareTo(Status otherStatus) {
        return Long.compare(this.timestamp, otherStatus.getTimestamp());
    }
}

