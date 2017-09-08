package me.bibo38.Bibo38Lib;

import java.util.Arrays;

public final class Base64
{
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String SINGLE_PADDING = "=";
	private static final String DOUBLE_PADDING = SINGLE_PADDING + SINGLE_PADDING;
	private static final int ENCODED_SIZE = 4;
	private static final int DECODED_SIZE = 3; // 3 Bytes to 4 characters
	
	
	private Base64() {}
	
	public static String encode(byte data[])
	{
		String ret = "";
		int conv[] = new int[ENCODED_SIZE];
		int padding = data.length % DECODED_SIZE;
		
		if(padding != 0)
		{
			padding = DECODED_SIZE - padding; // 1 byte to much means two bytes padding
			data = Arrays.copyOf(data, data.length + padding);
			
			// Ensure the rest is filled up with zeros
			data[data.length - 1] = 0;
			if(padding == 2)
				data[data.length - 2] = 0;
		}
			
		for(int i = 0; i < data.length; i += DECODED_SIZE)
		{
			conv[0] = (data[i] & 0xFF) >> 2;
			conv[1] = ((data[i] & 0x3) << 4) | ((data[i + 1] & 0xFF) >> 4);
			conv[2] = ((data[i + 1] & 0xF) << 2) | ((data[i + 2] & 0xFF) >> 6);
			conv[3] = data[i + 2] & 0x3F;
			
			for(int j = 0; j < ENCODED_SIZE; j++)
				ret += CHARACTERS.charAt(conv[j]);
		}
		
		if(padding != 0)
			ret = ret.substring(0, ret.length() - padding) + ((padding == 1)? SINGLE_PADDING : DOUBLE_PADDING);
		
		return ret;
	}
	
	public static byte[] decode(String encoded)
	{
		encoded = encoded.replace(SINGLE_PADDING, "");
		int padding = encoded.length() % ENCODED_SIZE;
		
		if(padding != 0)
		{
			if(padding == 1)
				throw new IllegalArgumentException("Single remaining encoded character is not possible");
			padding = ENCODED_SIZE - padding;
			encoded += (padding == 1)? SINGLE_PADDING : DOUBLE_PADDING;
		}
		
		byte ret[] = new byte[encoded.length() / ENCODED_SIZE * DECODED_SIZE - padding];
		int conv[] = new int[ENCODED_SIZE];
		
		for(int i = 0; i < ret.length; i += DECODED_SIZE)
		{
			char aktEnc[] = encoded.substring(0, ENCODED_SIZE).toCharArray();
			encoded = encoded.substring(ENCODED_SIZE);
			for(int j = 0; j < ENCODED_SIZE; j++)
				conv[j] = CHARACTERS.indexOf(aktEnc[j]);
			
			ret[i] = (byte) ((conv[0] << 2) | (conv[1] >> 4));
			
			if(i + 1 == ret.length)
				break;
			ret[i + 1] = (byte) (((conv[1] & 0xF) << 4) | (conv[2] >> 2));
			
			if(i + 2 == ret.length)
				break;
			ret[i + 2] = (byte) (((conv[2] & 0x3) << 6) | conv[3]);
		}
		
		return ret;
	}
}
