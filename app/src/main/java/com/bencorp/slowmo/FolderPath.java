package com.bencorp.slowmo;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by hp-pc on 4/27/2019.
 */

public  class FolderPath {
    public static final String folder_name = "SlowMo";
    public static final String sub_folder_name = "workArea";
    public static final String sub_folder_name2 = "Productions";
    private FolderPath(){}
    public static File pathToWorkZone(){
        File filepath = Environment.getExternalStoragePublicDirectory(folder_name);
        File files = new File(filepath,sub_folder_name);
        return files;
    }
    public static File filePath(String fileName){
        File filepath = Environment.getExternalStoragePublicDirectory(folder_name);
        File files = new File(filepath,sub_folder_name+"/"+fileName);
        return files;
    }

    public static File filePathResult(String fileName){
        File filepath = Environment.getExternalStoragePublicDirectory(folder_name);
        File files = new File(filepath,sub_folder_name2+"/"+fileName);
        return files;
    }
    public static boolean makeDir(){

        String state = Environment.getExternalStorageState();
        File filepath = Environment.getExternalStoragePublicDirectory(folder_name);
        File files = new File(filepath,sub_folder_name);

        if(!files.exists() && Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            return files.mkdirs();

        }
        File files2 = new File(filepath,sub_folder_name2);
        if(!files2.exists() && Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){

            files2.mkdirs();
            File hide = filePath(".nomedia");
            try {
                FileWriter fileWriter = new FileWriter(hide);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }
    public static String getFileName(String extension){
        String date = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(new Date());
        String rand = UUID.randomUUID().toString().substring(0,8);
        return "VID-"+date+"-"+rand+"."+extension;
    }
    public static String getFileNameSlow(String extension){
        String date = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(new Date());
        String rand = UUID.randomUUID().toString().substring(0,8);
        return "SLOW-"+date+"-"+rand+"."+extension;
    }
    public static String getFileNameAudio(String extension){
        String date = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(new Date());
        String rand = UUID.randomUUID().toString().substring(0,8);
        return "AUD-"+date+"-"+rand+"."+extension;
    }
}