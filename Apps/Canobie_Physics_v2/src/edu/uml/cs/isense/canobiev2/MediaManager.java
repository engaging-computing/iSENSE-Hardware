package edu.uml.cs.isense.canobiev2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.waffle.Waffle;

public class MediaManager extends Activity {

	private static Uri imageUri;

	private static final int CAMERA_PIC_REQUESTED = 1;
	private static final int UPLOAD_PIC_REQUESTED = 2;

	private static Waffle w;
	private static Context mContext;
	private static Button takePic;
	private static Button uploadPic;
	private static Button back;
	
	private static File f;
	
	private static boolean status400 = false;
	private static boolean uploadError = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_manager);

		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		mContext = this;
		w = new Waffle(mContext);

		takePic = (Button) findViewById(R.id.mediaPicture);
		uploadPic = (Button) findViewById(R.id.mediaUpload);
		
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
		
		uploadPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						UPLOAD_PIC_REQUESTED);
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
				f = convertImageUriToFile(imageUri);
				pushPicture();
				new UploadTask().execute();
			}
		} else if (requestCode == UPLOAD_PIC_REQUESTED) {
			if (resultCode == RESULT_OK) {
				Uri selectedImageUri = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImageUri,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();
				pushPicture();
				new UploadTask().execute();

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

	// Assists with differentiating between displays for dialogues
	private static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	// Adds pictures to the SD Card
	public void pushPicture() {
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}
		
		AmusementPark.uq.buildQueueFromFile();
	}
	
	
	// Adds videos to the SD Card
	public void pushVideo() {
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}
		
		AmusementPark.uq.buildQueueFromFile();
	}

	private class UploadTask extends AsyncTask<Void, Integer, Void> { // adds
		// picture
		// to
		// queue

	@Override
	protected void onPreExecute() {
	}
	
	@Override
	protected Void doInBackground(Void... voids) {
	
	uploader.run();
	publishProgress(100);
	
	return null;
	}
	
	@Override
	protected void onPostExecute(Void voids) {
	
	// dia.cancel();
	
	if (status400) {
	w.make("Your data cannot be uploaded to this project.  It has been closed.",
	Waffle.LENGTH_LONG, Waffle.IMAGE_X);
	} else if (uploadError) {
	// Do nothing - postRunnableWaffleError takes care of this
	// Waffle
	} else {
	w.make("Media saved!", Waffle.LENGTH_LONG, Waffle.IMAGE_CHECK);
	}
	
	AmusementPark.uq.buildQueueFromFile();
	
	uploadError = false;
	}
}
	
	private Runnable uploader = new Runnable() {
		@Override
		public void run() {

			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
			String projNum = mPrefs.getString("project_id", "Error");

			// if (dfm == null)

			JSONArray dataJSON = new JSONArray(); // data is set into JSONArray
													// to be uploaded

			if (!Connection.hasConnectivity(mContext))
				projNum = "-1";
			
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss", Locale.US);
			Date dt = new Date();
			String dateString = sdf.format(dt);
			
			QDataSet ds;
				
			/*create a dataset with picture and add it to queue*/
			ds = new QDataSet(dateString,
					"Media", QDataSet.Type.PIC,
					null, f, projNum, null);

			System.out.println("projectNum = " + projNum);

			AmusementPark.uq.addDataSetToQueue(ds);
		}
	};
	

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	}

}
