
package com.exoplatform.weather.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.exoplatform.weather.R;
import com.exoplatform.weather.model.WeatherDataModel;
import com.exoplatform.weather.model.WeatherPreferences;


public class ActivityScreenLocation extends Activity implements OnClickListener {
	/** For debugging */
	private static final String TAG = "ActivityScreenLocation";

	/** Querry to get location */
	private static final String GET_LOCATION_WOEID = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.places%20where%20text%3D%22";	
	
	/** Location error*/
	private static final int LOCATION_ERROR = -1;
	
	/** Locattion OK */
	private static final int LOCATION_OK = 0;
	
	/** NO WOEID */
	private static final int LOCATION_NOWOEID = 1;
	
	/** Get data failed */
	private static final int LOCATION_GET_FAILED = 2;
	
	/** Search button */
	private Button m_btnSearch;
	
	/** Input country */
	private EditText m_Country;
	
	/** Input city */
	private EditText m_City;
	
	/** Data model */
	private WeatherDataModel m_DataModel;
	
	/** Preference */
	private WeatherPreferences m_Preference;
	
	/** Dialog */
	ProgressDialog m_Dialog;
	

	/*********************************************************
	 * Call when first create activity
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * @author DatNQ
	 *********************************************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_location);

		boolean bResult = false;
		bResult = initializeData();
		if (bResult == true) {
			initializeView();
		}

		if (bResult == false) {
			Log.e(TAG,"onCreate Error");
		}
		
		/* Draw screen */
		drawScreen();
	}

	/*********************************************************
	 * Initialize view element
	 * 
	 * @return true: initialize data success false: initialize data false
	 * @author DatNQ
	 ********************************************************/
	private boolean initializeView() {
		boolean bResult = true;

		m_btnSearch = (Button) findViewById(R.id.btnSearch);
		m_Country = (EditText) findViewById(R.id.inputCountry);
		m_City = (EditText) findViewById(R.id.inputCity);

		if ( (m_btnSearch == null) || (m_Country == null) || (m_City == null)){
			Log.e(TAG,"initialize view error");
			bResult = false;
		}
		
		m_btnSearch.setOnClickListener(this);
		
		return bResult;
	}

	/*********************************************************
	 * Initialize data
	 * 
	 * @return true: initialize data success false: initialize data false
	 * @author DatNQ
	 ********************************************************/
	private boolean initializeData() {
		boolean bResult = true;
		
		m_DataModel = WeatherDataModel.getInstance();
		if (m_DataModel == null){
			Log.e(TAG,"Init data failed");
			return false;
		}
		
		m_Preference = WeatherPreferences.getInstance(getApplicationContext());
		if (m_Preference == null){
			Log.e(TAG,"Can not get preference");
			return false;
		}

		return bResult;
	}

	/*********************************************************
	 * Draw all element of screen
	 * 
	 * @author DatNQ
	 *********************************************************/
	private void drawScreen() {
		drawTitle();

	}

	/*********************************************************
	 * Draw title of screen
	 * 
	 * @author DatNQ
	 *********************************************************/
	private void drawTitle() {
	    setTitle(R.string.strSettingLocationTitle);
	}
	
	/***************************************************************************
	 * Get data by locatin
	 * @date May 9, 2011
	 * @time 7:54:47 AM
	 * @author DatNQ
	 **************************************************************************/
	private int getDataByLocatition(){
		String strLocationQuerry = createQueryByCityCountry();
		if (strLocationQuerry == null){
			Log.e(TAG,"Querry invalid");
			return LOCATION_ERROR;
		}
		
		String strWOEID = m_DataModel.getWOEIDByLocation(strLocationQuerry);
		if (strWOEID != null){
			m_Preference.setLocation(strWOEID);
			return LOCATION_OK;
		}
		
		return LOCATION_ERROR;
	}
	
	/***************************************************************************
	 * Create query by city and country
	 * 
	 * @return Query String
	 * @date May 9, 2011
	 * @time 8:02:29 AM
	 * @author DatNQ
	 **************************************************************************/
	private String createQueryByCityCountry(){
		StringBuffer strQuerryBuf = new StringBuffer();
		
		strQuerryBuf.append(m_Country.getText().toString().trim());
		strQuerryBuf.append(" ");
		strQuerryBuf.append(m_City.getText().toString().trim());

		return strQuerryBuf.toString().trim();
	}
	
	/***************************************************************************
	 * Create query to get WOEID
	 * @param strQuerry Location
	 * @return
	 * @date May 9, 2011
	 * @time 9:28:07 PM
	 * @author DatNQ
	 **************************************************************************/
	public static String createQuerryGetWoeid(String strQuerry){
		if (strQuerry == null){
			return null;
		}
		
		StringBuffer strQuerryBuf = new StringBuffer(GET_LOCATION_WOEID);
		strQuerryBuf.append(strQuerry);
		strQuerryBuf.append("%22&format=xml");

		return strQuerryBuf.toString();				
	}


	/***************************************************************************
	 * When button press
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * @author DatNQ
	 **************************************************************************/
	public void onClick(View arg0) {
		String strMsg = getString(R.string.strOnSearching);
		m_Dialog = ProgressDialog.show(ActivityScreenLocation.this, "",
				strMsg, true);	
		
		int nGetResult = getDataByLocatition();
		Intent changeResult = new Intent();
		switch (nGetResult){
		case LOCATION_OK:
			setResult(RESULT_OK, changeResult);
			break;
			
		case LOCATION_ERROR:
		case LOCATION_GET_FAILED:
		case LOCATION_NOWOEID:
			setResult(RESULT_CANCELED, changeResult);
			default:
				
		}
		
		m_Dialog.dismiss();
		finish();
	}
}