package marcelin.thierry.chatapp.classes;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import marcelin.thierry.chatapp.dto.Message;

@IgnoreExtraProperties
public class Messages implements Serializable, Comparable<Messages>{

    private String content, type, from, parent;
    private Long timestamp;
    private boolean seen, visible, edited;
    private String name, profilePic;

    private boolean isSelected = false;

    private boolean isSent;

    private boolean isReplyOn = false;

    private String messageId;

    private String color;

    private static String clickedMessageId = "";

    private Map<String, Object> read_by;

    private String channelName, channelImage;
    private Map<String, Object> c, l, r;
    private String thumb;

    @Exclude
    private String initialCommentId;
    @Exclude
    private String initialCommentContent;
    @Exclude
    private String initialChannelImage;
    @Exclude
    private String initialMessageType;
    @Exclude
    private String initialColor;
    @Exclude
    private Long initialTimestamp;
    @Exclude
    private int initialLikesCount;
    @Exclude
    private int initialCommentsCount;
    @Exclude
    private int seeInitalCount;

    @Exclude
    private List<String> admins = new ArrayList<>();

    // For Firebase
    public Messages() { }

    public Messages(String from) {
        this();
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

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
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

    public Map<String, Object> getL() {
        return l;
    }

    public Map<String, Object> getR() {
        return r;
    }

    @Exclude
    public List<String> getAdmins() {
        return admins;
    }
    @Exclude
    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Map<String, Object> getC() {
        return c;
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

    @Exclude
    public String getInitialCommentId() {
        return initialCommentId;
    }
    @Exclude
    public void setInitialCommentId(String initialCommentId) {
        this.initialCommentId = initialCommentId;
    }
    @Exclude
    public String getInitialCommentContent() {
        return initialCommentContent;
    }
    @Exclude
    public void setInitialCommentContent(String initialCommentContent) {
        this.initialCommentContent = initialCommentContent;
    }
    @Exclude
    public String getInitialChannelImage() {
        return initialChannelImage;
    }
    @Exclude
    public void setInitialChannelImage(String initialChannelImage) {
        this.initialChannelImage = initialChannelImage;
    }
    @Exclude
    public String getInitialMessageType() {
        return initialMessageType;
    }
    @Exclude
    public void setInitialMessageType(String initialMessageType) {
        this.initialMessageType = initialMessageType;
    }
    @Exclude
    public String getInitialColor() {
        return initialColor;
    }
    @Exclude
    public void setInitialColor(String initialColor) {
        this.initialColor = initialColor;
    }
    @Exclude
    public Long getInitialTimestamp() {
        return initialTimestamp;
    }
    @Exclude
    public void setInitialTimestamp(Long initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }
    @Exclude
    public int getInitialLikesCount() {
        return initialLikesCount;
    }
    @Exclude
    public void setInitialLikesCount(int initialLikesCount) {
        this.initialLikesCount = initialLikesCount;
    }
    @Exclude
    public int getInitialCommentsCount() {
        return initialCommentsCount;
    }
    @Exclude
    public void setInitialCommentsCount(int initialCommentsCount) {
        this.initialCommentsCount = initialCommentsCount;
    }
    @Exclude
    public int getSeeInitalCount() {
        return seeInitalCount;
    }
    @Exclude
    public void setSeeInitalCount(int seeInitalCount) {
        this.seeInitalCount = seeInitalCount;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Messages)) { return false; }
        return ((Messages) obj).messageId.equals(this.messageId);
    }


    @Override
    public int compareTo(Messages m) {
        Date d = new Date(getTimestamp());
        return d.compareTo(new Date(m.getTimestamp()));
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb){
        this.thumb = thumb;
    }

}
