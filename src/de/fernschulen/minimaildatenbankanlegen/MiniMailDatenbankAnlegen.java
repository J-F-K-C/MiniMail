//ACHTUNG!! Das Programm lässt sich nur ausführen, wenn derby.jar eingebunden ist
package de.fernschulen.minimaildatenbankanlegen;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Clob;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MiniMailDatenbankAnlegen extends JFrame {
	//automatisch über Eclipse erzeugt
	private static final long serialVersionUID = 7908452154759854933L;
	
	//für das Protokoll
	private String protokoll = "jdbc:derby:";
	 
	//für die Verbindung
	//ACHTUNG! Es gibt mehrere Connection-Klassen!
	//benötigt wird die Klasse aus dem Paket java.sql.
 	private Connection verbindung;

	
	//die innere Klasse für den ActionListener
	class MeinListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			//wurde auf Anlegen geklickt?
			if (e.getActionCommand().equals("anlegen"))
				//dann die Datenbank anlegen
				anlegenDB();
			//wurde auf neue Einträge geklickt?
			if (e.getActionCommand().equals("eintraege"))
				//dann die Testeinträge anlegen
				anlegenEintraege();
			//wurde auf Beenden geklickt?
			if (e.getActionCommand().equals("ende"))
				//dann beenden
				System.exit(0);
		}
	}


	public MiniMailDatenbankAnlegen(String titel) {
		super(titel);
		
		//ein FlowLayout
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		//die Schaltflächen
		JButton anlegen = new JButton("Datenbank anlegen");
		anlegen.setActionCommand("anlegen");
		JButton test = new JButton("Testeinträge erzeugen");
		test.setActionCommand("eintraege");
		JButton beenden = new JButton("Beenden");
		beenden.setActionCommand("ende");
		
		MeinListener listener = new MeinListener();
		anlegen.addActionListener(listener);
		test.addActionListener(listener);
		beenden.addActionListener(listener);
	
		add(anlegen);
		add(test);
		add(beenden);
		
		//Größe setzen, Standardverhalten festlegen und anzeigen
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void oeffnenDB(String arg) {
		try {
			verbindung = DriverManager.getConnection(arg);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
	
	private void schliessenDB() {
		//das Schließen löst bei Erfolg eine Exception aus!!!
		boolean erfolg = false;
		try {
			DriverManager.getConnection(protokoll + "mailDB; shutdown = true");
		}
		catch (SQLException e) {
			erfolg = true;
		}
		if (erfolg !=true) 
			JOptionPane.showMessageDialog(this, "Das DBMS konnte nicht heruntergefahren werden.");
	}
	
	public static void main(String[] args) {
		new MiniMailDatenbankAnlegen("Datenbank-Werkzeuge");
	}

	
	private void anlegenDB(){
		//die Datenbank ist mailDB, sie wird neu angelegt
		oeffnenDB(protokoll + "mailDB; create = true");
		JOptionPane.showMessageDialog(this, "Die Datenbank wurde angelegt bzw. geöffnet.");
		//ein Statement zum Erstellen der Tabelle
		//bitte auch hier auf die richtige Klasse aus dem Paket sql achten!
		try {
			Statement state = verbindung.createStatement();
			state.execute("CREATE TABLE gesendet(" +
					"iNummer int NOT NULL GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1)," +
					"empfaenger varchar(255) NOT NULL," +
					"betreff varchar(255)," +
					"inhalt CLOB(64 K)," +
					"PRIMARY KEY (iNummer))");
			JOptionPane.showMessageDialog(this, "Die Tabelle gesendet wurde angelegt.");
			state.execute("CREATE TABLE empfangen(" +
					"iNummer int NOT NULL GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1)," +
					"sender varchar(255) NOT NULL," +
					"betreff varchar(255)," +
					"inhalt CLOB(64 K)," +
					"PRIMARY KEY (iNummer))");
			JOptionPane.showMessageDialog(this, "Die Tabelle empfangen wurde angelegt.");
			
			//Verbindung schließen
			state.close();
			verbindung.close();
			//und die Datenbank schließen
			schliessenDB();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
	
	private void anlegenEintraege() {
		//die Datenbank öffnen
		oeffnenDB(protokoll + "mailDB");
		try {
			//einen Eintrag in der Tabelle empfangen anlegen
			//über ein vorbereitetes Statement
			PreparedStatement prepState;
			prepState = verbindung.prepareStatement("insert into empfangen (sender, betreff, inhalt) values (?,?,?)");
	        prepState.setString(1, "csiebeck@gmx.de");
	        prepState.setString(2, "Testnachricht");
	        prepState.setString(3, "Der Text der empfangenen Nachricht als CLOB");
	        //das Statement ausführen
			prepState.executeUpdate();
			verbindung.commit();
			
			//Änderungen von außen werden sichtbar und der Cursor kann beliebig bewegt werden
			Statement state;
			state = verbindung.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			//die Ergebnismenge erzeugen
			//dazu beschaffen wir alle Einträge in der Tabelle empfangen
	        ResultSet ergebnisMenge;
	        ergebnisMenge = state.executeQuery("SELECT * FROM empfangen");
	        //ganz nach vorne gehen
	        ergebnisMenge.first();
	        
	        //zum Test die  Werte in der Konsole ausgeben
	        //hier wird direkt die Spalte angegeben
	       	System.out.println(ergebnisMenge.getInt("iNummer"));
	       	System.out.println(ergebnisMenge.getString("sender"));
	       	System.out.println(ergebnisMenge.getString("betreff"));
	       	Clob clob;
	       	clob = ergebnisMenge.getClob("inhalt");
	       	System.out.println(clob.getSubString(1, (int) clob.length()));
	        
	       	//zum letzten Eintrag gehen
	        ergebnisMenge.last();
	        System.out.println("In der Tabelle empfangen stehen jetzt " + ergebnisMenge.getRow() + " Einträge");

	        //Verbindung schließen
	        prepState.close();
			state.close();
			ergebnisMenge.close();
			
			//und einen Eintrag für die Tabelle gesendet anlegen
			//über ein vorbereitetes Statement
			prepState = verbindung.prepareStatement("insert into gesendet (empfaenger, betreff, inhalt) values (?,?,?)");
	        prepState.setString(1, "csiebeck@gmx.de");
	        prepState.setString(2, "Testnachricht");
	        prepState.setString(3, "Der Text der gesendeten Nachricht als CLOB");
	        //das Statement ausführen
			prepState.executeUpdate();
			verbindung.commit();
			
			//Änderungen von außen werden sichtbar und der Cursor kann beliebig bewegt werden
			state = verbindung.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			//die Ergebnismenge erzeugen
			//dazu beschaffen wir alle Einträge in der Tabelle empfangen
	        ergebnisMenge = state.executeQuery("SELECT * FROM gesendet");
	        //ganz nach vorne gehen
	        ergebnisMenge.first();
	        
	        //zum Test die  Werte in der Konsole ausgeben
	        //hier wird direkt die Spalte angegeben
	       	System.out.println(ergebnisMenge.getInt("iNummer"));
	       	System.out.println(ergebnisMenge.getString("empfaenger"));
	       	System.out.println(ergebnisMenge.getString("betreff"));
	       	clob = ergebnisMenge.getClob("inhalt");
	       	System.out.println(clob.getSubString(1, (int) clob.length()));
	        
	       	//zum letzten Eintrag gehen
	        ergebnisMenge.last();
	        System.out.println("In der Tabelle gesendet stehen jetzt " + ergebnisMenge.getRow() + " Einträge");

	        //Verbindung schließen
	        prepState.close();
			state.close();
			
			verbindung.close();
			//und die Datenbank schließen
			schliessenDB();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
}
