package me.bibo38.Bibo38Lib.spout;

import java.io.File;
import java.io.IOException;

import me.bibo38.Bibo38Lib.Startfunc;

public class Spout extends Startfunc
{
	private WebServer server;
	
	/**
	 * Der Konstruktor für den gesamten Spout-Content der Bibo38Lib
	 * 
	 * @param dir Ordner für die Webserver-Dateien (normalerweise cache im plugin Ordner)
	 * @throws IOException Bei Fehlern beim Erstellen des Web-Servers
	 */
	public Spout(File dir) throws IOException
	{
		// Den Webserver zum Laufen bekommen
		server = new WebServer(main.getConfig().getInt("webport"), dir); // Zufälliger Port
		server.start();
		main.getLogger().info("Started WebServer on Port " + server.getPort());
	}
	
	/**
	 * Den WebServer zurückliefern lassen, um Dateien hinzuzufügen oder zu löschen
	 * 
	 * @return das WebServer Objekt
	 */
	public WebServer getServer()
	{
		return server;
	}
}
