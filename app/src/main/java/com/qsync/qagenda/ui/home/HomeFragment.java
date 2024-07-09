package com.qsync.qagenda.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.qsync.qagenda.R;
import com.qsync.qagenda.databinding.FragmentHomeBinding;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private Uri rootUri;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY-HH:mm");
    private LinearLayout eventsContainer;
    private SharedPreferences prefs;
    private Spinner timeDeltaSpinner;
    private String eventSummary;
    private int selectedHour;
    private int selectedMinute;
    private int selectedMonth;
    private int selectedDay;
    private int selectedYear;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        eventsContainer = binding.eventsLinearlayout;
        timeDeltaSpinner = binding.timeDeltaSpinner;

        rootUri = Uri.parse("content://com.qsync.qsync.fileprovider/apps/" + HomeFragment.this.getContext().getPackageName());

        Button add_event_button = binding.buttonAddEvent;
        add_event_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog();
                    }
                }
        );

        prefs = getContext().getSharedPreferences(getContext().getPackageName(), MODE_PRIVATE);
        //prefs.edit().putBoolean("firstrun", true).apply();
        Log.d(TAG, "PREFS :" + prefs.getBoolean("firstrun", true));
        if (prefs.getBoolean("firstrun", true)) {

            try{
                Intent intent = new Intent(Intent.ACTION_SYNC);
                intent.setClassName("com.qsync.qsync", "com.qsync.qsync.AppsIntentActivity");
                intent.putExtra("action_flag", "[INSTALL_APP]");
                intent.putExtra("package_name", getContext().getPackageName());
                Log.d(TAG, "starting activity with sync intent");
                startActivity(intent);
                prefs.edit().putBoolean("firstrun", false).apply();
            }catch (Exception e){
                Log.d(TAG,"An error occured while trying to tell qsync to create app task : ",e);
                // a the end, an error here would mainly mean that qsync is not installed.
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeFragment.this.getContext());
                builder.setTitle("QSync is missing");
                builder.setMessage("Please make sure QSync is installed on your device. You can install it from the play store.");
                // Set up the buttons
                builder.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                });
                Log.d("BACKEND API","LA FENETRE DE DIALOGUE VA ETRE AFFICHEE");
                builder.show();
            }

        } else {
            try {
                Log.d(TAG, "Trying to create file");
                checkFileCreated("agenda.ics");
                Log.d(TAG, "File created");
                displayEvents();
                Log.d(TAG, "File created and Event displayed !!!");

            } catch (java.lang.SecurityException | java.lang.IllegalArgumentException e) {
                Log.d(TAG, "Error while trying to access qsync content provider", e);
            }
        }

        // Set up the time delta spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_delta_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeDeltaSpinner.setAdapter(adapter);
        timeDeltaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                displayEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showEventSummaryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Event Summary");
        builder.setMessage("What's going on this day ?");

        // Set up the input
        EditText input;
        input = new EditText(getContext());
        builder.setView(input);

        String result;
        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            eventSummary = input.getText().toString();
            dialog.dismiss();

            // as this dialog was the last, write everything
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay,selectedHour,selectedMinute);
            writeDateToIcsFile(selectedCalendar.getTimeInMillis());

            // as all the others dialogs are show in cascade
            // the writing is done by the time we are here
            // we can now refresh the event
            displayEvents();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            eventSummary  = null;
            dialog.cancel();
        });
        builder.show();


    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int syear = calendar.get(Calendar.YEAR);
        int smonth = calendar.get(Calendar.MONTH);
        int sday = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, day) -> {


                    selectedDay = day;
                    selectedMonth = month;
                    selectedYear = year;

                    showTimePickerDialog();

                },
                syear, smonth, sday);

        datePickerDialog.show();
    }

    private void showTimePickerDialog(){
        // Get the current time as the default values for the picker
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and display it
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        selectedHour = hourOfDay;
                        selectedMinute = minute;

                        // now that we have all time related data, ask for event summary
                        showEventSummaryDialog();

                    }
                }, hour, minute, true); // true to display 24-hour format

        timePickerDialog.show();
    }

    private void writeDateToIcsFile(long timeInMillis) {
        Date date = new Date(timeInMillis);
        String formattedDate = dateFormat.format(date);

        String icsContent = "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "PRODID:-//qsync//qagenda//EN\n" +
                "BEGIN:VEVENT\n" +
                "UID:uid1@example.com\n" +
                "DTSTAMP:" + formattedDate + "\n" +
                "DTSTART:" + formattedDate + "\n" +
                "DTEND:" + formattedDate + "\n" +
                "SUMMARY:"+eventSummary+"\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ParcelFileDescriptor parcel = getContext().getContentResolver().openFile(
                        Uri.withAppendedPath(rootUri, "agenda.ics"),
                        "wa",
                        null
                );
                FileOutputStream os = new FileOutputStream(parcel.getFileDescriptor());
                os.write(icsContent.getBytes(StandardCharsets.UTF_8));
                os.close();
                parcel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkFileCreated(String fileName) {
        DocumentFile file = DocumentFile.fromSingleUri(
                getContext(),
                Uri.withAppendedPath(rootUri, fileName)
        );

        if (!file.exists()) {
            Intent intent = new Intent(Intent.ACTION_SYNC);
            intent.setClassName("com.qsync.qsync", "com.qsync.qsync.AppsIntentActivity");
            intent.putExtra("action_flag", "[CREATE_FILE]");
            intent.putExtra("package_name", getContext().getPackageName());
            intent.putExtra("file_name", fileName);
            intent.putExtra("mime_type", "*/*");
            Log.d(TAG, "starting activity with sync intent");
            startActivity(intent);
        }
    }

    private List<Event> readIcsFile() {
        List<Event> events = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                InputStream is = getContext().getContentResolver().openInputStream(Uri.withAppendedPath(rootUri, "agenda.ics"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                Event currentEvent = null;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("BEGIN:VEVENT")) {
                        currentEvent = new Event();
                    } else if (line.startsWith("DTSTART:")) {
                        if (currentEvent != null) {
                            String dateStr = line.substring("DTSTART:".length());
                            currentEvent.setStartDate(dateFormat.parse(dateStr));
                        }
                    } else if (line.startsWith("SUMMARY:")) {
                        if (currentEvent != null) {
                            currentEvent.setSummary(line.substring("SUMMARY:".length()));
                        }
                    } else if (line.startsWith("END:VEVENT")) {
                        if (currentEvent != null) {
                            events.add(currentEvent);
                            currentEvent = null;
                        }
                    }
                }

            } catch (ParseException e ) {
                throw new RuntimeException(e);
            }catch (FileNotFoundException e){
                checkFileCreated("agenda.ics");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return events;
    }

    private void displayEvents() {
        eventsContainer.removeAllViews();
        List<Event> events = readIcsFile();
        Collections.sort(events);

        String selectedDelta = timeDeltaSpinner.getSelectedItem() == null ? "This week" : timeDeltaSpinner.getSelectedItem().toString();
        Calendar calendar = Calendar.getInstance();

        if (selectedDelta.equals("This Week")) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        } else if (selectedDelta.equals("This Month")) {
            calendar.add(Calendar.MONTH, 1);
        } else if (selectedDelta.equals("This Year")) {
            calendar.add(Calendar.YEAR, 1);
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Date filterDate = calendar.getTime();

        for (Event event : events) {
            if (event.getStartDate().before(filterDate)) {
                CardView cardView = new CardView(getContext());
                cardView.setCardElevation(4);
                cardView.setRadius(8);

                LinearLayout cardLayout = new LinearLayout(getContext());
                cardLayout.setOrientation(LinearLayout.VERTICAL);
                cardLayout.setPadding(16, 16, 16, 16);

                TextView eventDateView = new TextView(getContext());
                eventDateView.setText(dateFormat.format(event.getStartDate()));
                eventDateView.setTextSize(16);

                TextView eventSummaryView = new TextView(getContext());
                eventSummaryView.setText(event.getSummary());
                eventSummaryView.setTextSize(20);
                eventSummaryView.setTypeface(null, Typeface.BOLD);

                cardLayout.addView(eventDateView);
                cardLayout.addView(eventSummaryView);
                cardView.addView(cardLayout);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(16, 16, 16, 16);
                cardView.setLayoutParams(params);

                eventsContainer.addView(cardView);
            }
        }
    }

    class Event implements Comparable<Event> {
        private Date startDate;
        private String summary;

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        @Override
        public int compareTo(Event other) {
            return this.startDate.compareTo(other.startDate);
        }
    }
}
