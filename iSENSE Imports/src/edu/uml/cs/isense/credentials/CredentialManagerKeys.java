package edu.uml.cs.isense.credentials;

import java.util.ArrayList;

import edu.uml.cs.isense.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class CredentialManagerKeys extends Activity {
	
	private Button bAdd;
  	private ListView lvkeys;
  	public ArrayAdapter keysArray;
  	public static ArrayList<String> keys;
  	
	        @Override
	        public void onCreate(Bundle savedInstanceState) {
	            super.onCreate(savedInstanceState);
	            setContentView(R.layout.credential_manager_keys);
	            	keys = readKeys (this, "ContributorKeys");
	        	
	        		bAdd = (Button) findViewById(R.id.button_add);
	        		lvkeys = (ListView) findViewById(R.id.lv_keys);
	        		
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

}
		



