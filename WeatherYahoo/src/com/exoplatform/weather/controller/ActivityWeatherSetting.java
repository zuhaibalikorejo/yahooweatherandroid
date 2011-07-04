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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.exoplatform.weather.view.ContextMenuAdapter;
import com.exoplatform.weather.view.ContextMenuItem;

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
	
	/** Request get location */
	private static final int REG_GET_WEATHER_START = 100;
	
	/** Request get location finish */
	private static final int REG_GET_WEATHER_FINISH = 101;
	
	/** Frequency update */
	private static final int ONE_MINUTE = 60*1000;
	
	/** Context menu */
	private static final int MENU_CONTEXT_0 = 0;	
	
	/** Context menu */
	private static final int MENU_CONTEXT_1 = 1;	
	
	/** Context menu */
	private static final int MENU_CONTEXT_2 = 2;
	
	/** Item 1 */
	private static final int SELECT_ITEM_1 = 0;
	
	/** Item 2 */
	private static final int SELECT_ITEM_2 = 1;
	
	/** Item 3 */
	private static final int SELECT_ITEM_3 = 2;
	
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
	
	/** Handle request */
	Handler m_HandleRequest;
	
	/** Dialog */
	//ProgressDialog m_ProgressDialog;
	
	/** Dialog */
	AlertDialog m_Dialog;	
	
	/** Runable */
	Runnable m_Runnable;
	
	/** For adapter of dialog */
	private ContextMenuAdapter m_contextAdapter;
	
	/** Dialog */
	AlertDialog m_Alert;

	
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
			selectWeatherSetting();
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
    		selectWeatherSetting();
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
    	requestUpdateWeather();
    }
    
    /***************************************************************************
     * Update weather
     * @date May 10, 2011
     * @time 9:50:25 PM
     * @author DatNQ
     **************************************************************************/
    private void requestUpdateWeather(){
    	Message msgFetchData = new Message();
    	msgFetchData.what = REG_GET_WEATHER_START;
    	m_HandleRequest.sendMessage(msgFetchData);    	
    }
    
	
	/***************************************************************************
	 * Handler request
	 * @date May 10, 2011
	 * @time 8:50:24 PM
	 * @author DatNQ
	 **************************************************************************/
	private void initializeHandleRequest(){
		m_Runnable = new Runnable(){

			@Override
			public void run() {
				requestUpdateWeather();
			}			
		};
		
		
	    /* Setting up handler for ProgressBar */
		m_HandleRequest = new Handler(){
			@Override
			public void handleMessage(Message message) {
				int nRequest = message.what;
				
				switch(nRequest){
				case REG_GET_WEATHER_START:
			    	String strWOEID = m_Preferneces.getLocation();
			    	if (strWOEID == null){
			    		Log.e(TAG,"Can not get WOEID");
			    		//m_ProgressDialog.dismiss();
			    		displayNotifyCation(R.string.strFetchFailed);
			    		return;
			    	} else {
				    	/* Get weather information */
				        m_WeatherInfo = m_DataModel.getWeatherData(strWOEID);			
			    	}
					
					Message msgRegSearch = new Message();
					msgRegSearch.what = REG_GET_WEATHER_FINISH;
					sendMessage(msgRegSearch);
					break;
					
				case REG_GET_WEATHER_FINISH:
			    	if (m_WeatherInfo != null){
			    		updateWeatherInfo(m_WeatherInfo);
			    		notifyUpdateTime();
			    	}					
					//m_ProgressDialog.dismiss();
					m_HandleRequest.postDelayed(m_Runnable, (ONE_MINUTE*m_Preferneces.getTimeUpdate()));
					break;
					 
					 default:
						 Log.e(TAG,"Can not handle this message");
						 break;
				}
			}
        };		
	}    
	
	/***************************************************************************
	 * Update weather
	 * @date May 12, 2011
	 * @time 11:12:59 PM
	 * @author DatNQ
	 **************************************************************************/
	private void notifyUpdateTime(){
		Intent intentSettingUpdate = new Intent(WidgetWeather.UPDATE_WEATHER);
		this.sendBroadcast(intentSettingUpdate);		
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
				Toast.LENGTH_LONG).show();    	
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

    
    private void selectTimeIntervalUpdating(){
    	final CharSequence[] items = {"30 minutes", "3 hours", "12 hours"};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.selectTimeUpdate);
    	builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	        /* Check to update setting */
    	    	int nTime = 30;
    	    	switch( item ){
    	    	case SELECT_ITEM_1:
    	    		nTime = 30;
    	    		break;
    	    	case SELECT_ITEM_2:
    	    		nTime = 180;
    	    	case SELECT_ITEM_3:
    	    		nTime = 720;
    	    		break;
    	    		
    	    		default:
    	    			break;
    	    	}
    	    	
    	    	m_Preferneces.setTimeUpdate(nTime);
    	    	m_Alert.dismiss();
    	    	updateWeatherInfo(m_WeatherInfo);
    	    }
    	});
    	
    	m_Alert = builder.create();  
    	m_Alert.show();
    }
    
    /***************************************************************************
     * Select temperature format
     * @date May 12, 2011
     * @time 11:21:27 PM
     * @author DatNQ
     **************************************************************************/
    private void selectTempFormat(){
    	final CharSequence[] items = {"Celsius", "Fahrenheit"};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.selectTemperatureUnit);
    	builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	        /* Check to update setting */
    	    	boolean bIsC = true;
    	    	switch( item ){
    	    	case SELECT_ITEM_1:
    	    		bIsC = true;
    	    		break;
    	    	case SELECT_ITEM_2:
    	    		bIsC = false;
    	    		
    	    		default:
    	    			break;
    	    	}
    	    	
    	    	m_Preferneces.setTempFmt(bIsC);
    	    	m_Alert.dismiss();
    	    	notifyUpdateTime();
    	    	updateWeatherInfo(m_WeatherInfo);
    	    }
    	});
    	m_Alert = builder.create();  
    	m_Alert.show();
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

    	initializeHandleRequest();
    	
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
    	
    	boolean bIsC = m_Preferneces.getTempFmt();
    	
    	String strFmt;
    	String strTemp = weatherInfo.getTemperature(WeatherInfo.TEMPERATURE_FMT_CELSIUS);
    	if (bIsC == true){
    		strFmt = getString(R.string.str_temperature_fmt); 
    	} else {
    		strFmt = getString(R.string.str_temperature_fmt_f);
    		strTemp = WeatherDataModel.convertC2F(strTemp);
    	}
    	
    	String strTemperature = String.format(strFmt, strTemp);
    	
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
    
    
	private AlertDialog createContextMenuSetting(Context context){
		/* Crate menu list */
		AlertDialog dialogMenu = null;
		List<ContextMenuItem> arrMenuItem = null;
		AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);

		/* Create menu item of context menu */
		arrMenuItem = _createContextMenuList();
		if (arrMenuItem == null){
			Log.e(TAG,"Can note create dialog item");
			return null;
		}

		this.m_contextAdapter = new ContextMenuAdapter(context,
				0, arrMenuItem);
		contextMenu.setAdapter(m_contextAdapter, new HandleSelectContextMenu());
		contextMenu.setInverseBackgroundForced(true);
		contextMenu.setTitle(R.string.title_context_menu_setting);
		contextMenu.setIcon(R.drawable.ic_context_menu);

		
		dialogMenu = contextMenu.create();
		dialogMenu.setCanceledOnTouchOutside(true);
		
		return dialogMenu;
	}
	
	private List<ContextMenuItem> _createContextMenuList(){
		ArrayList<ContextMenuItem> arrMenuItem = new ArrayList<ContextMenuItem>();

		/* Create first menu item base on menu state */
		ContextMenuItem itemContext1 = new ContextMenuItem(
				MENU_CONTEXT_0,
				R.string.context_menu_changeLocation,
				R.drawable.location_ic);

		ContextMenuItem itemContext2 = new ContextMenuItem(
				MENU_CONTEXT_1,
				R.string.context_menu_update_time,
				R.drawable.update_time);
		
		ContextMenuItem itemContext3 = new ContextMenuItem(
				MENU_CONTEXT_2,
				R.string.temperature_unit,
				R.drawable.temperature_ic);		
		
		/* Add context item to list */
		arrMenuItem.add(itemContext1);
		arrMenuItem.add(itemContext2);
		arrMenuItem.add(itemContext3);
		return arrMenuItem;
	}		
	
	/**************************************************************************
	 * Handle select context menu
	 * @author DatNQ
	 *
	 **************************************************************************/
	private class HandleSelectContextMenu implements 
					android.content.DialogInterface.OnClickListener{
		
		/*********************************************************
		 * Handle when select context menu item
		 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
		 * @author DatNQ
		 ********************************************************/
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			switch (which){
			case MENU_CONTEXT_0:
				selectSetting();
				break;

			case MENU_CONTEXT_1:
				selectTimeIntervalUpdating();
				break;
				
			case MENU_CONTEXT_2:
				selectTempFormat();
				break;
				
				default:
					Log.e(TAG,"Invalid context menu");
					break;
			}
		}
	}	
	
	/***************************************************************************
	 * Select setting
	 * @date May 12, 2011
	 * @time 11:26:52 PM
	 * @author DatNQ
	 **************************************************************************/
	private void selectWeatherSetting(){
		m_Dialog = createContextMenuSetting(this);
		if (m_Dialog != null){
			m_Dialog.show();
		}		
	}

}
/*******************************************************************************
 * END OF FILE
 ******************************************************************************/