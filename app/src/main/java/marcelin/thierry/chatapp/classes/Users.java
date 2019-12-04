package marcelin.thierry.chatapp.classes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Users implements Serializable{

    private String image;
    private String name;
    private String status;
    private String phoneNumber;
    private String thumbnail;
    private String uid;
    private String deviceToken;
    private String type;
    private String convoId;
    private String phone;
    private int amount;
    private List<Messages> messageList;
    @PropertyName("e")
    private Map<String, Long> exceptions;
    @PropertyName("conversation")
    private Map<String, Conversation> conversations;

    private Long timestamp;
    private boolean isAdmin;
    private String nameStoredInPhone;
    private static boolean isReady = false;
    private boolean isSelected = false;
    private static Map<String, String> localContacts = new HashMap<>();
    private String lastMessage;
    private boolean isSavedInContact = true;
    private String message;
    private String chatId;

    public Users(String image, String name, String status, String phoneNumber, String thumbnail,
                 Map<String, Long> exceptions, Map<String, Conversation> conversations) {
        this.image = image;
        this.name = name;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.thumbnail = thumbnail;
        this.exceptions = exceptions;
        this.conversations = conversations;
    }

    public Users() {
    }

    public static boolean isReady() {
        return isReady;
    }

    public static void setIsReady(boolean isReady) {
        Users.isReady = isReady;
    }


    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    /*
    public Users(String image, String name, String status) {
        this.image = image;
        this.name = name;
        this.status = status;
    }
     */


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNameStoredInPhone() {
        return nameStoredInPhone;
    }

    public void setNameStoredInPhone(String nameStoredInPhone) {
        this.nameStoredInPhone = nameStoredInPhone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @PropertyName("e")
    public void setExceptions(Map<String, Long> exceptions) {
        this.exceptions = exceptions;
    }


    @PropertyName("e")
    public Map<String, Long> getExceptions() {
        return exceptions;
    }

    @PropertyName("conversation")
    public Map<String, Conversation> getConversations() { return conversations; }

    @PropertyName("conversation")
    public void setConversations(Map<String, Conversation> conversations) {
        this.conversations = conversations;
    }

    @Exclude
    public static Map<String, String> getLocalContactList() {
        return localContacts;
    }

    @Exclude
    public boolean isSelected() {
        return isSelected;
    }

    @Exclude
    public void setSelected(boolean selected) {
        isSelected = selected;
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
    public boolean isAdmin() {
        return isAdmin;
    }

    @Exclude
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Exclude
    public boolean isSavedInContact() {
        return isSavedInContact;
    }

    @Exclude
    public void setSavedInContact(boolean savedInContact) {
        isSavedInContact = savedInContact;
    }

    @Exclude
    @Override
    public int hashCode() {
        return this.getUid().hashCode();
    }

    @Exclude
    public String getMessage() {
        return message;
    }

    @Exclude
    public void setMessage(String message) {
        this.message = message;
    }

    @Exclude
    public String getType() {
        return type;
    }

    @Exclude
    public void setType(String type) {
        this.type = type;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Exclude
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Users)) { return false; }
        Users u = (Users) obj;
        return u.getUid().equals(this.getUid());
    }

    @Exclude
    public String getChatId() {
        return chatId;
    }

    @Exclude
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    @Exclude
    public String getConvoId() {
        return convoId;
    }

    @Exclude
    public void setConvoId(String convoId) {
        this.convoId = convoId;
    }

    @Exclude
    public Long getTimestamp() {
        return timestamp;
    }

    @Exclude
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public int getAmount() {
        return amount;
    }

    @Exclude
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Exclude
    public List<Messages> getMessageList() {
        return messageList;
    }

    @Exclude
    public void setMessageList(List<Messages> messageList) {
        this.messageList = messageList;
    }
}
