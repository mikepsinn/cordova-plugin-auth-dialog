package com.quantimodo.ratingplugin.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.quantimodo.ratingplugin.Global;

public class BootCompletedReceiver extends BroadcastReceiver
{
	@Override public void onReceive(Context context, Intent intent)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Global.moodInterval = Integer.valueOf(prefs.getString("moodInterval", "1"));

		MoodTimeReceiver.setAlarm(context, Global.moodInterval);
	}
}
