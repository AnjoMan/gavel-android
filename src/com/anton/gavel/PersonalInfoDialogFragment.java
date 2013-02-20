package com.anton.gavel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
public class PersonalInfoDialogFragment extends DialogFragment{
	
	private View dialogView;
	private PersonalInfo mStartingInfo = null;
	
	public PersonalInfoDialogFragment(){
		//nothing!
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		dialogView = inflater.inflate(R.layout.dialog_personal_info, null);
		
	
		Spinner spinner = (Spinner) dialogView.findViewById(R.id.personal_cities_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
		        R.array.cities, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		
		builder.setView(dialogView)
			.setTitle("Personal Information")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int id) {
		                mListener.onDialogPositiveClick(PersonalInfoDialogFragment.this);
		            }
		        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                PersonalInfoDialogFragment.this.getDialog().cancel();
            }
        });
		populatePersonalInfo();
		return builder.create();
	}
	
	public interface PersonalInfoListener {
		public void onDialogPositiveClick(DialogFragment Dialog);
		//public void onDialogNegativeClick(DialogFragment Dialog);
	}
	PersonalInfoListener mListener;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PersonalInfoListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement PersonalInfoListener");
        }
    }
	
	public PersonalInfo getPersonalInfo(){//returns a personal info 
		PersonalInfo info = new PersonalInfo();
		for(int i = 0; i< info.FIELDS.length; i++){
			int key = info.FIELDS[i];
			
			if (key == info.CITY)
				info.setField(key, ((Spinner)dialogView.findViewById(key)).getSelectedItem().toString());
			else
				info.setField(key, ((EditText)dialogView.findViewById(key)).getText().toString());
		}
		return info;
	}
	public void setPersonalInfo(PersonalInfo info){
		mStartingInfo = info;
	}
	public boolean populatePersonalInfo(){
		/* this method populates the views of the dialog form, 
		 * pre-existing value are given to the dialog via passing
		 * a PersonalInfo object via setPersonalInfo()
		 * 
		 * if 'null' is passed into setPersonalInfo(), the fields are
		 * left blank*/
		if (mStartingInfo == null){ return false;}
		else {
			for (int i = 0; i < mStartingInfo.FIELDS.length; i++){
				int key = mStartingInfo.FIELDS[i]; //field key / view id
				
				if (key == mStartingInfo.CITY){
					String cities[] = getActivity().getResources().getStringArray(R.array.cities);
					List<String> Cities = Arrays.asList(cities);
					int position = Cities.indexOf(mStartingInfo.getField(key));
					((Spinner)dialogView.findViewById(key)).setSelection(position);
				} // set selected city for spinner
				else 
					((EditText)dialogView.findViewById(key)).setText(mStartingInfo.getField(key));
				 // set text
			}
			return true;
		}
	}
	

}
