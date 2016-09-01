package monsieurwave.earguard;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by julian on 9/1/16.
 */
public class AllSettings extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
