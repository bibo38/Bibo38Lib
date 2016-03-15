package me.bibo38.Bibo38Lib;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

public final class Compability
{
	private static final int MAGIC = 0xCAFEBABE;
	private static final int BUFFER_SIZE = 1024;
	
	private Compability() {}
	
	public static Class<?> loadCompatible(InputStream is, String name) throws Exception
	{
		Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
		defineClass.setAccessible(true);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(os);
		DataInputStream dis = new DataInputStream(is);
		
		String packageVersion = Utils.getPackageVersion();
		
		if(dis.readInt() != MAGIC) // Magic Bytes
			throw new ClassFormatError("Wrong magic Bytes");
		dos.writeInt(MAGIC);
		
		dos.writeInt(dis.readInt()); // Major/Minor Version
		
		int constantPoolCount = dis.readUnsignedShort();
		dos.writeShort(constantPoolCount);
		for(int i = 1; i < constantPoolCount; i++)
		{
			byte tag = dis.readByte();
			dos.writeByte(tag);
			int copySize = -1;
			
			if(tag < 0 || tag >= TagType.values().length)
				tag = (byte) TagType.INVALID.ordinal();
			switch(TagType.values()[tag])
			{
				case UTF8:
					String text = dis.readUTF();
					text = text.replaceAll("net/minecraft/server/[^/]+", "net/minecraft/server/" + packageVersion).
								replaceAll("org/bukkit/craftbukkit/[^/]+", "org/bukkit/craftbukkit/" + packageVersion);
					dos.writeUTF(text);
					break;
					
				case LONG:
				case DOUBLE: copySize = 8;
					break;
				case CLASS:
				case STRING:
				case METHOD_TYPE: copySize = 2;
					break;
				case INTEGER:
				case FLOAT:
				case FIELDREF:
				case METHODREF:
				case INTERFACE_METHODREF:
				case NAME_AND_TYPE:
				case INVOKE_DYNAMIC: copySize = 4;
					break;
				case METHOD_HANDLE: copySize = 3;
					break;
				default:
					// throw new ClassFormatError("Unknown Tag "+tag);
			}
			for(; copySize > 0; copySize--)
				os.write(is.read());
		}
		
		byte data[] = new byte[BUFFER_SIZE];
		int len;
		while((len = is.read(data)) != -1)
			os.write(data, 0, len);

		byte out[] = os.toByteArray();
		return (Class<?>) defineClass.invoke(Compability.class.getClassLoader(), name, out, 0, out.length);
	}
}

enum TagType
{
	INVALID, UTF8, UNK2, INTEGER, FLOAT,
	LONG, DOUBLE, CLASS, STRING, FIELDREF,
	METHODREF, INTERFACE_METHODREF, NAME_AND_TYPE, UNK13, UNK14,
	METHOD_HANDLE, METHOD_TYPE, UNK17, INVOKE_DYNAMIC;
}