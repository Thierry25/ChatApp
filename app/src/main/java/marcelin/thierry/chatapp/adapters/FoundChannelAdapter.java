package marcelin.thierry.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.ChannelSubscriberActivity;
import marcelin.thierry.chatapp.classes.Channel;

public class FoundChannelAdapter extends RecyclerView.Adapter<FoundChannelAdapter.ChannelViewHolder> {

    private List<Channel> mChannelFound;
    private Context mContext;

    public FoundChannelAdapter(List<Channel> mChannelFound, Context mContext) {
        this.mChannelFound = mChannelFound;
        this.mContext = mContext;
        setHasStableIds(true);
    }

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate
                (R.layout.found_channel_layout, parent, false);

        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {

        Channel currentChannel = mChannelFound.get(position);

        String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        holder.setProfilePic(currentChannel.getThumbnail());
        holder.channelSubscribers.setText(MessageFormat.format("{0}{1}", currentChannel.getSubscribers().size(), mContext.getString(R.string.subscribers)));
        holder.channelName.setText(currentChannel.getName());

        final DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference()
                .child("ads_channel").child(currentChannel.getName())
                .child("subscribers");

        assert phone != null;
        final DatabaseReference users_reference = FirebaseDatabase.getInstance().getReference()
                .child("ads_users").child(phone).child("conversation");

        holder.channelLayout.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

            builder.setPositiveButton(R.string.ok, (dialog, id) -> {

                users_reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("C-" + currentChannel.getName())){
                            Toast.makeText(view.getContext()
                                    , R.string.already
                                    , Toast.LENGTH_SHORT).show();
                        }else{

                            Map<String, Object> m = new HashMap<>();
                            m.put(phone, ServerValue.TIMESTAMP);
                            rootReference.updateChildren(m);

                            Map<String, Object> convoInfo = new HashMap<>();
                            convoInfo.put("id", currentChannel.getName());
                            convoInfo.put("phone_number", "");
                            convoInfo.put("timestamp",  ServerValue.TIMESTAMP);
                            convoInfo.put("type", "channel");
                            convoInfo.put("visible", true);
                            users_reference.child("C-"+currentChannel.getName()).updateChildren(convoInfo);

                            Intent goToSubChannelActivity = new Intent(view.getContext(),
                                    ChannelSubscriberActivity.class);
                            goToSubChannelActivity.putExtra("Channel_id", currentChannel.getName());
                            goToSubChannelActivity.putExtra("Channel_picture", currentChannel.getThumbnail());
                            view.getContext().startActivity(goToSubChannelActivity);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            });
            builder.setNegativeButton("CANCEL", (dialog, id) ->
                    Toast.makeText(view.getContext(),"Canceled", Toast.LENGTH_SHORT).show());

            AlertDialog dialog = builder.create();
            dialog.setTitle(mContext.getString(R.string.cha) + currentChannel.getName());
            dialog.setMessage(mContext.getResources().getString(R.string.subs_to_C));
            dialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return mChannelFound.size();
    }

    public class ChannelViewHolder extends RecyclerView.ViewHolder {

        LinearLayout channelLayout;
        CircleImageView channelImage;
        TextView channelName;
        TextView channelSubscribers;

        public ChannelViewHolder(View itemView) {
            super(itemView);

            channelLayout = itemView.findViewById(R.id.channelLayout);
            channelImage = itemView.findViewById(R.id.channelImage);
            channelName = itemView.findViewById(R.id.channel_name);
            channelSubscribers = itemView.findViewById(R.id.channel_subscribers);

        }

        public void setProfilePic(String thumbnail){

            channelImage = itemView.findViewById(R.id.channelImage);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(channelImage);

        }
    }
}
