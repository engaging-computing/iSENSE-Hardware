package edu.uml.cs.isense.riverwalk.dialogs;




import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import edu.uml.cs.isense.riverwalk.Main;
import edu.uml.cs.isense.riverwalk.R;

public class Continuous extends Activity {

	public static int continuous_interval;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.continuous_shooting);
		
	if (android.os.Build.VERSION.SDK_INT >= 11)
		setFinishOnTouchOutside(false);	
	
	final CheckBox continuous_cb = (CheckBox) findViewById(R.id.checkContinuous);
	continuous_cb.setChecked(Main.continuous); // if true then continuous_cb will be set to checked if not then it will not be checked
     
	final EditText continuous_time = (EditText) findViewById(R.id.etInterval);
	continuous_time.setText(String.valueOf(Main.continuousInterval));	 
	
	continuous_cb.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (continuous_cb.isChecked()){							//Continuous checkbox is checked
				Main.continuous = true;
				Main.addPicture.setVisibility(View.GONE);
				Main.takePicture.setText(R.string.takePicContinuous);
			}else{													//Continuous checkbox is not checked
				Main.continuous = false;
				Main.addPicture.setVisibility(View.VISIBLE);
				Main.takePicture.setText(R.string.takePicSingle);
			}
		}
	}); 
		
		
		
	
	
	
	
	final Button ok = (Button) findViewById(R.id.description_okay);
	ok.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				Main.continuousInterval = Integer.parseInt(continuous_time.getText().toString());
				if (Main.continuousInterval == 0)
					Main.continuousInterval = 1;
				finish();
			} catch(NumberFormatException e) {
				continuous_time.setError("Please Enter a Value.");
			}
		}
	});	
	}
}
