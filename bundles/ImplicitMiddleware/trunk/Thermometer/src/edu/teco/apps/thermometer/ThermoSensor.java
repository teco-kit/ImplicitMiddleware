/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package edu.teco.apps.thermometer;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.io.IScalarInput;
import com.sun.spot.sensorboard.io.ITemperatureInput;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.ISwitch;
import com.sun.spot.sensorboard.peripheral.ILightSensor; 
import com.sun.spot.sensorboard.peripheral.LEDColor;
import com.sun.spot.util.Utils;
import java.io.IOException;



public class ThermoSensor {
    private RemoteNotify              notify;
    private boolean                   stop = false;
    private static final int TIME_TO_SLEEP = 5000;

    public ThermoSensor() {

    }

    public ThermoSensor(RemoteNotify notify) {
    	System.out.println("Notify Object " + notify.toString());
    	this.notify = notify;
    }
    
    public static void Main(RemoteNotify notify) {
    	ThermoSensor thermoSens = new ThermoSensor(notify);

    	thermoSens.run();
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
    	ITriColorLED leds[]          = EDemoBoard.getInstance().getLEDs();
    	ILightSensor lightSensor     = EDemoBoard.getInstance().getLightSensor();
        
    	for(int i = 0; i < leds.length; i++){
            leds[i].setOn();                    // Enable this LED
            leds[i].setRGB(0,0,0);              // Set it to black
        }
        leds[0].setColor(LEDColor.TURQUOISE);   // See LEDColor for more predefined colors.
    	
    	double lastValue = 0;
    	while(true) {
    		try {
    			double val = 
    				EDemoBoard.getInstance().getADCTemperature().getCelsius();
    			
    			if (stop) return;
    			
    			if ((lastValue - val) > 1 || (val - lastValue) > 1)
    			{
    				notify.notifyMe(val);
        			lastValue = val;
    			}
    	          // heatIndication is scaled so that reaching 20 degrees away from 72 gives the maximum LED brightness (255)
                int heatIndication = (int) ((val - 28.0) * 255.0 / 20);
                if(heatIndication > 0){
                    leds[0].setRGB(heatIndication, 0, 0);      //above 28 degrees (room temp) in red
                } else {
                    leds[0].setRGB(0, 0, - (heatIndication));  //below 28 degrees (room temp) in blue
                }
                
                int lightIndication = lightSensor.getValue();     //ranges from 0 - 740
                leds[1].setRGB(0, lightIndication / 3, 0);        //Set LED green, will range from 0 - 246

    			
    			Utils.sleep(TIME_TO_SLEEP);
    			
    		} catch (Exception ignore) {}
    	}
    }
}
