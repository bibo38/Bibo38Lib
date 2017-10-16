package me.bibo38.Bibo38Lib.test.config;

import me.bibo38.Bibo38Lib.config.Configurable;

@Configurable
public class EasyConfig
{
	public transient int x = 5;
	public char works;

	public EasyConfig(char works, int x)
	{
		this.works = works;
		this.x = x;
	}

	public EasyConfig(char works)
	{
		this(works, 0);
	}
}
