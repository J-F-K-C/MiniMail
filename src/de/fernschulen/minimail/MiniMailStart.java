package de.fernschulen.minimail; 

import java.awt.FlowLayout; 
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener; 

import javax.swing.JButton; 
import javax.swing.JFrame; 

public class MiniMailStart extends JFrame{
	//automatisch über Eclipse erzeugt
	private static final long serialVersionUID = 3610196939297879702L;

	//die innere Klasse für den ActionListener 
	class MeinListener implements ActionListener { 
		@Override 
		public void actionPerformed(ActionEvent e) { 
			//wurde auf Senden geklickt? 
			if (e.getActionCommand().equals("senden")) 
				//dann das Senden starten 
				senden(); 
			//wurde auf Empfangen geklickt? 
			if (e.getActionCommand().equals("empfangen")) 
				//dann das Empfangen starten 
				empfangen(); 
			//wurde auf Beenden geklickt? 
			if (e.getActionCommand().equals("ende")) 
				//dann beenden 
				beenden(); 
		} 
	} 
	
	//der Konstruktor 
	public MiniMailStart(String titel) { 
		super(titel); 
		//ein FlowLayout 
		setLayout(new FlowLayout(FlowLayout.LEFT)); 

		//die Schaltflächen 
		JButton liste = new JButton("Senden"); 
		liste.setActionCommand("senden"); 
		JButton einzel = new JButton("Empfangen"); 
		einzel.setActionCommand("empfangen"); 
		JButton beenden = new JButton("Beenden"); 
		beenden.setActionCommand("ende"); 

		MeinListener listener = new MeinListener(); 
		liste.addActionListener(listener); 
		einzel.addActionListener(listener); 
		beenden.addActionListener(listener); 

		add(liste); 
		add(einzel); 
		add(beenden); 

		//Größe setzen, Standardverhalten festlegen und anzeigen 
		pack(); 
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		setVisible(true); 
	} 

	//die Dummy-Methoden 
	private void senden() { 
		new Senden();
	} 

	private void empfangen() { 
		new Empfangen();
	} 

	private void beenden() { 
		System.exit(0); 
	} 
} 
