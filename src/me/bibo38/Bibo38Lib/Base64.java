package me.bibo38.Bibo38Lib;

import java.util.Arrays;

import com.avaje.ebeaninternal.server.lib.util.StringParsingException;

public class Base64
{
	private final static String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	
	public static String encode(byte[] data)
	{
		String ret = "";
		int conv[] = new int[4];
		int padding = data.length % 3;
		
		if(padding != 0)
		{
			padding = 3 - padding; // 1 byte to much means two bytes padding
			data = Arrays.copyOf(data, data.length + padding);
			
			// Ensure the rest is filled up with zeros
			data[data.length-1] = 0;
			if(padding == 2)
				data[data.length-2] = 0;
		}
			
		for(int i = 0; i < data.length; i += 3)
		{
			conv[0] = (data[i] & 0xFF) >> 2;
			conv[1] = ((data[i] & 0x3) << 4) | ((data[i+1] & 0xFF) >> 4);
			conv[2] = ((data[i+1] & 0xF) << 2) | ((data[i+2] & 0xFF) >> 6);
			conv[3] = data[i+2] & 0x3F;
			
			for(int j = 0; j < 4; j++)
				ret += characters.charAt(conv[j]);
		}
		
		if(padding != 0)
			ret = ret.substring(0, ret.length() - padding) + ((padding == 1)? "=" : "==");
		
		return ret;
	}
	
	public static byte[] decode(String encoded)
	{
		encoded = encoded.replace("=", "");
		int padding = encoded.length() % 4;
		
		if(padding != 0)
		{
			if(padding == 1)
				throw new StringParsingException("Single remaining encoded character is not possible");
			padding = 4 - padding;
			encoded += (padding == 1)? "=" : "==";
		}
		
		byte[] ret = new byte[encoded.length() / 4 * 3 - padding];
		int[] conv = new int[4];
		
		for(int i = 0; i < ret.length; i += 3)
		{
			char[] aktEnc = encoded.substring(0, 4).toCharArray();
			encoded = encoded.substring(4);
			for(int j = 0; j < 4; j++)
				conv[j] = characters.indexOf(aktEnc[j]);
			
			ret[i] = (byte) ((conv[0] << 2) | (conv[1] >> 4));
			
			if(i+1 == ret.length)
				break;
			ret[i+1] = (byte) (((conv[1] & 0xF) << 4) | (conv[2] >> 2));
			
			if(i+2 == ret.length)
				break;
			ret[i+2] = (byte) (((conv[2] & 0x3) << 6) | conv[3]);
		}
		
		return ret;
	}
}
