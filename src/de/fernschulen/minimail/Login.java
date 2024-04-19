package de.fernschulen.minimail;

import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JOptionPane;
//Hier werden der Username und das Passwort in einer Datei gespeichert, welche dann im Emailprogramm geladen werden.
public class LoginDatenCreator {	

	public static void main(String[] args) {
		String benutzername = null;
		String password = null;
		String dateiName = "logindaten.dat";
		
		benutzername = JOptionPane.showInputDialog("Bitte den Benutzernamen eingeben");
		password = JOptionPane.showInputDialog("Bitte das Password eingeben");		
		
		if (benutzername != null && password != null) {
		try (RandomAccessFile datei = new RandomAccessFile(dateiName,"rw")){
			datei.writeUTF(benutzername);
			datei.writeUTF(password);
			datei.close();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fehler beim Schreiben");
		}
		
		try (RandomAccessFile datei = new RandomAccessFile(dateiName,"r")){
			System.out.println("Gespeicherter Benutzername: " + datei.readUTF());
			System.out.println("Gespeichertes Password: " + datei.readUTF());
			datei.close();
			
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fehler beim Laden");
		}
		}
	}
}
