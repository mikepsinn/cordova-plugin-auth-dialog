package com.msopentech.authDialog.things;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import com.msopentech.authDialog.R;

public class Question
{
	public final String name;
	public final String title;
	public final String description;

	public boolean isRequired;
	public int resultType;

	public final String iconPrefix;

	public Question(String name, String title, String description, String iconPrefix)
	{
		this.name = name;
		this.title = title;
		this.description = description;
		this.iconPrefix = iconPrefix;
		this.isRequired = false;
	}

	public static Question[] getAllQuestions(Context context)
	{
		Resources res = context.getResources();
		String[] questionTitles = res.getStringArray(R.array.questions);
		String[] questionDescriptions = res.getStringArray(R.array.questions_description);

		String[] selectedRequiredRatings;
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			selectedRequiredRatings = prefs.getStringSet("requiredRatings", null).toArray(new String[]{});
		}
		catch(NullPointerException e)
		{
			selectedRequiredRatings = new String[0];
		}

		Question[] questions = new Question[questionTitles.length];
		for(int i = 0; i < questionTitles.length; i++)
		{
			int startLocation = questionTitles[i].indexOf("[");
			int endLocation = questionTitles[i].indexOf("]");

			int nameStartLocation = questionTitles[i].indexOf("{");
			int nameEndLocation = questionTitles[i].indexOf("}");

			String iconPrefix = questionTitles[i].substring(startLocation + 1, endLocation);
			String moodName = questionTitles[i].substring(nameStartLocation + 1, nameEndLocation);
			questionTitles[i] = questionTitles[i].substring(endLocation + 1, questionTitles[i].length());

			questions[i] = new Question(moodName, questionTitles[i], questionDescriptions[i], iconPrefix);
		}

		questions[0].resultType = MoodThing.RATING_MOOD;
		questions[0].isRequired = true;
		questions[1].resultType = MoodThing.RATING_GUILTY;
		questions[2].resultType = MoodThing.RATING_ALERT;
		questions[3].resultType = MoodThing.RATING_AFRAID;
		questions[4].resultType = MoodThing.RATING_EXCITED;
		questions[5].resultType = MoodThing.RATING_IRRITABLE;
		questions[6].resultType = MoodThing.RATING_ASHAMED;
		questions[7].resultType = MoodThing.RATING_ATTENTIVE;
		questions[8].resultType = MoodThing.RATING_HOSTILE;
		questions[9].resultType = MoodThing.RATING_ACTIVE;
		questions[10].resultType = MoodThing.RATING_NERVOUS;
		questions[11].resultType = MoodThing.RATING_INTERESTED;
		questions[12].resultType = MoodThing.RATING_ENTHUSIASTIC;
		questions[13].resultType = MoodThing.RATING_JITTERY;
		questions[14].resultType = MoodThing.RATING_STRONG;
		questions[15].resultType = MoodThing.RATING_DISTRESSED;
		questions[16].resultType = MoodThing.RATING_DETERMINED;
		questions[17].resultType = MoodThing.RATING_UPSET;
		questions[18].resultType = MoodThing.RATING_PROUD;
		questions[19].resultType = MoodThing.RATING_SCARED;
		questions[20].resultType = MoodThing.RATING_INSPIRED;

		for(String requiredRatingType : selectedRequiredRatings)
		{
			for(int i = 1; i < questions.length; i++)   // Skip "Mood", which is always flagged as required
			{
				if(Integer.valueOf(requiredRatingType) == questions[i].resultType)
				{
					questions[i].isRequired = true;
					break;
				}
			}
		}

		return questions;
	}
}
