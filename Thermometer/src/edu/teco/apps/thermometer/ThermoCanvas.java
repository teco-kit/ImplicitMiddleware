/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package edu.teco.apps.thermometer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

public class ThermoCanvas extends Canvas {

   private static final long serialVersionUID = 1L;
   private static final int  thermoWidth      = 50;
   private static final int  thermoHeight     = 200;
   private static final int  thermoX          = 125;
   private static final int  thermoY          = 44;
   private int               scale            = 2;
   private int               offset           = 15;
   private int               blueTop          = (15+offset)*scale;
   private int               greenTop         = (27+offset)*scale;
   private int               redTop           = thermoHeight - 1;
   private int               blueHeight       = 0;
   private int               greenHeight      = 0;
   private int               redHeight        = 0;
   private String            msg              = "";

   
   public ThermoCanvas (int grad) {
      setTemp(grad);
   }
   
   public void setRed(int grad) {
      redTop  = (grad + offset) * scale;
   }
   
   public void setGreen(int grad) {
      greenTop = (grad + offset) * scale;
   }
   
   public void setBlue(int grad) {
      blueTop  = (grad + offset) * scale;
   }
   
   public void setTemp(int grad) {
      int scaledGrad = scale*(grad + offset);

      if (scaledGrad <= blueTop)
      {
         blueHeight  = scaledGrad;
         greenHeight = 0;
         redHeight   = 0;
      }
      else if (scaledGrad <= greenTop)
      {
         blueHeight  = blueTop;
         greenHeight = scaledGrad - blueHeight;
         redHeight   = 0;
      }
      else if (scaledGrad <= redTop)
      {
         blueHeight  = blueTop;
         greenHeight = greenTop - blueTop;
         redHeight   = scaledGrad - greenHeight - blueHeight;
      }
      else
      {
         blueHeight  = blueTop;
         greenHeight = greenTop - blueTop;
         redHeight   = redTop   - greenTop;
      }
            
      msg = Integer.toString(grad) + "\u00B0C"; // u00b0 is grad charecter
      this.repaint();
   }
   
   public void paint(Graphics g) {
      g.drawRect(thermoX, thermoY, thermoWidth, thermoHeight);
      int z = 0;
      for (int i = thermoHeight - offset*scale; i >= 0; i -= 5*scale)
      {
         if ( (z%2) == 0 )
         {
            g.drawLine(thermoX, thermoY + i, thermoX - 10, thermoY + i);
            g.drawLine(thermoX + thermoWidth, thermoY + i, 
                       thermoX + thermoWidth + 10, thermoY + i);
            g.drawString(Integer.toString(z*5), 
                         thermoX + thermoWidth + 15, 
                         thermoY + i);
         }
         else
         {
            g.drawLine(thermoX, thermoY + i, thermoX -  5, thermoY + i);
            g.drawLine(thermoX + thermoWidth, thermoY + i, 
                       thermoX + thermoWidth + 5, thermoY + i);
         }
         
         z++;
      }
      
      z = 1;
      for (int i = thermoHeight - offset*scale+ 5*scale; 
           i <= thermoHeight; i += 5*scale)
      {
         if ( (z%2) == 0 )
         {
            g.drawLine(thermoX, thermoY + i, thermoX - 10, thermoY + i);
            g.drawLine(thermoX + thermoWidth, thermoY + i, 
                       thermoX + thermoWidth + 10, thermoY + i);
            g.drawString(Integer.toString(-z*5), 
                         thermoX + thermoWidth + 10, 
                         thermoY + i);
         }
         else
         {
            g.drawLine(thermoX, thermoY + i, thermoX -  5, thermoY + i);
            g.drawLine(thermoX + thermoWidth, thermoY + i, 
                       thermoX + thermoWidth + 5, thermoY + i);
         }
         
         z++;
      }
      
      paintTemperatur(g);
   }
   
   public void update(Graphics g) {
      paintTemperatur(g);
   }
   
   private void paintTemperatur(Graphics g) {
      if (redHeight > 0)
      {
         fillRest(g, greenHeight +  blueHeight + redHeight);
         paintRed(g);
         paintGreen(g);
         paintBlue(g);
      }
      else if (greenHeight > 0)
      {
         fillRest(g, greenHeight +  blueHeight);
         paintGreen(g);
         paintBlue(g);
      }
      else if (blueHeight > 0)
      {
         fillRest(g, blueHeight);
         paintBlue(g);
      }
 
      if (!msg.equals(""))
      {
         g.setColor(Color.black);
         g.drawString(msg, 135, 100);
      }  
   }
   
   private void paintRed(Graphics g) {
      g.setColor(Color.red);
      g.fillRect(thermoX + 1, 
                 thermoY + thermoHeight - blueHeight 
                 - greenHeight - redHeight, 
                 thermoWidth - 1, redHeight);
   }
   
   private void paintBlue(Graphics g) {
      g.setColor(Color.blue);
      g.fillRect(thermoX + 1, 
                 thermoY + thermoHeight - blueHeight, 
                 thermoWidth - 1, blueHeight);
      
   }
   
   private void paintGreen(Graphics g) {
      g.setColor(Color.green);
      g.fillRect(thermoX + 1, 
                 thermoY + thermoHeight - blueHeight - greenHeight, 
                 thermoWidth - 1, greenHeight);
   }
   
   private void fillRest(Graphics g, int till) {
      g.setColor(this.getBackground());
      g.fillRect(thermoX + 1, 
                 thermoY + 1, 
                 thermoWidth - 1, thermoHeight - till);
   }
   
}
