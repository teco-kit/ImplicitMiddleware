package edu.teco.apps.thermometer;

import com.sun.spot.sensorboard.EDemoBoard;
//import com.sun.spot.sensorboard.io.ITemperatureInput;


public class ThermoSensor implements Runnable {
    private RemoteNotify    notify;
    private boolean         stop = false;
    
    public ThermoSensor() {

    }
    
    public ThermoSensor(RemoteNotify notify) {
    	this.notify = notify;
    }
    
    public static void Main(RemoteNotify notify) {
    	ThermoSensor thermoSens = new ThermoSensor(notify);
    	Thread t = new Thread(thermoSens);
        t.start();
    }
    
    public double getTemperature() {
    	try {
    		return EDemoBoard.getInstance().getADCTemperature().getCelsius();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return 0.0D;
    	}
    }
    
    public void stop() {
    	stop = true;
    }
    
    public void run() {
    	double lastValue = 0;
    	while(true) {
    		try {
    			double val = 
    				EDemoBoard.getInstance().getADCTemperature().getCelsius();
    			
    			if (stop) return;
    			
    			if (lastValue != val)
    			{
    				//System.out.println("Notifying about new temperatiure: " + val);
    				notify.notifyMe(val);
    			}
    			
    			lastValue = val;
    			
    			com.sun.spot.util.Utils.sleep(5000);
    			
    		} catch (Exception ignore) {}
    	}
    }
}
