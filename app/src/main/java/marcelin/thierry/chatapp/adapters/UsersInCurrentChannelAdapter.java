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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.OneToOneChatSettings;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.Users;

public class UsersInCurrentChannelAdapter extends RecyclerView.Adapter
        <UsersInCurrentChannelAdapter.UsersViewHolder> {

    private List<Users> mUsersInCurrentChannel;
    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel");

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Context mContext;

    public UsersInCurrentChannelAdapter(List<Users> mUsersInCurrentChannel, Context mContext) {
        this.mUsersInCurrentChannel = mUsersInCurrentChannel;
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

        Users currentUser = mUsersInCurrentChannel.get(position);

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
                builder.setTitle("Choose an option")
                        .setIcon(R.drawable.ic_option)
                        .setItems(R.array.options___, (dialog, id) ->{
                            switch (id){
                                case 0:
                                    AlertDialog.Builder alBuilder = new AlertDialog.Builder(view.getContext());
                                    alBuilder.setPositiveButton(R.string.ok, (dial, id_) ->{

                                        mChannelReference.child(currentUser.getChatId()).child("users")
                                                .child(currentUser.getPhoneNumber()).removeValue();
                                        mChannelReference.child(currentUser.getPhoneNumber())
                                                .child("conversation").child("C-"+currentUser.getChatId())
                                                .removeValue();
                                        Toast.makeText(view.getContext(), R.string.us_dl_chan,
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
                                    dialo.setTitle(R.string.delete_channel);
                                    dialo.setMessage(mContext.getResources().getString(R.string.sure_del) +
                                            " " + nameStored_ + " " + mContext.getResources().getString(R.string.fr_cha));
                                    dialo.show();

                                    break;
                                case 1:

                                    String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
                                    if (phone != null) {
                                        mUsersReference.child(phone).child("conversation").addChildEventListener
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
                                                            Toast.makeText(view.getContext(), R.string.no_conv
                                                                    , Toast.LENGTH_SHORT).show();
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

                AlertDialog dialog = builder.create();
                dialog.setIcon(R.drawable.ic_option);
                dialog.setTitle("Choose an option");
                dialog.show();
            }
        });

    }



    @Override
    public int getItemCount() {
        return mUsersInCurrentChannel.size();
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

    public void updateList(List<Users> newList){
        mUsersInCurrentChannel = new ArrayList<>();
        mUsersInCurrentChannel = new ArrayList<>();
        mUsersInCurrentChannel.addAll(newList);
        notifyDataSetChanged();
    }
}
