package me.bibo38.Bibo38Lib.config.converter;

import me.bibo38.Bibo38Lib.config.Converter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ConfigurationSerializableConverter implements Converter<ConfigurationSerializable>
{
	private static final String ITEMSTACK_META_KEY_NAME = "meta";

	@SuppressWarnings("unchecked")
	@Override
	public ConfigurationSerializable deserialize(Class<? extends ConfigurationSerializable> cl, Object data)
	{
		DelegateDeserialization delegate = cl.getAnnotation(DelegateDeserialization.class);
		if(delegate != null)
			return deserialize(delegate.value(), data);

		if(!(data instanceof Map))
			return null;

		Map<String, Object> mdata = (Map<String, Object>) data;

		if(ItemStack.class.equals(cl) && mdata.containsKey(ITEMSTACK_META_KEY_NAME))
		{
			// Pre deserialize to find the appropriate ItemMeta class, which depends on the item type
			ItemStack is = (ItemStack) ConfigurationSerialization.deserializeObject(mdata, cl);
			mdata.put(ITEMSTACK_META_KEY_NAME, deserialize(is.getItemMeta().getClass(), mdata.get(ITEMSTACK_META_KEY_NAME)));
		}

		return ConfigurationSerialization.deserializeObject(mdata, cl);
	}

	@Override
	public Object serialize(ConfigurationSerializable elem)
	{
		return elem.serialize();
	}
}
