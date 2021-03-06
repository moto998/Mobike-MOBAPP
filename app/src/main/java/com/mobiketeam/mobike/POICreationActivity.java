package com.mobiketeam.mobike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiketeam.mobike.network.POICreationTask;


public class POICreationActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String POI_LATITUDE = "com.mobike.mobike.poi_latitude";
    public static final String POI_LONGITUDE = "com.mobike.mobike.poi_longitude";
    public static final String POI_TITLE = "com.mobike.mobike.poi_title";
    public static final String POI_CATEGORY = "com.mobike.mobike.poi_category";
    public static final String POI_IS_ASSOCIATED = "com.mobike.mobike.poi_is_associated";

    private double latitude, longitude;
    private boolean recording;

    private Spinner categorySpinner;
    private int category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poicreation);

        //getSupportActionBar().hide();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ((Button) findViewById(R.id.create_button)).setOnClickListener(this);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra(MapsFragment.POI_LATITUDE, 300);
        longitude = intent.getDoubleExtra(MapsFragment.POI_LONGITUDE, 300);
        recording = intent.getBooleanExtra(MapsFragment.POI_RECORDING, false);

        if (recording)
            ((TextView) findViewById(R.id.message_textview)).setText(getResources().getString(R.string.poi_creation_message_recording));

        categorySpinner = (Spinner) findViewById(R.id.poi_category);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.poi_categories, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        categorySpinner.setAdapter(adapter);
        categorySpinner.setPrompt("Route type...");
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                category = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });
        category = 0;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_button:
                createPOI();
                break;
        }
    }

    private void createPOI() {
        String title = ((EditText) findViewById(R.id.title)).getText().toString();
        if (title.length() == 0) {
            Toast.makeText(this, "Please insert a name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (recording)
            savePOI(title);
        else
            sendPOI(title);
    }

    private void savePOI(String title) {
        // save POI in DB
        GPSDatabase db = new GPSDatabase(this);
        db.insertRowPOI(latitude, longitude, title, category);
        db.close();

        Intent data = new Intent();
        Bundle b = new Bundle();
        b.putDouble(POI_LATITUDE, latitude);
        b.putDouble(POI_LONGITUDE, longitude);
        b.putString(POI_TITLE, title);
        b.putInt(POI_CATEGORY, category);
        b.putBoolean(POI_IS_ASSOCIATED, true);
        data.putExtras(b);
        setResult(RESULT_OK, data);
        finish();
    }

    private void sendPOI(String title) {
        new POICreationTask(this, latitude, longitude, title, category).execute();

        Intent data = new Intent();
        Bundle b = new Bundle();
        b.putDouble(POI_LATITUDE, latitude);
        b.putDouble(POI_LONGITUDE, longitude);
        b.putString(POI_TITLE, title);
        b.putInt(POI_CATEGORY, category);
        b.putBoolean(POI_IS_ASSOCIATED, false);
        data.putExtras(b);
        setResult(RESULT_OK, data);
        finish();
    }
}
