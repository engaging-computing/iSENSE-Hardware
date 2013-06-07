package edu.uml.cs.isense.waffle;

import edu.uml.cs.isense.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class that creates custom-styled, visually-pleasing Toast messages, given a
 * particular set of user parameters.
 * 
 * Waffles will not be queued infinitesimally, unlike their Toast counterpart.
 * This prevents the flooding the Toast messages on an Android device.  As a
 * result, however, any Waffle message that attempts to be displayed whilst 
 * another Waffle is displaying will cause the Waffle message to be thrown
 * away (i.e. it will never show).
 * 
 * Some constants in this class are of type short or long when they could
 * be integers.  Using shorts/longs enables the ability to have 8 separate, 
 * similarly named "make" functions that enable full-user control over his or her
 * custom Waffle message.
 * 
 * @author Mike Stowell of the iSENSE Android-Development
 *         Team, with minor edits by Jeremy Poulin.
 * 
 */
public class Waffle {
	/**
	 * Boolean trigger that informs whether or not a Waffle message is currently
	 * being displayed.
	 * 
	 * @true If a Waffle message is currently being displayed in a view.
	 * @false If a Waffle message is not being displayed.
	 */
	public boolean isDisplaying;
	/**
	 * Optional boolean feature included that implements the ability to perform
	 * a particular task within 1.5 seconds of a particular Waffle message being
	 * displayed.
	 * 
	 * @true If a Waffle message has been displayed in the past 1.5 seconds
	 * @false If a Waffle message has not been displayed or has surpasssed 1.5
	 *        seconds in duration.
	 */
	public boolean canPerformTask;
	/**
	 * Context that this Waffle message will be displayed on.
	 */
	public Context context;
	/**
	 * Short-length constant for Waffle messages (equivalent to
	 * Toast.LENGTH_SHORT).
	 */
	public static long LENGTH_SHORT = 0;
	/**
	 * Long-length constant for Waffle messages (equivalent to
	 * Toast.LENGTH_LONG).
	 */
	public static long LENGTH_LONG = 1;
	/**
	 * ID for a green "check" image to be displayed on a Waffle message.
	 */
	public static short IMAGE_CHECK = 0;
	/**
	 * ID for a red "x" image to be displayed on a Waffle message.
	 */
	public static short IMAGE_X = 1;
	/**
	 * ID for a yellow "warning" image to be displayed on a Waffle message.
	 */
	public static short IMAGE_WARN = 2;

	/**
	 * Default constructor for the Waffle object.
	 * 
	 * @param c
	 *            Context required to display Waffle messages on the context's
	 *            view.
	 */
	public Waffle(Context c) {
		this.isDisplaying = false;
		this.canPerformTask = false;
		this.context = c;
	}
	
	/**
	 * Display a custom-styled Toast message for a specified duration, equipped
	 * with a "check" or "x" image and a custom background drawable.
	 * 
	 * @param message
	 *            Message to display in the view.
	 * @param length
	 *            Duration of the message (Waffle.LENGTH_SHORT or
	 *            Waffle.LENGTH_LONG).
	 * @param image_id
	 *            ID of the image to display in the view (Waffle.IMAGE_CHECK,
	 *            Waffle.IMAGE_X, or Waffle.IMAGE_WARN).           
	 * @param background_id
	 *            Resource ID of the background drawable to be used as the
	 *            background of the Waffle message (e.g. R.drawable.my_background).
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message, long length, short image_id, int background_id) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = null;
			switch (image_id) {
			case 0:
				layout = inflater.inflate(R.layout.toast_layout_check, null);
				break;
				
			case 1:
				layout = inflater.inflate(R.layout.toast_layout_x, null);
				break;
				
			case 2:
				layout = inflater.inflate(R.layout.toast_layout_warning, null);
				break;
				
			default:
				layout = inflater.inflate(R.layout.toast_layout_check, null);
				ImageView image = (ImageView) layout
						.findViewById(R.id.waffle_check);
				image.setVisibility(View.GONE);
				LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
				background.setBackgroundResource(R.drawable.toast_background_default);
				break;
			}

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);
			
			LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
			background.setBackgroundResource(background_id);

			Toast toast = new Toast(context);
			toast.setGravity(Gravity.BOTTOM, 0, 50);
			if (length == Toast.LENGTH_LONG)
				toast.setDuration(Toast.LENGTH_LONG);
			else
				toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();

			new NoToastTwiceTask().execute();
		}

	}

	/**
	 * Display a custom-styled Toast message for a specified duration, equipped
	 * with a "check" or "x" image.
	 * 
	 * @param message
	 *            Message to display in the view.
	 * @param length
	 *            Duration of the message (Waffle.LENGTH_SHORT or
	 *            Waffle.LENGTH_LONG).
	 * @param image_id
	 *            ID of the image to display in the view (Waffle.IMAGE_CHECK
	 *            or Waffle.IMAGE_X).
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message, long length, short image_id) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = null;
			switch (image_id) {
			case 0:
				layout = inflater.inflate(R.layout.toast_layout_check, null);
				break;
				
			case 1:
				layout = inflater.inflate(R.layout.toast_layout_x, null);
				break;
				
			case 2:
				layout = inflater.inflate(R.layout.toast_layout_warning, null);
				break;
				
			default:
				layout = inflater.inflate(R.layout.toast_layout_check, null);
				ImageView image = (ImageView) layout
						.findViewById(R.id.waffle_check);
				image.setVisibility(View.GONE);
				LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
				background.setBackgroundResource(R.drawable.toast_background_default);
				break;
			}

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);

			Toast toast = new Toast(context);
			toast.setGravity(Gravity.BOTTOM, 0, 50);
			if (length == Toast.LENGTH_LONG)
				toast.setDuration(Toast.LENGTH_LONG);
			else
				toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();

			new NoToastTwiceTask().execute();
		}

	}
	
	/**
	 * Display a custom-styled Toast message for a specified duration, equipped
	 * with a custom background drawable.
	 * 
	 * @param message
	 *            Message to display in the view.
	 * @param length
	 *            Duration of the message (Waffle.LENGTH_SHORT or
	 *            Waffle.LENGTH_LONG).
	 * @param background_id
	 *            Resource ID of the background drawable to be used as the
	 *            background of the Waffle message (e.g. R.drawable.my_background).
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message, long length, int background_id) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = null;

			layout = inflater.inflate(R.layout.toast_layout_check, null);

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);
			
			LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
			background.setBackgroundResource(background_id);
			
			ImageView image = (ImageView) layout
					.findViewById(R.id.waffle_check);
			image.setVisibility(View.GONE);

			Toast toast = new Toast(context);
			toast.setGravity(Gravity.BOTTOM, 0, 50);
			if (length == Toast.LENGTH_LONG)
				toast.setDuration(Toast.LENGTH_LONG);
			else
				toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();

			new NoToastTwiceTask().execute();
		}

	}
	
	/**
	 * Display a custom-styled Toast message, equipped
	 * with a "check" or "x" image and a custom background drawable.
	 * 
	 * @param message
	 *            Message to display in the view.
	 * @param image_id
	 *            ID of the image to display in the view (Waffle.IMAGE_CHECK
	 *            or Waffle.IMAGE_X).           
	 * @param background_id
	 *            Resource ID of the background drawable to be used as the
	 *            background of the Waffle message (e.g. R.drawable.my_background).
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message, short image_id, int background_id) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = null;
			switch (image_id) {
			case 0:
				layout = inflater.inflate(R.layout.toast_layout_check, null);
				break;
				
			case 1:
				layout = inflater.inflate(R.layout.toast_layout_x, null);
				break;
				
			case 2:
				layout = inflater.inflate(R.layout.toast_layout_warning, null);
				break;
				
			default:
				layout = inflater.inflate(R.layout.toast_layout_check, null);
				ImageView image = (ImageView) layout
						.findViewById(R.id.waffle_check);
				image.setVisibility(View.GONE);
				LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
				background.setBackgroundResource(R.drawable.toast_background_default);
				break;
			}

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);
			
			LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
			background.setBackgroundResource(background_id);

			Toast toast = new Toast(context);
			toast.setGravity(Gravity.BOTTOM, 0, 50);

			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();

			new NoToastTwiceTask().execute();
		}

	}

	/**
	 * Display a custom-styled Toast message for a specified duration.
	 * 
	 * @param message
	 *            Message to display in the view.
	 * @param length
	 *            Duration of the message (Waffle.LENGTH_SHORT or
	 *            Waffle.LENGTH_LONG).
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message, long length) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = inflater.inflate(R.layout.toast_layout_check, null);

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);

			ImageView image = (ImageView) layout
					.findViewById(R.id.waffle_check);
			image.setVisibility(View.GONE);
			
			LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
			background.setBackgroundResource(R.drawable.toast_background_default);

			Toast toast = new Toast(context);
			toast.setGravity(Gravity.BOTTOM, 0, 50);
			if (length == Toast.LENGTH_LONG)
				toast.setDuration(Toast.LENGTH_LONG);
			else
				toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();

			new NoToastTwiceTask().execute();
		}

	}
	
	/**
	 * Display a custom-styled Toast message, equipped
	 * with a "check" or "x" image.
	 * 
	 * @param message
	 *            Message to display in the view.
	 * @param image_id
	 *            ID of the image to display in the view (Waffle.IMAGE_CHECK
	 *            or Waffle.IMAGE_X).
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message, short image_id) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = null;
			switch (image_id) {
			case 0:
				layout = inflater.inflate(R.layout.toast_layout_check, null);
				break;
				
			case 1:
				layout = inflater.inflate(R.layout.toast_layout_x, null);
				break;
				
			case 2:
				layout = inflater.inflate(R.layout.toast_layout_warning, null);
				break;
				
			default:
				layout = inflater.inflate(R.layout.toast_layout_check, null);
				ImageView image = (ImageView) layout
						.findViewById(R.id.waffle_check);
				image.setVisibility(View.GONE);
				LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
				background.setBackgroundResource(R.drawable.toast_background_default);
				break;
			}

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);
			
			Toast toast = new Toast(context);
			toast.setGravity(Gravity.BOTTOM, 0, 50);

			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();

			new NoToastTwiceTask().execute();
		}

	}
	
	/**
	 * Display a custom-styled Toast message, equipped
	 * with a custom background drawable.
	 * 
	 * @param message
	 *            Message to display in the view.        
	 * @param background_id
	 *            Resource ID of the background drawable to be used as the
	 *            background of the Waffle message (e.g. R.drawable.my_background).
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message, int background_id) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = null;

			layout = inflater.inflate(R.layout.toast_layout_check, null);

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);
			
			ImageView image = (ImageView) layout
					.findViewById(R.id.waffle_check);
			image.setVisibility(View.GONE);
			
			LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
			background.setBackgroundResource(background_id);

			Toast toast = new Toast(context);
			toast.setGravity(Gravity.BOTTOM, 0, 50);

			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();

			new NoToastTwiceTask().execute();
		}

	}
	

	/**
	 * Display a custom-styled Toast message.
	 * 
	 * @param message
	 *            Message to display in the view.
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = inflater.inflate(R.layout.toast_layout_check, null);

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);

			ImageView image = (ImageView) layout
					.findViewById(R.id.waffle_check);
			image.setVisibility(View.GONE);

			LinearLayout background = (LinearLayout) layout.findViewById(R.id.toast_layout_root);
			background.setBackgroundResource(R.drawable.toast_background_default);
			
			Toast toast = new Toast(context);
			toast.setGravity(Gravity.BOTTOM, 0, 50);

			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();

			new NoToastTwiceTask().execute();
		}

	}

	@SuppressLint("NewApi")
	private class NoToastTwiceTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPreExecute() {
			isDisplaying = true;
			canPerformTask = true;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			try {
				Thread.sleep(1500);
				canPerformTask = false;
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				canPerformTask = false;
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			isDisplaying = false;
		}
	}

}
