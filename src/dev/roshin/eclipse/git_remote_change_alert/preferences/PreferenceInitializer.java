package dev.roshin.eclipse.git_remote_change_alert.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import dev.roshin.eclipse.git_remote_change_alert.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.CACHE_ENABLED_KEY, false);
		store.setDefault(PreferenceConstants.COOLDOWN_PERIOD_KEY, 0);
//		store.setDefault(PreferenceConstants.P_STRING, "Default value");
	}

}
