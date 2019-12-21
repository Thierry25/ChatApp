package marcelin.thierry.chatapp.dto;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ConversationAndUnreadMessage {
    @Embedded public Conversation conversation;
    @Relation(parentColumn = "conversationId", entityColumn = "conversation", entity = UnreadMessages.class)
    public List<UnreadMessages> messages;
}
