package com.system.ramoptimizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private NumberPicker hourPicker, minutePicker, amPmPicker;
    private ListView audioListView;
    private int selectedAudioIndex = 0;

    private final String[] audioNames = {
            "Nuclear Alert",
            "Freddy Music",
            "Freddy Scream",
            "Freddy Countdown",
            "Hardstyle Lala",
            "Hardstyle Coffin Dance",
            "Mario Kart Countdown",
            "Rocket League Countdown",
            "Say Wallahi",
            "Truck Horn"
    };

    private final int[] audioResIds = {
            R.raw.nuclear_alert,
            R.raw.freddy_music,
            R.raw.freddy_scream,
            R.raw.freddy_countdown,
            R.raw.hardstyle_lala,
            R.raw.hardstyle_cofin_dance,
            R.raw.mario_kart_countdown,
            R.raw.rocket_league_countdown,
            R.raw.say_wallahi,
            R.raw.truck_horn
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);
        amPmPicker = findViewById(R.id.amPmPicker);
        audioListView = findViewById(R.id.audioListView);
        Button setButton = findViewById(R.id.setButton);
        TextView statusText = findViewById(R.id.statusText);

        // Hour picker 1-12
        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);
        hourPicker.setWrapSelectorWheel(true);

        // Minute picker 00-59
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setFormatter(value -> String.format("%02d", value));
        minutePicker.setWrapSelectorWheel(true);

        // AM/PM picker
        amPmPicker.setMinValue(0);
        amPmPicker.setMaxValue(1);
        amPmPicker.setDisplayedValues(new String[]{"AM", "PM"});
        amPmPicker.setWrapSelectorWheel(false);

        // Audio list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, audioNames);
        audioListView.setAdapter(adapter);
        audioListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        audioListView.setItemChecked(0, true);
        audioListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedAudioIndex = position;
        });

        // Load saved state
        SharedPreferences prefs = getSharedPreferences("ramopt", MODE_PRIVATE);
        boolean isSet = prefs.getBoolean("isSet", false);
        if (isSet) {
            int savedHour = prefs.getInt("hour", 12);
            int savedMinute = prefs.getInt("minute", 0);
            int savedAmPm = prefs.getInt("ampm", 0);
            int savedAudio = prefs.getInt("audio", 0);
            hourPicker.setValue(savedHour);
            minutePicker.setValue(savedMinute);
            amPmPicker.setValue(savedAmPm);
            selectedAudioIndex = savedAudio;
            audioListView.setItemChecked(savedAudio, true);
            String amPmStr = savedAmPm == 0 ? "AM" : "PM";
            statusText.setText("Scheduled: " + savedHour + ":" + String.format("%02d", savedMinute) + " " + amPmStr + " — " + audioNames[savedAudio]);
        }

        setButton.setOnClickListener(v -> {
            int hour = hourPicker.getValue();
            int minute = minutePicker.getValue();
            int amPm = amPmPicker.getValue();

            // Save to prefs
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isSet", true);
            editor.putInt("hour", hour);
            editor.putInt("minute", minute);
            editor.putInt("ampm", amPm);
            editor.putInt("audio", selectedAudioIndex);
            editor.putInt("audioResId", audioResIds[selectedAudioIndex]);
            editor.apply();

            // Start service
            Intent serviceIntent = new Intent(this, SchedulerService.class);
            serviceIntent.putExtra("hour", hour);
            serviceIntent.putExtra("minute", minute);
            serviceIntent.putExtra("ampm", amPm);
            serviceIntent.putExtra("audioResId", audioResIds[selectedAudioIndex]);
            startForegroundService(serviceIntent);

            String amPmStr = amPm == 0 ? "AM" : "PM";
            statusText.setText("Scheduled: " + hour + ":" + String.format("%02d", minute) + " " + amPmStr + " — " + audioNames[selectedAudioIndex]);
            Toast.makeText(this, "Scheduled!", Toast.LENGTH_SHORT).show();
        });
    }
}
