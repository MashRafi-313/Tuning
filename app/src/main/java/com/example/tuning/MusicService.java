package com.example.tuning;

import static com.example.tuning.ApplicationClass.ACTION_NEXT;
import static com.example.tuning.ApplicationClass.ACTION_PLAY;
import static com.example.tuning.ApplicationClass.ACTION_PREVIOUS;
import static com.example.tuning.ApplicationClass.CHANNEL_ID_2;
import static com.example.tuning.PlayerActivity.listSongs;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
     int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"My Audio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class MyBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");
        if(myPosition != -1){
            playMedia(myPosition);
        }
        if(actionName!=null){
            switch (actionName){
                case "playPause":
                    if(actionPlaying!= null){
                        actionPlaying.playPauseBtnClicked();
                    }
                    break;
                case "next":
                    if(actionPlaying!= null){
                        actionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":
                    if(actionPlaying!= null){
                        actionPlaying.prevBtnClicked();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int StartPosition) {
        musicFiles = listSongs;
        position = StartPosition;
        if(mediaPlayer!= null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles != null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else{
            createMediaPlayer(position);
            mediaPlayer.start();
        }

    }

    void start(){
        mediaPlayer.start();
    }
    boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    void stop(){
        mediaPlayer.stop();
    }
    void  pause(){
        mediaPlayer.pause();
    }
    void release(){
        mediaPlayer.release();
    }
    int getDuration(){
        return mediaPlayer.getDuration();
    }
    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    void createMediaPlayer(int positionInner){
        position = positionInner;
        uri = Uri.parse( musicFiles.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(),uri);
    }
    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying!= null){
            actionPlaying.nextBtnClicked();
            if(mediaPlayer!=null){
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();
            }
        }
    }
    void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }
    @SuppressLint({"NotificationPermission", "NotificationId0"})
    void showNotification(int playPauseBtn){
        Intent intent = new Intent(this,PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,
                intent,0);

        Intent prevIntent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this,0,
                prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this,0,
                pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this,0,
                nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if(picture!=null){
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else{
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.bewedoc);
        }
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous,"Previous",prevPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .addAction(R.drawable.ic_baseline_skip_next,"Next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);
    }

    private  byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        try {
            retriever.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return art;
    }
}
