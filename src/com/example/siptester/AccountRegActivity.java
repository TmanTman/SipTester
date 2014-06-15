package com.example.siptester;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AccountRegActivity extends ActionBarActivity{
	
	//When this activity starts, loadFieldValues attempt to retreive the values from 
	//sharedpreferences and fill in the default values. 
	//When the "Save and Return" button is hit, the values as in the EditText fields
	//are saved to the sharedpreferences file
	
	//SharedPreferences file
	private static final String PREFS_NAME = "MyPrefsFile";
	
	//Class access to settings
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	
	//EditText windows
	EditText editUsername;
	EditText editPassword;
	EditText editServerIp;
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.account_reg); 
			//Get the SharedPreferences file
			settings = getSharedPreferences(PREFS_NAME, 0);
			editor = settings.edit();
			//Load the buttons on the screen
			editUsername = (EditText)findViewById(R.id.editText1);
			editPassword = (EditText)findViewById(R.id.editText2);
			editServerIp = (EditText)findViewById(R.id.editText3);
	        loadFieldValues();
	    }
	 
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        
	        // Inflate the menu; this adds items to the action bar if it is present.
	        getMenuInflater().inflate(R.menu.dail, menu);
	        return true;
	    }
	
	 
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        // Handle action bar item clicks here. The action bar will
	        // automatically handle clicks on the Home/Up button, so long
	        // as you specify a parent activity in AndroidManifest.xml.
	        int id = item.getItemId();
	        if (id == R.id.action_settings) {
	            return true;
	        }
	        return super.onOptionsItemSelected(item);
	    }
	 
	 public void sipDetailsSave(View v){
		 
		 //Get Username and save to Prefs
		 String username = editUsername.getText().toString();
		 if (username != "")
		 {
			 editor.putString("username", username);
		 }
		//Get ServerIP and save to Prefs
		 String serverip = editServerIp.getText().toString();
		 if (serverip != "")
		 {
			 editor.putString("serverip", serverip);
		 }
		//Get Password and save to Prefs
		 String password = editPassword.getText().toString();
		 if (username != "")
		 {
			 editor.putString("password", password);
		 } 
		 if (!editor.commit()){
				Toast.makeText(this, "Error saving account details", Toast.LENGTH_LONG).show();
			}
		 //Close this Activity and return to Dial Screens
		 Intent intent = new Intent(AccountRegActivity.this, DailActivity.class);
		 startActivity(intent);
	 }
	 
	 private void loadFieldValues() {

		 //Restore the previous values to the text boxes if they exist
		 if (settings.contains("username")) {
			 String username = settings.getString("username", "");
		 	editUsername.setText(username);
		 }
		 if (settings.contains("serverip")) {
			 String serverip = settings.getString("serverip", "");
			 editServerIp.setText(serverip);
		 }
		 if (settings.contains("password")) {
			String password = settings.getString("password", "");
			editPassword.setText(password);
		 }
		 
	 }

}
