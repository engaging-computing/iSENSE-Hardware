package edu.uml.cs.isense.credentials;

import java.util.ArrayList;

import edu.uml.cs.isense.R;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.waffle.Waffle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


public class CredentialManagerKeys extends Activity {
	
	private Button bAdd;
  	private ListView lvkeys;
  	private EditText newkey;
  	public ArrayAdapter keysArray;
  	public static ArrayList<String> keys;
  	private Waffle w;
  	
	private static final int PROJECT_REQUESTED = 104;

  	
	        @Override
	        public void onCreate(Bundle savedInstanceState) {
	            super.onCreate(savedInstanceState);
	            setContentView(R.layout.credential_manager_keys);
	            w = new Waffle(this.getApplicationContext());
	            	keys = readKeys (this, "ContributorKeys");
	        		final Button cancel = (Button) findViewById(R.id.button_cancel);
//	        		final Button bAdd = (Button) findViewById(R.id.button_add);
//	        		lvkeys = (ListView) findViewById(R.id.lv_keys);
//	        		newkey = (EditText) findViewById(R.id.edittext_key);

	        		// This is the array adapter, it takes the context of the activity as a 
	                // first parameter, the type of list view as a second parameter and your 
	                // array as a third parameter.
	                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, keys);
	                lvkeys.setAdapter(arrayAdapter); 
	                registerForContextMenu(lvkeys);

	                /*user clicks on a key in listview*/
	                lvkeys.setOnItemClickListener(new OnItemClickListener() {

						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							
						}
	                });
	        		
	        		bAdd.setOnClickListener(new OnClickListener(){

						public void onClick(View v) {
							if (newkey.length() != 0) {
							Intent iproject = new Intent(getApplicationContext(),
									Setup.class);
							iproject.putExtra("constrictFields", true);
							iproject.putExtra("app_name", "Pictures");
							startActivityForResult(iproject, PROJECT_REQUESTED);
							} else {
								w.make("Empty Key", Waffle.LENGTH_SHORT,
										Waffle.IMAGE_X);
							}
						}
			
	        		});
	        		
	        	}
	        
	        	/*menu to delete a key*/
//	        	@Override
//	        	public void onCreateContextMenu(ContextMenu menu, View v,
//	        	    ContextMenuInfo menuInfo) {
//	        		super.onCreateContextMenu(menu, v, menuInfo);
//	        		 
//	    	        MenuInflater inflater = getMenuInflater();
//	    	        inflater.inflate(R.menu.key_menu, menu);
//	        	}
//	        	
//	        	/*Remove a Key*/
//	        	@SuppressWarnings("unchecked")
//				@Override
//	        	public boolean onContextItemSelected(MenuItem item) {
//	        	      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//	        	      switch(item.getItemId()) {
//	        	         case R.id.MENU_ITEM_DELETE:
//	        	        	 keysArray.remove(info.position);
//	        	        	 ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
//	        	             lvkeys.setAdapter(arrayAdapter); 
//	        	             registerForContextMenu(lvkeys);
//	        	             
//	        	             return true;
//	        	         
//	        	          default:
//	        	        	  return super.onContextItemSelected(item);
//	        	      }
//	        	}
	        	
	        	public static ArrayList<String> readKeys (Context context, String prefix) {
	        	    SharedPreferences prefs = context.getSharedPreferences("ContributorKeys", Context.MODE_PRIVATE);

	        	    int size = prefs.getInt(prefix+"_size", 0);
	        	    ArrayList<String> data = new ArrayList<String>(size);

	        	    for(int i=0; i<size; i++)
	        	        data.add(prefs.getString(prefix+"_"+i, null));
	        	    
	        	    return data;

	        	}


	        	public static void writeKeys(Context context, ArrayList<String> list, String prefix){
	        	    SharedPreferences prefs = context.getSharedPreferences("ContributorKeys", Context.MODE_PRIVATE);
	        	    SharedPreferences.Editor editor = prefs.edit();

	        	    int size = prefs.getInt(prefix+"_size", 0);

	        	    // clear the previous data if exists
	        	    for(int i=0; i<size; i++)
	        	        editor.remove(prefix+"_"+i);

	        	    // write the current list
	        	    for(int i=0; i<list.size(); i++)
	        	        editor.putString(prefix+"_"+i, list.get(i));
	        	    
	        	    editor.putInt(prefix+"_size", list.size());
	        	    editor.commit();
	        	}
	        	
	        	@Override
	        	protected void onActivityResult(int requestCode, int resultCode, Intent data) { // passes in a request code
	        																					
	        		super.onActivityResult(requestCode, resultCode, data);

	        		if (requestCode == PROJECT_REQUESTED) { 
	        			if (resultCode == Activity.RESULT_OK) {
	        				SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
	        				String eidString = mPrefs.getString("project_id", "");
	        			
	        			}
	        		
	        		}
	        		
	        	}

	        	
	        	
	        	

}
		



