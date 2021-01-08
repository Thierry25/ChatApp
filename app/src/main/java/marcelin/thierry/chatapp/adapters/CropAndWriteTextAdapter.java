package marcelin.thierry.chatapp.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import marcelin.thierry.chatapp.R;

public class CropAndWriteTextAdapter extends RecyclerView.Adapter<CropAndWriteTextAdapter.UserViewHolder> {
    private  List<Uri> mArrayList;

    public CropAndWriteTextAdapter(List<Uri> mArrayList){
        this.mArrayList = mArrayList;
    }
    @NonNull
    @Override
    public CropAndWriteTextAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.crop_write_layout,parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CropAndWriteTextAdapter.UserViewHolder holder, int position) {
        Uri currentUri  = mArrayList.get(position);
        
        holder.setProfilePic(currentUri);
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setProfilePic(Uri thumbnail){

            ImageView img_to_crop = itemView.findViewById(R.id.img_to_crop);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(img_to_crop);

        }
    }

}
