package me.bibo38.Bibo38Lib.test.config;

import me.bibo38.Bibo38Lib.config.YamlOM;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class YamlOMTest
{
	@Test
	public void testWriteBasicConfiguration()
	{
		StringStreamProvider provider = new StringStreamProvider("");
		new YamlOM(new EasyConfig('y'), provider).save();

		Assertions.assertEquals("works: y\n", provider.output);
	}

	@Test
	public void testReadBasicConfiguration()
	{
		StringStreamProvider provider = new StringStreamProvider("works: y");
		EasyConfig cfg = new EasyConfig('n', 33);
		new YamlOM(cfg, provider).load();

		Assertions.assertEquals('y', cfg.works);
		Assertions.assertEquals(33, cfg.x);
	}

	@Test
	public void testTransientFieldsNotChanged()
	{
		StringStreamProvider provider = new StringStreamProvider("works: y\nx: 7");
		EasyConfig cfg = new EasyConfig('n', 33);
		new YamlOM(cfg, provider).load();

		Assertions.assertEquals('y', cfg.works);
		Assertions.assertEquals(33, cfg.x);
	}

	@Test
	public void testNoOverwriteOnEmptyFile()
	{
		StringStreamProvider provider = new StringStreamProvider("");
		EasyConfig cfg = new EasyConfig('n');
		new YamlOM(cfg, provider).load();

		Assertions.assertEquals('n', cfg.works);
	}

	@Test
	public void testNoOverwriteWhenDataNotMatchingClass()
	{
		StringStreamProvider provider = new StringStreamProvider("blablabla: 7");
		EasyConfig cfg = new EasyConfig('n');
		new YamlOM(cfg, provider).load();

		Assertions.assertEquals('n', cfg.works);
	}

	@Test
	public void testWritingNonConfigurableObject()
	{
		StringStreamProvider provider = new StringStreamProvider("");
		Assertions.assertThrows(IllegalArgumentException.class, new YamlOM(new NotConfigurable(), provider)::save);
	}

	@Test
	public void testReadingNonConfigurableObject()
	{
		StringStreamProvider provider = new StringStreamProvider("i: 7");
		Assertions.assertThrows(IllegalArgumentException.class, new YamlOM(new NotConfigurable(), provider)::load);
	}
}
