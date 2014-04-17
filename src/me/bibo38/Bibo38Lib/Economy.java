package me.bibo38.Bibo38Lib;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Economy extends Startfunc
{
	private net.milkbowl.vault.economy.Economy eco = null;
	
	/**
	 * Konstruktor für Economy
	 * Benötigt Vault!
	 * 
	 * @throws NullPointerException Falls kein Vault vorhanden ist
	 */
	public Economy() throws NullPointerException
	{
		if(main.vaultOn) // Konfiguriere Vault
		{
			RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp =
					main.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if(rsp == null)
			{
				throw new NullPointerException("Could not activate Vault!");
			}
			eco = rsp.getProvider();
		} else
		{
			throw new NullPointerException("You must have activated Vault!");
		}
	}
	
	/**
	 * Gibt den aktuellen Kontostand aus
	 * 
	 * @param player Der Spieler
	 * @return Der aktuelle Geldbetrag des Spielers
	 */
	public double getMoney(Player player)
	{
		return eco.getBalance(player.getName());
	}
	
	/**
	 * Gibt den aktuellen Kontostand aus
	 * 
	 * @param player Der Name des Spieler
	 * @return Der aktuelle Geldbetrag des Spielers
	 */
	public double getMoney(String player)
	{
		return eco.getBalance(player);
	}
	
	/**
	 * Setzt den Kontostand eines Spielers
	 * 
	 * @param player Des Spieler, dessen Geld gesetzt werden soll
	 * @param amount Der Geldbetrag, der gesetzt werden soll
	 */
	public void setMoney(Player player, double amount)
	{
		this.setMoney(player.getName(), amount);
	}
	
	/**
	 * Setzt den Kontostand eines Spielers
	 * 
	 * @param player Des Spieler, dessen Geld gesetzt werden soll
	 * @param amount Der Geldbetrag, der gesetzt werden soll
	 */
	public void setMoney(String player, double amount)
	{
		double aktamount = eco.getBalance(player);
		
		this.giveMoney(player, amount - aktamount); // Das Geld abziehen/aufladen
	}
	
	/**
	 * Dem Spieler einen Geldbetrag geben/abziehen
	 * 
	 * @param player Der Spieler
	 * @param amount Der Betrag, der aufgeladen(+) oder abgezugen(-) werden soll
	 */
	public void giveMoney(Player player, double amount)
	{
		this.giveMoney(player.getName(), amount);
	}
	
	/**
	 * Dem Spieler einen Geldbetrag geben/abziehen
	 * 
	 * @param player Der Spieler
	 * @param amount Der Betrag, der aufgeladen(+) oder abgezugen(-) werden soll
	 */
	public void giveMoney(String player, double amount)
	{
		if(amount == 0)
		{
			return;
		}
		
		if(amount < 0) // Bei kleineren Beträgen
		{
			eco.withdrawPlayer(player, -amount);
		} else
		{
			eco.depositPlayer(player, amount); // Sonst aufladen
		}
	}
	
	/**
	 * Den Währungsnamen ausgaben
	 * 
	 * @param plural Soll es der Plural sein
	 * @return Den Währungsnamen
	 */
	public String getCurrency(boolean plural)
	{
		if(plural)
			return eco.currencyNamePlural();
		else
			return eco.currencyNameSingular();
	}
}
