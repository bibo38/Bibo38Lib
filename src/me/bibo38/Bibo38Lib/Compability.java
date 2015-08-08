package me.bibo38.Bibo38Lib;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Compability
{
	private static final int magic = 0xCAFEBABE;
	
	public static Class<?> loadCompatible(InputStream is, String name) throws NoSuchMethodException, SecurityException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		ClassLoader cl = Compability.class.getClassLoader();
		Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
		defineClass.setAccessible(true);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(os);
		DataInputStream dis = new DataInputStream(is);
		
		String packageVersion = Utils.getPackageVersion();
		
		if(dis.readInt() != magic) // Magic Bytes
			throw new ClassFormatError("Wrong magic Bytes");
		dos.writeInt(magic);
		
		dos.writeInt(dis.readInt()); // Major/Minor Version
		
		int constantPoolCount = dis.readUnsignedShort();
		dos.writeShort(constantPoolCount);
		for(int i = 1; i < constantPoolCount; i++)
		{
			byte tag = dis.readByte();
			dos.writeByte(tag);
			int copySize = -1;
			switch(tag)
			{
				case 1: // Utf8
					String text = dis.readUTF();
					text = text.replaceAll("net/minecraft/server/[^/]+", "net/minecraft/server/"+packageVersion).
								replaceAll("org/bukkit/craftbukkit/[^/]+", "org/bukkit/craftbukkit/"+packageVersion);
					dos.writeUTF(text);
					break;
					
				case 5: // Long
				case 6: copySize = 8; // Double
					break;
				case 7: // Class
				case 8: // String
				case 16: copySize = 2; // MethodType
					break;
				case 3: // Integer
				case 4: // Float
				case 9: // Fieldref
				case 10: // Methodref
				case 11: // InterfaceMethodref
				case 12: // NameAndType
				case 18: copySize = 4; // InvokeDynamic
					break;
				case 15: copySize = 3; // MethodHandle
					break;
				default:
					// throw new ClassFormatError("Unknown Tag "+tag);
			}
			for(; copySize > 0; copySize--)
				os.write(is.read());
		}
		
		byte data[] = new byte[1024];
		int len;
		while((len = is.read(data)) != -1)
			os.write(data, 0, len);

		byte[] out = os.toByteArray();
		return (Class<?>) defineClass.invoke(cl, name, out, 0, out.length);
	}
}
