package marcelin.thierry.chatapp.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CommentChannel {
    private String commentId;
    private List<Messages> replyMessages;
    private String name;
    private String profilePic;
    private String channelName;
    private String content;
    private String parent;
    private String from;
    private Long timestamp;
    private String initialCommentId;
    private String initialCommentContent;
    private String initialChannelImage;
    private String initialMessageType;
    private String initialColor;
    private Long initialTimestamp;
    private int initialLikesCount;
    private int initialCommentsCount;
    private int seeInitalCount;
    private Map<String, Object> r;


    public CommentChannel() {
        this.replyMessages = new ArrayList<>();
    }

    public CommentChannel(String commentId) {
        this();
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public List<Messages> getReplyMessages() {
        return replyMessages;
    }

//    public void setReplyMessages(List<Messages> replyMessages) {
//        this.replyMessages = replyMessages;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getR() {
        return r;
    }

    public void setR(Map<String, Object> r) {
        this.r = r;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setReplyMessages(List<Messages> replyMessages) {
        this.replyMessages = replyMessages;
    }

    public String getInitialCommentId() {
        return initialCommentId;
    }

    public void setInitialCommentId(String initialCommentId) {
        this.initialCommentId = initialCommentId;
    }

    public String getInitialCommentContent() {
        return initialCommentContent;
    }

    public void setInitialCommentContent(String initialCommentContent) {
        this.initialCommentContent = initialCommentContent;
    }

    public String getInitialChannelImage() {
        return initialChannelImage;
    }

    public void setInitialChannelImage(String initialChannelImage) {
        this.initialChannelImage = initialChannelImage;
    }

    public String getInitialMessageType() {
        return initialMessageType;
    }

    public void setInitialMessageType(String initialMessageType) {
        this.initialMessageType = initialMessageType;
    }

    public String getInitialColor() {
        return initialColor;
    }

    public void setInitialColor(String initialColor) {
        this.initialColor = initialColor;
    }

    public Long getInitialTimestamp() {
        return initialTimestamp;
    }

    public void setInitialTimestamp(Long initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }

    public int getInitialLikesCount() {
        return initialLikesCount;
    }

    public void setInitialLikesCount(int initialLikesCount) {
        this.initialLikesCount = initialLikesCount;
    }

    public int getInitialCommentsCount() {
        return initialCommentsCount;
    }

    public void setInitialCommentsCount(int initialCommentsCount) {
        this.initialCommentsCount = initialCommentsCount;
    }

    public int getSeeInitalCount() {
        return seeInitalCount;
    }

    public void setSeeInitalCount(int seeInitalCount) {
        this.seeInitalCount = seeInitalCount;
    }

    @Override
    public int hashCode() {
        return this.commentId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommentChannel)) {
            return false;
        }
        return ((CommentChannel) obj).commentId.equals(this.commentId);
    }

}
