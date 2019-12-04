package marcelin.thierry.chatapp.classes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * @Author Thierry
 */

@IgnoreExtraProperties
public class Channel {

    private String image;
    private String thumbnail;
    private boolean isPrivate;
    private Map<String, Object> subscribers;
    private Map <String, Object> admins;
    private long timestamp;
    private String name;
    private String lastMessage;
    private String link;
    private String description;
    private String newName;

    private String password, email, question, answer, locked;

    @Exclude
    private String channelPrivacy;
    private boolean isSelected = false;

    public Channel(){}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Channel(String image, String thumbnail, boolean isPrivate, Map<String, Object>
            subscribers, Map<String, Object> admins, long timestamp, String name, String lastMessage,
                   String link, String description) {
        this.image = image;
        this.thumbnail = thumbnail;
        this.isPrivate = isPrivate;
        this.subscribers = subscribers;
        this.admins = admins;
        this.timestamp = timestamp;
        this.name = name;
        this.lastMessage = lastMessage;
        this.link = link;
        this.description = description;

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Map<String, Object> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Map<String, Object> subscribers) {
        this.subscribers = subscribers;
    }

    public Map<String, Object> getAdmins() {
        return admins;
    }

    public void setAdmins(Map<String, Object> admins) {
        this.admins = admins;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Exclude
    public String getChannelPrivacy() {
        return channelPrivacy;
    }

    @Exclude
    public void setChannelPrivacy(String groupPrivacy) {
        this.channelPrivacy = groupPrivacy;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @Exclude
    public boolean isSelected() {
        return isSelected;
    }

    @Exclude
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     *
     * Avoid duplicating channels
     */

    @Exclude
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Exclude
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Group)) {
            return false;
        }
        return ((Group) obj).getName().equals(this.name);
    }
}
