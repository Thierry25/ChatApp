package marcelin.thierry.chatapp.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Users;

public class TryAdapter extends RecyclerView.Adapter<TryAdapter.UserViewHolder> {

    private List<Users> mUsersList;

    public TryAdapter(List<Users> mUsersList) {
        this.mUsersList = mUsersList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.seen_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users currentUser = mUsersList.get(position);

        holder.mContactName.setText(currentUser.getNameStoredInPhone());
        holder.mProfileStatus.setText(getSmsTodayYestFromMilli(currentUser.getTimestamp()));

        holder.setProfilePic(currentUser.getThumbnail());

    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        CircleImageView mProfilePic;
        TextView mContactName, mProfileStatus;

        public UserViewHolder(View itemView) {
            super(itemView);

            mContactName = itemView.findViewById(R.id.contactName);
            mProfileStatus = itemView.findViewById(R.id.profileStatus);
        }

        public void setProfilePic(String thumbnail) {

            mProfilePic = itemView.findViewById(R.id.profilePic);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(mProfilePic);

        }
    }

    public String getSmsTodayYestFromMilli(long msgTimeMillis) {

        Calendar messageTime = Calendar.getInstance();
        messageTime.setTimeInMillis(msgTimeMillis);
        // get Currunt time
        Calendar now = Calendar.getInstance();

        final String strTimeFormate = "h:mm aa";
        final String strDateFormate = "dd/MM/yyyy h:mm aa";

        if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE)
                &&
                ((now.get(Calendar.MONTH) == messageTime.get(Calendar.MONTH)))
                &&
                ((now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)))
        ) {

            return "today at " + DateFormat.format(strTimeFormate, messageTime);

        } else if (
                ((now.get(Calendar.DATE) - messageTime.get(Calendar.DATE)) == 1)
                        &&
                        ((now.get(Calendar.MONTH) == messageTime.get(Calendar.MONTH)))
                        &&
                        ((now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)))
        ) {
            return "yesterday at " + DateFormat.format(strTimeFormate, messageTime);
        } else {
            return "date : " + DateFormat.format(strDateFormate, messageTime);
        }
    }
}
