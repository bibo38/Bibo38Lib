package me.bibo38.Bibo38Lib;

import me.bibo38.Bibo38Lib.config.Configurable;

@Configurable
public class Bibo38LibConfig
{
	public String lang                 = "en";
	public char helpcolor              = 'e';
	public boolean fullopperm          = true;
	public DatabaseConnection database = new DatabaseConnection();

	@Configurable
	public static class DatabaseConnection
	{
		public String url  = "jdbc:mysql://localhost/minecraft";
		public String user = "root";
		public String pass = "passwd";
	}
}
