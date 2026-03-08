package com.github.jerpent.bankcleaner;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BankCleanerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BankCleanerPlugin.class);
		RuneLite.main(args);
	}
}