package edu.teco.apps.thermometer;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ThermometerMain extends Frame implements TemperatureNotifiable {

   public static void main(String[] args) {
      ThermometerMain mainT = new ThermometerMain();
      mainT.setTitle("Thermometer");
      mainT.setSize(300, 350);
      mainT.setVisible(true);
      mainT.addWindowListener(new CloseHandler(true));
      
      //thermoTest(mainT);
      
      try {
    	 ThermoSensor tSens = new ThermoSensor();
    	 while (true) {
    		 mainT.setTemp((int)tSens.getTemperature());
    		 Thread.sleep(5000);
    	 }
      } catch (Exception ignore) {  }
//      ThermoSensor.Main(new RemoteNotify(mainT));
   }
   
   public void notifyMe(double grad) {
	   setTemp((int)grad);   
   }
   
   public static void thermoTest(ThermometerMain mainT) {
      try {
         Thread.sleep(5000);
         mainT.setTemp(30);
         Thread.sleep(5000);
         mainT.setTemp(28);
         Thread.sleep(5000);
         mainT.setTemp(25);
         Thread.sleep(10000);
         mainT.setTemp(18);
         Thread.sleep(10000);
         mainT.setTemp(10);
         Thread.sleep(10000);
         mainT.setTemp(12);
         Thread.sleep(10000);
         mainT.setTemp(19);
         Thread.sleep(10000);
         mainT.setTemp(29);
         Thread.sleep(10000);
         mainT.setTemp(120);
      } catch (Exception ignore) {  }
   }
   
   private static final long serialVersionUID = 1L;
   private ThermoCanvas      canvas           = null;
   private MenuBar           mBar             = null;
   private Menu              fMenu            = null;
   //private Menu              hMenu            = null;
   private Dialog            settingsDiag     = null;
   
   public ThermometerMain () {
      mBar            = new MenuBar();
      fMenu           = new Menu("File");
      MenuListener mL = new MenuListener();
      
      MenuItem item1  = new MenuItem("Settings");
      item1.addActionListener(mL);
      item1.setActionCommand("set");
      fMenu.add(item1);
      
      MenuItem item2  = new MenuItem("Quit");
      item2.addActionListener(mL);
      item2.setActionCommand("quit");
      fMenu.add(item2);
      
      mBar.add(fMenu);
      setMenuBar(mBar);
      
      this.setLayout(new BorderLayout());
      canvas =  new ThermoCanvas(0);
      this.add(canvas, BorderLayout.CENTER);
   }
   
   public void setTemp(int grad) {
      canvas.setTemp(grad);
   }
   
   class MenuListener implements ActionListener {

      public void actionPerformed(ActionEvent e) {
         if (e.getActionCommand().equals("quit"))
         {
            dispose();
            System.exit(0);
         }
         if (e.getActionCommand().equals("set"))
         {
        	if (settingsDiag == null)
        	{
        		settingsDiag = new SettingsDiag(getOwner(), canvas);
        		settingsDiag.addWindowListener(new CloseHandler(false));
        	}
        	
        	settingsDiag.setVisible(true);
         }
      } 
   }
}
