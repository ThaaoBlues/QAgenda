package com.ecosys.qagenda;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Note {

    private static final String TAG = "Note";
    private static final String NOTES_DIR_PATH = "notes";
    private static final String ANNUAIRE_PATH = NOTES_DIR_PATH+"/"+"annuaire.txt";
    private String timestamp_creation;
    private String timestamp_link_event;
    private String title;
    private Context context;
    private Uri rootUri;

    public Note(Context context, Uri rootUri) {
        this.context = context;
        this.rootUri = rootUri;
    }


    public String getTimestamp_creation() {
        return timestamp_creation;
    }

    public void setTimestamp_creation(String timestamp_creation) {
        this.timestamp_creation = timestamp_creation;
    }

    public String getTimestamp_link_event() {
        return timestamp_link_event;
    }

    public void setTimestamp_link_event(String timestamp_link_event) {
        this.timestamp_link_event = timestamp_link_event;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setFromAnnuaireEntry(String entry) {
        String[] parts = entry.split(";");
        setTimestamp_creation(parts[0]);
        if (parts.length > 1) {
            setTitle(parts[1]);
            if (parts.length > 2) {
                setTimestamp_link_event(parts[2]);
            }
        }
    }

    public String readNote() throws IOException {
        Uri noteUri = Uri.withAppendedPath(rootUri, NOTES_DIR_PATH + getTimestamp_creation() + ".txt");
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(noteUri, "r");
        FileInputStream fis = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String(fis.readAllBytes(), StandardCharsets.UTF_8);
        }

        return "";
    }



    public void saveNoteToFile(byte[] content) throws IOException {
        String notePath = NOTES_DIR_PATH + getTimestamp_creation() + ".txt";
        Uri noteUri = Uri.withAppendedPath(rootUri, notePath);
        DocumentFile noteFile = DocumentFile.fromSingleUri(context, noteUri);


        // blocks function is not crated as it starts a new activity
        Utils.checkFileCreated(context,TAG,rootUri,notePath,content);

        // new annuaire entry
        StringBuilder stb = new StringBuilder();
        stb.append(this.timestamp_creation);
        stb.append(";");
        stb.append(this.title);
        stb.append(";");
        stb.append(this.timestamp_link_event);
        stb.append("\n");


        // blocks function is not crated as it starts a new activity
        Utils.checkFileCreated(context,TAG,rootUri,ANNUAIRE_PATH,stb.toString().getBytes(StandardCharsets.UTF_8));


        ParcelFileDescriptor annuaireParcel = context.getContentResolver().openFileDescriptor(
                Uri.withAppendedPath(rootUri,ANNUAIRE_PATH),
                "wa"
        );

        FileWriter fw = new FileWriter(
                    annuaireParcel.getFileDescriptor()
        );

        fw.write(stb.toString());
        fw.close();


        ParcelFileDescriptor parcel = context.getContentResolver().openFileDescriptor(noteFile.getUri(), "rw");
        FileOutputStream fos = new FileOutputStream(parcel.getFileDescriptor());
        fos.write(content);
        fos.close();
    }

    public boolean deleteNote() {
        Uri annuaireUri = Uri.withAppendedPath(rootUri, ANNUAIRE_PATH);
        DocumentFile annuaireFile = DocumentFile.fromSingleUri(context, annuaireUri);

        try {
            ParcelFileDescriptor annuaireParcel = context.getContentResolver().openFileDescriptor(annuaireFile.getUri(), "rw");
            BufferedReader bfr = new BufferedReader(new FileReader(annuaireParcel.getFileDescriptor()));
            StringBuilder stb = new StringBuilder();
            String line;
            while ((line = bfr.readLine()) != null) {
                if (!line.startsWith(getTimestamp_creation())) {
                    stb.append(line).append("\n");
                }
            }
            bfr.close();

            BufferedWriter bfw = new BufferedWriter(new FileWriter(annuaireParcel.getFileDescriptor()));
            bfw.write(stb.toString());
            bfw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Uri noteUri = Uri.withAppendedPath(rootUri, NOTES_DIR_PATH + getTimestamp_creation() + ".txt");
        DocumentFile noteFile = DocumentFile.fromSingleUri(context, noteUri);
        return noteFile != null && noteFile.delete();
    }
}
