package me.bibo38.Bibo38Lib.database;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import me.bibo38.Bibo38Lib.Startfunc;
import me.bibo38.Bibo38Lib.Utils;

import com.avaje.ebean.validation.NotNull;

public class Database extends Startfunc
{
	private Connection con;
	private HashMap<Class<?>, DatabaseTable> tables = new HashMap<Class<?>, DatabaseTable>();
	
	public Database(String url, String user, String pass, Class<?>... classes) throws Exception
	{
		con = DriverManager.getConnection(url+"?autoReconnect=true", user, pass);
		
		for(Class<?> c : classes)
		{
			Table t = c.getAnnotation(Table.class);
			if(t == null)
				continue;
			
			DatabaseTable dt = new DatabaseTable();
			dt.mainClass = c;
			dt.name = t.name();
			
			HashSet<Field> fields = new HashSet<Field>();
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
			for(Class<?> akt : c.getInterfaces())
				fields.addAll(Arrays.asList(akt.getDeclaredFields()));
			if(c.getSuperclass() != null)
				fields.addAll(Arrays.asList(c.getSuperclass().getDeclaredFields()));
				
			
			TreeMap<String, Field> colums = new TreeMap<String, Field>();
			for(Field f : fields)
			{
				if(f.getAnnotation(Transient.class) != null)
					continue;
				if(f.getAnnotation(Id.class) != null)
					dt.id = f;
				colums.put(f.getName(), f);
			}
			dt.colums = colums;
			
			if(dt.id == null)
				continue;
			
			tables.put(c, dt);
			if(con.getMetaData().getTables(null, null, t.name(), null).next())
				continue; // Tabelle existiert
			
			String stm = "";
			for(String name : colums.keySet())
			{
				stm += ",`"+name+"` "+getSQLType(colums.get(name).getType());
				if(colums.get(name).getAnnotation(NotNull.class) != null)
					stm += " NOT NULL";
			}
			
			stm = "CREATE TABLE `"+t.name()+"` ("+stm.substring(1)+") DEFAULT CHARSET=utf8";
			
			exec(stm);
		}
	}
	
	public Database(Class<?>... classes) throws Exception
	{
		this(main.jdbcURL, main.jdbcUser, main.jdbcPass, classes);
	}
	
	private String getSQLType(Class<?> c)
	{
		if(c == int.class || c == Integer.class)
			return "int(11)";
		else if(c == long.class || c == Long.class)
			return "bigint(20)";
		else if(c == float.class || c == Float.class)
			return "float";
		else if(c == double.class || c == Double.class)
			return "double";
		else if(c == boolean.class || c == Boolean.class)
			return "tinyint(1)";
		else if(c == byte.class || c == Byte.class)
			return "tinyint";
		else if(c == short.class || c == Short.class)
			return "smallint";
		else
			return "text";
	}
	
	public DatabaseQuery find(Class<?> c)
	{
		DatabaseTable dt = tables.get(c);
		if(dt == null)
			throw new IllegalArgumentException("Not registered class "+c.getName());
		
		return new DatabaseQuery(con, dt);
	}
	
	public void reload(Object o)
	{
		DatabaseTable dt = tables.get(o.getClass());
		if(dt == null)
			throw new IllegalArgumentException("Not registered class "+o.getClass().getName());
		
		Statement stm = null;
		ResultSet res = null;
		
		try
		{
			int id = (Integer) Utils.getVal(dt.id, o);
			stm = con.createStatement();
			res = stm.executeQuery("SELECT * FROM `"+dt.name+"` WHERE `"+dt.id.getName()+"`='"+id+"'");
			if(!res.next())
				return;
			
			for(Iterator<Field> it = dt.colums.values().iterator(); it.hasNext();)
			{
				Field akt = it.next();
				if(akt == dt.id)
					continue;
				Utils.setVal(akt, o, res.getObject(akt.getName()));
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		close(stm, res, null);
	}
	
	protected static Object close(Statement stm, ResultSet res, Object ret)
	{
		if(stm != null)
			try {stm.close();} catch (Exception e) {}
		if(res != null)
			try {res.close();} catch(Exception e) {}
		return ret;
	}
	
	public Object get(Class<?> c, int id)
	{
		DatabaseTable dt = tables.get(c);
		if(dt == null)
			throw new IllegalArgumentException("Not registered class "+c.getName());
		
		Statement stm = null;
		ResultSet res = null;
		
		try
		{
			stm = con.createStatement();
			res = stm.executeQuery("SELECT * FROM `"+dt.name+"` WHERE `"+dt.id.getName()+"`='"+id+"'");
			if(!res.next())
				return close(stm, res, null);
			
			Object ret = c.newInstance();
			for(Iterator<Field> it = dt.colums.values().iterator(); it.hasNext();)
			{
				Field akt = it.next();
				Utils.setVal(akt, ret, res.getObject(akt.getName()));
			}
			return close(stm, res, ret);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return close(stm, res, null);
	}
	
	@SuppressWarnings("unchecked")
	public HashSet<Object> getAll(Class<?> c)
	{
		DatabaseTable dt = tables.get(c);
		if(dt == null)
			throw new IllegalArgumentException("Not registered class "+c.getName());
		
		Statement stm = null;
		ResultSet res = null;
		
		try
		{
			stm = con.createStatement();
			res = stm.executeQuery("SELECT * FROM `"+dt.name+"`");
			
			HashSet<Object> ret = new HashSet<Object>();
			Field[] colums = dt.colums.values().toArray(new Field[0]);
			
			while(res.next())
			{
				Object o = c.newInstance();
				for(Field akt : colums)
					Utils.setVal(akt, o, res.getObject(akt.getName()));
				ret.add(o);
			}
			return (HashSet<Object>) close(stm, res, ret);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return (HashSet<Object>) close(stm, res, null);
	}
	
	private void exec(String query)
	{
		Statement stm = null;
		try
		{
			stm = con.createStatement();
			stm.executeUpdate(query);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		close(stm, null, null);
	}
	
	public void delete(Object o)
	{
		DatabaseTable dt = tables.get(o.getClass());
		if(dt == null)
			throw new IllegalArgumentException("Not registered class "+o.getClass().getName());
		  
		try
		{
			String query = "DELETE FROM `"+dt.name+"` WHERE `"+dt.id.getName()+"`='"+Utils.getVal(dt.id, o)+"'";
			exec(query);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void save(Object o)
	{
		DatabaseTable dt = tables.get(o.getClass());
		if(dt == null)
			throw new IllegalArgumentException("Not registered class "+o.getClass().getName());
	    
		Statement stm = null;
		ResultSet res = null;
		
		try
		{
			int id = (Integer) Utils.getVal(dt.id, o);
			
			String query = "";
			stm = con.createStatement();
			Iterator<Field> it = dt.colums.values().iterator();
			if(id == 0)
			{
				// INSERT
				res = stm.executeQuery("SELECT MAX(`"+dt.id.getName()+"`) FROM `"+dt.name+"`");
				int newId;
				if(res.next())
					newId = res.getInt(1)+1;
				else
					newId = 1;
				Utils.setVal(dt.id, o, newId);
				
				while(it.hasNext())
					query += ",'"+((String) Utils.convert(Utils.getVal(it.next(), o), String.class))+"'";
				
				query = "INSERT INTO `"+dt.name+"` VALUES ("+query.substring(1)+")";
			} else
			{
				// UPDATE
				while(it.hasNext())
				{
					Field akt = it.next();
					if(akt == dt.id)
						continue;
					query += ",`"+akt.getName()+"`='"+((String) Utils.convert(Utils.getVal(akt, o), String.class))+"'";
				}
				query = "UPDATE `"+dt.name+"` SET "+query.substring(1)+
						" WHERE `"+dt.id.getName()+"`='"+id+"'";
			}
			System.out.println(query);
			stm.executeUpdate(query);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		close(stm, res, null);
	}
}
