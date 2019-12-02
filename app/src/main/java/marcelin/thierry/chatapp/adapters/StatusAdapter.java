package marcelin.thierry.chatapp.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devlomi.circularstatusview.CircularStatusView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Status;
import marcelin.thierry.chatapp.classes.UserStatus;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

  private List<UserStatus> mStatusList;
  private FirebaseAuth mAuth;

  public StatusAdapter(List<UserStatus> statusList) {
    this.mStatusList = statusList;
  }

  @NonNull
  @Override
  public StatusAdapter.StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_layout, parent, false);
    return new StatusViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull StatusAdapter.StatusViewHolder holder, int position) {

    holder.bind(mStatusList.get(position));

  }

  @Override
  public int getItemCount() {
    return mStatusList.size();
  }

  public class StatusViewHolder extends RecyclerView.ViewHolder {

    private CircularStatusView mCircularStatusView;
    private CircleImageView mProfilePic;
    private TextView mContact;// mStatusTimestamp;

    public StatusViewHolder(View itemView) {
      super(itemView);

      mCircularStatusView = itemView.findViewById(R.id.circular_status_view);
      mProfilePic = itemView.findViewById(R.id.profilePic);
      mContact = itemView.findViewById(R.id.contactName);

    }


    public void bind(UserStatus userStatus) {
      mAuth = FirebaseAuth.getInstance();
      String phoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
      mContact.setText(userStatus.getNameStoredInPhone());
      //Test should be done here to determine if it's image or video
     // mStatusTimestamp.setText(getSmsTodayYestFromMilli(userStatus.getTimestamp()));
      Picasso.get().load(userStatus.getContent()).placeholder(R.drawable.ic_avatar).into(mProfilePic);
      List<Status> statusList = userStatus.getStatusList();
      mCircularStatusView.setPortionsCount(statusList.size());
      int notSeenColor = itemView.getContext().getResources().getColor(R.color.light_blue);
      int seenColor = itemView.getContext().getResources().getColor(R.color.colorAccent);

      if (userStatus.areAllSeen()) {
        //set all portions color
        mCircularStatusView.setPortionsColor(seenColor);
      } else {
        for (int i = 0; i < statusList.size(); i++) {
          Status status = statusList.get(i);
          if(status.getSeenBy().containsKey(phoneNumber)){
            mCircularStatusView.setPortionColorForIndex(i,seenColor);
          }else{
            mCircularStatusView.setPortionColorForIndex(i, notSeenColor);
          }
        }

      }


      itemView.setOnClickListener(v -> {
        if (onStatusClickListener != null)
          onStatusClickListener.onStatusClick(mCircularStatusView, getAdapterPosition());
      });
    }
  }


  public interface OnStatusClickListener {
    void onStatusClick(CircularStatusView circularStatusView, int pos);
  }

  OnStatusClickListener onStatusClickListener;

  public void setOnStatusClickListener(OnStatusClickListener onStatusClickListener) {
    this.onStatusClickListener = onStatusClickListener;
  }

}
