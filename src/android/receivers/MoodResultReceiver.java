package com.msopentech.authDialog.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.msopentech.authDialog.databases.MoodDatabaseHelper;
import com.msopentech.authDialog.dialogs.MoodDialog;
import com.msopentech.authDialog.things.MoodThing;

public class MoodResultReceiver extends BroadcastReceiver
{
	public static final String INTENT_ACTION_DEPRESSED = "com.msopentech.authDialog.MOOD_RESULT_DEPRESSED";
	public static final String INTENT_ACTION_SAD = "com.msopentech.authDialog.MOOD_RESULT_SAD";
	public static final String INTENT_ACTION_OK = "com.msopentech.authDialog.MOOD_RESULT_OK";
	public static final String INTENT_ACTION_HAPPY = "com.msopentech.authDialog.MOOD_RESULT_HAPPY";
	public static final String INTENT_ACTION_ECSTATIC = "com.msopentech.authDialog.MOOD_RESULT_ECSTATIC";

	public static final String INTENT_ACTION_DIALOG = "com.msopentech.authDialog.MOOD_SHOW_DIALOG";

	@Override public void onReceive(Context context, Intent intent)
	{
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(MoodDialog.NOTIFICATION_ID);

		MoodTimeReceiver.cancelReminderAlarm(context);

		if(intent.getBooleanExtra("fromDialog", false))
		{
			int[] ratings = intent.getIntArrayExtra("results");
			setMood(context, ratings);
		}
		else
		{
			String action = intent.getAction();

			if (action.equals(INTENT_ACTION_DIALOG))
			{
				MoodDialog.show(context.getApplicationContext());
			}
			else
			{
				MoodDialog.dismiss(context, 2);

				if (action.equals(INTENT_ACTION_DEPRESSED))
				{
					setMood(context, MoodThing.RATING_VALUE_1);
				}
				else if (action.equals(INTENT_ACTION_SAD))
				{
					setMood(context, MoodThing.RATING_VALUE_2);
				}
				else if (action.equals(INTENT_ACTION_OK))
				{
					setMood(context, MoodThing.RATING_VALUE_3);
				}
				else if (action.equals(INTENT_ACTION_HAPPY))
				{
					setMood(context, MoodThing.RATING_VALUE_4);
				}
				else if (action.equals(INTENT_ACTION_ECSTATIC))
				{
					setMood(context, MoodThing.RATING_VALUE_5);
				}
			}
		}
	}

	private static void setMood(final Context context, int mood)
	{
		int[] ratings = new int[ MoodThing.NUM_RESULT_TYPES];
		ratings[MoodThing.RATING_MOOD] = mood;
		for(int i = 1; i <  MoodThing.NUM_RESULT_TYPES; i++)
		{
			ratings[i] = MoodThing.RATING_VALUE_NULL;
		}
		setMood(context, ratings);
	}

	public static final String ACTION_UPDATE = "ACTION_UPDATE";

	private static void setMood(final Context context, int[] ratings)
	{
		MoodDatabaseHelper database = new MoodDatabaseHelper(context);

		MoodThing moodThing = new MoodThing(System.currentTimeMillis() / 1000, ratings);
		moodThing.calculateAccurateRating();

		database.insert(context, moodThing);
		database.close();

		context.sendBroadcast(new Intent(ACTION_UPDATE));
	}
}
