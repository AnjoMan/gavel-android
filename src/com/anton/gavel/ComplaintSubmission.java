/*
 * This file is part of Gavel for Android.

    Gavel for Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gavel for Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Gavel for Android.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.anton.gavel;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.util.Log;


public class ComplaintSubmission {
	Map <String,String> post_params = new HashMap<String,String>();
	private Integer responseCode;
	Context mContext;
	
	public Map<String,String> complaintValues = new HashMap<String,String>();
	
		
		
	
	public ComplaintSubmission(GavelMain context, PersonalInfo person, Map<String,String> complaint){
		this.mContext = context;
		responseCode = -1;
		
		//make a map of complaints (as seen in spinner) to complaints (as should be submitted to web)
		//List<String> standardComplaints = new ArrayList<String>(Arrays.asList(mContext.getResources().getStringArray(R.array.complaints)));
		//List<String> complaintSubmitValues = new ArrayList<String>(Arrays.asList(mContext.getResources().getStringArray(R.array.complaint_submit_vals)));
		
		List<String> standardComplaints= ((GavelMain)mContext).getStandardComplaints();
		List<String> complaintSubmitValues = ((GavelMain)mContext).getComplaintSubmitValues();
		Iterator<String> standard = standardComplaints.iterator();
		Iterator<String> submit = complaintSubmitValues.iterator();
		
		while(standard.hasNext() && submit.hasNext())
		  complaintValues.put(standard.next(), submit.next());
		complaintValues.put("Other", "Other");//just in case i use "Other" it should still map
		
		
		
		
		//form fields
		post_params.put("__VIEWSTATE", "dDwxNzUyNDY0MDEzO3Q8O2w8aTwzPjs+O2w8dDw7bDxpPDU+O2k8MTE+O2k8MTM+Oz47bDx0PDtsPGk8MD47aTwyPjs+O2w8dDxwPGw8VGV4dDs+O2w8XDxkaXYgY2xhc3M9InN1Ym5hdiJcPg0KICBcPHVsXD4NCiAgICBcPGxpXD4NCiAgICAgIFw8YSBocmVmPSIvQ2l0eURlcGFydG1lbnRzL0NvbW11bml0eVNlcnZpY2UvIlw+DQogICAgICAgIFw8c3Ryb25nXD5Db21tdW5pdHkgU2VydmljZXNcPC9zdHJvbmdcPg0KICAgICAgXDwvYVw+DQogICAgXDwvbGlcPg0KICAgIFw8dWxcPg0KICAgIFw8L3VsXD4NCiAgICBcPGxpXD4NCiAgICAgIFw8YSBocmVmPSIvQ2l0eURlcGFydG1lbnRzL0NvbnRhY3RVcy8iXD4NCiAgICAgICAgXDxzdHJvbmdcPkNvbnRhY3QgVXNcPC9zdHJvbmdcPg0KICAgICAgXDwvYVw+DQogICAgXDwvbGlcPg0KICAgIFw8dWxcPg0KICAgIFw8L3VsXD4NCiAgICBcPGxpXD4NCiAgICAgIFw8YSBocmVmPSIvQ2l0eURlcGFydG1lbnRzL0NvcnBvcmF0ZVNlcnZpY2VzLyJcPg0KICAgICAgICBcPHN0cm9uZ1w+Q29ycG9yYXRlIFNlcnZpY2VzXDwvc3Ryb25nXD4NCiAgICAgIFw8L2FcPg0KICAgIFw8L2xpXD4NCiAgICBcPHVsXD4NCiAgICBcPC91bFw+DQogICAgXDxsaVw+DQogICAgICBcPGEgaHJlZj0iL0NpdHlEZXBhcnRtZW50cy9FbWVyZ2VuY3lTZXJ2aWNlcy8iXD4NCiAgICAgICAgXDxzdHJvbmdcPkVtZXJnZW5jeSBTZXJ2aWNlc1w8L3N0cm9uZ1w+DQogICAgI" + 
                "CBcPC9hXD4NCiAgICBcPC9saVw+DQogICAgXDx1bFw+DQogICAgXDwvdWxcPg0KICAgIFw8bGlcPg0KICAgICAgXDxhIGhyZWY9Ii9DaXR5RGVwYXJ0bWVudHMvSHVtYW5SZXNvdXJjZXMvIlw+DQogICAgICAgIFw8c3Ryb25nXD5IdW1hbiBSZXNvdXJjZXNcPC9zdHJvbmdcPg0KICAgICAgXDwvYVw+DQogICAgXDwvbGlcPg0KICAgIFw8dWxcPg0KICAgIFw8L3VsXD4NCiAgICBcPGxpXD4NCiAgICAgIFw8YSBocmVmPSIvQ2l0eURlcGFydG1lbnRzL0NpdHlNYW5hZ2VyLyJcPg0KICAgICAgICBcPHN0cm9uZ1w+T2ZmaWNlIG9mIHRoZSBDaXR5IE1hbmFnZXJcPC9zdHJvbmdcPg0KICAgICAgXDwvYVw+DQogICAgXDwvbGlcPg0KICAgIFw8dWxcPg0KICAgIFw8L3VsXD4NCiAgICBcPGxpXD4NCiAgICAgIFw8YSBocmVmPSIvQ2l0eURlcGFydG1lbnRzL1BsYW5uaW5nRWNEZXYvIlw+DQogICAgICAgIFw8c3Ryb25nXD5QbGFubmluZyAmIEVjb25vbWljIERldmVsb3BtZW50XDwvc3Ryb25nXD4NCiAgICAgIFw8L2FcPg0KICAgIFw8L2xpXD4NCiAgICBcPHVsXD4NCiAgICBcPC91bFw+DQogICAgXDxsaVw+DQogICAgICBcPGEgaHJlZj0iL0NpdHlEZXBhcnRtZW50cy9QdWJsaWNIZWFsdGgvIlw+DQogICAgICAgIFw8c3Ryb25nXD5QdWJsaWMgSGVhbHRoICYgU29jaWFsIFNlcnZpY2VzXDwvc3Ryb25nXD4NCiAgICAgIFw8L2FcPg0KICAgIFw8L2xpXD4NCiAgICBcPHVsXD4NCiAgICBcPC91bF" + 
                "w+DQogICAgXDxsaVw+DQogICAgICBcPGEgaHJlZj0iL0NpdHlEZXBhcnRtZW50cy9QdWJsaWNXb3Jrcy8iXD4NCiAgICAgICAgXDxzdHJvbmdcPlB1YmxpYyBXb3Jrc1w8L3N0cm9uZ1w+DQogICAgICBcPC9hXD4NCiAgICBcPC9saVw+DQogICAgXDx1bFw+DQogICAgXDwvdWxcPg0KICBcPC91bFw+DQpcPC9kaXZcPjs+Pjs7Pjt0PHA8bDxUZXh0Oz47bDxcPGRpdiBjbGFzcz0icG9wc3AiXD4NCiAgXDxkaXYgY2xhc3M9InBvcHVsYXJsaW5rcyJcPg0KICAgIFw8aDJcPlBvcHVsYXIgTGlua3NcPC9oMlw+DQogICAgXDx1bFw+DQogICAgICBcPGxpXD4NCiAgICAgICAgXDxhIGhyZWY9Imh0dHA6Ly93d3cuaHBsLmNhLyJcPkhhbWlsdG9uIFB1YmxpYyBMaWJyYXJ5XDwvYVw+DQogICAgICBcPC9saVw+DQogICAgICBcPGxpXD4NCiAgICAgICAgXDxhIGhyZWY9Imh0dHA6Ly93d3cuaGVjZmkuY2EvIlw+SEVDRkkgY29uY2VydHMgYW5kIGV2ZW50c1w8L2FcPg0KICAgICAgXDwvbGlcPg0KICAgICAgXDxsaVw+DQogICAgICAgIFw8YSBocmVmPSJodHRwOi8vd3d3LmludmVzdGluaGFtaWx0b24uY2EvIlw+SW52ZXN0IGluIEhhbWlsdG9uXDwvYVw+DQogICAgICBcPC9saVw+DQogICAgICBcPGxpXD4NCiAgICAgICAgXDxhIGhyZWY9Imh0dHA6Ly93d3cudG91cmlzbWhhbWlsdG9uLmNvbS8iXD5Ub3VyaXNtIEhhbWlsdG9uXDwvYVw+DQogICAgICBcPC9saVw+DQogICAgICBcPGxpXD4NCiAgICA" +
                "gICAgXDxhIGhyZWY9Imh0dHA6Ly93d3cuaGFtaWx0b25wb2xpY2Uub24uY2EvIlw+SGFtaWx0b24gUG9saWNlIFNlcnZpY2VzXDwvYVw+DQogICAgICBcPC9saVw+DQogICAgXDwvdWxcPg0KICBcPC9kaXZcPg0KXDwvZGl2XD47Pj47Oz47Pj47dDxwPHA8bDxJc0NPSENvbnRyb2xDaGFuZ2VkOz47bDxGYWxzZTs+Pjs+O2w8aTwwPjtpPDI+Oz47bDx0PHA8cDxsPFZpc2libGU7PjtsPG88Zj47Pj47PjtsPGk8Mz47PjtsPHQ8dDw7dDxpPDA+O0A8PjtAPD4+Oz47Oz47Pj47dDw7bDxpPDA+Oz47bDx0PDtsPGk8MT47PjtsPHQ8O2w8aTwwPjs+O2w8dDw7bDxpPDU1Pjs+O2w8dDxwPHA8bDxDYXVzZXNWYWxpZGF0aW9uOz47bDxvPHQ+Oz4+Oz47Oz47Pj47Pj47Pj47Pj47Pj47dDw7bDxpPDA+O2k8Mj47PjtsPHQ8cDxsPFRleHQ7PjtsPFw8ZGl2IGNsYXNzPSJmb290ZXIiXD4NCiAgXDxkaXYgY2xhc3M9ImZvb3RuYXYiXD4NCiAgICBcPGEgaHJlZj0iaHR0cDovL3d3dy5oYW1pbHRvbi5jYS9wb2xpY2llcy9UZXJtcyJcPlRlcm1zIG9mIFVzZVw8L2FcPg0KICAgIFw8YSBocmVmPSJodHRwOi8vd3d3LmhhbWlsdG9uLmNhL3BvbGljaWVzL1ByaXZhY3kiXD5Qcml2YWN5IFN0YXRlbWVudFw8L2FcPg0KICAgIFw8YSBocmVmPSJodHRwOi8vd3d3LmhhbWlsdG9uLmNhL3BvbGljaWVzL0FjY2Vzc2liaWxpdHkiXD5BY2Nlc3NpYmlsaXR5IFN0YXRlbWVudFw8L2FcPg0KICAgIFw8YSBocmVmPSJodHRw" +
                "Oi8vd3d3LmhhbWlsdG9uLmNhL0hlbHAvV2ViK0hlbHAvU2l0ZVJlcXVpcmVtZW50Ilw+U2l0ZSBSZXF1aXJlbWVudHNcPC9hXD4NCiAgICBcPGEgaHJlZj0iaHR0cDovL3d3dy5oYW1pbHRvbi5jYS9jb21tb24vQ29udGFjdFVzIlw+Q29udGFjdCBVc1w8L2FcPg0KICAgIFw8YSBocmVmPSJodHRwOi8vd3d3LmhhbWlsdG9uLmNhL2NvbW1vbi9TaXRlTWFwIlw+U2l0ZSBNYXBcPC9hXD4NCiAgICBcPGEgaHJlZj0iaHR0cDovL3d3dy5oYW1pbHRvbi5jYS9IZWxwL0NpdHkrb2YrSGFtaWx0b24rRkFRcyJcPkhlbHAgYW5kIEZBUXNcPC9hXD4NCiAgICBcPGEgdGFyZ2V0PSJfYmxhbmsiIGhyZWY9Imh0dHA6Ly93d3cub250YXJpby5jYS8iXD4NCiAgICAgIFw8aW1nIHNyYz0iaHR0cDovL3d3dy5oYW1pbHRvbi5jYS9IYW1pbHRvbi5Qb3J0YWwvSW5jL0ltYWdlcy9vbnRhcmlvX2xvZ28ucG5nIiBhbHQ9IiIgaWQ9Im9udGFyaW9sb2dvIiBib3JkZXI9IjAiIGhlaWdodD0iMjAiIHdpZHRoPSI2MCJcPg0KICAgIFw8L2FcPg0KICBcPC9kaXZcPg0KICBcPGRpdiBjbGFzcz0idG9wIlw+DQogICAgXDxhIGhyZWY9IiN0b3BvZnBhZ2UiXD5Ub3Agb2YgcGFnZVw8L2FcPg0KICBcPC9kaXZcPg0KICBcPHBcPkNvcHlyaWdodCDCqSAyMDEyIGhhbWlsdG9uLmNhIC0gSGFtaWx0b24sIE9udGFyaW8sIENhbmFkYVw8L3BcPg0KXDwvZGl2XD47Pj47Oz47dDw7bDxpPDA+O2k8MT47aTwyPjs+O2w8dDxwPGw8V" +
                "mlzaWJsZTs+O2w8bzxmPjs+Pjs7Pjt0PHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Ozs+O3Q8cDxsPFZpc2libGU7PjtsPG88Zj47Pj47Oz47Pj47Pj47Pj47Pj47bDxfeHBjTWV0YURhdGE7Pj5OwHs/MekAhEgMO/LfVIAa/1s7iw=="
                );
		post_params.put("COHShell:_ctl0:Button1", "Submit Form");
		post_params.put("idSearchString", "");
		
		
		//user information
		post_params.put("COHShell:_ctl0:qQ_FNAME", person.getField(R.id.first_name));
		post_params.put("COHShell:_ctl0:qQ_LNAME", person.getField(R.id.last_name));
		post_params.put("COHShell:_ctl0:qQ_HOMEPHONE", person.getField(R.id.primary_phone));
		post_params.put("COHShell:_ctl0:qQ_WORKNUMBER", person.getField(R.id.secondary_phone));
		post_params.put("COHShell:_ctl0:qQ_EMAIL", person.getField(R.id.email_address));
		post_params.put("COHShell:_ctl0:qQ_ADDRESS", person.getField(R.id.street_address));
		post_params.put("COHShell:_ctl0:qQ_PCODE", person.getField(R.id.postal_code));
		post_params.put("COHShell:_ctl0:qQ_CITY_CONTACT", person.getField(R.id.personal_cities_spinner));
		
		//complaint information
		post_params.put("COHShell:_ctl0:qQ_VADDRESS", complaint.get("location"));
		post_params.put("COHShell:_ctl0:qQ_CITY", complaint.get("city"));
		post_params.put("COHShell:_ctl0:qQ_VFNAME",complaint.get("firstName"));
		post_params.put("COHShell:_ctl0:qQ_VLNAME", complaint.get("lastName"));
		post_params.put("COHShell:_ctl0:qQ_COMPOLD", "");
		post_params.put("COHShell:_ctl0:qQ_COMP", complaintValues.get(complaint.get("complaint")));//extra mapping from spinner value to web form radio button value
		post_params.put("COHShell:_ctl0:qQ_OCOMP", complaint.get("otherComplaint"));
		post_params.put("COHShell:_ctl0:qQ_COMMENTS", complaint.get("complaintDetails"));
		
		LogComplaint(post_params);
		
	}
	
	private void LogComplaint(Map<String,String> params){
		for (String key : params.keySet())
			Log.d("Runs", "\tfield: " +key + "\n\t\tvalue: "+params.get(key));
	}
	
	public boolean submit() {
		
        
		//Body of your click handler
		Thread trd = new Thread(new Runnable(){
			@Override
			public void run(){
				String formPath = "http://www.hamilton.ca/Hamilton.Portal/Templates/COHShell.aspx?NRMODE=Published&NRORIGINALURL=%2fCityDepartments%2fCorporateServices%2fITS%2fForms%2bin%2bDevelopment%2fMunicipal%2bLaw%2bEnforcement%2bOnline%2bComplaint%2bForm%2ehtm&NRNODEGUID=%7b4319AA7C-7E5E-4D65-9F46-CCBEC9AB86E0%7d&NRCACHEHINT=Guest";
				String hideString = "document.getElementById('mainform').style.display = 'none';";
				 	//this javascript will be added to the page if we are successful - we can look
					//for it to identify whether our form was successfully submitted
				
				// Create a new HttpClient and Post Header
				HttpClient httpClient = new DefaultHttpClient();
				
				ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
				
				for (String key : post_params.keySet())
					parameters.add(new BasicNameValuePair(key, post_params.get(key)));
					//convert to a list of name-value pairs (dum, dee dum  -  duuuuumm...)
				
				HttpPost request = new HttpPost(formPath);
				
				try {
					request.setEntity(new UrlEncodedFormEntity(parameters));
					
					HttpResponse httpResponse = httpClient.execute(request);
					HttpEntity responseEntity = httpResponse.getEntity();
					
					String response = responseEntity==null ? "" : EntityUtils.toString(responseEntity, EntityUtils.getContentCharSet(responseEntity));
						//read the html page posted in response to our request
				
					//appendLog(response); // logs html page response to a text file on sd card
					
					//send results to main thread via handler
					Handler handler = ((GavelMain)mContext).submissionHandler;
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putInt("responseCode", httpResponse.getStatusLine().getStatusCode());// not necessary
					bundle.putBoolean("succeeded", httpResponse.getStatusLine().getStatusCode()==200 && response.contains(hideString));
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				} 
				catch (ClientProtocolException e) {
					Log.d("Runs", "Client Protocol error");
				} catch (IOException e) {
					Log.d("Runs", "IO error");
				}//code to do the HTTP request
				
				
			}
		});
		trd.start();
		
		//
        
	    return this.responseCode == 200;
				
	}
	
	public void appendLog(String text)
	{       
	   File logFile = new File("sdcard/log.txt");
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
	
	
	
	
	

}
