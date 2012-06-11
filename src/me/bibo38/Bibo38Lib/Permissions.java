package me.bibo38.Bibo38Lib;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Permissions
{
	protected static Bibo38Lib main;
	
	private String father; // Der Vater von den Permissions, also das Plugin
	private net.milkbowl.vault.permission.Permission perm = null; // Um Verwechselung zu vermeiden
	
	/**
	 * Konstruktor zum Erzeugen von Permissions
	 * 
	 * @param efather Hauptknoten der Permissions
	 */
	public Permissions(String efather)
	{
		father = efather + ".";
		
		// Konfigurieren von Vault
		if(main.vaultOn)
		{
			RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp =
					main.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if(rsp == null)
			{
				return;
			}
			perm = rsp.getProvider();
		}
	}
	
	/**
	 * Prüft, ob die Klasse korrekt mit Vault konfiguriert wurde.
	 * 
	 * @return true falls falsch konfiguriert
	 */
	public boolean vaultError()
	{
		if(perm == null) // Falls perm noch null ist ist ein Fehler aufgetreten
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Funktion zum Überprüfen von Permissions
	 * für Plugins
	 * 
	 * @param player Spieler für den die Permission zutreffen soll
	 * @param permstr Permission Node
	 * @return true falls der Spieler die Permission besitzt
	 */
	public boolean hasPerm(Player player, String permstr)
	{
		if(main.vaultOn && !this.vaultError())
		{
			return perm.has(player, father + permstr);
		} else
		{
			return player.hasPermission(father + permstr);
		}
	}
}
