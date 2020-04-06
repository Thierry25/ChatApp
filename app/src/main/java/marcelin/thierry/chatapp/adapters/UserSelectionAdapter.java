package marcelin.thierry.chatapp.adapters;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import marcelin.thierry.chatapp.classes.Users;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder> {

    private List<Users> usersStored;

    //@author fmarcelin
    public UserSelectionAdapter(List<Users> usersStored) {
        this.usersStored = usersStored;
        setHasStableIds(true);

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate
                (R.layout.users_main_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        final Users user = usersStored.get(position);
        holder.mProfileName.setText(String.format("~%s", user.getName()));
        holder.mProfileStatus.setText(user.getStatus());
        //holder.mContactName.setText(usersStoredNames.get(position));
        //@author fmarcelin

      //  holder.mContactsLayout.setBackgroundColor(user.isSelected() ? Color.parseColor("#20BF9F") : Color.parseColor("#F6F6F6"));
        if(user.isSelected()){
            holder.mUserProfile.setImageResource(R.drawable.ic_check_circle);
        }else{
            holder.setProfilePic(user.getThumbnail());
        }
        holder.mContactName.setText(user.getNameStoredInPhone());
   //     holder.setProfilePic(user.getThumbnail());

        holder.mContactsLayout.setOnClickListener(view -> {
            user.setSelected(!user.isSelected());
            if(user.isSelected()){
                holder.mUserProfile.setImageResource(R.drawable.ic_check_circle);
            }else{
                holder.setProfilePic(user.getThumbnail());
            }
          //  holder.mContactsLayout.setBackgroundColor(user.isSelected() ? Color.parseColor("#20BF9F") : Color.parseColor("#F6F6F6"));
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

    public class UserViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView mContactName;
        TextView mProfileStatus;
        TextView mProfileName;
        CircleImageView mUserProfile;
        ConstraintLayout mContactsLayout;

        public UserViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mContactName = mView.findViewById(R.id.contactName);
            mProfileStatus = mView.findViewById(R.id.profileStatus);
            mProfileName = mView.findViewById(R.id.profileName);
            mUserProfile = mView.findViewById(R.id.profilePic);
            //mContactsLayout = mView.findViewById(R.id.mainContactGroup);
            mContactsLayout = mView.findViewById(R.id.mainCLayout);
        }

        public void setProfilePic(String thumbnail){

            mUserProfile = mView.findViewById(R.id.profilePic);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(mUserProfile);


        }
    }

    public void updateList(List<Users> newList){
        usersStored = new ArrayList<>();
        usersStored.addAll(newList);
        notifyDataSetChanged();
    }
}
