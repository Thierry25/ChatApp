package marcelin.thierry.chatapp.dto;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Relation;

public class ConversationAndLastMessage {
    @Embedded private Conversation conversation;
    @ColumnInfo(name = "messageContent")
    private String messageContent;
    @ColumnInfo(name = "messageTimestamp")
    private Long messageTimestamp;
    @ColumnInfo(name = "seen")
    private boolean seen;

    public ConversationAndLastMessage(Conversation conversation, String messageContent,
                                      Long messageTimestamp, boolean seen) {
        this.conversation = conversation;
        this.messageContent = messageContent;
        this.messageTimestamp = messageTimestamp;
        this.seen = seen;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Long getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(Long messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
