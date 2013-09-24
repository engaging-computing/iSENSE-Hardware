package edu.uml.cs.isense.collector.splash;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.objects.RProjectField;
import edu.uml.cs.isense.waffle.Waffle;

public class ProjectCreate extends Activity {

	public static Context mContext;
	public static Waffle w;
	
	private API api;
	
	private Spinner fieldSpin;
	private LinearLayout fieldScroll;
	private ScrollView fieldScrollHolder;
	
	private static final int FIELD_TYPE_TIMESTAMP 		= 0;
	private static final int FIELD_TYPE_NUMBER	  		= 1;
	private static final int FIELD_TYPE_TEXT			= 2;
	private static final int FIELD_TYPE_LOCATION		= 3;
	
	private int locationCount   = 0;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_create);

		mContext = this;
		w = new Waffle(mContext);
		
		api = API.getInstance(mContext);
		api.useDev(true);
		
		// Action bar customization for API >= 14
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			ActionBar bar = getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#111133")));
			bar.setIcon(getResources()
					.getDrawable(R.drawable.rsense_logo_right));
			bar.setDisplayShowTitleEnabled(false);
			int actionBarTitleId = Resources.getSystem().getIdentifier(
					"action_bar_title", "id", "android");
			if (actionBarTitleId > 0) {
				TextView title = (TextView) findViewById(actionBarTitleId);
				if (title != null) {
					title.setTextColor(Color.WHITE);
					title.setTextSize(24.0f);
				}
			}
		}
		
		final EditText projectName = (EditText) findViewById(R.id.project_create_name);

		// Set listeners for the buttons
		final Button cancel = (Button) findViewById(R.id.project_create_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		final Button ok = (Button) findViewById(R.id.project_create_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				final ArrayList<RProjectField> fields = new ArrayList<RProjectField>();
				
				for (int i = 0; i < fieldScroll.getChildCount(); i++) {
					View child = fieldScroll.getChildAt(i);
					RProjectField field = new RProjectField();
					EditText fieldName = (EditText) child.findViewById(R.id.project_field_name);
					EditText units	   = (EditText) child.findViewById(R.id.project_field_units);
					
					switch ((Integer) child.getTag()) {
					case FIELD_TYPE_TIMESTAMP:
						field.name = fieldName.getText().toString();
						field.type = RProjectField.TYPE_TIMESTAMP;
						field.unit = "milliseconds"; // TODO - should there be milliseconds?
						fields.add(field);
						break;
						
					case FIELD_TYPE_NUMBER:
						field.name = fieldName.getText().toString();
						field.type = RProjectField.TYPE_NUMBER;
						field.unit = units.getText().toString();
						fields.add(field);
						break;
						
					case FIELD_TYPE_TEXT:
						field.name = fieldName.getText().toString();
						field.type = RProjectField.TYPE_TEXT;
						field.unit = "";	// TODO - should we even set to blank?
						fields.add(field);
						break;
						
					case FIELD_TYPE_LOCATION:
						field.name = "Latitude";
						field.type = RProjectField.TYPE_LAT;
						field.unit = "deg";
						fields.add(field);
						
						field = new RProjectField();
						field.name = "Longitude";
						field.type = RProjectField.TYPE_LON;
						field.unit = "deg";
						fields.add(field);

						break;
						
					default:
						break;
					}
				}
				
				Thread uploadThread = new Thread() {
			        public void run(){
			            try {
			            	api.createSession("mobile", "mobile");
							api.createProject(projectName.getText().toString(), fields); 
			            } catch (Exception e) {
			                e.printStackTrace();
			            }
			        }
			    };
			    uploadThread.start();
			}
		});
		
		final Button addField = (Button) findViewById(R.id.project_create_add_field_button);
		addField.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = fieldSpin.getSelectedItemPosition();
				
				if (position == FIELD_TYPE_LOCATION)
					if (locationCount == 0)
						locationCount++;
					else {
						w.make("Cannot add more than one location.",
								Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);
						return;
					}
			
				addFieldType(position);
			}
			
		});
		
		fieldScrollHolder 	= (ScrollView) 		findViewById(R.id.project_create_fields_scroll);
		fieldSpin 			= (Spinner) 		findViewById(R.id.project_create_fields_spinner);
		fieldScroll 		= (LinearLayout) 	findViewById(R.id.project_create_fields_view);

	}
	
	private void addFieldType(int tag) {
		
	    final View v = (tag == FIELD_TYPE_LOCATION) 
	    		? View.inflate(mContext, R.layout.project_field_location, null) 
	    		: View.inflate(mContext, R.layout.project_field, null);;
		
		v.setTag(tag);
		
		EditText fieldName = (EditText) v.findViewById(R.id.project_field_name);
		EditText units	   = (EditText) v.findViewById(R.id.project_field_units);
		
		if (tag != FIELD_TYPE_LOCATION) {
			fieldName.setImeOptions(EditorInfo.IME_ACTION_DONE);
			fieldName.setSingleLine(true);
			units.setImeOptions(EditorInfo.IME_ACTION_DONE);
			units.setSingleLine(true);
		}
		
		switch(tag) {
		case FIELD_TYPE_TIMESTAMP:
			fieldName.setText("Timestamp");
			units.setVisibility(View.INVISIBLE);
			break;
			
		case FIELD_TYPE_NUMBER:
			fieldName.setHint("number");
			break;
			
		case FIELD_TYPE_TEXT:
			fieldName.setHint("text");
			units.setVisibility(View.INVISIBLE);
			break;
			
		case FIELD_TYPE_LOCATION:
			break;
			
		default:
			break;
		
		}
		
		ImageView x = (ImageView) v.findViewById(R.id.project_field_x);
		x.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				fieldScroll.removeView(v);
				if ((Integer) v.getTag() == FIELD_TYPE_LOCATION) {
					locationCount--;
				}
			}
		});
		
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutParams.setMargins(1, 1, 1, 1);
		
		fieldScroll.addView(v, layoutParams);
		scrollDown();
	}
	
	private void scrollDown() {
	    Thread scrollThread = new Thread(){
	        public void run(){
	            try {
	                sleep(200);
	                ProjectCreate.this.runOnUiThread(new Runnable() {
	                    public void run() {
	                        fieldScrollHolder.fullScroll(View.FOCUS_DOWN);
	                    }    
	                });
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    };
	    scrollThread.start();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}