package qlikapps.flashlight;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class LockScreenWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        /*Intent receiver = new Intent(context, LockFlashlightWidgetReceiver.class);
        receiver.setAction("COM_FLASHLIGHT");
        receiver.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, receiver, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.lockscreen_widget_layout);
        views.setOnClickPendingIntent(R.id.imageView, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, views);*/

        Intent intent = new Intent(context, Main.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lockscreen_widget_layout);
        views.setOnClickPendingIntent(R.id.widgetView, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }
}

