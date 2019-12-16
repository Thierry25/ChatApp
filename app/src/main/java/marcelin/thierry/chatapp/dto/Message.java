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
    public Message(String messageType, String messageContent, long messageTimestamp,
                   String messageFrom, boolean visible, boolean seen, String conversationId,
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

    public Message(String messageId, String messageType, String messageContent, long messageTimestamp,
                   String messageFrom, boolean visible, boolean seen, String conversationId,
                   String parentMessageId) {
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
}
