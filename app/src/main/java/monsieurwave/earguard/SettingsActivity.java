package monsieurwave.earguard;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.WindowManager;

/**
 * Created by julian on 9/1/16.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AllSettings())
                .commit();
    }
}
