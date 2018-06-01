package edu.bil466.SearchTwitter;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;


public class FavouriteTwitterSearches extends Activity {

	//user's favourite searches
	private SharedPreferences savedSearches;  
	// table that contains tag and edit buttons
	private TableLayout queryTableLayout;   
	private EditText queryEditText;
	private EditText tagEditText;
	private Button saveButton;
	private Button clearButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// load shared prefs file "searches"
		savedSearches = getSharedPreferences("searches", MODE_PRIVATE);
		
		// get references to your GUI elements using the R file
		queryTableLayout = (TableLayout) findViewById(R.id.queryTableLayout);
		tagEditText = (EditText) findViewById(R.id.tagEditText);
		queryEditText = (EditText) findViewById(R.id.queryEditText);
		saveButton = (Button) findViewById(R.id.saveButton);
		clearButton = (Button) findViewById(R.id.clearTags);
		
		// Register you event handler code (listeners)
		saveButton.setOnClickListener(saveButtonListener);
		clearButton.setOnClickListener(clearButtonListener);	
		
		// Add previously saved searches' tag and edit button.
		refreshButtons(null);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	public OnClickListener saveButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			//check whether query or tag fields are empty or not.
			if(queryEditText.getText().length()>0 && tagEditText.getText().length()>0)
			{			
				makeTag(queryEditText.getText().toString(), tagEditText.getText().toString());
				queryEditText.setText("");
				tagEditText.setText("");
				
				//hide the input keyboard
				InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				manager.hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);
			}
			else // Alert the user so that we cannot accept empty strings
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(FavouriteTwitterSearches.this);
				builder.setTitle(R.string.missingAlertTitle);
				builder.setMessage(R.string.missingAlertMessage);
				
				builder.setPositiveButton(R.string.OK, null);
				
				//builder.show();
				
				AlertDialog errorDialog = builder.create();
				errorDialog.show();
			}
			
		}
	};

	public OnClickListener clearButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(FavouriteTwitterSearches.this);
			builder.setTitle(R.string.deleteAllAlertTitle);
			builder.setMessage(R.string.deleteAllAlertMessage);
			
			// This will add an OK button in order to get the confirmation from the user
			builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					// TODO Auto-generated method stub
					clearButtons();
					
					// in order to delete the shared preferences data
					SharedPreferences.Editor editor = savedSearches.edit();
					editor.clear();
					editor.commit();
					
				}
			});
			
			builder.setCancelable(true);
			builder.setNegativeButton(R.string.cancel, null); // no action if the user cancels the delete all request.
			
			AlertDialog confirmDialog = builder.create();
			confirmDialog.show();
			
		}
	};
	
	// remove all buttons for saved searches.
	private void clearButtons()
	{
		queryTableLayout.removeAllViews();
	}

	public OnClickListener queryButtonListener = new View.OnClickListener() {	
		@Override
		public void onClick(View v) {
			String buttonText = ((Button)v).getText().toString();
			String query = savedSearches.getString(buttonText, "");
			
			String urlString = getString(R.string.searchURL) + query;
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
			startActivity(webIntent);
		}
	};
	
	public OnClickListener editButtonListener = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			// we need to know the associated tag of the button.
			//Get a reference to the tablerow view that contains this clicked edit button
			TableRow buttonTableRow = (TableRow)v.getParent();
			Button associatedTagButton = (Button)buttonTableRow.findViewById(R.id.newTagButton);
			
			String tag = associatedTagButton.getText().toString();
			
			tagEditText.setText(tag);
			// Get the associated query of the tag from shared preferences
			String query = savedSearches.getString(tag, "");
			queryEditText.setText(query);
		}
	};
	
	/**
	 * Add or update a new tag-query pair using shared prefs.
	 * Update the GUI accordingly.
	 * @param query
	 * @param tag
	 */
	private void makeTag(String query, String tag)
	{
		String originalQuery = savedSearches.getString(tag, null);
		SharedPreferences.Editor prefereEditor = savedSearches.edit();
		prefereEditor.putString(tag, query);
		prefereEditor.apply();
		
		if(originalQuery == null)
		{
			refreshButtons(tag);
		}
	}
	
	private void addTagGUI(String tag, int index)
	{
		// obtain a layout inflator object/service using the Context.
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// inflate the new tag-query button.
		View newTagView = layoutInflater.inflate(R.layout.new_tag_view, null);
		
		// ontain references to tag and edit buttons and set their event listeners
		Button newTagButton = (Button) newTagView.findViewById(R.id.newTagButton);
		newTagButton.setText(tag);
		newTagButton.setOnClickListener(queryButtonListener);
		
		Button newEditButton = (Button) newTagView.findViewById(R.id.newEditButton);		
		newEditButton.setOnClickListener(editButtonListener);
		
		// finally, add tag-edit buttons to the corresponding index in table layout.
		queryTableLayout.addView(newTagView, index);
	}
	
	
	private void refreshButtons(String newTag)
	{
		String [] tags = savedSearches.getAll().keySet().toArray(new String [0]);
		Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER);
		
		if(newTag != null)
		{
			addTagGUI(newTag, Arrays.binarySearch(tags, newTag));
			
		}
		else
		{
			for(int i=0;i<tags.length;++i)
				addTagGUI(tags[i], i);
		}
	}

}
