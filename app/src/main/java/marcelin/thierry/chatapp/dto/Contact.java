package marcelin.thierry.chatapp.dto;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "contacts")
public class Contact {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "contactId")
    private String contactId;

    @NonNull
    @ColumnInfo(name = "contactName")
    private String contactName;

    @NonNull
    @ColumnInfo(name = "phoneNumber")
    private String phoneNumber;

    @ColumnInfo(name = "handleName")
    private  String handleName;

    @Ignore
    public Contact(String contactName, String phoneNumber, String handleName) {
        contactId = UUID.randomUUID().toString();
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.handleName = handleName;
    }

    public Contact(String contactId, String contactName, String phoneNumber, String handleName) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.handleName = handleName;
    }

    public String getContactId() {
        return contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getHandleName() {
        return handleName == null ? "" : handleName;
    }
}
