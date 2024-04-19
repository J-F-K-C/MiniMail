package de.fernschulen.minimail;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

public class Empfangen extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4858518050401873900L;
	
	private JTable tabelle;
	private DefaultTableModel modell;
	//Aufgabe 2: Die Buttons zum Weiterleiten und Antworten
	private JButton forward, answer;
	private JToolBar leiste;
	//Aufgabe 1: fuer den Benutzernamen, Passowrd und den Namen der Datei wo die Daten gespeichert sind.
	private String loginName = "", loginPW = "", dateiName = "logindaten.dat";
	
	class MeinWindowAdapter extends WindowAdapter{
		@Override
		public void windowOpened(WindowEvent e) {
			nachrichtenEmpfangen();
		}
	}
	
	class MeinListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("forward")) {
				//Aufgabe 2: ist keine Nachricht ausgewaehlt, passiert nichts
				if (tabelle.getSelectedRow() == -1) 
					return;
				//Aufgabe 2: Fuer das Weiterleiten werden aus der ausgewaehlten Zeile
				// der Betreff und der Inhalt an die Methode weiterleiten uebergeben.
				int zeile = tabelle.getSelectedRow();
				String betreff, inhalt;				
				betreff = tabelle.getModel().getValueAt(zeile, 2).toString();
				inhalt = tabelle.getModel().getValueAt(zeile, 3).toString();				
				weiterleiten(betreff, inhalt);
			}
			if (e.getActionCommand().equals("answer")) {
				//Aufgabe 2: ist keine Nachricht ausgewaehlt, passiert nichts
				if (tabelle.getSelectedRow() == -1)
					return;
				//Aufgabe 2: Fuer das Antworten werden aus der ausgewaehlten Zeile
				//der Sender, der Betreff und der Inhalt and die Methode antworten uebergeben
				int zeile = tabelle.getSelectedRow();
				String sender, betreff, inhalt;				
				sender = tabelle.getModel().getValueAt(zeile, 1).toString();
				betreff = tabelle.getModel().getValueAt(zeile, 2).toString();
				inhalt = tabelle.getModel().getValueAt(zeile, 3).toString();
				antworten(sender, betreff, inhalt);
			}
		}		
	}
	
	Empfangen(){
		super();
		//Aufgabe 1: Die Methode wird im Konstruktor ausgefuehrt, damit die Daten bei Start geladen sind.
		loginDatenLaden();
		
		setTitle("E-Mail empfangen");
		setLayout(new BorderLayout());
		//Aufgabe 2: die Symbolleiste fuer die Buttons erzeugen
		leiste = symbolleiste();
		add(leiste, BorderLayout.NORTH);
		
		setVisible(true);
		setSize(700, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		addWindowListener(new MeinWindowAdapter());
		
		tabelleErstellen();
		tabelleAktualisieren();
	}
	
	private JToolBar symbolleiste() {
		JToolBar leiste = new JToolBar();
		//Aufgabe 2: die Buttons fuer das Weiterleiten und das Antworten werden initialisiert
		// und mit einem Icon versehen
		forward = new JButton("Weiterleiten");
		answer = new JButton("Antworten");		
		forward.setIcon(new ImageIcon("icons/mail-forward.gif"));
		answer.setIcon(new ImageIcon("icons/mail-reply.gif"));
		forward.setActionCommand("forward");
		answer.setActionCommand("answer");
		forward.setToolTipText("Leitet eine Email weiter");
		answer.setToolTipText("eine Email beantworten");
		//Aufgabe 2: die Buttons den Listener hinzufuegen
		MeinListener listener = new MeinListener();
		forward.addActionListener(listener);
		answer.addActionListener(listener);
		//Aufgabe 2: die Buttons der Symbolleiste hinzufuegen
		leiste.add(forward);
		leiste.add(answer);
		
		return leiste;
	}
	
	private void tabelleErstellen() {
		String[] spaltenNamen = {"ID", "Sender", "Betreff", "Text"};
		
		modell = new DefaultTableModel();
		modell.setColumnIdentifiers(spaltenNamen);
		
		tabelle = new JTable();
		tabelle.setModel(modell);
		tabelle.setDefaultEditor(Object.class, null);
		tabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tabelle.setFillsViewportHeight(true);
		
		JScrollPane scroll = new JScrollPane(tabelle);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		add(scroll);
		
		tabelle.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int zeile = tabelle.getSelectedRow();
					String sender, betreff, inhalt, ID;
					ID = tabelle.getModel().getValueAt(zeile, 0).toString();
					sender = tabelle.getModel().getValueAt(zeile, 1).toString();
					betreff = tabelle.getModel().getValueAt(zeile, 2).toString();
					inhalt = tabelle.getModel().getValueAt(zeile, 3).toString();
					new Anzeige(Empfangen.this, true, ID, sender, betreff, inhalt);
				}
			}
		});
	}
	
	private void tabelleAktualisieren() {
		Connection verbindung;
		ResultSet ergebnisMenge;
		String sender, betreff, inhalt, ID;
		
		modell.setRowCount(0);
		
		try {
			verbindung = MiniDBTools.oeffnenDB("jdbc:derby:emailDB");
			ergebnisMenge = MiniDBTools.liefereErgebnis(verbindung, "SELECT * FROM empfangen");
			
			while (ergebnisMenge.next()) {
				ID = ergebnisMenge.getString("iNummer");
				sender = ergebnisMenge.getString("sender");
				betreff = ergebnisMenge.getString("betreff");
				Clob clob;
				clob = ergebnisMenge.getClob("inhalt");
				inhalt = clob.getSubString(1, (int)clob.length());
				
				modell.addRow(new Object[] {ID, sender, betreff, inhalt});
			}
			ergebnisMenge.close();
			verbindung.close();
			MiniDBTools.schliessenDB("jdbc:derby:emailDB");
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
	
	private void nachrichtenEmpfangen() {
		nachrichtenAbholen();
		tabelleAktualisieren();
	}
	
	private void nachrichtenAbholen() {
		//Aufgabe 1: Username und Passwort werden ermittelt
		String benutzername = loginName;
		String kennwort = loginPW;
		String server = "pop3.web.de";
		
		Properties eigenschaften = new Properties();
		eigenschaften.put("mail.store.protocol", "pop3");
		eigenschaften.put("mail.pop3.host", server);
		eigenschaften.put("mail.pop3.port", "995");
		eigenschaften.put("mail.pop3.starttls.enable", "true");
		
		Session sitzung = Session.getDefaultInstance(eigenschaften);
		
		try (Store store = sitzung.getStore("pop3s")){
			store.connect(server, benutzername, kennwort);
			Folder posteingang  = store.getFolder("INBOX");
			posteingang.open(Folder.READ_WRITE);
			
			Message nachrichten[] = posteingang.getMessages();
			
			if (nachrichten.length != 0) {
				JOptionPane.showMessageDialog(this, "Es gibt "+ posteingang.getUnreadMessageCount() + " neue Nachrichten.");
				for(Message nachricht : nachrichten)
					nachrichtVerarbeiten(nachricht);
			}
			else
				JOptionPane.showMessageDialog(this, "Es gibt keine neuen Nachrichten");
			
			posteingang.close(true);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
	
	private void nachrichtVerarbeiten(Message nachricht) {
		try {
			if (nachricht.isMimeType("text/plain")) {
				String sender = nachricht.getFrom()[0].toString();
				String betreff = nachricht.getSubject();
				String inhalt = nachricht.getContent().toString();
				nachrichtSpeichern(sender, betreff, inhalt);
				nachricht.setFlag(Flags.Flag.DELETED, true);
			}
			else
				JOptionPane.showMessageDialog(this, "Der Typ der Nachricht " + nachricht.getContentType() + "kann nicht verarbeitet werden.");
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
	
	private void nachrichtSpeichern( String sender, String betreff, String inhalt) {
		Connection verbindung;
		verbindung=MiniDBTools.oeffnenDB("jdbc:derby:emailDB");
		
		try {
			PreparedStatement prepState;
			prepState = verbindung.prepareStatement("insert into empfangen (sender, betreff, inhalt) values (?, ?, ?)");
			prepState.setString(1, sender);
			prepState.setString(2, betreff);
			prepState.setString(3, inhalt);
			prepState.executeUpdate();
			
			verbindung.commit();
			
			prepState.close();
			verbindung.close();
			MiniDBTools.schliessenDB("jdbc:derby:emailDB");
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
	//Aufgabe 2: es wird die Klasse Weiterleiten aufgerufen. Als Argument wird der Frame, der Dialog modal, der Betreff und der Inhalt an den Konstruktor uebermittelt.
	private void weiterleiten(String betreff, String inhalt) {
		new Weiterleiten(this, true, betreff, inhalt);
	}
	//Aufgabe 2: es wird die Klasse Antworten aufgerufen. Als Argument wird der Frame, der Dialog modal, der Sender, der Betreff und der Inhalt an den Konstruktor uebermittelt.
	private void antworten(String sender, String betreff, String inhalt) {
		new Antworten(this, true, sender, betreff, inhalt);
	}
	//Aufgabe 1: zuerst wird der Benutzername, dann das Password geladen - die Reihenfolge muss so bleiben,
			//da die Daten in der selben Reihenfolge gespeichert wurden.
			//Die geladenen Daten werden je einer String Variable zugeordnet.
	private void loginDatenLaden() {
		
		try (RandomAccessFile datei = new RandomAccessFile(dateiName,"r")){
			loginName = datei.readUTF();
			loginPW = datei.readUTF();
			datei.close();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Fehler beim Logindaten laden");
		}
	}
}
