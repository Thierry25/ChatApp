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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.Users;

public class T extends RecyclerView.Adapter<T.UsersViewHolder> {

    private List<Users> usersStored;
    private Map<String, Object> mChatUsers = new HashMap<>();

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private static final DatabaseReference mNotificationsDatabase = FirebaseDatabase.getInstance()
            .getReference().child("ads_notifications");

    private String mCurrentUserPhone;


    public T(List<Users> usersStored) {
        this.usersStored = usersStored;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_main_layout,
                parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {

        final Users user = usersStored.get(position);

        mCurrentUserPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        String mChatPhone = user.getPhoneNumber();

        holder.mProfileName.setText("~"+user.getName());
        String value = user.getStatus();
        if(value.length() > 25){
            value = value.substring(0, 21) + "...";
            holder.mProfileStatus.setText(value);
        }
        holder.mProfileStatus.setText(user.getStatus());
        //holder.mContactName.setText(usersStoredNames.get(position));
        //@author fmarcelin
        String val = user.getNameStoredInPhone();
        if(val.length() > 30){
            val = val.substring(0, 27) + "...";
            holder.mContactName.setText(val);
        }

        holder.mContactName.setText(user.getNameStoredInPhone());
        holder.setProfilePic(user.getThumbnail());

        holder.mView.setOnClickListener(view -> {

            mChatUsers.put(mCurrentUserPhone, true);
            mChatUsers.put(mChatPhone, true);

            String myReference = "ads_users/" + mCurrentUserPhone + "/" + "conversation/";
            String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

            String chat_reference = "ads_chat/";

            sendMessage(user.getAmount(), mChatPhone, user, holder, myReference, otherUserReference, chat_reference, user.getMessageList());


        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getItemCount() {
        return usersStored.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView mContactName;
        TextView mProfileStatus;
        TextView mProfileName;
        CircleImageView userProfilePic;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mContactName = mView.findViewById(R.id.contactName);
            mProfileStatus = mView.findViewById(R.id.profileStatus);
            mProfileName = mView.findViewById(R.id.profileName);
            userProfilePic = mView.findViewById(R.id.profilePic);
        }

        public void setProfilePic(String thumnail){

            userProfilePic = mView.findViewById(R.id.profilePic);

            Picasso.get().load(thumnail).placeholder(R.drawable.ic_avatar).into(userProfilePic);

        }
    }

    public void updateList(List<Users> newList){
        usersStored = new ArrayList<>();
        usersStored.addAll(newList);
        notifyDataSetChanged();
    }

    private void sendMessage(int amount, String mChatPhone, Users user, UsersViewHolder holder, String myReference, String otherUserReference, String chat_reference, List<Messages>m){
        for (int z = 0; z < amount; z++){
            // Creating conversation_id for each message
            DatabaseReference conversation_push = mRootReference.child("ads_users")
                    .child(mCurrentUserPhone).push();
            String conversation_id = conversation_push.getKey();

            final String message_reference = "ads_messages/";

            // Creating push_id for each message
            DatabaseReference msg_push = mRootReference.child("ads_messages").push();

            String push_id = msg_push.getKey();

            // Going under my phone and check if there is a child "conversation"
            int finalZ = z;
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
                                                    messageMap.put("content", m.get(finalZ).getContent());
                                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                    messageMap.put("type", m.get(finalZ).getType());
                                                    messageMap.put("parent", "Default");
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
                                                    messageMap.put("content", m.get(finalZ).getContent());
                                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                    messageMap.put("type",  m.get(finalZ).getType());
                                                    messageMap.put("parent", "Default");
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
                                messageMap.put("content",  m.get(finalZ).getContent());
                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                messageMap.put("type",  m.get(finalZ).getType());
                                messageMap.put("parent", "Default");
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
        }
    }
}
