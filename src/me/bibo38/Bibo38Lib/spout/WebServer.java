package me.bibo38.Bibo38Lib.spout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;

import me.bibo38.Bibo38Lib.Startfunc;

public class WebServer extends Thread
{
	private static final int BUFFER_SIZE = 1024;
	private static HashSet<WebServer> runningServer = new HashSet<WebServer>();

	private File mainDir;
	private int port;
	
	private ServerSocket server;
	private boolean stopserver = false;
	
	private HashMap<String, WebService> services = new HashMap<String, WebService>();
	
	/**
	 * Der Konstruktor des WebServers
	 * 
	 * @param eport Der Port des Servers! 0 ist ein zufälliger Port
	 * @param emainDir Der Hauptordner des Web-Servers
	 * @throws IOException Bei Fehlern, z.B. schon benutzter Port
	 */
	public WebServer(int eport, File emainDir) throws IOException
	{
		mainDir = emainDir;
		
		server = new ServerSocket(eport);
		port = server.getLocalPort();
		Startfunc.getMain().getLogger().info("Started WebServer on Port " + port);
		runningServer.add(this);
	}
	
	/**
	 * Den vom Webserver benutzten port zurückliefern lassen!
	 * 
	 * @return Der Port
	 */
	public int getPort()
	{
		return port;
	}
	
	/**
	 * Die IP-Adresse des Servers ermitteln
	 * 
	 * @return Einen String mit der Ip-Adresse
	 */
	public String getIp()
	{
		return Startfunc.getMain().getServer().getIp();
	}
	
	/**
	 * Eine Datei auf den Webserver kopieren
	 * 
	 * @param in InputStream der Datei
	 * @param prefix Ein beliebiger Präfix
	 * @param suffix Der Dateien Suffix (ohne ., sondern nur z.B. png)
	 * @return Den URL zur hochgeladenen Datei
	 * @throws IOException Fehler beim Erstellen der Datei
	 */
	public String addFile(InputStream in, String prefix, String suffix) throws IOException
	{
		// Datei schreiben
		File file = File.createTempFile(prefix, "." + suffix, mainDir);
		
		OutputStream out = new FileOutputStream(file);
		while(true)
		{
			int tmp = in.read();
			if(tmp < 0)
			{
				break;
			} else
			{
				out.write(tmp);
			}
		}
		out.flush();
		out.close();
		
		file.deleteOnExit(); // Am Ende löschen
		return "http://" + this.getIp() + ":" + this.getPort() + "/" + file.getName();
	}
	
	/**
	 * Datei vom Web-Server löschen
	 * 
	 * @param name Den Namen der Datei
	 * @return true, falls erfolgreich gelöscht
	 */
	public boolean removeFile(String name)
	{
		if(!(new File(mainDir, name)).exists())
		{
			return false;
		}
		
		new File(mainDir, name).delete();
		return true;
	}
	
	@Override
	/**
	 * Der Web-Server
	 */
	public void run() // Mini Web Server :)
	{
		while(!stopserver)
		{
			// Auf Anfragen warten
			try
			{
				Socket sock = server.accept();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				OutputStream out = sock.getOutputStream();
				
				StringBuffer daten = new StringBuffer();
				while(true)
				{
					String tmp = in.readLine();
					
					if(tmp == null || tmp.isEmpty()) // Doppeltes \n?
					{
						break;
					} else
					{
						daten.append(tmp + "\n");
					}
				}
				// System.out.println("Empfangen: " + daten.toString());
				// Bild suchen
				// Dateinamen finden
				if(daten.toString().equals(""))
				{
					continue;
				}
				// System.out.println(daten.toString());
				Header header = new Header(daten.toString());
				//System.out.println(header.getFile() + "\n" + header.getMethod() + "\n" + header.getVersion() + "\n" + header.toString());
				String datei = header.getFile();
				
				// Zurücksenden
				if(services.containsKey(datei))
				{
					// Es gibt den Service, also diesen aufrufen
					services.get(datei).recive(datei, header, out, sock.getInetAddress());
				} else if(new File(mainDir, datei).exists())
				{
					out.write("HTTP/1.0 200 Ok\r\n\r\n".getBytes());
					
					// Datei zurückgeben
					InputStream orgin = new FileInputStream(new File(mainDir, datei));
					byte tmp[] = new byte[BUFFER_SIZE];
					while(true)
					{
						int cnt;
						if((cnt = orgin.read(tmp)) < 0)
						{
							break;
						} else
						{
							out.write(tmp, 0, cnt);
						}
					}
					
					orgin.close();
				} else
				{
					out.write("HTTP/1.0 404 Not Found\r\n\r\n".getBytes());
				}
				
				out.flush();
				sock.shutdownOutput();
				
				sock.close();
			} catch(SocketException e)
			{
				// Nichts machen, da es ein Connection Reset sein könnte, einfach nur chillen
			} catch (IOException e)
			{
				// Pech gehabt, Fehler beim Abrufen
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Registriert einen Service
	 * 
	 * @param file Die Datei, bei der der Service aktiviert werden soll
	 * @param service Der WebService
	 * @return true bei erfolgreicher Registrierung
	 */
	public boolean registerService(String file, WebService service)
	{
		if(services.containsKey(file))
		{
			return false;
		}
		
		services.put(file, service); // WebService registrieren
		
		return true;
	}
	
	public void stopServer()
	{
		stopserver = true;
		try
		{
			server.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		server = null;
		port = -1;
		runningServer.remove(this);
	}
	
	public static void stopAll()
	{
		for(WebServer s : runningServer)
			s.stopServer();
		runningServer.clear();
	}
	
	public static Header acceptCors(Header header)
	{
		// CORS behandeln und akzeptieren, sodass Content gesendet werden darf
		// https://developer.mozilla.org/en-US/docs/HTTP_access_control
		// Ersteinmal prüfen
		
		if(header.getMethod().equals("OPTIONS"))
		{
			// Ok, klappt
			// neuen Header erstellen
			Header neuhead = new Header(200, 1.0);
			neuhead.setHeader("Access-Control-Allow-Origin", header.getHeader("Origin"));
			neuhead.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
			neuhead.setHeader("Access-Control-Allow-Headers", header.getHeader("Access-Control-Request-Headers"));
			neuhead.setHeader("Access-Control-Max-Age", "1728000");
			
			return neuhead;
		} else
		{
			return null;
		}
	}
}
