package marcelin.thierry.chatapp.dto;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "messages")
public class Message {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "messageId")
    private String messageId;

    @NonNull
    @ColumnInfo(name = "messageType")
    private String messageType;

    @NonNull
    @ColumnInfo(name = "messageContent")
    private String messageContent;

    @NonNull
    @ColumnInfo(name = "messageTimestamp")
    private long messageTimestamp;

    @NonNull
    @ColumnInfo(name = "messageFrom")
    private String messageFrom;

    @NonNull
    @ColumnInfo(name = "visible")
    private boolean visible;

    @NonNull
    @ColumnInfo(name = "seen")
    private boolean seen;

    @NonNull
    @ColumnInfo(name = "conversation")
    private String conversationId;

    @ColumnInfo(name = "parentMessage")
    private String parentMessageId;

    @Ignore
    public Message(@NonNull String messageType, @NonNull String messageContent, long messageTimestamp,
                   @NonNull String messageFrom, boolean visible, boolean seen, @NonNull String conversationId,
                   String parentMessageId) {
        this.messageId = UUID.randomUUID().toString();
        this.messageType = messageType;
        this.messageContent = messageContent;
        this.messageTimestamp = messageTimestamp;
        this.messageFrom = messageFrom;
        this.visible = visible;
        this.seen = seen;
        this.conversationId = conversationId;
        this.parentMessageId = parentMessageId;
    }

    public Message(@NonNull String messageId, @NonNull String messageType, @NonNull String messageContent,
                   long messageTimestamp, @NonNull String messageFrom, boolean visible, boolean seen,
                   @NonNull String conversationId, String parentMessageId) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.messageContent = messageContent;
        this.messageTimestamp = messageTimestamp;
        this.messageFrom = messageFrom;
        this.visible = visible;
        this.seen = seen;
        this.conversationId = conversationId;
        this.parentMessageId = parentMessageId;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    @NonNull
    public String getMessageType() {
        return messageType;
    }

    @NonNull
    public String getMessageContent() {
        return messageContent;
    }

    public long getMessageTimestamp() {
        return messageTimestamp;
    }

    @NonNull
    public String getMessageFrom() {
        return messageFrom;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isSeen() {
        return seen;
    }

    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    public String getParentMessageId() {
        return parentMessageId;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    public void setMessageType(@NonNull String messageType) {
        this.messageType = messageType;
    }

    public void setMessageContent(@NonNull String messageContent) {
        this.messageContent = messageContent;
    }

    public void setMessageTimestamp(long messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    public void setMessageFrom(@NonNull String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    public void setParentMessageId(String parentMessageId) {
        this.parentMessageId = parentMessageId;
    }
}
