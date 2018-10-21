package com.community.hanrynzale.mapcar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class onAppKilled extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        deleteCache(onAppKilled.this);
    }

    private static void deleteCache(Context context){
        try{
            File dir = context.getCacheDir();
            deleteDir(dir);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir){
        if(dir != null && dir.isDirectory()){
            String[] children = dir.list();
            for (int i=0; i< children.length; i++){
                boolean sucess = deleteDir(new File(dir, children[i]));
                if(!sucess){
                    return false;
                }
            }
            return dir.delete();
        }else if(dir!=null && dir.isFile()){
            return dir.delete();
        }else{
            return false;
        }
    }
}
