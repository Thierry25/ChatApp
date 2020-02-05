package marcelin.thierry.chatapp.classes;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Map;

@IgnoreExtraProperties
public class Messages implements Serializable {

    private String content, type, from, parent;
    private Long timestamp;
    private boolean seen, visible;
    private String name, profilePic;

    private boolean isSelected = false;

    private boolean isSent;

    private boolean isReplyOn = false;

    private String messageId;

    private String color;

    private static String clickedMessageId = "";

    private Map<String, Object> read_by;

    private String channelName, channelImage;
    // For Firebase
    public Messages() { }

    public Messages(String from) {
        this.from = from;
    }

    public Messages(String content, boolean seen, Long timestamp, String type, String parent, String from, boolean visible) {
        this.content = content;
        this.seen = seen;
        this.timestamp = timestamp;
        this.type = type;
        this.parent = parent;
        this.from = from;
        this.visible = visible;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isSeen() {
        return seen;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Exclude
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Exclude
    public String getMessageId() {
        return messageId;
    }

    @Exclude
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Exclude
    public boolean isSent() {
        return isSent;
    }

    @Exclude
    public void setSent(boolean sent) {
        isSent = sent;
    }

    public Map<String, Object> getRead_by() {
        return read_by;
    }

    public void setRead_by(Map<String, Object> read_by) {
        this.read_by = read_by;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Exclude
    public static String getClickedMessageId() {
        return clickedMessageId;
    }

    @Exclude
    public static void setClickedMessageId(String clickedMessageId) {
        Messages.clickedMessageId = clickedMessageId;
    }

    @Exclude
    public String getChannelName() {
        return channelName;
    }
    @Exclude
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
    @Exclude
    public String getChannelImage() {
        return channelImage;
    }
    @Exclude
    public void setChannelImage(String channelImage) {
        this.channelImage = channelImage;
    }

    @Exclude
    public String getName() {
        return name;
    }
    @Exclude
    public void setName(String name) {
        this.name = name;
    }
    @Exclude
    public String getProfilePic() {
        return profilePic;
    }
    @Exclude
    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public boolean isReplyOn() {
        return isReplyOn;
    }

    public void setReplyOn(boolean replyOn) {
        isReplyOn = replyOn;
    }

    @Override
    public int hashCode() {
        return this.messageId.hashCode();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Messages)) { return false; }
        return ((Messages) obj).messageId.equals(this.messageId);
    }
}
