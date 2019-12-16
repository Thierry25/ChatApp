package marcelin.thierry.chatapp.dto;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ConversationAndMessage {
    @Embedded public Conversation conversation;
    @Relation(
            parentColumn = "conversationId",
            entityColumn = "conversation"
    )
    public List<Message> message;
}
