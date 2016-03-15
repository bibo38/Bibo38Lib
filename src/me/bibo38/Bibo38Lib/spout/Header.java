package me.bibo38.Bibo38Lib.spout;

import java.util.HashMap;
import java.util.Iterator;

public class Header
{
	private HashMap<String, String> header;
	
	private boolean request; // Ist es eine Anfrage(GET ... HTTP/...) oder Antwort(HTTP/... 200 Ok)
	
	private int status; // Für Antwort
	private byte content[] = null;
	
	private double version;
	private String file, method;
	
	private HashMap<Integer, String> httpcodes;
	
	/**
	 * Konstruktor für einen HTTP Header
	 * 
	 * @param header Der Header als ein voller String
	 */
	public Header(String eheader)
	{
		header = new HashMap<String, String>();
		header.clear();
		
		if("HTTP".equals(eheader.substring(0, 4)))
		{
			request = false;
		} else
		{
			request = true;
		}
		
		// Header parsen
		boolean first = true;
		for(String part : eheader.split("\n"))
		{
			if(first)
			{
				first = false;
				if(request)
				{
					method = part.substring(0, part.indexOf(" "));
					version = Double.parseDouble(part.substring(part.indexOf(" HTTP/") + 6, part.indexOf(" HTTP/") + 9));
					
					file = part.substring(part.indexOf(" ") + 2);
					file = file.substring(0, file.indexOf(" "));
				} else
				{
					// Response :)
					version = Double.parseDouble(part.substring(6, 9));
					status = Integer.parseInt(part.substring(part.indexOf(" ") + 1, part.indexOf(" ") + 4));
				}
			} else if(!part.isEmpty())
			{
				String key = part.substring(0, part.indexOf(":"));
				String value = part.substring(part.indexOf(":") + 2);
				header.put(key, value); // Den Header hinzufügen
			}
		}
	}
	
	/**
	 * Konstruktor zum Erstellen eigener Header
	 * 
	 * @param efile Die aufzurufende Datei
	 * @param emethod Die zu verwendene Methode
	 * @param eversion Die HTTP Version
	 */
	public Header(String efile, String emethod, double eversion)
	{
		request = true;
		file = efile;
		method = emethod;
		version = eversion;
		
		header = new HashMap<String, String>();
		header.clear();
	}
	
	/**
	 * Konstruktor zum Erstellen eines eigenen Response Header
	 * 
	 * @param estatus Der Status der Response
	 * @param eversion Die HTTP Version
	 * @param econtent Der HTTP Response Content
	 */
	public Header(int estatus, double eversion, byte econtent[])
	{
		this(estatus, eversion);
		content = econtent;
	}
	
	/**
	 * Konstruktor zum Erstellen eines eigenen Response Header
	 * 
	 * @param estatus Der Status der Response
	 * @param eversion Die HTTP Version
	 */
	public Header(int estatus, double eversion)
	{
		request = false; // Antwort
		status = estatus;
		version = eversion;
		
		header = new HashMap<String, String>();
		header.clear();
	}
	
	/**
	 * Funktion zum Ermitteln der HTTP Methode
	 * 
	 * @return Http Methode (z.b. GET)
	 * @throws NullPointerException
	 */
	public String getMethod() throws NullPointerException
	{
		if(!request)
		{
			throw new NullPointerException("Es gibt keine Methode in einer Response");
		}
		
		return method;
	}
	
	/**
	 * Funktion zum Ermitteln der HTTP Version
	 * 
	 * @return Die HTTP Version (1.0 oder 1.1)
	 */
	public double getVersion()
	{
		return version;
	}
	
	/**
	 * Funktion zum Ermitteln der angefragten Datei
	 * 
	 * @return Die angefragte Datei
	 * @throws NullPointerException
	 */
	public String getFile() throws NullPointerException
	{
		if(!request)
		{
			throw new NullPointerException("Es gibt keine Datei in einer Response");
		}
		
		return file;
	}
	
	/**
	 * Funktion zum Ermitteln des Wertes eines spezifischen Headers
	 * 
	 * @param name Der Name des Headers
	 * @return Den Wert des Headers oder null falls dieser nicht existiert
	 */
	public String getHeader(String name)
	{
		return header.get(name);
	}
	
	/**
	 * Funktion zum setzen des Wertes eines spezifischen Headers
	 * 
	 * @param name Der Name des Headers
	 * @param value Den Wert des Headers
	 */
	public void setHeader(String name, String value)
	{
		header.put(name, value);
	}
	
	/**
	 * Funktion zum Ermitteln des Statuses einer Response
	 * 
	 * @return den Statuscode
	 * @throws NullPointerException
	 */
	public int getStatus() throws NullPointerException
	{
		if(request)
		{
			throw new NullPointerException("Es gibt keinen Status in einem Request");
		}
		
		return status;
	}
	
	/**
	 * Funktion zum ermitteln des Content einer Response
	 * 
	 * @return Den Content der Response
	 * @throws NullPointerException
	 */
	public byte[] getContent() throws NullPointerException
	{
		if(request)
		{
			throw new NullPointerException("Es gibt keinen Status in einem Request");
		}
		
		return content;
	}
	
	@Override
	public String toString()
	{
		httpcodes = new HashMap<Integer, String>();
		httpcodes.clear();
		
		httpcodes.put(200, "Ok");
		httpcodes.put(201, "Created");
		httpcodes.put(202, "Accepted");
		httpcodes.put(203, "Non Authoritative Information");
		httpcodes.put(204, "No Content");
		httpcodes.put(205, "Reset Content");
		httpcodes.put(206, "Partial Content");
		
		httpcodes.put(300, "Multiple Choices");
		httpcodes.put(301, "Moved Permanently");
		httpcodes.put(302, "Moved Temporarily");
		httpcodes.put(303, "See Other");
		httpcodes.put(304, "Not Modified");
		httpcodes.put(305, "Use Proxy");
		
		httpcodes.put(400, "Bad Request");
		httpcodes.put(401, "Unauthorized");
		httpcodes.put(402, "Payment Required");
		httpcodes.put(403, "Forbidden");
		httpcodes.put(404, "Not Found");
		httpcodes.put(405, "Method Not Allowed");
		httpcodes.put(406, "Not Acceptable");
		httpcodes.put(407, "Proxy Authentication Required");
		httpcodes.put(408, "Request Timeout");
		httpcodes.put(409, "Conflict");
		httpcodes.put(410, "Gone");
		httpcodes.put(411, "Length Required");
		httpcodes.put(412, "Precondition Failed");
		httpcodes.put(413, "Request Entity Too Large");
		httpcodes.put(414, "Request-URI Too Long");
		httpcodes.put(415, "Unsupported Media Type");
		httpcodes.put(416, "Request range not satisfiable");
		httpcodes.put(417, "Expectation failed");
		
		httpcodes.put(500, "Internal Server Error");
		httpcodes.put(501, "Not Implemented");
		httpcodes.put(502, "Bad Gateway");
		httpcodes.put(503, "Service Unavailable");
		httpcodes.put(504, "Gateway Timeout");
		httpcodes.put(505, "HTTP Version Not Supported");
		
		
		String output = "";
		
		// Erste Zeile
		if(request)
		{
			output += this.getMethod() + " /" + this.getFile() + " HTTP/" + this.getVersion() + "\r\n";
		} else
		{
			output += "HTTP/" + this.getVersion() + " " + this.getStatus() + " " + httpcodes.get(this.getStatus()) + "\r\n";
		}
		
		Iterator<String> it = header.keySet().iterator();
		
		while(it.hasNext())
		{
			String key = it.next();
			output += key + ": " + header.get(key) + "\r\n";
		}
		
		output += "\r\n"; // Ende
		
		if(request || content == null)
		{
			return output;
		} else
		{
			return output + new String(this.getContent());
		}
	}
}
