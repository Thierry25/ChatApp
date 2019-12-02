package marcelin.thierry.chatapp.adapters;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.ChatActivity;
import marcelin.thierry.chatapp.activities.ProfilePictureActivity;

import marcelin.thierry.chatapp.classes.Users;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.UserviewHolder>{

    private List<Users> usersStored;

    //@author fmarcelin
    public ContactAdapter(List<Users> usersStored) {
        this.usersStored = usersStored;

    }

    @NonNull
    @Override
    public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_main_layout,
                parent, false);
        return new UserviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserviewHolder holder, final int position) {

        final Users user = usersStored.get(position);
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
        // TODO: Add view profile pic feature

        holder.userProfilePic.setOnClickListener(view -> {

            Intent profilePicIntent = new Intent(view.getContext(), ProfilePictureActivity.class);
            profilePicIntent.putExtra("user_picture", user.getThumbnail());
            //@author
            profilePicIntent.putExtra("user_name", user.getNameStoredInPhone());
            view.getContext().startActivity(profilePicIntent);
        });

        holder.mView.setOnClickListener(view -> {

            Intent chatIntent = new Intent(view.getContext(), ChatActivity.class);
            //@author
            chatIntent.putExtra("user_name", user.getNameStoredInPhone());
            chatIntent.putExtra("user_phone", user.getPhoneNumber());
            chatIntent.putExtra("user_picture", user.getThumbnail());

            view.getContext().startActivity(chatIntent);
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

    public static class UserviewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView mContactName;
        TextView mProfileStatus;
        TextView mProfileName;
        CircleImageView userProfilePic;

        public UserviewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mContactName = mView.findViewById(R.id.contactName);
            mProfileStatus = mView.findViewById(R.id.profileStatus);
            mProfileName = mView.findViewById(R.id.profileName);
            userProfilePic = mView.findViewById(R.id.profilePic);

        }

        public void setProfilePic(String thumbnail){

             userProfilePic = mView.findViewById(R.id.profilePic);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(userProfilePic);

        }
    }

    public void updateList(List<Users> newList){
        usersStored = new ArrayList<>();
        usersStored.addAll(newList);
        notifyDataSetChanged();
    }

}
