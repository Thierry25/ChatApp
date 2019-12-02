package marcelin.thierry.chatapp.classes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Conversation implements Comparable<Conversation> {

    private String id, type, phone_number, from;
    private long timestamp, messageTimestamp;
    private boolean seen;

    @Exclude
    private String lastMessage, profile_image, name;

    @Exclude
    private int unreadMessages;

    @Exclude
    private boolean isSent;

    public Conversation(){}

    public Conversation(String id, String type, String phone_number, long timestamp) {
        this.id = id;
        this.type = type;
        this.phone_number = phone_number;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timeStamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public String getLastMessage() {
        return lastMessage;
    }

    @Exclude
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Exclude
    public String getProfile_image() {
        return profile_image;
    }

    @Exclude
    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    @Exclude
    public String getName() {
        return name;
    }

    @Exclude
    public void setName(String name) {
        this.name = name;
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    @Exclude
    public long getMessageTimestamp() {
        return messageTimestamp;
    }

    @Exclude
    public void setMessageTimestamp(long messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    @Exclude
    public boolean isSeen() {
        return seen;
    }

    @Exclude
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Exclude
    public String getFrom() {
        return from;
    }

    @Exclude
    public void setFrom(String from) {
        this.from = from;
    }

    @Exclude
    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Exclude
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Conversation)) { return false; }
        Conversation c = (Conversation) obj;
        return c.getId().equals(this.getId());
    }

    @Exclude
    @Override
    public int compareTo(Conversation c) {
        return this.getMessageTimestamp() > c.getMessageTimestamp() ? 1 : -1;
    }

}
