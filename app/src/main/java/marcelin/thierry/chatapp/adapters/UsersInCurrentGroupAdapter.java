package marcelin.thierry.chatapp.adapters;


import android.content.Context;
import android.content.Intent;
import android.provider.Contacts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.OneToOneChatSettings;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.Users;

public class UsersInCurrentGroupAdapter extends RecyclerView.Adapter
        <UsersInCurrentGroupAdapter.UsersViewHolder> {

    private List<Users> mUsersInCurrentGroup;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final DatabaseReference mGroupReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_group");
    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private Context mContext;

    public UsersInCurrentGroupAdapter(List<Users> mUsersInCurrentGroup, Context mContext) {
        this.mUsersInCurrentGroup = mUsersInCurrentGroup;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_in_current_group,
                parent, false);

        return new UsersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {

        Users currentUser = mUsersInCurrentGroup.get(position);

        holder.mContactName.setText(currentUser.getNameStoredInPhone());
        holder.mAdminText.setVisibility(currentUser.isAdmin() ? View.VISIBLE : View.GONE);
        holder.mProfileName.setVisibility(currentUser.isAdmin() ? View.GONE : View.VISIBLE);
        holder.mProfileStatus.setText(currentUser.getStatus());
        holder.setProfilePic(currentUser.getThumbnail());

        if (!currentUser.isAdmin()) {
            holder.mProfileName.setText(currentUser.getName());
        }

        holder.cstLayout.setOnClickListener(view -> {
            if(currentUser.getNameStoredInPhone().equals(mContext.getResources().getString(R.string.you))){

                Toast.makeText(view.getContext(), mContext.getResources().getString(R.string.you), Toast.LENGTH_SHORT).show();

            } else if(!currentUser.isSavedInContact()){

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(R.string.choose_option)
                        .setIcon(R.drawable.ic_option)
                        .setItems(R.array.options_, (dialog, which) -> {
                            switch (which) {
                                case 0:

                                    Intent addContactIntent = new Intent(Contacts.Intents.Insert.ACTION,
                                            Contacts.People.CONTENT_URI);
                                    addContactIntent.putExtra(Contacts.Intents.Insert.PHONE, currentUser.
                                            getNameStoredInPhone());
                                    view.getContext().startActivity(addContactIntent);

                                    break;

                                case 1:

                                    Intent goToOneAndOneChat = new Intent(view.getContext()
                                            , OneToOneChatSettings.class);

                                    goToOneAndOneChat.putExtra("user_phone",
                                            currentUser.getPhoneNumber());
                                    goToOneAndOneChat.putExtra("user_name",
                                            currentUser.getNameStoredInPhone());
                                    goToOneAndOneChat.putExtra("user_picture",
                                            currentUser.getThumbnail());

                                    view.getContext().startActivity(goToOneAndOneChat);

                                    break;

                                default:
                                    return;
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

            } else{

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(R.string.choose_option)
                        .setIcon(R.drawable.ic_option)
                        .setItems(R.array.options__, (dialog, id) ->{
                            switch (id){
                                case 0:
                                    //Make Admin
                                    String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
                                    mGroupReference.child(currentUser.getChatId()).child("admins").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(phone)){
                                                String nameStored = Users.getLocalContactList().get(currentUser
                                                        .getPhoneNumber());
                                                nameStored = nameStored != null && nameStored.length() > 0  ? nameStored :
                                                        currentUser.getPhoneNumber();

                                                AlertDialog.Builder buil = new AlertDialog.Builder(view.getContext());
                                                String finalNameStored = nameStored;
                                                buil.setPositiveButton(R.string.ok, (di, wi) ->{
                                                    Map<String, Object> m = new HashMap<>();
                                                    m.put(currentUser.getPhoneNumber(), ServerValue.TIMESTAMP);
                                                    mGroupReference.child(currentUser.getChatId())
                                                            .child("admins").updateChildren(m);
                                                    Toast.makeText(view.getContext(), finalNameStored + " "
                                                            + R.string.now_as   , Toast.LENGTH_SHORT).show();

                                                });
                                                buil.setNegativeButton(R.string.cancel, (di, wi) ->{
                                                    Toast.makeText(view.getContext(), R.string.cancel,
                                                            Toast.LENGTH_SHORT).show();

                                                });

                                                AlertDialog aD = buil.create();
                                                aD.setIcon(R.drawable.ic_warning);
                                                aD.setTitle(R.string.admin);
                                                aD.setMessage(mContext.getResources().getString(R.string.make_ad) + nameStored + " "
                                                        + mContext.getResources().getString(R.string.gr_ad));
                                                aD.show();
                                            }else{
                                                Toast.makeText(view.getContext(), R.string.ad_er_to_show, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
//                                    String nameStored = Users.getLocalContactList().get(currentUser
//                                            .getPhoneNumber());
//                                    nameStored = nameStored != null && nameStored.length() > 0  ? nameStored :
//                                            currentUser.getPhoneNumber();
//
//                                    AlertDialog.Builder buil = new AlertDialog.Builder(view.getContext());
//                                    String finalNameStored = nameStored;
//                                    buil.setPositiveButton(R.string.ok, (di, wi) ->{
//                                        Map<String, Object> m = new HashMap<>();
//                                        m.put(currentUser.getPhoneNumber(), ServerValue.TIMESTAMP);
//                                        mGroupReference.child(currentUser.getChatId())
//                                                .child("admins").updateChildren(m);
//                                        Toast.makeText(view.getContext(), finalNameStored + " "
//                                             + R.string.now_as   , Toast.LENGTH_SHORT).show();
//
//                                    });
//                                    buil.setNegativeButton(R.string.cancel, (di, wi) ->{
//                                        Toast.makeText(view.getContext(), R.string.cancel,
//                                                Toast.LENGTH_SHORT).show();
//
//                                    });
//
//                                    AlertDialog aD = buil.create();
//                                    aD.setIcon(R.drawable.ic_warning);
//                                    aD.setTitle(R.string.admin);
//                                    aD.setMessage(mContext.getResources().getString(R.string.make_ad) + nameStored + " "
//                                    + mContext.getResources().getString(R.string.gr_ad));
//                                    aD.show();


                                    break;
                                case 1:
                                    // Remove User

                                    AlertDialog.Builder alBuilder = new AlertDialog.Builder(view.getContext());
                                    alBuilder.setPositiveButton(R.string.ok, (dial, id_) ->{

                                        mGroupReference.child(currentUser.getChatId()).child("users")
                                                .child(currentUser.getPhoneNumber()).removeValue();
                                        mUsersReference.child(currentUser.getPhoneNumber())
                                                .child("conversation").child("G-"+currentUser.getChatId())
                                                .removeValue();
                                        Toast.makeText(view.getContext(), R.string.us_del_gr,
                                                Toast.LENGTH_SHORT).show();



                                    });
                                    alBuilder.setNegativeButton(R.string.cancel, (dial, id_)->{
                                        Toast.makeText(view.getContext(), R.string.deletion_c,
                                                Toast.LENGTH_SHORT).show();

                                    });
                                    String nameStored_ = Users.getLocalContactList().get(currentUser
                                            .getPhoneNumber());
                                    nameStored_ = nameStored_ != null && nameStored_.length() > 0  ? nameStored_ :
                                            currentUser.getPhoneNumber();

                                    AlertDialog dialo = alBuilder.create();
                                    dialo.setIcon(R.drawable.ic_warning);
                                    dialo.setTitle(R.string.delete);
                                    dialo.setMessage(mContext.getResources().getString(R.string.sure_del)+
                                            " " + nameStored_ + " " +mContext.getResources().getString(R.string.fr_gr));
                                    dialo.show();

//                                    mGroupReference.child(currentUser.getChatId()).child("users")
//                                            .child(currentUser.getPhoneNumber()).removeValue();

                                    break;
                                case 2:
                                    // Get Info
                                    String phonee = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
                                    if (phonee != null) {
                                        mUsersReference.child(phonee).child("conversation").addChildEventListener
                                                (new ChildEventListener() {
                                                    @Override
                                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                        Conversation c = dataSnapshot.getValue(Conversation.class);
                                                        if(c == null){
                                                            return;
                                                        }
                                                        if (c.getPhone_number().contains(currentUser.getPhoneNumber())) {
                                                            Intent goToOneAndOneChat = new Intent(view.getContext(), OneToOneChatSettings.class);
                                                            goToOneAndOneChat.putExtra("user_phone", currentUser.getPhoneNumber());
                                                            goToOneAndOneChat.putExtra("user_name", currentUser.getNameStoredInPhone());
                                                            goToOneAndOneChat.putExtra("user_picture", currentUser.getThumbnail());
                                                            goToOneAndOneChat.putExtra("chat_id", dataSnapshot.getKey());

                                                            view.getContext().startActivity(goToOneAndOneChat);
                                                        }else{
                                                            //fix
//                                                            Intent chatIntent = new Intent(view.getContext(), ChatActivity.class);
//                                                            //@author
//                                                            if(currentUser.getNameStoredInPhone() == null){
//                                                                return;
//                                                            }
//                                                            chatIntent.putExtra("user_name", currentUser.getNameStoredInPhone());
//                                                            chatIntent.putExtra("user_phone", currentUser.getPhoneNumber());
//                                                            chatIntent.putExtra("user_picture", currentUser.getThumbnail());
//
//                                                            view.getContext().startActivity(chatIntent);
                                                        }
                                                    }

                                                    @Override
                                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                                    }

                                                    @Override
                                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                                    }

                                                    @Override
                                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                    }

                                    break;
                                    default:
                                        return;
                            }

                        });

                AlertDialog dia = builder.create();
                dia.show();

            }
        });

    }



    @Override
    public int getItemCount() {
        return mUsersInCurrentGroup.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView mContactName;
        TextView mProfileStatus;
        TextView mProfileName;
        TextView mAdminText;
        CircleImageView userProfilePic;
        ConstraintLayout cstLayout;

         public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mContactName = mView.findViewById(R.id.contactName);
            mProfileStatus = mView.findViewById(R.id.profileStatus);
            mProfileName = mView.findViewById(R.id.profileName);
            userProfilePic = mView.findViewById(R.id.profilePic);
            mAdminText = mView.findViewById(R.id.adminText);
            cstLayout = mView.findViewById(R.id.cstLayout);

        }

        public void setProfilePic(String thumnail){

            userProfilePic = mView.findViewById(R.id.profilePic);

            Picasso.get().load(thumnail).placeholder(R.drawable.ic_avatar).into(userProfilePic);

        }
    }
}
