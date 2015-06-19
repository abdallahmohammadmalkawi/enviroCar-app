package org.envirocar.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;

import org.envirocar.app.injection.InjectionForApplication;
import org.envirocar.app.injection.Injector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * @author dewall
 */
public class NotificationHandler {
    public static final int STATE_SEARCHING = 100;
    @Inject
    @InjectionForApplication
    protected Context mContext;
    private int mId = 1133;
    private NotificationManager mNotificationManager;
    private PendingIntent mBaseContentIntent;

    /**
     * @param context
     */
    public NotificationHandler(Context context) {
        // Inject ourselves
        ((Injector) context).injectObjects(this);

        // get the system service for notifications.
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context
                .NOTIFICATION_SERVICE);

        // Set up the pending intent for the Mainactivity
        Intent baseIntent = new Intent(mContext, BaseMainActivity.class);
        mBaseContentIntent = PendingIntent.getActivity(mContext, 0, baseIntent, 0);
    }

    /**
     * @action Can also contain the http status code with error if fail
     */
    public void createNotification(String action) {
        String notification_text = "";
        if (action.equals("success")) {
            notification_text = mContext.getString(R.string.upload_notification_success);
        } else if (action.equals("start")) {
            notification_text = mContext.getString(R.string.upload_notification);
        } else {
            notification_text = action;
        }

        Intent intent = new Intent(mContext, BaseMainActivity.class);
        PendingIntent pintent = PendingIntent.getActivity(mContext, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("enviroCar")
                        .setContentText(notification_text)
                        .setContentIntent(pintent)
                        .setTicker(notification_text);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());

    }

    public void startNotificationForService(Service service) {
        Intent notificationIntent = new Intent(mContext, BaseMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.bigText("super big text");
        bigTextStyle.setBigContentTitle("super big content text");

        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentTitle("super content title")
                .setContentText("super content text")
                .setContentIntent(pendingIntent)
                .setStyle(bigTextStyle)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .addAction(android.R.drawable.button_onoff_indicator_off, "wurst", null);

        service.startForeground(mId, builder.build());
    }

    public void setNotificationState(NotificationState state) {
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setContentTitle(state.getNotificationTitle());
        builder.setContentText(state.getNotificationContent());
        builder.setSmallIcon(state.getSmallIconId());

        // TODO
        builder.setLargeIcon(getBitmap(mContext.getResources().getDrawable(state.getLargeIconId())));
        builder.setContentIntent(mBaseContentIntent);

        if (state.isShowingBigText()) {
            Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
            bigTextStyle.bigText(state.getNotificationTitle());
            bigTextStyle.setBigContentTitle(state.getNotificationContent());
            builder.setStyle(bigTextStyle);
        }

        for (NotificationActionHolder holder : state.getActions()) {
            builder.addAction(holder.mActionIcon, holder.mActionTitle, holder.mPendingIntent);
        }

        mNotificationManager.notify(mId, builder.build());
    }

    /**
     * TODO move into a static utils class
     *
     * @param drawable
     * @return
     */
    private Bitmap getBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Enumeration reflecting the possible states of the application.
     */
    public enum NotificationState implements NotificationContent {
        UNCONNECTED {
            @Override
            public String getNotificationTitle() {
                return "Device is unconnected.";
            }

            @Override
            public String getNotificationContent() {
                return "The device is not connected to any OBD device yet.";
            }

            @Override
            public boolean isShowingBigText() {
                return true;
            }
        },
        DISCOVERING {
            @Override
            public String getNotificationTitle() {
                return "Discovering";
            }

            @Override
            public String getNotificationContent() {
                return "Discovering for other bluetooth devices in the near Environment.";
            }

            @Override
            public boolean isShowingBigText() {
                return true;
            }

            @Override
            public NotificationActionHolder[] getActions() {
                List<NotificationActionHolder> list = new ArrayList<NotificationActionHolder>();
                list.add(new NotificationActionHolder(android.R.drawable
                        .ic_menu_close_clear_cancel, "juhu", null));
                return super.getActions();
            }
        },
        OBD_FOUND {
            @Override
            public String getNotificationTitle() {
                return "OBD Device in Range.";
            }

            @Override
            public String getNotificationContent() {
                return "The selected OBD device is found. Trying to connect";
            }

            @Override
            public boolean isShowingBigText() {
                return true;
            }
        },
        CONNCECTED {
            @Override
            public String getNotificationTitle() {
                return "OBD is connected.";
            }

            @Override
            public String getNotificationContent() {
                return "Successfully connected to the OBD device.";
            }

            @Override
            public boolean isShowingBigText() {
                return false;
            }
        };


        @Override
        public int getSmallIconId() {
            return android.R.drawable.alert_light_frame;
        }

        @Override
        public int getLargeIconId() {
            return R.drawable.ic_launcher;
        }


        @Override
        public NotificationActionHolder[] getActions() {
            return new NotificationActionHolder[0];
        }
    }

    /**
     * Interface for the NotificationState enumeration that provides access
     * to the state specific settings.
     */
    private interface NotificationContent {
        /**
         * Returns the string for the notification title.
         *
         * @return the title of the notification.
         */
        String getNotificationTitle();

        /**
         * Returns the string for the notification content.
         *
         * @return the content of the notification.
         */
        String getNotificationContent();

        /**
         * Returns a boolean value that indicates whether the notification is showing a big text.
         *
         * @return true if the notification should be shown in a big text style.
         */
        boolean isShowingBigText();

        /**
         * Returns the id of the small icon to be shown in the notification.
         *
         * @return id of the small icon.
         */
        int getSmallIconId();

        /**
         * Return the id of the large icon to be shown in the notification.
         *
         * @return id of the large icon.
         */
        int getLargeIconId();

        /**
         * Returns the possible interactions to be visualized in the notification bar as
         * NotificaitonActionHolder.
         *
         * @return the actions to be added to the notification
         */
        NotificationActionHolder[] getActions();
    }

    /**
     * Holder class that holds the action specific details for notifications.
     */
    private static final class NotificationActionHolder {
        int mActionIcon;
        String mActionTitle;
        PendingIntent mPendingIntent;

        /**
         * Constructor.
         *
         * @param actionIcon    The icon of the action.
         * @param actionTitle   The title of the action.
         * @param intent        The pending intent of the action.
         */
        NotificationActionHolder(int actionIcon, String actionTitle, PendingIntent intent){
            this.mActionIcon = actionIcon;
            this.mActionTitle = actionTitle;
            this.mPendingIntent = intent;
        }
    }
}
