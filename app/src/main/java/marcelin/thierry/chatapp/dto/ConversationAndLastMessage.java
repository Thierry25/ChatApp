package marcelin.thierry.chatapp.dto;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ConversationAndLastMessage {
    @Embedded public Message message;
    @Relation(
            parentColumn = "messageId",
            entityColumn = "lastMessageId"
    )
    public Conversation conversation;
}
