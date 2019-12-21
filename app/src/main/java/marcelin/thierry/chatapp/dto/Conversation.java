package marcelin.thierry.chatapp.dto;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "conversations")
public class Conversation {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "conversationId")
    private String conversationId;

    @NonNull
    @ColumnInfo(name = "conversationType")
    private String conversationType;

    @NonNull
    @ColumnInfo(name = "conversationInterlocutor")
    private String interlocutor;

    @ColumnInfo(name = "conversationImagePath")
    private String imgPath;

    @NonNull
    @ColumnInfo(name = "conversationTimestamp")
    private long conversationTimestamp;

    @ColumnInfo(name = "lastMessageId")
    private String lastMessageId;

    @ColumnInfo(name = "unreadMessageCount")
    private int unreadMessageCount;

    @Ignore
    public Conversation(@NonNull String conversationType, @NonNull String interlocutor, String imgPath,
                        long conversationTimestamp, String lastMessageId, int unreadMessageCount) {
        this.conversationId = UUID.randomUUID().toString();
        this.conversationType = conversationType;
        this.interlocutor = interlocutor;
        this.imgPath = imgPath;
        this.conversationTimestamp = conversationTimestamp;
        this.lastMessageId = lastMessageId;
        this.unreadMessageCount = unreadMessageCount;
    }

    public Conversation(@NonNull String conversationId, @NonNull String conversationType,
                        @NonNull String interlocutor, String imgPath, long conversationTimestamp,
                        String lastMessageId, int unreadMessageCount) {
        this.conversationId = conversationId;
        this.conversationType = conversationType;
        this.interlocutor = interlocutor;
        this.imgPath = imgPath;
        this.conversationTimestamp = conversationTimestamp;
        this.lastMessageId = lastMessageId;
        this.unreadMessageCount = unreadMessageCount;
    }

    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    @NonNull
    public String getConversationType() {
        return conversationType;
    }

    @NonNull
    public String getInterlocutor() {
        return interlocutor;
    }

    public String getImgPath() {
        return imgPath;
    }

    @NonNull
    public long getConversationTimestamp() {
        return conversationTimestamp;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    public void setConversationType(@NonNull String conversationType) {
        this.conversationType = conversationType;
    }

    public void setInterlocutor(@NonNull String interlocutor) {
        this.interlocutor = interlocutor;
    }

    public void setConversationTimestamp(long conversationTimestamp) {
        this.conversationTimestamp = conversationTimestamp;
    }
}
