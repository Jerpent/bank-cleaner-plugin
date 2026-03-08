package com.github.jerpent.bankcleaner;

import lombok.Value;

@Value
public class RedundantItem
{
	public enum Category
	{
		SELL,       // tradeable + outclassed
		DROP,       // untradeable + outclassed
		DEGRADEABLE // needs manual review
	}

	int itemId;
	String name;
	String reason;          // e.g. "Outclassed by Abyssal whip"
	String dominatedBy;     // name of the better item, null for DEGRADEABLE
	String statComparison;  // HTML string showing differing stats, null for DEGRADEABLE
	Category category;
	boolean ignored;
}
