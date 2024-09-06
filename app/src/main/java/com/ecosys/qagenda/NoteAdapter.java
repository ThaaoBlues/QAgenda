package com.ecosys.qagenda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onEditClick(Note note);
        void onDeleteClick(Note note);
    }

    private List<Note> notes;
    private OnNoteClickListener listener;

    public NoteAdapter(List<Note> notes, OnNoteClickListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNoteDate;
        public TextView tvNoteContent;
        public Button btnEdit;
        public Button btnDelete;

        public NoteViewHolder(View itemView) {
            super(itemView);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_card, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        final Note note = notes.get(position);

        // properly format timestamp to human readable date


        // Convert the timestamp string to a long value
        long timestamp = Long.parseLong(note.getTimestamp_creation());

        // Convert the timestamp to a LocalDateTime
        LocalDateTime dateTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

            // Define the desired date format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

            // Format the LocalDateTime to a readable date string
            String readableDate = dateTime.format(formatter);
            holder.tvNoteDate.setText(readableDate);
        }


        try {
            String ctt = note.readNote();
            // display only note first line
            holder.tvNoteContent.setText(ctt.split("\n")[0]+"\n...");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditClick(note);
            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
}
