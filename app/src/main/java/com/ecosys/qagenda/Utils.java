package com.ecosys.qagenda;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    public static void checkFileCreated(Context context, String TAG, Uri rootUri, String fileRelativePath, byte[] content) {
        DocumentFile file = DocumentFile.fromSingleUri(context, Uri.withAppendedPath(rootUri, fileRelativePath));

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
        if (!file.exists()) {
            Intent intent = new Intent(Intent.ACTION_SYNC);
            intent.setClassName("com.ecosys.ecosys", "com.ecosys.ecosys.AppsIntentActivity");
            intent.putExtra("action_flag", "[CREATE_FILE]");
            intent.putExtra("package_name", context.getPackageName());
            intent.putExtra("file_path", fileRelativePath);
            intent.putExtra("mime_type", "*/*");
            Log.d(TAG, "starting activity with sync intent");
            context.startActivity(intent);
        }
    }
}
