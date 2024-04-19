package de.fernschulen.minimail;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class NeueNachricht extends JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6977844606043674314L;
	
	private JTextField empfaenger, betreff;
	private JTextArea inhalt;
	private JButton ok, abbrechen;
	//Aufgabe 1: fuer den Usernamen, Passwort und den Namen der Datei in welcher die Daten gespeichert sind.
	private String loginName = "", loginPW = "", dateiName = "logindaten.dat";
	
	class NeuListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("senden"))
				senden();
			if (e.getActionCommand().equals("abbrechen"))
				dispose();
		}		
	}
	
	public NeueNachricht(JFrame parent, boolean modal) {
		super(parent, modal);
		//Aufgabe 1: Die Methode wird im Konstruktor ausgefuehrt, damit die Daten beim Start geladen werden
		loginDatenLaden();
		
		initGui();
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	private void initGui(){
		setLayout(new BorderLayout());
		JPanel oben = new JPanel();
		oben.setLayout(new GridLayout(0,2));
		oben.add(new JLabel("Empfaenger:"));
		empfaenger = new JTextField();
		oben.add(empfaenger);
		oben.add(new JLabel("Betreff:"));
		betreff = new JTextField();
		oben.add(betreff);
		add(oben, BorderLayout.NORTH);
		inhalt = new JTextArea();
		inhalt.setLineWrap(true);
		inhalt.setWrapStyleWord(true);
		JScrollPane scroll = new JScrollPane(inhalt);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scroll);
		
		JPanel unten = new JPanel();
		ok = new JButton("Senden");
		ok.setActionCommand("senden");
		abbrechen = new JButton("Abbrechen");
		abbrechen.setActionCommand("abbrechen");
		
		NeuListener listener = new NeuListener();
		ok.addActionListener(listener);
		abbrechen.addActionListener(listener);
		
		unten.add(ok);
		unten.add(abbrechen);
		add(unten, BorderLayout.SOUTH);
		
		setSize(600, 300);
		setVisible(true);
	}
	
	private void senden() {
		Session sitzung;
		
		sitzung = verbindungHerstellen();
		nachrichtVerschicken(sitzung);
		nachrichtSpeichern();
	}
	
	private Session verbindungHerstellen() {
		//Aufgabe 1: Username und Passwort werden aus Variablen ermittelt.
		String benutzername = loginName;
		String kennwort = loginPW;
		String server = "smtp.web.de";
		
		Properties eigenschaften = new Properties();
		eigenschaften.put("mail.smtp.auth", "true");
		eigenschaften.put("mail.smtp.starttls.enable", "true");
		eigenschaften.put("mail.smtp.host", server);
		eigenschaften.put("mail.smtp.port", "587");
		
		Session sitzung = Session.getInstance(eigenschaften, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(benutzername, kennwort);
			}
		});
		
		return sitzung;
	}
	
	private void nachrichtVerschicken(Session sitzung) {
		String absender = loginName;
		
		try {
			MimeMessage nachricht = new MimeMessage(sitzung);
			nachricht.setFrom(new InternetAddress(absender));
			nachricht.setRecipients(Message.RecipientType.TO, InternetAddress.parse(empfaenger.getText()));
			nachricht.setSubject(betreff.getText());
			nachricht.setText(inhalt.getText());
			Transport.send(nachricht);
			
			JOptionPane.showMessageDialog(this, "Die Nachricht wurde verschickt.");
			
			dispose();
		}
		catch(MessagingException e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
	
	private void nachrichtSpeichern() {
		Connection verbindung;
		
		verbindung = MiniDBTools.oeffnenDB("jdbc:derby:emailDB");
		
		try {
			PreparedStatement prepState;
			prepState = verbindung.prepareStatement("insert into gesendet (empfaenger, betreff, inhalt) values (?,?,?)");
			prepState.setString(1, empfaenger.getText());
			prepState.setString(2, betreff.getText());
			prepState.setString(3, inhalt.getText());
			prepState.executeUpdate();
			verbindung.commit();
			
			prepState.close();
			verbindung.close();
			MiniDBTools.schliessenDB("jdbc:derby:emailDB");
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
	
	private void loginDatenLaden() {
		//Aufgabe 1: zuerst wird der Username, dann das Passwort geladen - die Reihenfolge muss so bleiben, da die Daten in dieser Reihenfolge gespeichert wurden.
		//Die geladenen Daten werden je einer String Variable zugeordnet.
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
