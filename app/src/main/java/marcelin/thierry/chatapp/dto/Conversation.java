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

    @ColumnInfo(name = "conversationTimestamp")
    private long conversationTimestamp;

    @Ignore
    public Conversation(String conversationType, String interlocutor, String imgPath,
                        long conversationTimestamp) {
        this.conversationId = UUID.randomUUID().toString();
        this.conversationType = conversationType;
        this.interlocutor = interlocutor;
        this.imgPath = imgPath;
        this.conversationTimestamp = conversationTimestamp;
    }

    public Conversation(String conversationId, String conversationType, String interlocutor,
                        String imgPath, long conversationTimestamp) {
        this.conversationId = conversationId;
        this.conversationType = conversationType;
        this.interlocutor = interlocutor;
        this.imgPath = imgPath;
        this.conversationTimestamp = conversationTimestamp;
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

    public long getConversationTimestamp() {
        return conversationTimestamp;
    }
}
