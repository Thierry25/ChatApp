package marcelin.thierry.chatapp.classes;

import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;

import marcelin.thierry.chatapp.activities.ReceiveActivity;
import marcelin.thierry.chatapp.activities.ReceiveVideoActivity;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    // private final String CHANNEL_ID = "ADS Messenger"
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData().size() < 1){
            return;
        }

        if(remoteMessage.getData().get("channel_id").length() == 20) {

            Intent intent = new Intent(this, ReceiveVideoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("channel_id", remoteMessage.getData().get("channel_id"));
            intent.putExtra("user_phone", remoteMessage.getData().get("user_phone"));
            getApplicationContext().startActivity(intent);

        } else{

            Intent intent = new Intent(this, ReceiveActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("channel_id", remoteMessage.getData().get("channel_id"));
            intent.putExtra("user_phone", remoteMessage.getData().get("user_phone"));
            getApplicationContext().startActivity(intent);
        }
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


}
