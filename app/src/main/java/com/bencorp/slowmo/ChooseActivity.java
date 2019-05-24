package com.bencorp.slowmo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import android.Manifest;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class ChooseActivity extends AppCompatActivity {

    private TextView mTextMessage;
    ArrayList<String> permissions;
    final static int REQUEST_ALL_PERMISSIONS = 777;
    final static int REQUEST_VIDEO = 999;
    final static int REQUEST_SONG = 888;
    TextView video_button;
    TextView song_button;
    static Boolean requestVideo = false;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    RelativeLayout loading;
    String videofilename;
    String slowVideofilename;

    String audiofilename;
    String displayName;
    FFmpeg fFmpeg;
    Uri videoUri;
    Uri audioUri;
    File workableFile;
    File musicWorkableFile;
    File musicWorkableFileLoop;
    File slowVideo;
    TextView loadingMsg;
    Long videoLength;
    String audioExtension;
    File producedVideo;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    return true;
                case R.id.navigation_slowmo:

                    slowMotionVideo();


                    return true;
                case R.id.navigation_notifications:

                    return true;
            }
            return false;
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        //mTextMessage = (TextView) findViewById(R.id.message);
        try {
            setUpVideoStudio();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        grantAllPermissions();

    }
    private void grantAllPermissions(){
        permissions = new ArrayList<>();
        if(ActivityCompat.checkSelfPermission(ChooseActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(ActivityCompat.checkSelfPermission(ChooseActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissions.size() > 0){
            ActivityCompat.requestPermissions(ChooseActivity.this,permissions.toArray(new String[permissions.size()]),REQUEST_ALL_PERMISSIONS);
        }else{
            FolderPath.makeDir();
            initControllers();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_ALL_PERMISSIONS:
                for (int i = 0; i < grantResults.length; i++){
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                        Toast.makeText(this,"All permissions must be accepted before using the app",Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                break;

        }
        FolderPath.makeDir();
        initControllers();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void initControllers(){
        video_button = (TextView) findViewById(R.id.video_button);
        song_button = (TextView) findViewById(R.id.song_button);
        video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadVideoToWorkArea();
            }
        });
        song_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSongToWorkArea();
            }
        });
        loading = (RelativeLayout) findViewById(R.id.progressBar);
        loadingMsg = (TextView) findViewById(R.id.loading_msg);
        //videofilename = FolderPath.getFileName();
    }
    private void loadVideoToWorkArea(){
        startBackgroundThread();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent,REQUEST_VIDEO);

    }
    private void loadSongToWorkArea(){
        startBackgroundThread();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("audio/*");
        startActivityForResult(intent,REQUEST_SONG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(data != null && resultCode == RESULT_OK){
            loadingMsg.setText("Loading media, please wait");
            makeLoadingDialogVisible();
            switch (requestCode){
                case REQUEST_VIDEO:
                    videoUri = data.getData();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getOriginalName(data.getData(),"video");
                        }
                    },2000);


                    break;
                case REQUEST_SONG:
                    audioUri = data.getData();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getOriginalName(data.getData(),"audio");
                        }
                    },2000);
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("ScordIt Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBackgroundThread.quitSafely();
        }
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void makeLoadingDialogVisible(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        loading.setVisibility(View.VISIBLE);
    }
    private void getOriginalName(Uri uri, String media){
        String uriString = uri.toString();
        File myFIle = new File(uriString);
        String path = myFIle.getAbsolutePath();

        if(uriString.startsWith("content://")){
            Cursor cursor = null;
            cursor = getApplicationContext().getContentResolver().query(uri,null,null,null,null);
            if(cursor !=  null && cursor.moveToFirst()){
                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
                Toast.makeText(getApplicationContext(),"name1: "+displayName,Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(),"Something went terribly wrong, please try again",Toast.LENGTH_LONG).show();
            }
        }else if(uriString.startsWith("file://")){
            displayName = myFIle.getName();
            Toast.makeText(getApplicationContext(),"name2: "+displayName,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),"Something went terribly wrong, please try again",Toast.LENGTH_LONG).show();
        }
        String[] nameExplode = displayName.split("\\.");
        if(media.equals("video")){
            videofilename = FolderPath.getFileName(nameExplode[nameExplode.length - 1]);
            slowVideofilename = FolderPath.getFileNameSlow(nameExplode[nameExplode.length - 1]);
            parseVideo(uri);
        }else{
            audioExtension = nameExplode[nameExplode.length - 1];
            audiofilename = FolderPath.getFileNameAudio(nameExplode[nameExplode.length - 1]);
            saveVideo(uri,"audio");
        }

    }
    private void makeLoadingDialogInvisible(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        loading.setVisibility(View.INVISIBLE);
    }
    private void parseVideo(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getApplicationContext(),uri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Long timeInMillisec = Long.parseLong(time);
        Long duration = timeInMillisec/1000;
        Long hours = duration/3600;
        Long minutes = (duration - hours * 3600)/60;
        Long seconds = duration - (hours * 3600 + minutes * 60);
        if (seconds > 17 || minutes > 0){
            //Toast.makeText(this,"seconds: "+String.valueOf(seconds)+" minutes: "+String.valueOf(minutes)+" Too big",Toast.LENGTH_LONG).show();
            trimVideo(0,17000,uri);
        }else{
            saveVideo(uri,"video");
        }
        videoLength = seconds;

    }
    private void saveVideo(Uri uri, String media){
       try {
            AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(uri,"r");
            FileInputStream inputStream = videoAsset.createInputStream();
            OutputStream outputStream = new FileOutputStream(FolderPath.filePath((media.equals("video"))? videofilename:audiofilename));

            byte[] buffer = new byte[1024];
            int len;
            while((len = inputStream.read(buffer)) > 0){
                outputStream.write(buffer,0,len);
            }
            inputStream.close();
            outputStream.close();

           if(media.equals("video")){
               workableFile = FolderPath.filePath(videofilename);
               Glide.with(this)
                       .load(workableFile)
                       .centerCrop()
                       .into((ImageView) findViewById(R.id.video_card));
           }else{
               musicWorkableFile = FolderPath.filePath(audiofilename);
               Glide.with(this)
                       .load(R.drawable.ic_music)
                       .centerCrop()
                       .into((ImageView) findViewById(R.id.song_card));
           }
            stopBackgroundThread();
            makeLoadingDialogInvisible();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setUpVideoStudio() throws FFmpegNotSupportedException {
        if(fFmpeg == null){
            fFmpeg = FFmpeg.getInstance(this);

            fFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Toast.makeText(getApplicationContext(),"Failure to load studio",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(),"Success in loading studio",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        }
    }
    private void trimVideo(int start,int finish,Uri uri){
        File trimDestination = FolderPath.filePath(videofilename);
        workableFile = trimDestination;
        String originalPath = getFileOriginalPath(getApplicationContext(),uri);
        int duration = (finish - start)/1000;
        String[] commands = new String[]{"-ss",""+start/1000,"-y","-i",originalPath,"-t",""+(finish - start)/1000,"-vcodec","mpeg4","-b:v","2097152","-b:a","48000","-ac","2","-ar","22050",trimDestination.getAbsolutePath()};
        try {
            executeVideoStudioCode(commands,"Video has been trimmed to 17 seconds");
        } catch (FFmpegCommandAlreadyRunningException e) {
            Toast.makeText(getApplicationContext(),"failed to call execute method, why: "+e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private String getFileOriginalPath(Context context, Uri uri) {
        Cursor cursor = null;
        try {


            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        }catch (Exception e){

            Toast.makeText(context,"failed to get real path, why: "+e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return "";
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
    }

    private void executeVideoStudioCode(final String[] commands, final String successMessage) throws FFmpegCommandAlreadyRunningException {

        fFmpeg.execute(commands,new ExecuteBinaryResponseHandler(){
            @Override
            public void onFailure(String message) {
                Toast.makeText(getApplicationContext(),"failed to alter video, why: "+message,Toast.LENGTH_LONG).show();
                if(successMessage.equals("slower")){
                    Toast.makeText(getApplicationContext(),"slowmo Failed",Toast.LENGTH_LONG).show();
                }
                stopBackgroundThread();
                makeLoadingDialogInvisible();
                super.onFailure(message);
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onProgress(String message) {
                super.onProgress(message);
            }

            @Override
            public void onSuccess(String message) {

                if(successMessage.equals("Video has been trimmed to 17 seconds")){
                    Glide.with(getApplicationContext())
                            .load(workableFile)
                            .centerCrop()
                            .into((ImageView) findViewById(R.id.video_card));
                }
                if(successMessage.equals("slower")){
                    actualSlowMotion();
                    return;
                }else if(successMessage.equals("looper")){
                    actualSlowMotionAfterLoop();
                    return;
                }
                if(successMessage.equals("Video produced successfully")){
                    deleteConstructionMaterials();
                    nextStepDialog();
                }

                stopBackgroundThread();
                makeLoadingDialogInvisible();
                Toast.makeText(getApplicationContext(),successMessage,Toast.LENGTH_LONG).show();
                super.onSuccess(message);
            }
        });
    }

    private void slowMotionVideo(){
        if(videofilename == null || audiofilename == null){
            Toast.makeText(this,"Select video and music",Toast.LENGTH_LONG).show();
            return;
        }
        startBackgroundThread();
        loadingMsg.setText("Producing video please wait: reducing video speed");
        makeLoadingDialogVisible();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                slowVideo = FolderPath.filePath(slowVideofilename);
                String[] commands = new String[]{"-y","-i",workableFile.getAbsolutePath(),"-filter_complex","[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a]","-map","[v]","-map","[a]","-b:v","2097k","-r","60","-vcodec","mpeg4",slowVideo.getAbsolutePath()};
                if(videoLength <= 10){
                    commands = new String[]{"-y","-i",workableFile.getAbsolutePath(),"-filter_complex","[0:v]setpts=4*PTS[v];[0:a]atempo=0.5[a]","-map","[v]","-map","[a]","-b:v","2097k","-r","60","-vcodec","mpeg4",slowVideo.getAbsolutePath()};
                }

                //String[] commands = new String[]{"-i",workableFile.getAbsolutePath(),"-i",musicWorkableFile.getAbsolutePath(),"-c:v","copy","-c:a","aac","-map","0:v:0","-map","1:a:0","-shortest",producedVideo.getAbsolutePath()};

                try {
                    executeVideoStudioCode(commands,"slower");
                } catch (FFmpegCommandAlreadyRunningException e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        },2000);

    }
    private void actualSlowMotion(){
        if(getLengthOfFile(musicWorkableFile)[0] == 0 && getLengthOfFile(slowVideo)[1] > getLengthOfFile(musicWorkableFile)[1]){
            loopShortAdudio();
            return;
        }
        loadingMsg.setText("Producing video please wait: adding audio");
        producedVideo = FolderPath.filePathResult(videofilename);
        String[] commands = new String[]{"-i",slowVideo.getAbsolutePath(),"-i",musicWorkableFile.getAbsolutePath(),"-c:v","copy","-c:a","aac","-map","0:v:0","-map","1:a:0","-shortest",producedVideo.getAbsolutePath()};

        try {
            executeVideoStudioCode(commands,"Video produced successfully");
        } catch (FFmpegCommandAlreadyRunningException e) {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    private void actualSlowMotionAfterLoop(){
        loadingMsg.setText("Producing video please wait: adding audio");
        producedVideo = FolderPath.filePathResult(videofilename);
        String[] commands = new String[]{"-i",slowVideo.getAbsolutePath(),"-i",musicWorkableFileLoop.getAbsolutePath(),"-c:v","copy","-c:a","aac","-map","0:v:0","-map","1:a:0","-shortest",producedVideo.getAbsolutePath()};

        try {
            executeVideoStudioCode(commands,"Video produced successfully");
        } catch (FFmpegCommandAlreadyRunningException e) {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void loopShortAdudio(){
        loadingMsg.setText("Producing video please wait: looping short audio");
        musicWorkableFileLoop = FolderPath.filePath(FolderPath.getFileName(audioExtension));
        String[] commands = new String[]{"-i",musicWorkableFile.getAbsolutePath(),"-i",musicWorkableFile.getAbsolutePath(),"-filter_complex","[0:0][1:0]concat=n=2:v=0:a=1[out]","-map","[out]",musicWorkableFileLoop.getAbsolutePath()};
        try {
            executeVideoStudioCode(commands,"looper");
        } catch (FFmpegCommandAlreadyRunningException e) {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    private Long[] getLengthOfFile(File theFile){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getApplicationContext(),Uri.fromFile(theFile));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Long timeInMillisec = Long.parseLong(time);
        Long duration = timeInMillisec/1000;
        Long hours = duration/3600;
        Long minutes = (duration - hours * 3600)/60;
        Long seconds = duration - (hours * 3600 + minutes * 60);
        Long[] returnValues = new Long[]{minutes,seconds};
        return returnValues;
        //return new Long seconds;
    }

    @Override
    public void onBackPressed() {
        if(fFmpeg != null){
            if(fFmpeg.isFFmpegCommandRunning()){
                cancelDialog();
            }else {
                Toast.makeText(getApplicationContext(),"no command running",Toast.LENGTH_LONG).show();
                super.onBackPressed();
            }
        }else{
            Toast.makeText(getApplicationContext(),"studio is null",Toast.LENGTH_LONG).show();
            super.onBackPressed();
        }
    }
    private void cancelDialog(){
        new AlertDialog.Builder(ChooseActivity.this)
                .setCancelable(true)
                .setTitle("Process is running")
                .setMessage("Are you sure you want to cancel running process?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fFmpeg.killRunningProcesses();
                        stopBackgroundThread();
                        makeLoadingDialogInvisible();
                        Toast.makeText(getApplicationContext(),"Process cancelled",Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
    private void nextStepDialog(){
        new AlertDialog.Builder(ChooseActivity.this)
                .setCancelable(true)
                .setTitle("Production Successful")
                .setMessage("Are you sure you want to cancel running process?")
                .setPositiveButton("Share video", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri videoUri = FileProvider.getUriForFile(getApplicationContext(),getPackageName()+".provider",producedVideo);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("video/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM,videoUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if(shareIntent.resolveActivity(getPackageManager()) != null){
                            startActivity(shareIntent);
                        }else {
                            Toast.makeText(getApplicationContext(),"No applictaion found for that",Toast.LENGTH_LONG).show();

                        }
                    }
                })
                .setNegativeButton("Open with", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri videoUri = FileProvider.getUriForFile(getApplicationContext(),getPackageName()+".provider",producedVideo);
                        final Intent watchIntent = new Intent(Intent.ACTION_VIEW);
                        watchIntent.setDataAndType(videoUri, "video/*");
                        watchIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if(watchIntent.resolveActivity(getPackageManager()) != null){
                            startActivity(watchIntent);
                        }else{
                            Toast.makeText(getApplicationContext(),"No applictaion found for that",Toast.LENGTH_LONG).show();
                        }
                    }
                }).show();
    }
    private void deleteConstructionMaterials(){
        File container = FolderPath.pathToWorkZone();
        File[] files = container.listFiles();
        for(int i = 0; i < files.length; i++){
            File eachFile = files[i];
            if(!eachFile.getName().equals(".nomedia")){
                eachFile.delete();
            }

        }
    }
}
