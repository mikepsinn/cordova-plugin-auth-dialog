package com.msopentech.authDialog.things;

import com.msopentech.authDialog.Log;
import com.msopentech.authDialog.Utils;

import com.msopentech.authDialog.model.MeasurementSet;
import com.msopentech.authDialog.model.Measurement;

import java.io.Serializable;
import java.util.HashMap;

public class MoodThing implements Serializable
{
	public static final int NUM_RESULT_TYPES = 22;

	public static final int RATING_VALUE_NULL = 0;
	public static final int RATING_VALUE_1 = 1;
	public static final int RATING_VALUE_2 = 2;
	public static final int RATING_VALUE_3 = 3;
	public static final int RATING_VALUE_4 = 4;
	public static final int RATING_VALUE_5 = 5;

	public static final int RATING_MOOD = 0;
	public static final int RATING_GUILTY = 1;
	public static final int RATING_ALERT = 2;
	public static final int RATING_AFRAID = 3;
	public static final int RATING_EXCITED = 4;
	public static final int RATING_IRRITABLE = 5;
	public static final int RATING_ASHAMED = 6;
	public static final int RATING_ATTENTIVE = 7;
	public static final int RATING_HOSTILE = 8;
	public static final int RATING_ACTIVE = 9;
	public static final int RATING_NERVOUS = 10;
	public static final int RATING_INTERESTED = 11;
	public static final int RATING_ENTHUSIASTIC = 12;
	public static final int RATING_JITTERY = 13;
	public static final int RATING_STRONG = 14;
	public static final int RATING_DISTRESSED = 15;
	public static final int RATING_DETERMINED = 16;
	public static final int RATING_UPSET = 17;
	public static final int RATING_PROUD = 18;
	public static final int RATING_SCARED = 19;
	public static final int RATING_INSPIRED = 20;
	public static final int RATING_ACCURATE_MOOD = 21;

	public static int averageMood;

	public long timestamp;
	public long timestampMillis;
	public int[] ratings;

	public double normalizedTimestamp;

	/*
	 * @param timestamp     timestamp in seconds
	 * @param ratings       an array of ratings (length 21)
	 */
	public MoodThing(long timestamp, int[] ratings)
	{
		this.timestamp = timestamp;
		this.timestampMillis = timestamp * 1000;
		
		if(ratings.length == NUM_RESULT_TYPES)
		{
			this.ratings = ratings;
		}
		else
		{
			throw new IllegalArgumentException("Length of \"ratings\" should be 22, but is " + ratings.length);
		}
	}

	// TODO rewrite this to support a measurement set?
	/*public MoodThing(QuantimodoMeasurement record)
	{
		this.timestamp = record.timestamp;
		this.timestampMillis = record.timestamp * 1000;

		ratings = new int[NUM_RESULT_TYPES];
		for(int i = 0; i < NUM_RESULT_TYPES; i++)
		{
			ratings[i] = RATING_VALUE_NULL;
		}
		if(record.unit.equals("/5"))
		{
			ratings[RATING_MOOD] = (int) record.value;
		}
		else if(record.unit.equals("%"))
		{
			ratings[RATING_ACCURATE_MOOD] = (int) record.value;
			ratings[RATING_MOOD] = getOneToFiveMood();
		}
		else
		{
			throw new IllegalArgumentException("Unit of record is invalid. Unit should be either /5 or %, but is " + record.unit);
		}
	}*/

	public int getOneToHundredMood()
	{
		if(ratings[RATING_ACCURATE_MOOD] != RATING_VALUE_NULL)
		{
			return ratings[RATING_ACCURATE_MOOD];
		}
		else
		{
			return (int) (((double) (ratings[RATING_MOOD]- 1) / 4) * 100) ;
		}
	}
	public int getOneToFiveMood()
	{
		if(ratings[RATING_ACCURATE_MOOD] != RATING_VALUE_NULL)
		{
			return Utils.roundToNearestInteger((((double) ratings[RATING_ACCURATE_MOOD] / 100) * 5) + 1);
		}
		else
		{
			return ratings[RATING_MOOD];
		}
	}

	@SuppressWarnings("ConstantConditions") // Not true, confused by switch statement breaks
	public void calculateAccurateRating()
	{
		Log.i("Calculating accurate mood");

		int totalScore = 50;
		boolean allFilledIn = true;
		for(int i = 2; i < NUM_RESULT_TYPES; i++)
		{
			if(ratings[i] == MoodThing.RATING_VALUE_NULL)
			{
				allFilledIn = false;
				break;
			}

			switch(i)
			{
			case RATING_GUILTY:
				totalScore -= ratings[i];
				Log.i("Guilty: -= " + ratings[i]);
				break;
			case RATING_ALERT:
				totalScore += ratings[i];
				Log.i("Alert: += " + ratings[i]);
				break;
			case RATING_AFRAID:
				totalScore -= ratings[i];
				Log.i("Afraid: -= " + ratings[i]);
				break;
			case RATING_EXCITED:
				totalScore += ratings[i];
				Log.i("Excited: += " + ratings[i]);
				break;
			case RATING_IRRITABLE:
				totalScore -= ratings[i];
				Log.i("Irritable: -= " + ratings[i]);
				break;
			case RATING_ASHAMED:
				totalScore -= ratings[i];
				Log.i("Ashamed: -= " + ratings[i]);
				break;
			case RATING_ATTENTIVE:
				totalScore += ratings[i];
				Log.i("Attentive: += " + ratings[i]);
				break;
			case RATING_HOSTILE:
				totalScore -= ratings[i];
				Log.i("Hostile: -= " + ratings[i]);
				break;
			case RATING_ACTIVE:
				totalScore += ratings[i];
				Log.i("Active: += " + ratings[i]);
				break;
			case RATING_NERVOUS:
				totalScore -= ratings[i];
				Log.i("Nervous: -= " + ratings[i]);
				break;
			case RATING_INTERESTED:
				totalScore += ratings[i];
				Log.i("Interested: += " + ratings[i]);
				break;
			case RATING_ENTHUSIASTIC:
				totalScore += ratings[i];
				Log.i("Enthusiastic: += " + ratings[i]);
				break;
			case RATING_JITTERY:
				totalScore -= ratings[i];
				Log.i("Jittery: -= " + ratings[i]);
				break;
			case RATING_STRONG:
				totalScore += ratings[i];
				Log.i("Strong: += " + ratings[i]);
				break;
			case RATING_DISTRESSED:
				totalScore -= ratings[i];
				Log.i("Distressed: -= " + ratings[i]);
				break;
			case RATING_DETERMINED:
				totalScore += ratings[i];
				Log.i("Determined: += " + ratings[i]);
				break;
			case RATING_UPSET:
				totalScore -= ratings[i];
				Log.i("Upset: -= " + ratings[i]);
				break;
			case RATING_PROUD:
				totalScore += ratings[i];
				Log.i("Proud: += " + ratings[i]);
				break;
			case RATING_SCARED:
				totalScore -= ratings[i];
				Log.i("Scared: -= " + ratings[i]);
				break;
			case RATING_INSPIRED:
				totalScore += ratings[i];
				Log.i("Inspired: += " + ratings[i]);
				break;
			}
		}

		if(allFilledIn)
		{
			ratings[RATING_ACCURATE_MOOD] = totalScore;
			Log.i("Accurate mood: " + ratings[RATING_ACCURATE_MOOD] + "%");
		}
		else
		{
			Log.i("Not all questions were filled in, cannot calculate accurate mood");
		}
	}

	public HashMap<Integer, MeasurementSet> toMeasurementSets(HashMap<Integer, MeasurementSet> measurementSets)
	{
		// These two are a bit special, since only one of the two should be uploaded
		if(ratings[RATING_ACCURATE_MOOD] != RATING_VALUE_NULL)
		{
			if(!measurementSets.containsKey(RATING_ACCURATE_MOOD))
			{
				measurementSets.put(RATING_ACCURATE_MOOD, new MeasurementSet("Overall Mood", null, "Mood", "%", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
			}
			measurementSets.get(RATING_ACCURATE_MOOD).measurements.add(new Measurement(this.timestamp, ratings[MoodThing.RATING_ACCURATE_MOOD]));
		}
		else
		{
			if(!measurementSets.containsKey(RATING_MOOD))
			{
				measurementSets.put(RATING_MOOD, new MeasurementSet("Overall Mood", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
			}
			measurementSets.get(RATING_MOOD).measurements.add(new Measurement(this.timestamp, ratings[MoodThing.RATING_MOOD]));
		}



		// Loop through remaining mood rating types
		for(int i = 0; i < MoodThing.NUM_RESULT_TYPES; i++)
		{
			// If the user inputted this rating
			if(ratings[i] != RATING_VALUE_NULL)
			{
				// Check if we already have a measurement set for this rating, if not, create one.
				if(!measurementSets.containsKey(i))
				{
					switch(i)
					{
					case RATING_GUILTY:
						measurementSets.put(i, new MeasurementSet("Guiltiness", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_ALERT:
						measurementSets.put(i, new MeasurementSet("Alertness", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_AFRAID:
						measurementSets.put(i, new MeasurementSet("Fear", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_EXCITED:
						measurementSets.put(i, new MeasurementSet("Excitability", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_IRRITABLE:
						measurementSets.put(i, new MeasurementSet("Irritability", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_ASHAMED:
						measurementSets.put(i, new MeasurementSet("Shame", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_ATTENTIVE:
						measurementSets.put(i, new MeasurementSet("Attentiveness", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_HOSTILE:
						measurementSets.put(i, new MeasurementSet("Hostility", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_ACTIVE:
						measurementSets.put(i, new MeasurementSet("Activeness", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_NERVOUS:
						measurementSets.put(i, new MeasurementSet("Nervousness", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_INTERESTED:
						measurementSets.put(i, new MeasurementSet("Interest", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_ENTHUSIASTIC:
						measurementSets.put(i, new MeasurementSet("Enthusiasm", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_JITTERY:
						measurementSets.put(i, new MeasurementSet("Jitteriness", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_STRONG:
						measurementSets.put(i, new MeasurementSet("Resilience", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_DISTRESSED:
						measurementSets.put(i, new MeasurementSet("Distress", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_DETERMINED:
						measurementSets.put(i, new MeasurementSet("Determination", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_UPSET:
						measurementSets.put(i, new MeasurementSet("Upsettedness", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_PROUD:
						measurementSets.put(i, new MeasurementSet("Pride", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_SCARED:
						measurementSets.put(i, new MeasurementSet("Scaredness", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					case RATING_INSPIRED:
						measurementSets.put(i, new MeasurementSet("Inspiration", null, "Mood", "/5", MeasurementSet.COMBINE_MEAN, "MoodiModo"));
						break;
					}
				}

				// Add the measurement to the proper measurement set
				Measurement newMeasurement = new Measurement(this.timestamp, ratings[i]);
				measurementSets.get(i).measurements.add(newMeasurement);
			}
		}

		// Return the filled hashmap
		return measurementSets;
	}
}
