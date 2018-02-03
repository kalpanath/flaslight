package qlikapps.flashlight;

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Intent;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;


public class SwipelightDashClock extends DashClockExtension {
    private static final String TAG = "ExampleExtension";

    public static final String PREF_NAME = "pref_name";

    @Override
    protected void onUpdateData(int reason) {
        // Get preference value.
        /*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sp.getString(PREF_NAME, getString(R.string.pref_name_default));*/

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.collinguarino.flashlight");

        // Publish the extension data update.
        publishUpdate(new ExtensionData()
                .visible(true)
                .icon(R.drawable.swipelightinverted)
                .status("Flashlight")
                //.expandedTitle("Flashlight")
                /*.expandedBody("Thanks for checking out this example extension for DashClock.")
                .contentDescription("Completely different text for accessibility if needed.")*/
                .clickIntent(launchIntent));
    }
}