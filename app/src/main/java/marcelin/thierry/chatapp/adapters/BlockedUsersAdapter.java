package marcelin.thierry.chatapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Users;

public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.BlockedUserViewHolder> {

    private List<Users> mUsersList;
    private Context mContext;
    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_users");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public BlockedUsersAdapter(List<Users> mUsersList, Context mContext) {
        this.mUsersList = mUsersList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public BlockedUsersAdapter.BlockedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_main_layout, parent, false);
        return new BlockedUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockedUsersAdapter.BlockedUserViewHolder holder, int position) {

        Users currentUser = mUsersList.get(position);
        holder.setProfilePic(currentUser.getThumbnail());
        String value = currentUser.getStatus();
        if(value.length() > 25){
            value = value.substring(0, 21) + "...";
            holder.profileStatus.setText(value);
        }else {
            holder.profileStatus.setText(currentUser.getStatus());
        }

        String val = currentUser.getNameStoredInPhone();
        if(val.length() > 30){
            val = val.substring(0, 27) + "...";
            holder.contactName.setText(val);
        }
        holder.contactName.setText(currentUser.getNameStoredInPhone());

        String mCurrentPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        holder.cstLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setPositiveButton(R.string.ok, (id, dialog) ->{
                    mUsersReference.child(mCurrentPhone).child("blocked").child(currentUser.getPhoneNumber()).removeValue();
                    mUsersReference.child(currentUser.getPhoneNumber()).child("blocked_by").child(mCurrentPhone).removeValue();
                    Toast.makeText(view.getContext(), R.string.u_unblock, Toast.LENGTH_SHORT).show();

                });
                builder.setNegativeButton(R.string.cancel, (id, dialog) ->{
                    Toast.makeText(view.getContext(), R.string.cancel, Toast.LENGTH_SHORT).show();
                });

                AlertDialog dial = builder.create();
                dial.setIcon(R.drawable.ic_warning);
                dial.setTitle(R.string.unblock);

                dial.setMessage(mContext.getResources().getString(R.string.unb_text) + " " + currentUser.getNameStoredInPhone() +"?");
                dial.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }

    public class BlockedUserViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout cstLayout;
        CircleImageView profilePiic;
        TextView contactName;
        TextView profileStatus;

        public BlockedUserViewHolder(View itemView) {
            super(itemView);

            profilePiic = itemView.findViewById(R.id.profilePic);
            contactName = itemView.findViewById(R.id.contactName);
            profileStatus = itemView.findViewById(R.id.profileStatus);
            cstLayout = itemView.findViewById(R.id.mainCLayout);

        }

        public void setProfilePic(String thumnail){

            profilePiic = itemView.findViewById(R.id.profilePic);

            Picasso.get().load(thumnail).placeholder(R.drawable.ic_avatar).into(profilePiic);


        }
    }
}
