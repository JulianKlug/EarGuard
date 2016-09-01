package monsieurwave.earguard;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by julian on 9/1/16.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AllSettings())
                .commit();
    }
}
