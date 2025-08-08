package jaru.ori.gui.gpslog.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import jaru.ori.logic.gpslog.AppDataForService;
import jaru.ori.logic.gpslog.TickerLoc;

public class LocalizacionService extends Service {
    private TickerLoc oTickerLoc;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Crear el canal de notificación si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "canal_localizacion",
                    getString(R.string.ORI_MI00026), // Nombre del canal
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        // Crear notificación obligatoria para el servicio en primer plano
        Notification notification = new NotificationCompat.Builder(this, "canal_localizacion")
                .setContentTitle(getString(R.string.ORI_MI00026))
                .setContentText(getString(R.string.ORI_MI00027))
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        startForeground(1, notification);

        // Iniciar el hilo de localización
        oTickerLoc = new TickerLoc();

        oTickerLoc.setOParametro(AppDataForService.oParametro);
        oTickerLoc.setOGpsInterno(AppDataForService.oGpsInterno);
        oTickerLoc.setORegistro(AppDataForService.oRegistroLoc);
        oTickerLoc.setNRetardo(AppDataForService.nRetardo);
        oTickerLoc.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (oTickerLoc != null) oTickerLoc.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
