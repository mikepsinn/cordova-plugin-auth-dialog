package com.msopentech.authDialog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import com.msopentech.authDialog.receivers.MoodResultReceiver;
import com.msopentech.authDialog.receivers.MoodTimeReceiver;
import com.msopentech.authDialog.things.MoodThing;
import com.msopentech.authDialog.things.Question;

public class MoodDialog
{
	private final int NOTIFICATION_ID = 133710;
	private static final String TAG = "MyActivity";

	private final Context mContext;

	private View overlayView;
	private LinearLayout lnAskMoreQuestions;
	private LinearLayout lnCurrentQuestion;
	private ImageButton imAskMoreQuestions;
	private ImageButton[] moodButtons;
	private TextView tvQuestion;
	private TextView tvQuestionDescription;

	private static boolean moodDialogShowing;
	private static boolean askMoreQuestions;

	private static Question[] questions;
	private static int currentQuestion;

	private static int[] reportedMoods;

	public MoodDialog(Context context) {
		mContext = context;
		showNotification(mContext);
	}

	private int getStringId(String name) {
		Resources resources = mContext.getResources();
		return resources.getIdentifier(name, "string", mContext.getPackageName());
	}

	private int getLayoutId(String name) {
		Resources resources = mContext.getResources();
		return resources.getIdentifier(name, "layout", mContext.getPackageName());
	}

	public void showNotification(Context context)
	{
		MoodTimeReceiver.setReminderAlarm(context);

		Intent intent = new Intent(context, MoodResultReceiver.class);
		intent.setAction(MoodResultReceiver.INTENT_ACTION_DEPRESSED);
		PendingIntent intentDepressed = PendingIntent.getBroadcast(context, 0, intent, 0);

		Intent intent1 = new Intent(context, MoodResultReceiver.class);
		intent1.setAction(MoodResultReceiver.INTENT_ACTION_SAD);
		PendingIntent intentSad = PendingIntent.getBroadcast(context, 0, intent1, 0);

		Intent intent2 = new Intent(context, MoodResultReceiver.class);
		intent2.setAction(MoodResultReceiver.INTENT_ACTION_OK);
		PendingIntent intentOk = PendingIntent.getBroadcast(context, 0, intent2, 0);

		Intent intent3 = new Intent(context, MoodResultReceiver.class);
		intent3.setAction(MoodResultReceiver.INTENT_ACTION_HAPPY);
		PendingIntent intentHappy = PendingIntent.getBroadcast(context, 0, intent3, 0);

		Intent intent4 = new Intent(context, MoodResultReceiver.class);
		intent4.setAction(MoodResultReceiver.INTENT_ACTION_ECSTATIC);
		PendingIntent intentEcstatic = PendingIntent.getBroadcast(context, 0, intent4, 0);

		Intent intent5 = new Intent(context, MoodResultReceiver.class);
		intent5.setAction(MoodResultReceiver.INTENT_ACTION_DIALOG);
		PendingIntent intentShowPopup = PendingIntent.getBroadcast(context, 0, intent5, 0);

		Resources res = context.getResources();
		Resources resources = mContext.getResources();
		int notif_mood_title = resources.getIdentifier("notif_mood_title", "string", mContext.getPackageName());
		Notification noti = new NotificationCompat.Builder(context)
				.setContentTitle(res.getString(getStringId("notif_mood_title")))
				.setContentText(res.getString(getStringId("notif_mood_subtitle")))
				.setSmallIcon(R.drawable.ic_action_appicon)
				.setContentIntent(intentShowPopup).build();

		if (Build.VERSION.SDK_INT >= 16)
		{
			RemoteViews contentView = new RemoteViews(context.getPackageName(), getLayoutId("notification_mood"));
			contentView.setOnClickPendingIntent(R.id.btDepressed, intentDepressed);
			contentView.setOnClickPendingIntent(R.id.btSad, intentSad);
			contentView.setOnClickPendingIntent(R.id.btOk, intentOk);
			contentView.setOnClickPendingIntent(R.id.btHappy, intentHappy);
			contentView.setOnClickPendingIntent(R.id.btEcstatic, intentEcstatic);
			noti.bigContentView = contentView;
		}

		noti.tickerText = res.getString(getStringId("notif_mood_title"));

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(NOTIFICATION_ID, noti);
	}

	public void show(final Context context)
	{
		if (moodDialogShowing)
		{
			Log.i(TAG,"MoodDialog showing, not showing new dialog");
			return;
		}
		moodDialogShowing = true;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		askMoreQuestions = prefs.getBoolean("askMoreQuestions", false);


		currentQuestion = 0;
		reportedMoods = new int[22];
		for(int i = 1; i < 22; i++)
		{
			reportedMoods[i] = MoodThing.RATING_VALUE_NULL;
		}

		questions = Question.getAllQuestions(context);
		Arrays.sort(questions, new Comparator<Question>()
		{
			@Override public int compare(Question q1, Question q2)
			{
				if(q1.resultType == MoodThing.RATING_MOOD)  // Always have mood first
				{
					return 1;
				}
				else if(q1.isRequired && q2.isRequired)     // Then sort by required/not required
				{
					return 0;
				}
				else if(q1.isRequired)
				{
					return -1;
				}
				else
				{
					return 1;
				}
			}
		});

		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
				PixelFormat.TRANSPARENT);
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		if (overlayView != null)
		{
			try
			{
				windowManager.removeView(overlayView);
			}
			catch (Exception ignored)
			{
			}
		}

		overlayView = LayoutInflater.from(context).inflate(getLayoutId("dialog_mood"), null);
		overlayView.getBackground().setAlpha(0);
		final Handler handler = new Handler();
		final Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < 255; i += 5)
				{
					final int alpha = i;

					handler.post(new Runnable()
					{
						public void run()
						{
							overlayView.getBackground().setAlpha(alpha);
						}
					});
					try
					{
						Thread.sleep(10);
					}
					catch (Exception e)
					{
						break;
					}
				}
				handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						overlayView.getBackground().setAlpha(255);
					}
				});
			}
		};
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				new Thread(run).start();
			}
		}, 600);

		lnCurrentQuestion = (LinearLayout) overlayView.findViewById(R.id.lnCurrentQuestion);

		lnAskMoreQuestions = (LinearLayout) overlayView.findViewById(R.id.lnAskMoreQuestions);
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_up);
		anim.setStartOffset(300);
		lnAskMoreQuestions.startAnimation(anim);

		View btAskMoreQuestions = lnAskMoreQuestions.findViewById(R.id.btAskMoreQuestions);
		btAskMoreQuestions.setOnClickListener(onAskMoreQuestionsClicked);
		imAskMoreQuestions = (ImageButton) lnAskMoreQuestions.findViewById(R.id.imAskMoreQuestions);
		if (askMoreQuestions)
		{
			imAskMoreQuestions.setImageResource(R.drawable.ic_checked);
			lnCurrentQuestion.startAnimation(anim);
			lnCurrentQuestion.setVisibility(View.VISIBLE);

		}
		else
		{
			imAskMoreQuestions.setImageResource(R.drawable.ic_unchecked);
			if(questions[1].isRequired)    // If the question at pos 1 is required we have > 1 required question
			{
				if(lnCurrentQuestion.getVisibility() != View.VISIBLE)
				{
					lnCurrentQuestion.startAnimation(anim);
					lnCurrentQuestion.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				lnCurrentQuestion.setVisibility(View.GONE);
			}
		}

		tvQuestion = (TextView) lnCurrentQuestion.findViewById(R.id.tvQuestion);
		tvQuestionDescription = (TextView) lnCurrentQuestion.findViewById(R.id.tvQuestionDescription);
		tvQuestion.setText(styleQuestionTitle(context, questions[0].title, R.style.MoodPopup_QuestionKeyWord));
		tvQuestionDescription.setText(questions[0].description);

		moodButtons = new ImageButton[5];

		moodButtons[0] = (ImageButton) overlayView.findViewById(R.id.btDepressed);
		moodButtons[0].setOnClickListener(onMoodButtonClicked);
		moodButtons[0].setTag(MoodThing.RATING_VALUE_1);
		anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_up);
		anim.setStartOffset(300);
		moodButtons[0].startAnimation(anim);

		moodButtons[1] = (ImageButton) overlayView.findViewById(R.id.btSad);
		moodButtons[1].setOnClickListener(onMoodButtonClicked);
		moodButtons[1].setTag(MoodThing.RATING_VALUE_2);
		anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_up);
		anim.setStartOffset(150);
		moodButtons[1].startAnimation(anim);

		moodButtons[2] = (ImageButton) overlayView.findViewById(R.id.btOk);
		moodButtons[2].setOnClickListener(onMoodButtonClicked);
		moodButtons[2].setTag(MoodThing.RATING_VALUE_3);
		anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_up);
		anim.setStartOffset(50);
		moodButtons[2].startAnimation(anim);

		moodButtons[3] = (ImageButton) overlayView.findViewById(R.id.btHappy);
		moodButtons[3].setOnClickListener(onMoodButtonClicked);
		moodButtons[3].setTag(MoodThing.RATING_VALUE_4);
		anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_up);
		anim.setStartOffset(150);
		moodButtons[3].startAnimation(anim);

		moodButtons[4] = (ImageButton) overlayView.findViewById(R.id.btEcstatic);
		moodButtons[4].setOnClickListener(onMoodButtonClicked);
		moodButtons[4].setTag(MoodThing.RATING_VALUE_5);
		anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_up);
		anim.setStartOffset(300);
		moodButtons[4].startAnimation(anim);

		windowManager.addView(overlayView, params);
	}

	public static void dismiss(final Context context, final int buttonNum)
	{
		if (overlayView != null)
		{
			Log.i(TAG,"Overlay not null");
			final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			final Handler handler = new Handler();
			final Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					for (int i = 255; i > 0; i -= 8)
					{
						final int alpha = i;

						handler.post(new Runnable()
						{
							public void run()
							{
								overlayView.getBackground().setAlpha(alpha);
							}
						});
						try
						{
							Thread.sleep(10);
						}
						catch (Exception e)
						{
							break;
						}
					}
					handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							long longestDelay = 0;

							Animation anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_down);
							anim.setFillAfter(true);
							anim.setFillEnabled(true);
							lnAskMoreQuestions.startAnimation(anim);

							for (int i = buttonNum; i > -1; i--)
							{
								anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_down);
								long delay = (buttonNum - i) * 75;
								if (delay > longestDelay)
								{
									longestDelay = delay;
								}
								anim.setFillAfter(true);
								anim.setFillEnabled(true);
								anim.setStartOffset(delay);
								moodButtons[i].startAnimation(anim);
							}
							for (int i = buttonNum + 1; i < moodButtons.length; i++)
							{
								anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_down);
								long delay = (i - buttonNum) * 75;
								if (delay > longestDelay)
								{
									longestDelay = delay;
								}
								anim.setFillAfter(true);
								anim.setFillEnabled(true);
								anim.setStartOffset(delay);
								moodButtons[i].startAnimation(anim);
							}

							if (lnCurrentQuestion.getVisibility() == View.VISIBLE)
							{
								anim = AnimationUtils.loadAnimation(context, R.anim.mood_slide_down);
								anim.setStartOffset(longestDelay + 75);
								anim.setAnimationListener(new Animation.AnimationListener()
								{
									@Override public void onAnimationStart(Animation animation)
									{
									}

									@Override public void onAnimationEnd(Animation animation)
									{
										try
										{
											windowManager.removeView(overlayView);
											Log.i(TAG,"Removed view");
										}
										catch (IllegalArgumentException ignored)
										{
										}

										moodDialogShowing = false;
									}

									@Override public void onAnimationRepeat(Animation animation)
									{
									}
								});
								lnCurrentQuestion.startAnimation(anim);
							}
							else
							{
								handler.postDelayed(new Runnable()
								{
									@Override public void run()
									{
										try
										{
											windowManager.removeView(overlayView);
											Log.i(TAG,"Removed view");
										}
										catch (IllegalArgumentException ignored)
										{
											Log.i(TAG,"Error removing view");
										}

										moodDialogShowing = false;
									}
								}, longestDelay + 250);
							}
						}
					});
				}
			};
			new Thread(run).start();
		}
	}

	private static View.OnClickListener onMoodButtonClicked = new View.OnClickListener()
	{
		@Override public void onClick(View view)
		{
			int reportedMood = (Integer) view.getTag();
			int reportedType = questions[currentQuestion].resultType;
			reportedMoods[reportedType] = reportedMood;

			if (currentQuestion + 1 < questions.length && (askMoreQuestions || questions[currentQuestion + 1].isRequired))
			{
				nextQuestion(view.getContext());
			}
			else
			{
				dismiss(view.getContext(), reportedMood - 1);
				reportMood(view.getContext());
			}
		}
	};

	private static View.OnClickListener onAskMoreQuestionsClicked = new View.OnClickListener()
	{
		@Override public void onClick(View view)
		{
			if (!questions[currentQuestion].isRequired) // If the current question is not required the "Ask more questions" button is a close button
			{
				dismiss(view.getContext(), 2);
				reportMood(view.getContext());
			}
			else
			{
				askMoreQuestions = !askMoreQuestions;
				PreferenceManager.getDefaultSharedPreferences(view.getContext()).edit().putBoolean("askMoreQuestions", askMoreQuestions).commit();

				if (askMoreQuestions)
				{
					imAskMoreQuestions.setImageResource(R.drawable.ic_checked);
					lnCurrentQuestion.setVisibility(View.VISIBLE);
				}
				else
				{
					imAskMoreQuestions.setImageResource(R.drawable.ic_unchecked);
					if(!questions[1].isRequired)    // If the question at position 1 is required we have more than 1 required question
					{
						lnCurrentQuestion.setVisibility(View.GONE);
					}
				}
			}
		}
	};

	public static void reportMood(Context context)
	{
		Intent intent = new Intent(context, MoodResultReceiver.class);
		intent.putExtra("fromDialog", true);
		intent.putExtra("results", reportedMoods);
		context.sendBroadcast(intent);
	}

	private static void nextQuestion(final Context context)
	{
		currentQuestion++;

		Animation fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out_fast);
		fadeOut.setAnimationListener(new Animation.AnimationListener()
		{
			@Override public void onAnimationStart(Animation animation)
			{
			}

			@Override public void onAnimationEnd(Animation animation)
			{
				tvQuestion.setText(styleQuestionTitle(context, questions[currentQuestion].title, R.style.MoodPopup_QuestionKeyWord));
				tvQuestionDescription.setText(questions[currentQuestion].description);

				Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in_fast);
				tvQuestionDescription.setAnimation(fadeIn);
				tvQuestion.startAnimation(fadeIn);
			}

			@Override public void onAnimationRepeat(Animation animation)
			{
			}
		});
		tvQuestionDescription.setAnimation(fadeOut);
		tvQuestion.startAnimation(fadeOut);

		// Animate "Ask more questions" row to a close button if we move to the not required questions
		if (!questions[currentQuestion].isRequired && questions[currentQuestion - 1].isRequired)
		{
			final TextView tvAskMoreQuestions = (TextView) lnAskMoreQuestions.findViewById(R.id.tvAskMoreQuestions);

			fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out_fast);
			fadeOut.setAnimationListener(new Animation.AnimationListener()
			{
				@Override public void onAnimationStart(Animation animation)
				{
				}

				@Override public void onAnimationEnd(Animation animation)
				{
					tvAskMoreQuestions.setText(getStringId("popup_askmorequestions_cancel);
							imAskMoreQuestions.setImageResource(R.drawable.ic_cancel_dark);

					Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in_fast);
					tvAskMoreQuestions.setAnimation(fadeIn);
					imAskMoreQuestions.startAnimation(fadeIn);
				}

				@Override public void onAnimationRepeat(Animation animation)
				{
				}
			});
			tvAskMoreQuestions.setAnimation(fadeOut);
			imAskMoreQuestions.startAnimation(fadeOut);
		}

		reorderButtons(context);
	}

	private static void reorderButtons(final Context context)
	{
		final Handler handler = new Handler();
		Runnable run = new Runnable()
		{
			@Override public void run()
			{
				// Reorder the buttons
				Random random = new Random();
				for (int i = moodButtons.length - 1; i > -1; i--)
				{
					int newIndex = random.nextInt(i + 1);
					Object tag = moodButtons[newIndex].getTag();
					moodButtons[newIndex].setTag(moodButtons[i].getTag());
					moodButtons[i].setTag(tag);
				}

				String iconPrefix = questions[currentQuestion].iconPrefix;
				Class res = R.drawable.class;

				for (final ImageButton moodButton : moodButtons)
				{
					// Get the drawable for the new button
					final Drawable drawable;
					switch ((Integer) moodButton.getTag())
					{
						case MoodThing.RATING_VALUE_1:
							drawable = context.getResources().getDrawable(getDrawableIdForButton(res, 1, iconPrefix));
							break;
						case MoodThing.RATING_VALUE_2:
							drawable = context.getResources().getDrawable(getDrawableIdForButton(res, 2, iconPrefix));
							break;
						case MoodThing.RATING_VALUE_3:
							drawable = context.getResources().getDrawable(getDrawableIdForButton(res, 3, iconPrefix));
							break;
						case MoodThing.RATING_VALUE_4:
							drawable = context.getResources().getDrawable(getDrawableIdForButton(res, 4, iconPrefix));
							break;
						case MoodThing.RATING_VALUE_5:
							drawable = context.getResources().getDrawable(getDrawableIdForButton(res, 5, iconPrefix));
							break;
						default:
							drawable = context.getResources().getDrawable(getDrawableIdForButton(res, 2, iconPrefix));
					}

					final Animation animIn = AnimationUtils.loadAnimation(context, R.anim.flip_in);
					final Animation animOut = AnimationUtils.loadAnimation(context, R.anim.flip_out);

					// Start the animation on the main thread
					handler.post(new Runnable()
					{
						@Override public void run()
						{
							moodButton.startAnimation(animIn);
						}
					});

					// Delay starting the reentry animation
					handler.postDelayed(new Runnable()
					{
						@Override public void run()
						{
							moodButton.setImageDrawable(drawable);
							moodButton.startAnimation(animOut);
						}
					}, animIn.getDuration());

					// Sleep 75ms (time between the animations)
					try
					{
						Thread.sleep(75);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		Thread thread = new Thread(run);
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		thread.start();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static SpannableString styleQuestionTitle(Context context, String value, int style)
	{
		int startLocation = value.indexOf("{");
		int endLocation = value.indexOf("}") - 1;

		value = value.replace("{", "");
		value = value.replace("}", "");

		SpannableString styledText = new SpannableString(value);
		styledText.setSpan(new TextAppearanceSpan(context, style), startLocation, endLocation, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return styledText;
	}

	private static HashMap<String, Integer> drawableIds = new HashMap<String, Integer>();
	private static int getDrawableIdForButton(Class<?> resClass, int rating, String iconPrefix)
	{
		try
		{
			if(rating == 3)
			{
				return R.drawable.ic_mood_3;
			}
			else if(drawableIds.containsKey(iconPrefix + rating))
			{
				return drawableIds.get(iconPrefix + rating);
			}
			else
			{
				Field idField = resClass.getDeclaredField("ic_mood_" + iconPrefix + "_" + rating);
				int id = idField.getInt(idField);
				drawableIds.put(iconPrefix + rating, id);
				return id;
			}
		}
		catch(Exception ignored)
		{
		}
		return R.drawable.icon;
	}
}
