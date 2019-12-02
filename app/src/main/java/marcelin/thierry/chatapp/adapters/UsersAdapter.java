package marcelin.thierry.chatapp.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Users;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<Users> usersInGroup;

    public UsersAdapter(List<Users> usersInGroup) {
        this.usersInGroup = usersInGroup;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_in_group,
                parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        Users currentUser = usersInGroup.get(position);

        holder.nameUser.setText(currentUser.getNameStoredInPhone());
        holder.setProfilePic(currentUser.getThumbnail());

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
        return usersInGroup.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageUser;
        TextView nameUser;

        public UserViewHolder(View itemView) {
            super(itemView);

            imageUser = itemView.findViewById(R.id.imageUser);
            nameUser = itemView.findViewById(R.id.nameUser);
        }

        public void setProfilePic(String thumbnail){

            imageUser = itemView.findViewById(R.id.imageUser);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(imageUser);

        }
    }
}
