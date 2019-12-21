package marcelin.thierry.chatapp.dto;

import androidx.room.DatabaseView;
import androidx.room.Embedded;

@DatabaseView("SELECT * FROM messages WHERE seen = 0")
public class UnreadMessages {
    @Embedded public Message message;
}
