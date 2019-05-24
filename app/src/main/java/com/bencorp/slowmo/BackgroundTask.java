package com.bencorp.slowmo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

/**
 * Created by hp-pc on 4/28/2019.
 */

public class BackgroundTask extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        if(ChooseActivity.requestVideo){

        }else{

        }
        return START_STICKY;
    }
}
