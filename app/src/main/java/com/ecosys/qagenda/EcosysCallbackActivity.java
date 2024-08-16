package com.ecosys.qagenda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.Preferences;

public class EcosysCallbackActivity extends AppCompatActivity {

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

        switch (intent.getStringExtra("action_flag")){
            case "[INSTALL_APP]":

                String secureId = intent.getStringExtra("secure_id");
                Log.d(TAG,"Recovering secure_id : "+secureId);
                SharedPreferences prefs = this.getSharedPreferences(this.getPackageName(),MODE_PRIVATE);
                prefs.edit().putString("secure_id",secureId).apply();
                intent = new Intent(EcosysCallbackActivity.this,MainActivity.class);
                intent.putExtra("fragment_to_load","agenda");
                startActivity(intent);


                break;

            default:
                Uri uri = intent.getData();

                if (uri != null) {
                    // Take persistable URI permission
                    final int takeFlags = intent.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);


                    // used when we call file creation for a new note
                    // as user already typed the content he wants
                    File tmp = new File(getFilesDir(),"note_content.tmp");

                    if(tmp.exists()){
                        try {

                            BufferedReader bfr = new BufferedReader(new FileReader(tmp));
                            FileWriter fr = new FileWriter(
                                    getContentResolver().openFileDescriptor(uri,"w").getFileDescriptor()
                            );
                            while (bfr.ready()){
                                fr.write(bfr.readLine());
                            }

                            bfr.close();
                            fr.close();

                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        // don't forget to remove tempoprary file !
                        tmp.delete();
                    }

                    // Now you can work with the URI
                    // For example, you can list files, open input/output streams, etc.
                    Log.d(TAG,"Got persistent permissions for : "+uri.getPath());

                }else{
                    Log.d(TAG,"An error occured while getting persistent permissions to QSync.");
                }

                String rp = uri.getPath().replace("content://com.ecosys.ecosys.fileprovider/apps/com.ecosys.qagenda/","");

                intent = new Intent(EcosysCallbackActivity.this,MainActivity.class);

                if(rp.startsWith("notes")){
                    intent.putExtra("fragment_to_load","notes");

                }else{
                    intent.putExtra("fragment_to_load","agenda");
                }

                startActivity(intent);

        }

        }





}