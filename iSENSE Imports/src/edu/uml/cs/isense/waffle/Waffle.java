package edu.uml.cs.isense.waffle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.R;

/**
 * Class that creates custom-styled, visually-pleasing Toast messages, given a
 * particular set of user parameters.
 * 
 * @author Mike Stowell and Jeremy Poulin of the iSENSE Android-Development
 *         Team.
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
	public static int LENGTH_SHORT = 0;
	/**
	 * Long-length constant for Waffle messages (equivalent to
	 * Toast.LENGTH_LONG).
	 */
	public static int LENGTH_LONG = 1;
	/**
	 * ID for a green "check" image to be displayed on a Waffle message.
	 */
	public static int IMAGE_CHECK = 0;
	/**
	 * ID for a red "x" image to be displayed on a Waffle message.
	 */
	public static int IMAGE_X = 1;

	/**
	 * Default constructor for the Waffle object.
	 * 
	 * @param c
	 *            - Context required to display Waffle messages on the context's
	 *            view.
	 */
	public Waffle(Context c) {
		this.isDisplaying = false;
		this.canPerformTask = false;
		this.context = c;
	}

	/**
	 * Display a custom-styled Toast message for a specified duration, equipped
	 * with a "check" or "x" image.
	 * 
	 * @param message
	 *            - Message to display in the view.
	 * @param length
	 *            - Duration of the message (Waffle.LENGTH_SHORT or
	 *            Waffle.LENGTH_LONG).
	 * @param image_id
	 *            - ID of the image to display in the view (Waffle.IMAGE_CHECK
	 *            or Waffle.IMAGE_X).
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message, int length, int image_id) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = null;
			if (image_id != 1)
				layout = inflater.inflate(R.layout.toast_layout_check, null);
			else
				layout = inflater.inflate(R.layout.toast_layout_x, null);

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
	 * Display a custom-styled Toast message for a specified duration.
	 * 
	 * @param message
	 *            - Message to display in the view.
	 * @param length
	 *            - Duration of the message (Waffle.LENGTH_SHORT or
	 *            Waffle.LENGTH_LONG).
	 * 
	 * @return void
	 * 
	 */
	@SuppressLint("NewApi")
	public void make(String message, int length) {

		if (!isDisplaying) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = inflater.inflate(R.layout.toast_layout_check, null);

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);

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
	 * Display a custom-styled Toast message.
	 * 
	 * @param message
	 *            - Message to display in the view.
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
