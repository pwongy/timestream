package com.nightcap.previously;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * About the app.
 */
public class AboutActivity extends AppCompatActivity {
    private String TAG = "AboutActivity";
    String versionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate xml layout
        setContentView(R.layout.activity_about);

        // Get reference to the toolbar
//        Toolbar toolbar = (Toolbar) findViewById(R.id.about_toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Version number
        TextView versionView = (TextView) findViewById(R.id.about_version);
        versionNumber = "<Unknown>";

        try {
            versionNumber = getApplicationContext().getPackageManager()
                    .getPackageInfo(getApplicationContext().getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String versionText = getString(R.string.about_version)+ " " + versionNumber;
        versionView.setText(versionText);

        // Build date
        TextView buildDateView = (TextView) findViewById(R.id.about_build_date);
        String buildDateText = getString(R.string.about_build_date) + ": "
                + getString(R.string.build_date);
        buildDateView.setText(buildDateText);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If app icon in Action Bar clicked, go home
                finish();
                return true;
            case R.id.action_email_feedback:
                // Intent to send feedback via email
                final Intent emailFeedback = new Intent(android.content.Intent.ACTION_SEND);
                String devEmails[] = { "paul.wong.88@gmail.com", "andrian.sue@outlook.com",
                        "williamxn.z@gmail.com" };

                emailFeedback.setType("plain/text");
                emailFeedback.putExtra(android.content.Intent.EXTRA_EMAIL, devEmails);
                emailFeedback.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback for " +
                        getString(R.string.app_name) + " | Version " + versionNumber);
                emailFeedback.putExtra(android.content.Intent.EXTRA_TEXT,
                        "General comments:\n" +
                                "> \n\n" +
                                "Bugs:\n" +
                                "> \n\n" +
                                "Suggestions:\n" +
                                ">" + " ");

                startActivity(emailFeedback);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
