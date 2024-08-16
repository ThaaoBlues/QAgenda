package com.ecosys.qagenda.ui.notes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecosys.qagenda.Note;
import com.ecosys.qagenda.NoteAdapter;
import com.ecosys.qagenda.R;
import com.ecosys.qagenda.Utils;
import com.ecosys.qagenda.databinding.FragmentNotesBinding;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    private FragmentNotesBinding binding;
    private RecyclerView recyclerView;
    private Button btnAddNote;
    private List<Note> notes;
    private NoteAdapter adapter;
    private Uri rootUri;
    private String NOTES_ANNUAIRE_PATH = "notes/annuaire.txt";

    private static final String TAG = "NotesFragment";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotesViewModel dashboardViewModel =
                new ViewModelProvider(this).get(NotesViewModel.class);

        binding = FragmentNotesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        rootUri = Uri.parse("content://com.ecosys.ecosys.fileprovider/apps/" + NotesFragment.this.getContext().getPackageName());


        recyclerView =binding.recyclerView;
        btnAddNote = binding.btnAddNote;

        notes = loadNotes();

        adapter = new NoteAdapter(notes, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditNoteActivity.class);
            intent.putExtra("flag","[CREATE_NOTE]");
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onEditClick(Note note) {
        Intent intent = new Intent(getContext(), EditNoteActivity.class);
        intent.putExtra("flag","[EDIT_NOTE]");
        intent.putExtra("note_date", note.getTimestamp_creation());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Note note) {
        note.deleteNote();
        notes.remove(note);
        adapter.notifyDataSetChanged();
    }

    private List<Note> loadNotes() {
        Uri annuaireUri = Uri.withAppendedPath(rootUri, NOTES_ANNUAIRE_PATH);
        DocumentFile annuaireFile = DocumentFile.fromSingleUri(getContext(), annuaireUri);

        Utils.checkFileCreated(getContext(),TAG,rootUri,NOTES_ANNUAIRE_PATH,null);

        ArrayList<Note> annuaire = new ArrayList<>();
        try {
            ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(annuaireFile.getUri(), "r");
            BufferedReader bfr = new BufferedReader(new FileReader(pfd.getFileDescriptor()));
            String entry;
            while ((entry = bfr.readLine()) != null) {
                Note note = new Note(getContext(), rootUri);
                note.setFromAnnuaireEntry(entry);
                annuaire.add(note);
            }
            bfr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return annuaire;
    }


}