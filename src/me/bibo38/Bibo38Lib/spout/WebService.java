package me.bibo38.Bibo38Lib.spout;

import java.io.OutputStream;
import java.net.InetAddress;

public interface WebService
{
	/**
	 * Wird beim Empfangen von einem Request aufgerufen
	 * 
	 * @param file Die Datei, die angefordert wurde
	 * @param header Der Header von der Anfrage
	 * @param out Der Output Stream für die Rückgabe
	 * @param addr Die Internet Adresse vom Absender
	 */
	void recive(String file, Header header, OutputStream out, InetAddress addr);
}
