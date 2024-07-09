package com.qsync.qagenda;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class QSyncCallbackActivity extends AppCompatActivity {

    private static final String TAG = "QAgenda";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qsync_callback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri != null) {
            // Take persistable URI permission
            final int takeFlags = intent.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, takeFlags);

            // Now you can work with the URI
            // For example, you can list files, open input/output streams, etc.
            Log.d(TAG,"Got persistent permissions for : "+uri.getPath());

        }else{
            Log.d(TAG,"An error occured while getting persistent permissions to QSync.");
        }


        intent = new Intent(QSyncCallbackActivity.this,MainActivity.class);
        startActivity(intent);

    }



}