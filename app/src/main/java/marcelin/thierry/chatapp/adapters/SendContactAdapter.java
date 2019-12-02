package marcelin.thierry.chatapp.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.Users;

public class SendContactAdapter extends RecyclerView.Adapter<SendContactAdapter.ContactViewHolder> {

    private List<Users> mUsersList;
    private Map<String, Object> mChatUsers = new HashMap<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private static final DatabaseReference mNotificationsDatabase = FirebaseDatabase.getInstance()
            .getReference().child("ads_notifications");

    private String mCurrentUserPhone;

    public SendContactAdapter(List<Users> mUsersList) {
        this.mUsersList = mUsersList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_contact_layout, parent, false);
        return new ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {

        Users user = mUsersList.get(position);

        mCurrentUserPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
        String mChatPhone = user.getPhone();

        String val = user.getNameStoredInPhone();
        if(val.length() > 30){
            val = val.substring(0, 27) + "...";
            holder.mContactName.setText(val);
        }else{
            holder.mContactName.setText(user.getNameStoredInPhone());
        }

        holder.mView.setOnClickListener(view -> {

            mChatUsers.put(mCurrentUserPhone, true);
            mChatUsers.put(mChatPhone, true);

            String myReference = "ads_users/" + mCurrentUserPhone + "/" + "conversation/";
            String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

            String chat_reference = "ads_chat/";

            // Creating conversation_id for each message
            DatabaseReference conversation_push = mRootReference.child("ads_users")
                    .child(mCurrentUserPhone).push();
            String conversation_id = conversation_push.getKey();

            final String message_reference = "ads_messages/";

            // Creating push_id for each message
            DatabaseReference msg_push = mRootReference.child("ads_messages").push();

            String push_id = msg_push.getKey();

            // Going under my phone and check if there is a child "conversation"
            mRootReference.child("ads_users").child(mCurrentUserPhone)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("conversation")) {

                                final Conversation[] c = new Conversation[1];
                                List<Conversation> listConvo = new ArrayList<>();
                                final boolean[] isThere = {false};
                                final String[] mConvoRef = new String[1];

                                mRootReference.child("ads_users")
                                        .child(mCurrentUserPhone)
                                        .child("conversation")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                    c[0] = d.getValue(Conversation.class);
                                                    listConvo.add(c[0]);
                                                }
                                                for (int i = 0; i < listConvo.size(); i++) {
                                                    if (listConvo.get(i).getPhone_number().equals(mChatPhone)) {
                                                        isThere[0] = true;
                                                        mConvoRef[0] = listConvo.get(i).getId();
                                                    }
                                                }
                                                if (isThere[0]) {

                                                    // Getting reference to conversation_id under ads_user and push new messages
                                                    DatabaseReference addNewMessage =
                                                            mRootReference.child("ads_chat")
                                                                    .child(mConvoRef[0])
                                                                    .child("messages").child(push_id);

                                                    mRootReference.child("ads_chat").child(mConvoRef[0]).child("lastMessage")
                                                            .setValue(push_id);

                                                    Map<String, Object> messageMap = new HashMap<>();
                                                    messageMap.put("content", user.getMessage());
                                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                    messageMap.put("type", "contact");
                                                    messageMap.put("parent", user.getNameStoredInPhone());
                                                    messageMap.put("visible", true);
                                                    messageMap.put("from", mCurrentUserPhone);
                                                    messageMap.put("seen", false);


                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                    msgContentMap.put(message_reference +
                                                            push_id, messageMap);

                                                    mRootReference.updateChildren(msgContentMap,
                                                            (databaseError, databaseReference) -> {
                                                            });


                                                    Map<String, Object> chatUnderId = new HashMap<>();
                                                    chatUnderId.put("msgId", push_id);
                                                    chatUnderId.put("seen", false);
                                                    chatUnderId.put("visible", true);
                                                    chatUnderId.put("timestamp",
                                                            ServerValue.TIMESTAMP);

                                                    addNewMessage.updateChildren(chatUnderId,
                                                            (databaseError, databaseReference) -> {

                                                                HashMap<String, Object> notificationData
                                                                        = new HashMap<>();
                                                                notificationData.put("from",
                                                                        mCurrentUserPhone);
                                                                notificationData.put("message",
                                                                        user.getMessage());

                                                                mNotificationsDatabase.child(mChatPhone)
                                                                        .push().setValue(notificationData)
                                                                        .addOnCompleteListener(task1 -> {

                                                                            if (task1.isSuccessful()) {

                                                                                Toast.makeText(
                                                                                        holder.mView
                                                                                                .getContext(),
                                                                                        "Link Sent",
                                                                                        Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });
                                                                //mp1.start();
                                                                //TODO: add sent mark

                                                            });


                                                } else {


                                                    /**
                                                     * Adding the information under ads_users of my phone and the other user,
                                                     * if the phone number doesn't match the other user
                                                     *
                                                     */

                                                    Map<String, Object> infoToAddUnderMe
                                                            = new HashMap<>();
                                                    infoToAddUnderMe.put("id", conversation_id);
                                                    infoToAddUnderMe.put("type", "chat");
                                                    infoToAddUnderMe.put("visible", true);
                                                    infoToAddUnderMe.put("phone_number",
                                                            mChatPhone);
                                                    infoToAddUnderMe.put("timestamp",
                                                            ServerValue.TIMESTAMP);

                                                    Map<String, Object> mapForCurrentUser
                                                            = new HashMap<>();
                                                    mapForCurrentUser.put(myReference +
                                                            conversation_id, infoToAddUnderMe);

                                                    mRootReference.updateChildren(mapForCurrentUser);

                                                    Map<String, Object> infoToAddUnderOther
                                                            = new HashMap<>();

                                                    infoToAddUnderOther.put("id", conversation_id);
                                                    infoToAddUnderOther.put("type", "chat");
                                                    infoToAddUnderOther.put("visible", true);
                                                    infoToAddUnderOther.put("phone_number",
                                                            mCurrentUserPhone);
                                                    infoToAddUnderOther.put("timestamp",
                                                            ServerValue.TIMESTAMP);

                                                    Map<String, Object> mapForOtherUser
                                                            = new HashMap<>();
                                                    mapForOtherUser.put(otherUserReference +
                                                            conversation_id, infoToAddUnderOther);

                                                    mRootReference.updateChildren(mapForOtherUser);

                                                    /**
                                                     * Adding information into ads_chat &
                                                     * ads_messages
                                                     */

                                                    DatabaseReference usersInChat = mRootReference
                                                            .child("ads_chat").child(conversation_id)
                                                            .child("messages").child(push_id);

                                                    Map<String, Object> messageMap = new HashMap<>();
                                                    messageMap.put("content", user.getMessage());
                                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                    messageMap.put("type", "contact");
                                                    messageMap.put("parent", user.getNameStoredInPhone());
                                                    messageMap.put("visible", true);
                                                    messageMap.put("from", mCurrentUserPhone);
                                                    messageMap.put("seen", false);


                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                    msgContentMap.put(message_reference +
                                                            push_id, messageMap);

                                                    //Adding message
                                                    mRootReference.updateChildren(msgContentMap,
                                                            (databaseError, databaseReference) -> {
                                                            });

                                                    Map<String, Object> chatRefMap = new HashMap<>();
                                                    chatRefMap.put("users", mChatUsers);
                                                    chatRefMap.put("seen", false);
                                                    chatRefMap.put("visible", true);
                                                    chatRefMap.put("lastMessage", push_id);

                                                    Map<String, Object> messageUserMap =
                                                            new HashMap<>();

                                                    messageUserMap.put(chat_reference +
                                                            conversation_id, chatRefMap);


                                                    mRootReference.updateChildren(messageUserMap);

                                                    Map<String, Object> chatUnderId = new HashMap<>();
                                                    chatUnderId.put("msgId", push_id);
                                                    chatUnderId.put("seen", false);
                                                    chatUnderId.put("visible", true);
                                                    chatUnderId.put("timestamp",
                                                            ServerValue.TIMESTAMP);

                                                    usersInChat.updateChildren(chatUnderId,
                                                            (databaseError, databaseReference) -> {

                                                                HashMap<String, Object> notificationData
                                                                        = new HashMap<>();
                                                                notificationData.put("from",
                                                                        mCurrentUserPhone);
                                                                notificationData.put("message",
                                                                        user.getMessage());

                                                                mNotificationsDatabase.child(mChatPhone)
                                                                        .push().setValue(notificationData)
                                                                        .addOnCompleteListener(task1 -> {

                                                                            if (task1.isSuccessful()) {

                                                                                Toast.makeText(
                                                                                        holder.mView.getContext(),
                                                                                        "Link Sent",
                                                                                        Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });
                                                                //mp1.start();
                                                                //TODO: add sent mark

                                                            });
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                            } else {

                                // if there is no conversation at all, create it

                                /**
                                 * Adding the information under ads_users
                                 */
                                Map<String, Object> infoToAddUnderMe
                                        = new HashMap<>();
                                infoToAddUnderMe.put("id", conversation_id);
                                infoToAddUnderMe.put("type", "chat");
                                infoToAddUnderMe.put("visible", true);
                                infoToAddUnderMe.put("phone_number",
                                        mChatPhone);
                                infoToAddUnderMe.put("timestamp",
                                        ServerValue.TIMESTAMP);

                                Map<String, Object> mapForCurrentUser
                                        = new HashMap<>();
                                mapForCurrentUser.put(myReference +
                                        conversation_id, infoToAddUnderMe);

                                mRootReference.updateChildren(mapForCurrentUser);

                                Map<String, Object> infoToAddUnderOther
                                        = new HashMap<>();

                                infoToAddUnderOther.put("id", conversation_id);
                                infoToAddUnderOther.put("type", "chat");
                                infoToAddUnderOther.put("visible", true);
                                infoToAddUnderOther.put("phone_number",
                                        mCurrentUserPhone);
                                infoToAddUnderOther.put("timestamp",
                                        ServerValue.TIMESTAMP);

                                Map<String, Object> mapForOtherUser
                                        = new HashMap<>();
                                mapForOtherUser.put(otherUserReference +
                                        conversation_id, infoToAddUnderOther);

                                mRootReference.updateChildren(mapForOtherUser);

                                /**
                                 * Adding information into ads_chat &
                                 * ads_messages
                                 */

                                DatabaseReference usersInChat = mRootReference
                                        .child("ads_chat").child(conversation_id)
                                        .child("messages").child(push_id);

                                Map<String, Object> messageMap = new HashMap<>();
                                messageMap.put("content", user.getMessage());
                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                messageMap.put("type", "contact");
                                messageMap.put("parent", user.getNameStoredInPhone());
                                messageMap.put("visible", true);
                                messageMap.put("from", mCurrentUserPhone);
                                messageMap.put("seen", false);

                                Map<String, Object> msgContentMap = new HashMap<>();
                                msgContentMap.put(message_reference +
                                        push_id, messageMap);

                                //Adding message
                                mRootReference.updateChildren(msgContentMap,
                                        (databaseError, databaseReference) -> {
                                        });

                                Map<String, Object> chatRefMap = new HashMap<>();
                                chatRefMap.put("users", mChatUsers);
                                chatRefMap.put("seen", false);
                                chatRefMap.put("visible", true);
                                chatRefMap.put("lastMessage", push_id);

                                Map<String, Object> messageUserMap =
                                        new HashMap<>();

                                messageUserMap.put(chat_reference +
                                        conversation_id, chatRefMap);

                                mRootReference.updateChildren(messageUserMap);

                                Map<String, Object> chatUnderId = new HashMap<>();
                                chatUnderId.put("msgId", push_id);
                                chatUnderId.put("seen", false);
                                chatUnderId.put("visible", true);
                                chatUnderId.put("timestamp",
                                        ServerValue.TIMESTAMP);

                                usersInChat.updateChildren(chatUnderId,
                                        (databaseError, databaseReference) -> {

                                            HashMap<String, Object> notificationData
                                                    = new HashMap<>();
                                            notificationData.put("from",
                                                    mCurrentUserPhone);
                                            notificationData.put("message",
                                                    user.getMessage());

                                            mNotificationsDatabase.child(mChatPhone)
                                                    .push().setValue(notificationData)
                                                    .addOnCompleteListener(task1 -> {

                                                        if (task1.isSuccessful()) {

                                                            Toast.makeText(holder.mView.getContext(),
                                                                    "Link Sent",
                                                                    Toast.LENGTH_SHORT).show();

                                                        }
                                                    });
                                            //mp1.start();
                                            //TODO: add sent mark

                                        });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        });

    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mProfilePic;
        private TextView mContactName;

        View mView;

        public ContactViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mProfilePic = mView.findViewById(R.id.profilePic);
            mContactName = mView.findViewById(R.id.contactName);

        }
    }

    public void updateList(List<Users> newList){
        mUsersList = new ArrayList<>();
        mUsersList.addAll(newList);
        notifyDataSetChanged();
    }
}
