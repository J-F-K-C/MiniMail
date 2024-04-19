//ACHTUNG!! Das Programm lässt sich nur fehlerfrei ausführen, wenn javax.activation-1.2.0.jar und javax.mail.jar eingebunden sind
package de.fernschulen.minimail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

public class ErsteMail {

	private static Session verbindungHerstellen() {
		//die Zugangsdaten
		//Sie müssen sie an Ihr Konto anpassen
		String benutzername = "fernschulenjava@gmail.com";
		String kennwort = "***";

		//der Server
		String server = "smtp.gmail.com";

		//die Eigenschaften setzen
		Properties eigenschaften = new Properties();
		//die Authentifizierung über TLS
		eigenschaften.put("mail.smtp.auth", "true");
		eigenschaften.put("mail.smtp.starttls.enable", "true");
		//der Server
		eigenschaften.put("mail.smtp.host", server);
		//der Port zum Versenden
		eigenschaften.put("mail.smtp.port", "587");

		// das Session-Objekt erstellen
		Session sitzung = Session.getInstance(eigenschaften, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(benutzername, kennwort);
			}
		});

		return sitzung;
	}

	private static void nachrichtVerschicken(Session sitzung) {
		//der Empfänger
		String empfaenger = "csiebeck@gmx.de";
		//der Absender
		String absender = "fernschulenjava@gmail.com";

		try {
			//eine neue Nachricht vom Typ MimeMessage erzeugen
			MimeMessage nachricht = new MimeMessage(sitzung);
			//den Absender setzen
			nachricht.setFrom(new InternetAddress(absender));
			//den Empfänger
			nachricht.setRecipients(Message.RecipientType.TO, InternetAddress.parse(empfaenger));
			//den Betreff
			nachricht.setSubject("Ein erster Test");
			//und den Text
			nachricht.setText("Ich bin eine Testnachricht aus einem eigenen Programm.");
			//die Nachricht verschicken
			Transport.send(nachricht);

			JOptionPane.showMessageDialog(null, "Die Nachricht wurde verschickt."); 
		}
		catch (MessagingException e) {
			JOptionPane.showMessageDialog(null, "Problem: \n" + e.toString()); 
		}		
	}

	public static void main(String[] args) {
		//die Sitzung herstellen 
		Session sitzung = verbindungHerstellen();
		//und die Nachricht verschicken
		nachrichtVerschicken(sitzung);
	}
}
