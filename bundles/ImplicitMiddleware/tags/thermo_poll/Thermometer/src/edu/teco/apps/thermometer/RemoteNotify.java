package edu.teco.apps.thermometer;

public class RemoteNotify {
	   
	private TemperatureNotifiable notify = null;
	
	public RemoteNotify(TemperatureNotifiable notify) {
		this.notify = notify;
		
	}
	public void notifyMe(double grad) {
	  System.out.println("notify " + grad);
      notify.notifyMe((int)grad);   
   }
}
