package com.karkalt.soap2day.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.karkalt.soap2day.R;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromUrl extends AsyncTask<String, Long, String> {

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notification;
    int id;
    OutputStream output;
    WeakReference<Context> context;
    String path;
    String downloadName;
    boolean isCancelled;

    public DownloadFileFromUrl(Context context, String path, String downloadName, int id) {
        this.context = new WeakReference<>(context);
        this.path = path;
        this.downloadName = downloadName;
        this.id = id;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        notificationManager = (NotificationManager) context.get().getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent();
        intent.setAction("com.karkalt.soap2day.utils.DownloadFileFromUrl." + id);
        PendingIntent actionIntent = PendingIntent.getBroadcast(context.get(), 0, intent, 0);

        notification = new NotificationCompat.Builder(context.get(), id + "");
        notification.setContentTitle(downloadName)
                .setContentText("")
                .setAutoCancel(false)
                .setDefaults(0)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_download)
                .addAction(R.drawable.ic_close, "Cancel", actionIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id + "", "Video Downloader", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Video Downloader");
            channel.setSound(null, null);
            channel.enableLights(false);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);

        }
        notification.setProgress(100, 0, true);

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                assert action != null;
                if (action.equals("com.karkalt.soap2day.utils.DownloadFileFromUrl." + id)){
                    isCancelled = true;
                }

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.karkalt.soap2day.utils.DownloadFileFromUrl." + id);
        context.get().registerReceiver(receiver, filter);

    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d("LOL", "doInBackground: " + path);
        String basePath = FilenameUtils.getPath(path);
        if (!new File(basePath).exists()) {
            boolean basePathCreated = new File(basePath).mkdirs();
            if (basePathCreated) {
                int count;
                try {
                    URL url = new URL(strings[0]);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    long lengthOfFile = connection.getContentLength();
                    notification.setContentText("0MB / " + (int)(lengthOfFile / 1024f / 1024f) + "MB");
                    notificationManager.notify(id, notification.build());
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);
                    output = new FileOutputStream(path);
                    byte[] data = new byte[1024];
                    long total = 0;
                    long lastProgress = 0;
                    int i = 0;
                    while ((count = input.read(data)) != -1) {
                        i++;
                        if (isCancelled) {
                            break;
                        }
                        total += count;
                        long progress = (int) ((total * 100) / lengthOfFile);
                        if (progress > 100) {
                            progress = 100;
                        }
                        if (progress != lastProgress) {
                            publishProgress(progress, total, lengthOfFile);
                        }
                        if (i >= 1024) {
                            i = 0;
                            publishProgress(progress, total, lengthOfFile);
                        }
                        output.write(data, 0, count);
                        lastProgress = progress;
                    }
                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }

    protected void onProgressUpdate(Long... progress) {
        notification.setProgress(100, progress[0].intValue(), false);
        notification.setContentText((int)(progress[1] / 1024f / 1024f) + "MB / " + (int)(progress[2] / 1024f / 1024f) + "MB");
        notificationManager.notify(id, notification.build());
        super.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(String file_url) {
        if (!isCancelled) {
            notification = new NotificationCompat.Builder(context.get(), id + "");
            notification.setContentTitle(downloadName)
                    .setContentText("Download complete")
                    .setAutoCancel(false)
                    .setDefaults(0)
                    .setOngoing(false)
                    .setSmallIcon(R.drawable.ic_download_complete);
            notificationManager.notify(id, notification.build());
        } else {
            new File(path).delete();
            notificationManager.cancel(id);
        }
    }
}