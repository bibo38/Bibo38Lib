package me.bibo38.Bibo38Lib;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Permissions extends Startfunc
{
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
	 * PrÃ¼ft, ob die Klasse korrekt mit Vault konfiguriert wurde.
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
	 * Funktion zum ÃœberprÃ¼fen von Permissions
	 * fÃ¼r Plugins
	 * 
	 * @param player Der zu Ã¼berprÃ¼fende Spieler
	 * @param permstr Permission Node
	 * @param showError Soll ein Fehler dem Spieler gezeigt werden?
	 * @return true falls der Spieler die Permission besitzt
	 */
	public boolean hasPerm(Player player, String permstr, boolean showError)
	{
		if(player == null) // Falls es die konsole ist!
		{
			return true; // Hat Zugriff
		}
		
		boolean erg = false;
		if(main.vaultOn && !this.vaultError())
		{
			if(perm.has(player, father + permstr) || perm.has(player, father + "*") || perm.has(player, "*"))
			{
				erg = true;
			} else
			{
				erg = false;
			}
		} else
		{
			if(player.hasPermission(father + permstr) || player.hasPermission(father + "*") || player.hasPermission("*"))
			{
				erg = true;
			} else
			{
				erg = false;
			}
		}
		
		if(!erg && showError) // Falls es nicht erlaubt ist!
		{
			player.sendMessage(ChatColor.RED + "You don't have permissions to perform this operation!");
		}
		
		return erg;
	}
	
	/**
	 * Funktion zum ÃœberprÃ¼fen von Permissions
	 * fÃ¼r Plugins und die Ausgabe einer Fehlermeldung an den Player
	 * 
	 * @param player Spieler fÃ¼r den die Permission zutreffen soll
	 * @param permstr Permission Node
	 * @return true falls der Spieler die Permission besitzt
	 */
	public boolean hasPerm(Player player, String permstr)
	{
		return this.hasPerm(player, permstr, true);
	}
	
	/**
	 * Funktion zum ÃœberprÃ¼fen von Permissions
	 * fÃ¼r Plugins und die Ausgabe einer Fehlermeldung an den Player
	 * 
	 * @param cs Der CommandSender, kann eine Konsole sein
	 * @param permstr Permission Node
	 * @return true falls der CommandSender Berechtigungen hat
	 */
	public boolean hasPerm(CommandSender cs, String permstr)
	{
		if(cs instanceof Player)
		{
			return this.hasPerm((Player) cs, permstr, true);
		} else
		{
			return true;
		}
	}
	
	/**
	 * Funktion zum ÃœberprÃ¼fen von Permissions
	 * fÃ¼r Plugins
	 * 
	 * @param cs Der CommandSender, kann eine Konsole sein
	 * @param permstr Permission Node
	 * @param showError Soll eine Fehlermeldung an den CommandSender gesendet werden?
	 * @return true falls der CommandSender Berechtigungen hat
	 */
	public boolean hasPerm(CommandSender cs, String permstr, boolean showError)
	{
		if(cs instanceof Player)
		{
			return this.hasPerm((Player) cs, permstr, showError);
		} else
		{
			return true;
		}
	}
	
	/**
	 * Fügt eine Permission einem Player für eine
	 * spezielle Welt hinzu!
	 * 
	 * @param player Der Spielername
	 * @param permission Die Permission
	 * @param welt Die Welt
	 */
	public void addPerm(String player, String permission, World welt)
	{
		if(main.vaultOn)
		{
			perm.playerAdd(welt, player, father + permission);
		} else
		{
			Player spieler = main.getServer().getPlayer(player);
			
			PermissionAttachment pattach = spieler.addAttachment(main);
			pattach.setPermission(father + permission, true);
			spieler.removeAttachment(pattach);
		}
	}
	
	/**
	 * Fügt eine Permission einem Player für eine
	 * spezielle Welt hinzu!
	 * 
	 * @param player Der Spielername
	 * @param permission Die Permission
	 * @param welt Der Weltenname
	 */
	public void addPerm(String player, String permission, String welt)
	{
		this.addPerm(player, permission, main.getServer().getWorld(welt));
	}
	
	/**
	 * Fügt eine globale Permissions für
	 * einen Player hinzu
	 * 
	 * @param player Der Spielername
	 * @param permission Die Permission
	 */
	public void addPerm(String player, String permission)
	{
		this.addPerm(player, permission, (World) null);
	}
	
	/**
	 * Entfernt eine Permission eines Players
	 * für eine spezielle Welt!
	 * 
	 * @param player Der Spielername
	 * @param permission Die Permission
	 * @param welt Die Welt
	 */
	public void remPerm(String player, String permission, World welt) // Permission nur auf false setzen nicht löschen :)
	{
		if(main.vaultOn)
		{
			perm.playerRemove(welt, player, permission);
		} else
		{
			Player spieler = main.getServer().getPlayer(player);
			
			PermissionAttachment pattach = spieler.addAttachment(main);
			pattach.setPermission(permission, false);
			spieler.removeAttachment(pattach);
		}
	}
	
	/**
	 * Entfernt eine Permission eines Players
	 * für eine spezielle Welt!
	 * 
	 * @param player Der Spielername
	 * @param permission Die Permission
	 * @param welt Der Weltenname
	 */
	public void remPerm(String player, String permission, String welt)
	{
		this.remPerm(player, permission, main.getServer().getWorld(welt));
	}
	
	/**
	 * Entfernt eine globale Permission
	 * von einen Player!
	 * 
	 * @param player Der Spielername
	 * @param permission Die Permission
	 */
	public void remPerm(String player, String permission)
	{
		this.remPerm(player, permission, (World) null);
	}
}
