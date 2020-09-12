package marcelin.thierry.chatapp.classes;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.ChannelSubscriberActivity;
import marcelin.thierry.chatapp.activities.ChatActivity;
import marcelin.thierry.chatapp.activities.GroupChatActivity;
import marcelin.thierry.chatapp.activities.MainActivity;
import marcelin.thierry.chatapp.activities.ReactionActivity;
import marcelin.thierry.chatapp.activities.ReceiveActivity;
import marcelin.thierry.chatapp.activities.ReceiveVideoActivity;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    // private final String CHANNEL_ID = "ADS Messenger"
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() < 1) {
            return;
        }

        if (remoteMessage.getData().get("channel_id") != null) {

            if(Objects.requireNonNull(remoteMessage.getData().get("channel_id")).length() == 20) {
                Log.i("FIRE_VIDEO_DATA", "CALLED");
                Intent intent = new Intent(this, ReceiveVideoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("channel_id", remoteMessage.getData().get("channel_id"));
                intent.putExtra("user_phone", remoteMessage.getData().get("user_phone"));
                getApplicationContext().startActivity(intent);

            }else if(Objects.requireNonNull(remoteMessage.getData().get("channel_id")).length() == 18){
                Log.i("FIRE_LIVE_DATA", "CALLED");
                Intent intent = new Intent(this, ReactionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("channel_id", remoteMessage.getData().get("channel_id"));
                intent.putExtra("user_phone", remoteMessage.getData().get("user_phone"));
                getApplicationContext().startActivity(intent);
            } else {
                Log.i("FIRE_CALL_DATA", "CALLED");
                Intent intent = new Intent(this, ReceiveActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("channel_id", remoteMessage.getData().get("channel_id"));
                intent.putExtra("user_phone", remoteMessage.getData().get("user_phone"));
                getApplicationContext().startActivity(intent);
            }
            return;
        }

        if (remoteMessage.getData().get("cid") != null) {
            Log.i("FIRE_CONVERSATION", "CALLED");
            String conversationId = remoteMessage.getData().get("cid");
            String conversationType = remoteMessage.getData().get("k");
            String fromPhone = remoteMessage.getData().get("p");
            String fromName = remoteMessage.getData().get("n");
            String fromImg = remoteMessage.getData().get("im");
            String icon = remoteMessage.getData().get("i");
            String message = remoteMessage.getData().get("m");

            int count = getUnreadMessageCount(conversationId);
            setUnreadMessageCount(conversationId, count++);

            //TODO: [fm] create cases for type of conversation
            Intent i;
            switch (conversationType) {
                case "chat":
                    Log.i("FIRE_CHAT", "CALLED");
                    i = new Intent(this, ChatActivity.class);
                    //TODO: [fm] get name from contact instead
                    i.putExtra("user_name", fromName);
                    i.putExtra("user_phone", fromPhone);
                    i.putExtra("user_picture", fromImg);
                    i.putExtra("chat_id", conversationId);
                    break;

                case "channel":
                    Log.i("FIRE_CHANNEL", "CALLED");
                    i = new Intent(this, ChannelSubscriberActivity.class);
                    break;

                case "group":
                    Log.i("FIRE_GROUP", "CALLED");
                    i = new Intent(this, GroupChatActivity.class);
                    break;

                default:
                    Log.i("FIRE_MAIN", "CALLED");
                    i = new Intent(this, MainActivity.class);
                    break;
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i,
                    PendingIntent.FLAG_ONE_SHOT);

            initChannels(this);
            NotificationCompat.Builder notificationBuilder = null;
            try {
                notificationBuilder = new NotificationCompat.Builder(this, "ads_notification")
                        .setSmallIcon(R.drawable.ic_insert_emoticon)
                        .setContentTitle(URLDecoder.decode(fromName, "UTF-8"))
                        .setContentText(URLDecoder.decode(message, "UTF-8"))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent);
            } catch (UnsupportedEncodingException e) {
                Log.i("FIRE_NOTIFICATION", "NOTIFICATION BUILDER CALLED");
                e.printStackTrace();
            }

            if (notificationBuilder != null) {
                Log.i("FIRE_NOTIFICATION", "NOTIFICATION MANAGER CALLED");
                NotificationManager notificationManager = (NotificationManager) getSystemService(
                        Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, notificationBuilder.build());
            } else {
                Log.i("FIRE_NOTIFICATION", "FAILED");
            }
            return;
        }

        Log.i("FIRE_FUNCTION", "NOT IMPLEMENTED");

        //String from_user_id = remoteMessage.getData().get("from_user_id");
/*
        String userPhone = "";
        String name = "";

        if(remoteMessage.getData().size() > 0){
            userPhone = remoteMessage.getData().get("from_user_id");
            name = Users.getLocalContactList().get(userPhone);
            name = name != null && name.length() > 0 ? name :
                        userPhone;
        }
        String clickAction = Objects.requireNonNull(remoteMessage.getNotification()).getClickAction();

        sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), userPhone, clickAction, name);
    }

    private void sendNotification(String messageTitle, String messageBody, String userPhone, String clickAction, String name){

        Intent resultIntent = new Intent(clickAction);
        resultIntent.putExtra("user_phone", userPhone);
        resultIntent.putExtra("user_name", name);
0
        PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_ONE_SHOT
                        );

        String channelId = getString(R.string.default_notification_channel_id);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(messageTitle)
                                .setContentText(messageBody)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId,
                            "Channel human readable title",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(channel);
                    }
                }

                if (notificationManager != null) {
                    notificationManager.notify(0 /* ID of notification */ //, notificationBuilder.build());
                //}

    }

    private void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("ads_notification",
                "ADS Channel",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("ADS Message Notification");
        notificationManager.createNotificationChannel(channel);
    }

    private void setUnreadMessageCount(String conversationId, int count) {
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putInt(conversationId, count);
        editor.apply();
    }

    private int getUnreadMessageCount(String conversationId) {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        return prefs.getInt(conversationId, 0);
    }


}
