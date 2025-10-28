package com.grzeluu.lookupplant.utils.notification;

import static com.grzeluu.lookupplant.utils.TimeUtils.getTimestampForNotification;
import static com.grzeluu.lookupplant.utils.notification.PlantNotification.CHANNEL_ID;
import static com.grzeluu.lookupplant.utils.notification.PlantNotification.ID_EXTRA;
import static com.grzeluu.lookupplant.utils.notification.PlantNotification.MESSAGE_EXTRA;
import static com.grzeluu.lookupplant.utils.notification.PlantNotification.TITLE_EXTRA;

import android.app.AlarmManager;
import androidx.core.app.AlarmManagerCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.base.App;
import com.grzeluu.lookupplant.model.UserPlant;

public class NotificationUtils {
    private static final String TAG = "NotificationUtils";

    public static void scheduleNotificationForPlant(Context context, UserPlant plant) {
        String title = context.getString(R.string.plant_notification_title);
        String message = context.getString(R.string.plant_notification_message, plant.getName());

        Intent intent = new Intent(context.getApplicationContext(), PlantNotification.class);
        intent.putExtra(MESSAGE_EXTRA, message);
        intent.putExtra(TITLE_EXTRA, title);
        intent.putExtra(ID_EXTRA, getNotificationID(plant.getId()));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(),
                getNotificationID(plant.getId()),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long days = plant.getDaysToClosestAction();
        Long time = getTimestampForNotification(days);

        // проверяем возможность ставить точные алармы
        boolean canExact;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canExact = alarmManager.canScheduleExactAlarms();
        } else {
            canExact = true;
        }

        if (canExact) {
            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                Log.d(TAG, "Exact alarm scheduled for plant " + plant.getId());
            } catch (SecurityException se) {
                Log.w(TAG, "SecurityException scheduling exact alarm - fallback", se);
                // fallback: ставим inexact alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        } else {
            if (context instanceof android.app.Activity) {
                Intent intentSettings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intentSettings);
            }

            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                Log.d(TAG, "Inexact alarm scheduled (fallback) for plant " + plant.getId());
            } catch (SecurityException se) {
                Log.w(TAG, "Still can't schedule alarm", se);
            }
        }
    }

    public static void deleteNotification(Context context, String id){
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(getNotificationID(id));
    }

    public static void createNotificationChannel(Context context) {
        String name = context.getString(R.string.notification_channel_name);
        String desc = context.getString(R.string.notification_channel_description);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    name,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(desc);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static int getNotificationID(String id) {
        String subId = id.substring(id.length() - 13, id.length() - 4);
        return Integer.parseInt(subId);
    }
}
