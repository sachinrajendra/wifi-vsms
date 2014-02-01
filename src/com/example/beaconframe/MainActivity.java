package com.example.beaconframe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import com.example.beaconframe.R;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	WifiManager wifiManager;
	List<ScanResult> wifiList;
	String received_msg;
	TextView tv;
	WifiManager w;
	String FILENAME = "messages";
	FileOutputStream fos;
	FileInputStream fis;
	String string = "this\n";
	String[]  arr = new String[100];
	List<String> list_arr = new ArrayList<String>();
	int count;
	ArrayAdapter<String> adapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recMessagethread();
        
        
        count=0;
        
        read_from_file();
        
        ListView l=(ListView) findViewById(R.id.listView1);
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list_arr);
        l.setAdapter(adapter);
        //l.setDivider(null);
        
        //createWifiAccessPoint();
    }
    
    
    protected void onPause()  {
    	super.onPause();
    	Log.i("pause", "onpause called");
    	print_to_file();
    }
    
    protected void onResume()  {
    	super.onResume();
    	Log.i("pause", "onresume called");
    	count=0;
    	list_arr.clear();
    	adapter.notifyDataSetChanged();
    	read_from_file();
    }
    
    
    
    
    void read_from_file(){
    	int in = 2;
    	String out="";
    	char ch;
    	
    	
    	try {
			fis =  openFileInput(FILENAME);
		} catch (FileNotFoundException e) {
			Log.i("rff", "couldnt open file");
			return;
			
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
    	
    	
    	while(true){
    		//Log.i("rff", in+"");
    		try {
    			in = fis.read();
    			//Log.i("rff", in+"");
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		if(in==-1){
    			Log.i("rff", "exited");
    			break;
    		}
    		ch = (char)in;
    		if(ch=='@'){
    			//arr[count] = out;
    			list_arr.add(out);
    			Log.i("rff", out);
    			count++;
    			out="";
    		}
    		else{
    			out+=ch;
    		}
    	}
    }
    
    void print_to_file(){
    	int l = count;
    	try{
			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        } 
        catch(FileNotFoundException e) {
			e.printStackTrace();
		}
        
        for(int i=0;i<l;i++){
        	String string = list_arr.get(i)+"@";
        	try {
				fos.write(string.getBytes());
				Log.i("pause " , "printed on file");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    private void receive_message(){
    	IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        Log.i("Access point " , "2");
    	
    	//mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	registerReceiver(new BroadcastReceiver(){
    		public void onReceive(Context c, Intent i) {
                wifiList = w.getScanResults();
                Log.i("No of Access point " , ""+wifiList.size());
                int m;
                for(m = 0; m < wifiList.size(); m++)
                {
                       received_msg = wifiList.get(m).SSID;
                       Log.i("AP " , received_msg);
                       if(received_msg.charAt(0)=='@'&&received_msg.charAt(1)=='!'){
                    	   received_msg=received_msg.substring(2);
                    	   Log.i("message " , received_msg);
                    	   break;
                       }
                }
                
                if(m==wifiList.size()){
                	received_msg="";
                }
                else{
                	list_arr.add(received_msg+"\n");
            		count++;
            		adapter.notifyDataSetChanged();
//            		/mEdit.setText ("");
                	//tv =(TextView)findViewById(R.id.textView1);
                	//tv.setText(received_msg);
                }
                receive_message();
    		}
    	}, i);
    }
    
    class Task implements Runnable {
		@Override
		public void run() {
			//while(true){
				Log.i("thread " , "scan started");
	    		receive_message();
	    		//SystemClock.sleep(150);
	    	//}
		}
	}
    
    private void recMessagethread(){
    	if(!w.isWifiEnabled())
        {
            w.setWifiEnabled(true);          
        }
    	new Thread(new Task()).start();
    }
    //public void recMessage(View view){
    	//Button getButton = (Button) findViewById(R.id.button2);
    	//getButton.setVisibility(View.GONE);
    	//while(true){
    		
    		//receive_message();
    		//SystemClock.sleep(2000);
    	//}
    //}
    
    public void sendMessage(View view){
    	EditText mEdit   = (EditText)findViewById(R.id.editText1);
    	String message = mEdit.getText().toString();
    	if(message!=""){
    		createWifiAccessPoint("@!"+message);
    		//list_arr.add(message+"\n");
    		//count++;
    		//adapter.notifyDataSetChanged();
    		mEdit.setText ("");
    	}
    }
    
    private void createWifiAccessPoint(String ssid) {
        if(wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(false);          
        }       
        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();   
        boolean methodFound=false;
        for(Method method: wmMethods){
            if(method.getName().equals("setWifiApEnabled")){
                methodFound=true;
                WifiConfiguration netConfig = new WifiConfiguration();
                netConfig.SSID = ssid; 
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
               
                try {
                	boolean apstatus=(Boolean) method.invoke(wifiManager, netConfig, false);
                    apstatus=(Boolean) method.invoke(wifiManager, netConfig,true);
                    //apstatus=(Boolean) method.invoke(wifiManager, netConfig,false);
                    for (Method isWifiApEnabledmethod: wmMethods)
                    {
                        if(isWifiApEnabledmethod.getName().equals("isWifiApEnabled")){
                            while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){
                            };
                            for(Method method1: wmMethods){
                                if(method1.getName().equals("getWifiApState")){
                                    int apstate;
                                    apstate=(Integer)method1.invoke(wifiManager);
                                }
                            }
                        }
                    }
                    
                    if(apstatus)
                    {
                        Log.d("Splash Activity", "Access Point created");   
                    }else
                    {
                        Log.d("Splash Activity", "Access Point creation failed");   
                    }
                    SystemClock.sleep(10000);
                    apstatus=(Boolean) method.invoke(wifiManager, netConfig,false);
                    if(!w.isWifiEnabled())
                    {
                        w.setWifiEnabled(true);          
                    }
                    
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }      
        }
        if(!methodFound){
            Log.d("Splash Activity", "cannot configure an access point");
        }}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
