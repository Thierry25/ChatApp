package marcelin.thierry.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Messages;

public class DocsAdapter extends RecyclerView.Adapter<DocsAdapter.DocumentViewHolder> {

    private List<Messages> mMessagesList;
    private Context mContext;

    public DocsAdapter(List<Messages> mMessagesList, Context mContext) {
        this.mMessagesList = mMessagesList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public DocsAdapter.DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_layout, parent,
                false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocsAdapter.DocumentViewHolder holder, int position) {

        Messages m = mMessagesList.get(position);

        holder.timestamp.setText(getDate(m.getTimestamp()));

        String s = m.getContent();
        if(s.length() > 30){
            s = s.substring(0, 26) + "...";
            holder.title.setText(s);
        }else{
            holder.title.setText(m.getContent());
        }
        holder.rLayout.setOnClickListener(view -> {
            Intent myIntent = new Intent(Intent.ACTION_VIEW);
            myIntent.setData(Uri.parse(m.getContent()));
            String x = mContext.getResources().getString(R.string.choose_app);
            Intent docToOpen = Intent.createChooser(myIntent, x);
            view.getContext().startActivity(docToOpen);
        });

    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class DocumentViewHolder extends RecyclerView.ViewHolder {

        private TextView timestamp;
        private TextView title;
        private LinearLayout rLayout;

        public DocumentViewHolder(View itemView) {
            super(itemView);

            timestamp = itemView.findViewById(R.id.timestamp);
            title = itemView.findViewById(R.id.title);
            rLayout = itemView.findViewById(R.id.rLayout);
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        try {
            cal.setTimeInMillis(time);
            return DateFormat.format("MMM dd, HH:mm", cal).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

