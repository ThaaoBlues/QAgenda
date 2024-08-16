package com.ecosys.qagenda.ui.notes;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.ecosys.qagenda.Note;
import com.ecosys.qagenda.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {




    private EditText etNoteContent;
    private Button btnSaveNote;
    private String noteDate;
    private Uri rootUri;

    private static final String TAG = "EditNotesActivity";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final String NOTES_DIR_PATH = "notes";
    private Note note = new Note(EditNoteActivity.this,rootUri);
    ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        etNoteContent = findViewById(R.id.etNoteContent);
        btnSaveNote = findViewById(R.id.btnSaveNote);

        Intent intent = getIntent();

        String flag = intent.getStringExtra("flag");

        rootUri = Uri.parse("content://com.ecosys.ecosys.fileprovider/apps/" + getPackageName());

        switch (flag){
            case "[CREATE_NOTE]":
                note.setTimestamp_creation(String.valueOf(
                        System.currentTimeMillis()
                ));
                break;


            case"[EDIT_NOTE]":
                note.setTimestamp_creation(intent.getStringExtra("note_date"));
                try {
                    etNoteContent.setText(note.readNote());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
        }

        btnSaveNote.setOnClickListener(v -> {
            note.setTitle("Note");
            try {
                note.saveNoteToFile(etNoteContent.getText().toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finish();
        });
    }
}