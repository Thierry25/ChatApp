package marcelin.thierry.chatapp.classes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Chat {

    private static Map<String, Users> gChatList = new LinkedHashMap<>();
    private String msgId;
    private boolean seen;
    private boolean visible;
    private Long timestamp;


    public Chat() {}

    public Chat(boolean visible, String msgId, boolean seen, Long timestamp) {
        this.visible = visible;
        this.msgId = msgId;
        this.seen = seen;
        this.timestamp = timestamp;
    }
    /**
     * The list of chat users
     *
     * @return the list
     */
    @Exclude
    public static Map<String, Users> chatUsers() {
        return gChatList;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Long getTimestamp() { return  this.timestamp; }

    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }




}
