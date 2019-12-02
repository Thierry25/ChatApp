package marcelin.thierry.chatapp.classes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class Group {

    private String image;
    private String thumbnail;
    private boolean isPrivate;
    private Map<String, Object> users;
    private Map <String, Object> admins;
    private long timestamp;
    private String name;
    private String lastMessage;
    private String link;
    private String newName;

    @Exclude
    private String groupPrivacy;
    private boolean isSelected = false;

    // Firebase Purposes
   public Group(){}


    public Group(String image, String thumbnail, boolean isPrivate, Map<String, Object> users,
                 Map<String, Object> admins, long timestamp, String name, String lastMessage,
                 String link, String newName) {
        this.image = image;
        this.isPrivate = isPrivate;
        this.users = users;
        this.admins = admins;
        this.timestamp = timestamp;
        this.thumbnail = thumbnail;
        this.name = name;
        this.lastMessage = lastMessage;
        this.link = link;
        this.newName = newName;

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

    public Map<String, Object> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Object> users) {
        this.users = users;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Exclude
    public String getGroupPrivacy() {
        return groupPrivacy;
    }

    @Exclude
    public void setGroupPrivacy(String groupPrivacy) {
        this.groupPrivacy = groupPrivacy;
    }


    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
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

    // Verify if the hash code

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
