package de.fernschulen.minimail;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

public class Senden extends JFrame {
	//automatisch über Eclipse ergänzt
	private static final long serialVersionUID = 5398891945440024921L;

	//für die Aktion
	private MeineAktionen sendenAct;

	//für die Tabelle
	private JTable tabelle;
	//für das Modell
	private DefaultTableModel modell;

	//eine innere Klasse für die Aktionen
	class MeineAktionen extends AbstractAction {
		//automatisch über Eclipse ergänzt
		private static final long serialVersionUID = 8673560298548765044L;

		//der Konstruktor 
		public MeineAktionen(String text, ImageIcon icon, String beschreibung, KeyStroke shortcut, String actionText) {
			//den Konstruktor der übergeordneten Klasse mit dem Text und dem Icon aufrufen
			super(text, icon);
			//die Beschreibung setzen für den Bildschirmtipp
			putValue(SHORT_DESCRIPTION, beschreibung);
			//den Shortcut
			putValue(ACCELERATOR_KEY, shortcut);
			//das ActionCommand
			putValue(ACTION_COMMAND_KEY, actionText);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("senden"))
				senden();
		}
	}

	//der Konstruktor
	Senden() {
		super();

		setTitle("E-Mail senden");
		//wir nehmen ein Border-Layout
		setLayout(new BorderLayout());

		//die Aktionen erstellen
		sendenAct = new MeineAktionen("Neue E-Mail", 
				new ImageIcon("icons/mail-generic.gif"), 
				"Erstellt eine neue E-Mail", 
				null,
				"senden");
		//die Symbolleiste oben einfügen
		add(symbolleiste(), BorderLayout.NORTH);

		setVisible(true);
		setSize(700, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		//die Tabelle erstellen und anzeigen
		tabelleErstellen();
		tabelleAktualisieren();
	}

	//die Symbolleiste erzeugen und zurückgeben
	private JToolBar symbolleiste() {
		JToolBar leiste = new JToolBar();
		//die Symbole über die Aktionen einbauen
		leiste.add(sendenAct);

		//die Leiste zurückgeben
		return (leiste);
	}


	//zum Erstellen der Tabelle
	private void tabelleErstellen() {
		//für die Spaltenbezeichner
		String[] spaltenNamen = {"ID", "Empfänger", "Betreff", "Text"};

		//ein neues Standardmodell erstellen
		modell = new DefaultTableModel();
		//die Spaltennamen setzen
		modell.setColumnIdentifiers(spaltenNamen);
		//die Tabelle erzeugen
		tabelle = new JTable();
		//und mit dem Modell verbinden
		tabelle.setModel(modell);
		//wir haben keinen Editor, können die Tabelle also nicht bearbeiten
		tabelle.setDefaultEditor(Object.class, null);
		//es sollen immer alle Spalten angepasst werden
		tabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		//und die volle Größe genutzt werden
		tabelle.setFillsViewportHeight(true);
		//die Tabelle setzen wir in ein Scrollpane
		JScrollPane scroll = new JScrollPane(tabelle);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scroll);
		
		//einen Maus-Listener ergänzen
		tabelle.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				//war es ein Doppelklick?
				if (e.getClickCount() == 2) {
					//die Zeile beschaffen
					int zeile = tabelle.getSelectedRow();
					//die Daten beschaffen
					String empfaenger, betreff, inhalt, ID;
					ID = tabelle.getModel().getValueAt(zeile, 0).toString();
					empfaenger = tabelle.getModel().getValueAt(zeile, 1).toString();
					betreff = tabelle.getModel().getValueAt(zeile, 2).toString();
					inhalt = tabelle.getModel().getValueAt(zeile, 3).toString();
					//und anzeigen
					//übergeben wird der Frame der äußeren Klasse
					new Anzeige(Senden.this, true, ID, empfaenger, betreff, inhalt);
				}
			}
		});
	}

	//zum Aktualisieren der Tabelle
	private void tabelleAktualisieren() {
		//für den Datenbankzugriff
		Connection verbindung;
		ResultSet ergebnisMenge;
		
		//für die Spalten
		String empfaenger, betreff, inhalt, ID;
		//die Inhalte löschen
		modell.setRowCount(0);

		try{
			//Verbindung herstellen und Ergebnismenge beschaffen
			verbindung = MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
			ergebnisMenge = MiniDBTools.liefereErgebnis(verbindung, "SELECT * FROM gesendet");
			//die Einträge in die Tabelle schreiben
			while (ergebnisMenge.next()) {
				ID = ergebnisMenge.getString("iNummer");
				empfaenger = ergebnisMenge.getString("empfaenger");
				betreff = ergebnisMenge.getString("betreff");
				//den Inhalt vom CLOB beschaffen und in einen String umbauen
				Clob clob;
				clob = ergebnisMenge.getClob("inhalt");
				inhalt = clob.getSubString(1, (int) clob.length());

				//die Zeile zum Modell hinzufügen
				//dazu benutzen wir ein Array vom Typ Object
				modell.addRow(new Object[] {ID, empfaenger, betreff, inhalt} );
			}
			//die Verbindungen wieder schließen und trennen
			ergebnisMenge.close();
			verbindung.close();
			MiniDBTools.schliessenDB("jdbc:derby:mailDB");
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}

	//zum Senden
	private void senden() {
		//den Dialog für eine neue Nachricht modal anzeigen
		new NeueNachricht(this, true);
		//nach dem Versenden lassen wir die Anzeige aktualisieren
		tabelleAktualisieren();
	}
}