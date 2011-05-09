/******************************************************************************
 * Class       : ActivityWeatherSetting.java				         		  *
 * Main Weather activity, in this demo apps i use API from yahoo, you can     *
 * use other weather web service which you prefer                             *
 *                                                                            *
 * Version     : v1.0                                                         *
 * Date        : May 06, 2011                                                 *
 * Copyright (c)-2011 DatNQ some right reserved                               *
 * You can distribute, modify or what ever you want but WITHOUT ANY WARRANTY  *
 * Be honest by keep credit of this file                                      *
 *                                                                            *
 * If you have any concern, feel free to contact with me via email, i will    *
 * check email in free time                                                   * 
 * Email: nguyendatnq@gmail.com                                               *
 * ---------------------------------------------------------------------------*
 * Modification Logs:                                                         *
 *   KEYCHANGE  DATE          AUTHOR   DESCRIPTION                            *
 * ---------------------------------------------------------------------------*
 *    -------   May 06, 2011  DatNQ    Create new                             *
 ******************************************************************************/
package com.exoplatform.weather.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.exoplatform.weather.R;
import com.exoplatform.weather.model.WeatherDataModel;
import com.exoplatform.weather.model.WeatherInfo;
import com.exoplatform.weather.model.WeatherPreferences;
import com.exoplatform.weather.model.YahooWeatherHelper;

/*******************************************************************************
 * Purpose of this class for Activity setting yahoo weather service
 * First of all with this requirement i think Widget is better (From user view)
 * but currently i have a litter free time, so i create only Activity version
 * 
 * I hope we will have widget version in near future when i have enough free time
 * @author DatNQ
 *
 ******************************************************************************/
public class ActivityWeatherSetting extends Activity {
	
	/** TAG, for debugging */
	private static final String TAG = "ActivityYahoo";
	
	/** Dialog type */
	private static final int DIALOG_TYPE_USER_AGREEMENT = 1;
	
	/** Change location */
	private static final int REG_CHANGELOCATION = 1;
	
	/** Weather infomation */
	private WeatherInfo m_WeatherInfo;
	
	/** Weather setting */
	private WeatherPreferences m_Preferneces;
	
	/** Model data */
	private WeatherDataModel m_DataModel;
	
	/** Location */
	private TextView m_TextLocation;
	
	/** Temperature */
	private TextView m_Temperature;
	
	/** Humimidy */
	private TextView m_Humimidy;
	
	/** State */
	private TextView  m_Visibility;
	
	/** Time */
	private TextView m_Date;
	
	/** Icon */
	private ImageView m_WeatherIcon;
	
	
	/***************************************************************************
	 * On create called when start weather setting activity
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * @date May 7, 2011
	 * @time 5:17:48 PM
	 * @author DatNQ
	 **************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_setting);
        
        /* Initialize data */
        boolean bResult = initializeData();
        
        if (bResult != false ){
        	/* Initialize view */
        	bResult = initializeView();
        }
        
        if (bResult == false){
        	Log.e(TAG,"Init data failed");
        	/* Add notify here and quit app */
        	finish();
        	return;
        }
        
		if (m_Preferneces.getAcceptAgreement() == false){
			showDialog(DIALOG_TYPE_USER_AGREEMENT);
		} else {
			/* Draw screen */
			drawWeatherScreen();
		}        
    }
    
    /***************************************************************************
     * Create Dialog
     * @see android.app.Activity#onCreateDialog(int)
     * @date May 7, 2011
     * @time 5:18:50 PM
     * @author DatNQ
     **************************************************************************/
	@Override
	protected Dialog onCreateDialog(int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.str_user_agreement);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.str_agree, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	   m_Preferneces.setAccpetAgreement(true);
   			/* Draw screen */
   			drawWeatherScreen();
           }
        });
        builder.setNegativeButton(R.string.str_disagree, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                System.exit(0);
           }
        });
        AlertDialog alert = builder.create();
        return alert;
	 }    
    
	/***************************************************************************
	 * Create option menu
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * @date May 7, 2011
	 * @time 8:58:39 PM
	 * @author DatNQ
	 **************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        /* Apply custom menu */
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_weather_screen, menu);

        return true;
    }
    
    /***************************************************************************
     * Handle option menu
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     * @date May 7, 2011
     * @time 8:58:13 PM
     * @author DatNQ
     **************************************************************************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int nSelectID = item.getItemId();
    	
    	switch(nSelectID){
    	case R.id.menu_about:
    		displayCV();
    		break;
    	case R.id.menu_setting:
    		selectSetting();
    		break;
    		
    		default:
    			Log.e(TAG,"Why you don't handle this case");
    			break;
    	}
    	
        return super.onOptionsItemSelected(item);
    }	    
    
    /***************************************************************************
     * Handle action result
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     * @date May 9, 2011
     * @time 9:53:21 PM
     * @author DatNQ
     **************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode){
    	case REG_CHANGELOCATION:
    		updateDataOfCurrentLocation();
    		break;
    		
    		default:
    			Log.w(TAG,"Not handle request code:"+requestCode);
    			break;
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    /***************************************************************************
     * Change location
     * @date May 9, 2011
     * @time 9:57:52 PM
     * @author DatNQ
     ***************************************************************************/
    private void updateDataOfCurrentLocation(){
		String strMsg = getString(R.string.strFetchingData);
		ProgressDialog m_Dialog = ProgressDialog.show(ActivityWeatherSetting.this, "",
				strMsg, true);	
		
    	String strWOEID = m_Preferneces.getLocation();
    	if (strWOEID == null){
    		Log.e(TAG,"Can not get WOEID");
    		m_Dialog.dismiss();
    		displayNotifyCation(R.string.strFetchFailed);
    		return;
    	}
    	
    	/* Get weather information */
        m_WeatherInfo = m_DataModel.getWeatherData(strWOEID);
    	if (m_WeatherInfo != null){
    		updateWeatherInfo(m_WeatherInfo);
    	}
    	
    	m_Dialog.dismiss();
    	displayNotifyCation(R.string.strFetchSuccess);
    }
    
    /***************************************************************************
     * Display notification
     * @param nResID
     * @date May 9, 2011
     * @time 10:04:13 PM
     * @author DatNQ
     **************************************************************************/
    private void displayNotifyCation(int nResID){
		Toast.makeText(getApplicationContext(), getString(nResID),
				Toast.LENGTH_SHORT).show();    	
    }
    
    
    /***************************************************************************
     * Select setting
     * @date May 7, 2011
     * @time 8:57:55 PM
     * @author DatNQ
     **************************************************************************/
    private void selectSetting(){
		Intent intent = new Intent(ActivityWeatherSetting.this,ActivityScreenLocation.class);
		startActivityForResult(intent, REG_CHANGELOCATION);    	
    }
    
    /***************************************************************************
     * Initialize view
     * @return
     * @date May 9, 2011
     * @time 3:30:41 AM
     * @author DatNQ
     **************************************************************************/
    private boolean initializeView(){
    	m_TextLocation = (TextView)findViewById(R.id.location);
    	m_Temperature = (TextView)findViewById(R.id.temperature);
    	m_Humimidy = (TextView)findViewById(R.id.humidityValue);
    	m_Visibility = (TextView)findViewById(R.id.visiValue);
    	m_WeatherIcon = (ImageView) findViewById(R.id.weather_icon);
    	m_Date = (TextView)findViewById(R.id.dateTime);
    	
    	if ((m_TextLocation == null) || (m_Temperature == null) || 
    			(m_Humimidy == null) || (m_WeatherIcon == null) ||
    			(m_Visibility == null) || (m_Date == null)){
    		Log.e(TAG,"View init failed");
    		return false;
    	}
    	
    	return true;
    }
    
    /***************************************************************************
     * Initialize data
     * @return true if success, false if failed
     * @date May 7, 2011
     * @time 6:48:46 AM
     * @author DatNQ
     **************************************************************************/
    private boolean initializeData(){
    	/* Get application context */
    	Context appContext = this.getApplicationContext();
    	
    	/* Get preference instance */
    	m_Preferneces = WeatherPreferences.getInstance(appContext);
    	if (m_Preferneces == null){
    		Log.e(TAG, "Get preference instance failed, check please");
    		return false;
    	}
    	
    	/* Get instance of data model */
    	m_DataModel = WeatherDataModel.getInstance();
    	if (m_DataModel == null){
    		Log.e(TAG,"Can not get data model");
    		return false;
    	}

    	return true;
    }    
    
    /***************************************************************************
     * Draw weather screen, about if need
     * @date May 7, 2011
     * @time 5:38:13 PM
     * @author DatNQ
     **************************************************************************/
    private void drawWeatherScreen(){
    	drawTitle();
    	
    	/* Request update */
    	updateDataOfCurrentLocation();
    	
		if (m_Preferneces.getMyCvSetting() == true){
			displayCV();
		} 
    }
    
    /***************************************************************************
     * Update weather information
     * @param weatherInfo
     * @date May 9, 2011
     * @time 3:38:08 AM
     * @author DatNQ
     **************************************************************************/
    private void updateWeatherInfo(WeatherInfo weatherInfo){
    	if (weatherInfo == null){
    		Log.e(TAG,"Weather is null");
    		return;
    	}
    	
    	String strCode = weatherInfo.getCode();
    	int nCode = getImageByCode(strCode);
    	m_WeatherIcon.setImageResource(nCode);
    	
    	
    	String strFmt = getString(R.string.str_temperature_fmt);
    	String strTemperature = String.format(strFmt, 
    			weatherInfo.getTemperature(WeatherInfo.TEMPERATURE_FMT_CELSIUS));
    	
    	m_TextLocation.setText(weatherInfo.getCity());
    	m_Temperature.setText(strTemperature);
    	m_Date.setText(weatherInfo.getDate());
    	
    	strFmt = getString(R.string.str_humidity_fmt);
    	String strHumidity = String.format(strFmt, weatherInfo.getHumidity());
    	m_Humimidy.setText(strHumidity);
    	strFmt = getString(R.string.str_visi_fmt);
    	String strVisi = String.format(strFmt, weatherInfo.getVisibility());
    	m_Visibility.setText(strVisi);
    }
    
    /***************************************************************************
     * Get weather icon
     * @param nCode
     * @return
     * @date May 9, 2011
     * @time 4:42:52 AM
     * @author DatNQ
     **************************************************************************/
    private int getImageByCode(String strCode){
    	int nImageCode = R.drawable.a0;
    	
    	if (strCode == null){
    		Log.e(TAG,"Code is null");
    		return nImageCode;
    	}
    	
    	int nCode = Integer.parseInt(strCode);
    	
    	int nNumber= YahooWeatherHelper.m_ImageArr.length;
    	for (int i=0; i < nNumber; i++){
    		if (nCode == YahooWeatherHelper.m_ImageArr[i][1]){
    			return YahooWeatherHelper.m_ImageArr[i][0];
    		}
    	}
    	return nImageCode;
    }
    
    /***************************************************************************
     * Draw title
     * @date May 7, 2011
     * @time 9:50:23 PM
     * @author DatNQ
     **************************************************************************/
    private void drawTitle(){
    	setTitle(R.string.strSettingTitle);
    }
    
    /***************************************************************************
     * Display my CV
     * @date May 7, 2011
     * @time 5:39:04 PM
     * @author DatNQ
     **************************************************************************/
    private void displayCV(){
    	WeatherCustomDialog cvNguyenQuocDat = new WeatherCustomDialog(this);
		if (cvNguyenQuocDat != null){
			cvNguyenQuocDat.show();    	
		}
    }


}
/*******************************************************************************
 * END OF FILE
 ******************************************************************************/