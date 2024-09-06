package com.ecosys.qagenda;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    public static void checkFileCreated(Context context, String TAG, Uri rootUri, String fileRelativePath, byte[] content) {

        Log.d(TAG,"Checking file creation : "+rootUri.toString()+"&rel="+fileRelativePath);
        DocumentFile file = DocumentFile.fromSingleUri(context,Uri.withAppendedPath(
                rootUri,
                fileRelativePath
        ));
        /*for(String part : fileRelativePath.split("/")){
            Log.d(TAG,part);
            file = file.findFile(part);
            if(file == null ){
                break;
            }
        }*/

        // if a content is provided, setup a temporary file for the callback class to write
        // right after being given the rights
        if(content != null){
            try {
                File tmp = new File(context.getFilesDir(),"note_content.tmp");
                tmp.createNewFile();
                FileOutputStream fos = new FileOutputStream(tmp);
                fos.write(content);
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //Log.d(TAG,"File existence :"+file.exists());
        if (file == null || !file.exists()) {
            Log.d(TAG,"Creating file : "+fileRelativePath);
            Intent intent = new Intent(Intent.ACTION_SYNC);
            intent.setClassName("com.ecosys.ecosys", "com.ecosys.ecosys.AppsIntentActivity");
            intent.putExtra("action_flag", "[CREATE_FILE]");
            intent.putExtra("package_name", context.getPackageName());
            intent.putExtra("file_path", fileRelativePath);
            intent.putExtra("mime_type", "*/*");
            SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(),MODE_PRIVATE);
            intent.putExtra("secure_id",prefs.getString("secure_id","[NO PREFS]"));
            Log.d(TAG, "starting activity with sync intent");
            context.startActivity(intent);
        }
    }
}
