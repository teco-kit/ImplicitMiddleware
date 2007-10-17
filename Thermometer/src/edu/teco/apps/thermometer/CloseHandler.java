package edu.teco.apps.thermometer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CloseHandler extends WindowAdapter {
	
   private boolean leave = false;
   
   public CloseHandler(boolean leave) {
	   this.leave = leave;
   }
   public void windowClosing(WindowEvent e) {
      e.getWindow().dispose();
      if (leave)
    	  System.exit(0);
   }
}
