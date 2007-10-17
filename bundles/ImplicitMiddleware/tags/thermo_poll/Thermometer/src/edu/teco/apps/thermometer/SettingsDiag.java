package edu.teco.apps.thermometer;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsDiag extends Dialog {
	
	private static final long serialVersionUID = 1L;
	private Choice            refreshType      = null;
	private ThermoCanvas      thermo           = null;

	
	SettingsDiag(Window parent, ThermoCanvas thermo) {
		super(parent, "Settings");
	
		this.setSize(250, 100);
		this.thermo = thermo;
		
		Panel refTypePanel = new Panel();
		refTypePanel.setLayout(new FlowLayout());
		
		Label refTypeLabel = new Label("Refresh Type");
		refTypePanel.add(refTypeLabel);
		
		refreshType = new Choice();
		refreshType.add("Poll");
		refreshType.add("Event based");
		refTypePanel.add(refreshType);
		
		this.add(refTypePanel, BorderLayout.NORTH);
		
		Panel buttonsPanel = new Panel();
		buttonsPanel.setLayout(new FlowLayout());
		ButtonListener bL = new ButtonListener();
		
		Button okButton = new Button("Ok");
	    okButton.addActionListener(bL);
	    okButton.setActionCommand("ok");
		buttonsPanel.add(okButton);
		Button cancelButton = new Button("Cancel");
	    cancelButton.addActionListener(bL);
	    cancelButton.setActionCommand("cancel");
		buttonsPanel.add(cancelButton);
		
		this.add(buttonsPanel, BorderLayout.SOUTH);
		
	}
	
	class ButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("ok"))
			{
				dispose();
			}
			if (e.getActionCommand().equals("cancel"))
			{
				dispose();
			}
		} 
	}
	
}
