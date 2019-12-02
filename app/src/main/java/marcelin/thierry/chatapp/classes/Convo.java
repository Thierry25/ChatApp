package marcelin.thierry.chatapp.classes;

import java.util.Map;

public class Convo {

    private String lastMessage;
    private Map<String, Object> messages;
    private boolean seen;

    private Map<String, Object> users;
    private boolean visible;

    private String msgId;

    public Convo(){}

    public Convo(String lastMessage, Map<String, Object> messages, boolean  seen,
                Map<String, Object> users, boolean visible) {
        this.lastMessage = lastMessage;
        this.messages = messages;
        this.seen = seen;
        this.users = users;
        this.visible = visible;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Map<String, Object> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, Object> messages) {
        this.messages = messages;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }


    public Map<String, Object> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Object> users) {
        this.users = users;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
