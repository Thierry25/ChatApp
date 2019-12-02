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
import marcelin.thierry.chatapp.classes.Channel;

public class MoodChannelAdapter extends RecyclerView.Adapter<MoodChannelAdapter.ChannelViewAdapter> {

    private List<Channel> mChannelList;

    public MoodChannelAdapter(List<Channel> mChannelList) {
        this.mChannelList = mChannelList;
    }

    @NonNull
    @Override
    public ChannelViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_main_layout_for_contact_group,
                parent, false);
        return new ChannelViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewAdapter holder, int position) {

        Channel currentChannel = mChannelList.get(position);
        holder.mProfileName.setText(MessageFormat.format("{0}subscribers ", String.valueOf(currentChannel.getSubscribers().size())));
        if(!currentChannel.getNewName().equals("")){
            holder.mContactName.setText(String.format("~%s", currentChannel.getNewName()));
        }else{
            holder.mContactName.setText(String.format("~%s", currentChannel.getName()));
        }

        holder.mProfileStatus.setText(currentChannel.getLink());
        //holder.mContactName.setText(usersStoredNames.get(position));
        //@author fmarcelin
        holder.mContactsLayout.setBackgroundColor(currentChannel.isSelected() ? Color.CYAN : Color.WHITE);
        holder.setProfilePic(currentChannel.getThumbnail());

        holder.mContactsLayout.setOnClickListener(view -> {
            currentChannel.setSelected(!currentChannel.isSelected());
            holder.mContactsLayout.setBackgroundColor(currentChannel.isSelected() ? Color.CYAN : Color.WHITE);

        });

    }

    @Override
    public int getItemCount() {
        return mChannelList.size();
    }

    public class ChannelViewAdapter extends RecyclerView.ViewHolder {

        View mView;
        TextView mContactName;
        TextView mProfileStatus;
        TextView mProfileName;
        CircleImageView mUserProfile;
        ConstraintLayout mContactsLayout;

        public ChannelViewAdapter(View itemView) {
            super(itemView);

            mView = itemView;

            mContactName = mView.findViewById(R.id.contactName);
            mProfileStatus = mView.findViewById(R.id.profileStatus);
            mProfileName = mView.findViewById(R.id.profileName);
            mUserProfile = mView.findViewById(R.id.profilePic);
            mContactsLayout = mView.findViewById(R.id.mainContactGroup);
        }

        public void setProfilePic(String thumbnail){

            mUserProfile = mView.findViewById(R.id.profilePic);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(mUserProfile);


        }
    }
}
