package marcelin.thierry.chatapp.classes;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.NotificationAdapter;

public class NotificationDropDownMenu extends PopupWindow {

    private Context mContext;
    private RecyclerView notification_rv;
    private NotificationAdapter notificationAdapter;
    private List<ReplyNotification> replyNotificationList;

    public NotificationDropDownMenu(Context context, List<ReplyNotification> replyNotificationList) {
        super(context);
        this.mContext = context;
        this.replyNotificationList = replyNotificationList;
        setupView();
    }

    public void setNotificationSelectedListener(NotificationAdapter.NotificationSelectedListener notificationSelectedListener) {
        notificationAdapter.setNotificationSelectedListener(notificationSelectedListener);
    }

    private void setupView(){
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_popup, null);

        notification_rv = view.findViewById(R.id.notification_rv);
        notification_rv.setHasFixedSize(true);
        notification_rv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        notification_rv.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));

        notificationAdapter = new NotificationAdapter(mContext, replyNotificationList);
        notification_rv.setAdapter(notificationAdapter);
        view.setBackgroundResource(R.drawable.border);
        setContentView(view);
    }

}
