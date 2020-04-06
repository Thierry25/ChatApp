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

import java.text.MessageFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Group;

public class MoodGroupAdapter extends RecyclerView.Adapter<MoodGroupAdapter.GroupViewAdapter> {

    private List<Group> mGroupList;

    public MoodGroupAdapter(List<Group> mGroupList) {
        this.mGroupList = mGroupList;
    }

    @NonNull
    @Override
    public GroupViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_main_layout,
                parent, false);
        return new GroupViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewAdapter holder, int position) {

        Group currentGroup = mGroupList.get(position);
        if(!currentGroup.getNewName().equals("")){
            holder.mContactName.setText(String.format("~%s", currentGroup.getNewName()));
        }else{
            holder.mContactName.setText(String.format("~%s", currentGroup.getName()));
        }

        holder.mProfileStatus.setText(currentGroup.getLink());
        //holder.mContactName.setText(usersStoredNames.get(position));
        //@author fmarcelin
        //holder.mContactsLayout.setBackgroundColor(currentGroup.isSelected() ? Color.CYAN : Color.WHITE);
        holder.mProfileName.setText(MessageFormat.format("{0}participants", String.valueOf(currentGroup.getUsers().size())));
        if(currentGroup.isSelected()){
            holder.mUserProfile.setImageResource(R.drawable.ic_check_circle);
        }else{
            holder.setProfilePic(currentGroup.getThumbnail());
        }
        //holder.setProfilePic(currentGroup.getThumbnail());

        holder.mContactsLayout.setOnClickListener(view -> {
            currentGroup.setSelected(!currentGroup.isSelected());
         //   holder.mContactsLayout.setBackgroundColor(currentGroup.isSelected() ? Color.CYAN : Color.WHITE);
            if(currentGroup.isSelected()){
                holder.mUserProfile.setImageResource(R.drawable.ic_check_circle);
            }else{
                holder.setProfilePic(currentGroup.getThumbnail());
            }

        });

    }

    @Override
    public int getItemCount() {
        return mGroupList.size();
    }

    public class GroupViewAdapter extends RecyclerView.ViewHolder {

        View mView;
        TextView mContactName;
        TextView mProfileStatus;
        TextView mProfileName;
        CircleImageView mUserProfile;
        ConstraintLayout mContactsLayout;

        public GroupViewAdapter(View itemView) {
            super(itemView);

            mView = itemView;

            mContactName = mView.findViewById(R.id.contactName);
            mProfileStatus = mView.findViewById(R.id.profileStatus);
            mProfileName = mView.findViewById(R.id.profileName);
            mUserProfile = mView.findViewById(R.id.profilePic);
            mContactsLayout = mView.findViewById(R.id.mainCLayout);
        }

        public void setProfilePic(String thumbnail){

            mUserProfile = mView.findViewById(R.id.profilePic);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(mUserProfile);


        }
    }
}
