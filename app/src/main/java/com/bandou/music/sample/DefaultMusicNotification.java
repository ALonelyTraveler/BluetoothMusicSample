package com.bandou.music.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import com.bandou.library.util.AppUtils;
import com.bandou.music.model.AudioInfo;

/**
 * @ClassName: DefaultMusicNotification
 * @Description: say something
 * @author: chenwei
 * @version: V1.0
 * @Date: 16/7/27 下午2:20
 */
public class DefaultMusicNotification {
    private int notificationId;
    private int iconRes;
    private Context mContext;

    public DefaultMusicNotification(Context context,int iconRes) {
        notificationId = (int) (System.currentTimeMillis() / 1000);
        this.mContext = context;
        this.iconRes = iconRes;
    }

    public void showNotification(AudioInfo info) {
        if (info == null) {
            return;
        }
        PendingIntent pendingIntent = getPendingIntent();
        // 通过Notification.Builder来创建通知，注意API Level
        // API11之后才支持
        Notification notify2 = new Notification.Builder(mContext)
                .setSmallIcon(iconRes) // 设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap
                // icon)
                .setTicker(info.getName())// 设置在status
                // bar上显示的提示文字
                .setContentTitle(AppUtils.getAppName(mContext, mContext.getPackageName()))// 设置在下拉status
                // bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
                .setContentText(info.getName())// TextView中显示的详细内容
                .setContentIntent(pendingIntent) // 关联PendingIntent
                .getNotification(); // 需要注意build()是在API level
        // 16及之后增加的，在API11中可以使用getNotificatin()来代替
        notify2.flags = Notification.FLAG_ONGOING_EVENT;
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId, notify2);
    }

    public void hideNotification() {
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notificationId);
    }

    public void progressNotification(int progress) {

    }

    public PendingIntent getPendingIntent() {
        return null;
    }
}
