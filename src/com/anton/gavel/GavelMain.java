package com.anton.gavel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.anton.gavel.PersonalInfoDialogFragment.PersonalInfoListener;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class GavelMain extends SherlockFragmentActivity implements PersonalInfoListener, OnItemSelectedListener, OnItemLongClickListener, OnClickListener{
	private static final int DIALOG_PI = 1;
	private static final int DIALOG_ABOUT = 2;
	private static final int DIALOG_SUBMISSION_ERR = 3;
	private static final int DIALOG_OTHER_COMPLAINT = 4;
	private Spinner complaintSpinner;
	private ArrayAdapter<String> complaintsAdapter;
	
	
	
	private PersonalInfo mPersonalInfo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gavel_main);

        
		// set up edit text input style for complaints (multiline, capitalize sentences)
		EditText edit = (EditText) findViewById(R.id.complaint_body);
		edit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		// set up cities spinner
		Spinner citiesSpinner = (Spinner) findViewById(R.id.cities_spinner);
		ArrayAdapter<CharSequence> citiesAdapter = ArrayAdapter.createFromResource(this,
		        R.array.cities, android.R.layout.simple_spinner_item);
		citiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		citiesSpinner.setAdapter(citiesAdapter);
		
		// set up complaint spinner
		List<String> complaints_list = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.complaints)));
		
		complaintSpinner = (Spinner) findViewById(R.id.complaint_spinner);
		complaintsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, complaints_list);
		complaintsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		complaintSpinner.setAdapter(complaintsAdapter);
		complaintSpinner.setOnItemSelectedListener(this);
		complaintSpinner.setOnItemLongClickListener(this);// this doesn't actually work yet bc not supported by API - events don't get fired
		

		//attach location listener to button
		findViewById(R.id.location_button).setOnClickListener(this);
		
		// make link in disclaimer clickable
		TextView disclaimer = (TextView) findViewById(R.id.disclaimer_textview);
		disclaimer.setMovementMethod(LinkMovementMethod.getInstance());
		
		// check &or load shared preferences to populate saved personal information
		mPersonalInfo = new PersonalInfo();
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		mPersonalInfo.loadFromPreferences(preferences);
		
			
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.activity_gavel_main,  menu);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case R.id.menu_personal_info:
				this.createDialog(DIALOG_PI);
				return true;
			case R.id.menu_about:
				this.createDialog(DIALOG_ABOUT);
				return true;
			case R.id.menu_send_complaint:
				
				if (mPersonalInfo.isComplete()){
					Map<String,String> complaintMap = new HashMap<String,String>();
					complaintMap.put("firstName", ((EditText)findViewById(R.id.complaint_first_name)).getText().toString());
					complaintMap.put("secondName", ((EditText)findViewById(R.id.complaint_last_name)).getText().toString());
					complaintMap.put("location", ((EditText)findViewById(R.id.complaint_address)).getText().toString());
					complaintMap.put("city", ((Spinner)findViewById(R.id.cities_spinner)).getSelectedItem().toString());
					complaintMap.put("complaintDetails", ((EditText)findViewById(R.id.complaint_body)).getText().toString());
					
					//Check if complaint is 'other' and assign accordingly
					List<String> standardComplaints = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.complaints)));
					
					String complaint = ((Spinner)findViewById(R.id.complaint_spinner)).getSelectedItem().toString();
					String otherComplaint = "";
					if (!standardComplaints.contains(complaint)){
						otherComplaint = complaint;
						complaint = "Other";					
					}// if complaint is not in the standard list, submit as an 'otherComplaint'; else submit "" as 'otherComplaint
					
					complaintMap.put("complaint", complaint);
					complaintMap.put("otherComplaint",otherComplaint);
					
					ComplaintSubmission submission = new ComplaintSubmission(this,mPersonalInfo, complaintMap);
					Boolean successful = submission.submit();
					
					
				} else {
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
					alertDialog.setTitle("Incomplete")//title
						.setMessage("Your personal information is incomplete. Select 'Edit Personal Information' from the menu and fill in all required fields")//insert textview from above
						.setPositiveButton("Done", new DialogInterface.OnClickListener() {
				            @Override public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }})	
						.setIcon(R.drawable.ic_launcher)	
						.show(); //display
					
				}
				
				return true;
				
		    default:
		    	return true;
		}
	}
	
	Handler submissionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			Integer response = bundle.getInt("responseCode");
			  
			if (response.equals(200)){
				//refresh activity
				finish();
				startActivity(getIntent());

			} else {
				createDialog(DIALOG_SUBMISSION_ERR);
				
				
			}
			  
		}
	};
	
	
	public void createDialog(int id){
		switch (id){
		case DIALOG_PI:
			//call custom dialog for collecting Personal Info
			PersonalInfoDialogFragment personalInfoDialog = new PersonalInfoDialogFragment();
			personalInfoDialog.setPersonalInfo(mPersonalInfo);
			personalInfoDialog.show(getSupportFragmentManager(), "PersonalInfoDialogFragment"); 
	        break;
		case DIALOG_ABOUT:	
			//construct a simple dialog to show text
			
			//get about text
			TextView aboutView = new TextView(this);
			aboutView.setText(R.string.about_text);
			aboutView.setMovementMethod(LinkMovementMethod.getInstance());//enable links
			aboutView.setPadding(50, 30, 50, 30);
			
			//build dialog
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
			alertDialog.setTitle("About")//title
				.setView(aboutView)//insert textview from above
				.setPositiveButton("Done", new DialogInterface.OnClickListener() {
		            @Override public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }})	
				.setIcon(R.drawable.ic_launcher)	
				.create() //build
				.show(); //display
			break;
			
		case DIALOG_SUBMISSION_ERR:
			AlertDialog.Builder submissionErrorDialog = new AlertDialog.Builder(this);
			submissionErrorDialog.setTitle("Submission Error")//title
				.setMessage("There was a problem submitting your complaint on the City's website.")//insert textview from above
				.setPositiveButton("Done", new DialogInterface.OnClickListener() {
		            @Override public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }})	
				.setIcon(R.drawable.ic_launcher)	
				.show(); //display
			break;
			
		case DIALOG_OTHER_COMPLAINT:
			break;
		}
		
		
		
	}

	@Override
	public void onDialogPositiveClick(DialogFragment Dialog) {
		//this method handles return of Personal Information dialog
		
		if (Dialog instanceof PersonalInfoDialogFragment){
			mPersonalInfo = ((PersonalInfoDialogFragment)Dialog).getPersonalInfo();
			//Log.d("Runs", mPersonalInfo.toString());
			
			// save personal information to file.
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			mPersonalInfo.saveToPreferences(preferences);
		}
	}
	/*
	@Override
	public void onDialogNegativeClick(DialogFragment Dialog) {
		// TODO Auto-generated method stub	
	} */ //method to handle 'cancel' of Personal Information Dialog

	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// handles clicks on complaintSpinner Items - get/add new complaint
		
		if (complaintSpinner.getSelectedItem().toString().equals("Other...")){
			final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			final EditText input = new EditText(this); 
			input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_CLASS_TEXT);
				// capitalize letters + seperate words
			AlertDialog.Builder getComplaintDialog = new AlertDialog.Builder(this);                 
			getComplaintDialog.setTitle("Other...")
							.setView(input)
							.setMessage("Give a categorical title for your complaint:")
			 				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
							    public void onClick(DialogInterface dialog, int whichButton) {  
							        String value = input.getText().toString();
							        // add the item to list and make it selected
							        complaintsAdapter.insert(value, 0);
							        complaintSpinner.setSelection(0,true);
							        complaintsAdapter.notifyDataSetChanged();
							        
							        imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
							        	//hide keyboard
							        return;                  
							    }  
			 				})
							.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						        public void onClick(DialogInterface dialog, int which) {
						        	imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
						        		//hide keyboard						        	
						        	dialog.cancel(); }
						    })
						    .create();
			 				
			input.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);// show keyboard
            
            getComplaintDialog.show();//show dialog
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position,
			long arg3) {
		//thiswait for response from thread method doesn't do anything because Spinners don't support long clicks (last known Dec, 2012)
		AlertDialog.Builder deleteItem = new AlertDialog.Builder(this);
		deleteItem.setTitle("Delete")
				.setMessage("Delete '" + complaintsAdapter.getItem(position) + "'?" )
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
				    public void onClick(DialogInterface dialog, int whichButton) {  
				        complaintsAdapter.remove(complaintsAdapter.getItem(position));     
				    }  
					})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }
			    })
			    .show();
		
		return false;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onClick(View arg0) {
		//click listener for location button
		
	    
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		//String provider = locationManager.getBestProvider(criteria, true);
		
		locationManager.requestSingleUpdate(criteria, new LocationListener(){
			@Override
			public void onLocationChanged(Location location) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
		            // Invoking reverse geocoding in an AsyncTask. 
		            (new ReverseGeocodingTask(getBaseContext())).execute(new Location[] {location});
				}
			
			}
			@Override public void onProviderDisabled(String provider) { }
			@Override public void onProviderEnabled(String provider) { }
			@Override public void onStatusChanged(String provider, int status, Bundle extras) { }
			
		}, null);
		
		
		
	}
	
	// AsyncTask encapsulating the reverse-geocoding API.  Since the geocoder API is blocked,
	// we do not want to invoke it from the UI thread.
	private class ReverseGeocodingTask extends AsyncTask<Location, Void, List<Address> >{
	    Context mContext;

	    public ReverseGeocodingTask(Context context) {
	        super();
	        mContext = context;
	    }
	    
	    @Override
	    protected List<Address> doInBackground(Location... params) {
	        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

	        Location loc = params[0];
	        List<Address> addresses = null;
	        try {
	            // Call the synchronous getFromLocation() method by passing in the lat/long values.
	            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        if (addresses != null && addresses.size() > 0) {
	            Address address = addresses.get(0);
	            // Format the first line of address (if available), city, and country name.
	            String addressText = String.format("%s, %s, %s",
	                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
	                    address.getLocality(),
	                    address.getCountryName());
	        }
	        return addresses;
	    }
	    
	    protected void onPostExecute(List<Address> result){
	    	
	    	String address = "";
	    	if (result != null){
		    	for (int i = 0; i <= result.get(0).getMaxAddressLineIndex(); i++){
		    		address += " " + result.get(0).getAddressLine(i);
		    	}
		    	address.trim();
		    	
	    	} else {address = "failed";}// if address == null, output 'failed', since dialogs don't work from here
	    	
	    	((EditText)findViewById(R.id.complaint_address)).setText(address);
	    }
	}


	
	
	

}
