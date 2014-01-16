package edu.uml.cs.isense.canobiev2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.waffle.Waffle;

public class MediaManager extends Activity {

	private static Uri imageUri;
	private static Uri videoUri;

	private static final int CAMERA_PIC_REQUESTED = 1;
	private static final int CAMERA_VID_REQUESTED = 2;

	private static Waffle w;
	private static Context mContext;
	private static TextView mediaCountLabel;
	private static Button takePic;
	private static Button takeVid;
	private static Button back;

	public static int mediaCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_manager);

		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		mContext = this;
		w = new Waffle(mContext);

		mediaCountLabel = (TextView) findViewById(R.id.mediaCounter);
		mediaCountLabel.setText(mContext.getString(R.string.picAndVidCount)
				+ mediaCount);

		takePic = (Button) findViewById(R.id.mediaPicture);
		
		/*Take Picture*/
		takePic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {

					ContentValues values = new ContentValues();

					imageUri = getContentResolver().insert(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							values);

					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
					intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
					startActivityForResult(intent, CAMERA_PIC_REQUESTED);

				} else {
					w.make("Permission isn't granted to write to external storage.  Please enable to take pictures.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				}

			}
		});

		/*Record Video*/
		takeVid = (Button) findViewById(R.id.mediaVideo);
		takeVid.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {

					ContentValues valuesVideos = new ContentValues();

					videoUri = getContentResolver().insert(
							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							valuesVideos);

					Intent intentVid = new Intent(
							MediaStore.ACTION_VIDEO_CAPTURE);
					intentVid.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
					intentVid.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
					startActivityForResult(intentVid, CAMERA_VID_REQUESTED);

				} else {
					w.make("Permission isn't granted to write to external storage.  Please enable to record videos.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				}
			}
		});

		back = (Button) findViewById(R.id.mediaBack);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_PIC_REQUESTED) {
			if (resultCode == RESULT_OK) {
				File f = convertImageUriToFile(imageUri);
				AmusementPark.pictures.add(f);
				mediaCount++;
				mediaCountLabel.setText(getString(R.string.picAndVidCount)
						+ mediaCount);
				pushPicture();
			}
		} else if (requestCode == CAMERA_VID_REQUESTED) {
			if (resultCode == RESULT_OK) {
				File f = convertVideoUriToFile(videoUri, this);
				AmusementPark.videos.add(f);
				mediaCount++;
				mediaCountLabel.setText("" + getString(R.string.picAndVidCount)
						+ mediaCount);
				pushVideo();
			}
		}
	}

	// Converts the captured picture's uri to a file that is save-able to the SD
	// Card
	@SuppressLint("NewApi")
	public static File convertImageUriToFile(Uri imageUri) {

		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {

			String[] proj = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID,
					MediaStore.Images.ImageColumns.ORIENTATION };
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			CursorLoader cursorLoader = new CursorLoader(mContext, imageUri,
					proj, selection, selectionArgs, sortOrder);

			Cursor cursor = cursorLoader.loadInBackground();

			int file_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			int orientation_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			if (cursor.moveToFirst()) {
				cursor.getString(orientation_ColumnIndex);
				return new File(cursor.getString(file_ColumnIndex));
			}
			return null;

		} else {

			Cursor cursor = null;
			try {
				String[] proj = { MediaStore.Images.Media.DATA,
						MediaStore.Images.Media._ID,
						MediaStore.Images.ImageColumns.ORIENTATION };
				ContentResolver cr = mContext.getContentResolver();
				cursor = cr.query(imageUri, proj, // Which columns
													// to return
						null, // WHERE clause; which rows to return (all rows)
						null, // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int file_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				int orientation_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
				if (cursor.moveToFirst()) {
					@SuppressWarnings("unused")
					String orientation = cursor
							.getString(orientation_ColumnIndex);
					return new File(cursor.getString(file_ColumnIndex));
				}
				return null;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			// return new File(imageUri.getPath());
		}
	}

	// Converts the recorded video's uri to a file that is save-able to the SD
	// Card
	@SuppressLint("NewApi")
	public static File convertVideoUriToFile(Uri videoUri, Activity activity) {

		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {

			String[] proj = { MediaStore.Video.Media.DATA,
					MediaStore.Video.Media._ID };
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			CursorLoader cursorLoader = new CursorLoader(mContext, videoUri,
					proj, selection, selectionArgs, sortOrder);

			Cursor cursor = cursorLoader.loadInBackground();
			int file_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			if (cursor.moveToFirst()) {
				return new File(cursor.getString(file_ColumnIndex));
			}
			return null;

		} else {

			Cursor cursor = null;

			try {
				String[] proj = { MediaStore.Video.Media.DATA,
						MediaStore.Video.Media._ID };
				ContentResolver cr = mContext.getContentResolver();
				cursor = cr.query(videoUri, proj, // Which columns
													// to return
						null, // WHERE clause; which rows to return (all rows)
						null, // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int file_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				if (cursor.moveToFirst()) {
					return new File(cursor.getString(file_ColumnIndex));
				}
				return null;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

	}

	// Assists with differentiating between displays for dialogues
	private static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	// Adds pictures to the SD Card
	public void pushPicture() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss", Locale.US);
		Date dt = new Date();

		String dateString = sdf.format(dt);

		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}

		for (int i = 0; i < AmusementPark.pictures.size(); i++) {
			File f = AmusementPark.pictures.get(i);
			File newFile = new File(folder, dateString + ".jpeg");
			f.renameTo(newFile);
			AmusementPark.pictures.add(newFile);
		}
		AmusementPark.pictures.clear();
	}

	// Adds videos to the SD Card
	public void pushVideo() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss", Locale.US);
		Date dt = new Date();

		String dateString = sdf.format(dt);

		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}

		for (int i = 0; i < AmusementPark.videos.size(); i++) {
			File f = AmusementPark.videos.get(i);
			File newFile = new File(folder, dateString + ".3gp");
			f.renameTo(newFile);
		}
		AmusementPark.videos.clear();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (AmusementPark.pictures.size() > 0)
			pushPicture();
		if (AmusementPark.videos.size() > 0)
			pushVideo();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	}

}
