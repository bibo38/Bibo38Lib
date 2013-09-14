package me.bibo38.Bibo38Lib.database;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import me.bibo38.Bibo38Lib.Utils;

public class DatabaseQuery
{
	private Connection con;
	private DatabaseTable dt;
	
	private String sortedBy = "";
	private WhereQuery where;
	private boolean asc = true;
	
	protected DatabaseQuery(Connection con, DatabaseTable dt)
	{
		this.con = con;
		this.dt = dt;
	}
	
	public WhereQuery where()
	{
		where = new WhereQuery(this);
		return where;
	}
	
	public DatabaseQuery sortedBy(String arg, Ordering o)
	{
		sortedBy = arg;
		asc = (o == Ordering.ASCEND);
		return this;
	}
	
	public Object[] find()
	{
		String query = "";
		query = "SELECT * FROM `"+dt.name+"` WHERE "+where;
		if(sortedBy != "")
			query += " ORDER BY `"+sortedBy+"`"+(asc? "ASC" : "DESC");
		
		Statement stm = null;
		ResultSet res = null;
		
		try
		{
			stm = con.createStatement();
			res = stm.executeQuery(query);
			ArrayList<Object> list = new ArrayList<Object>();
			Field[] colums = dt.colums.values().toArray(new Field[0]);
			
			while(res.next())
			{
				Object o = dt.mainClass.newInstance();
				for(Field f : colums)
					Utils.setVal(f, o, res.getString(f.getName()));
				list.add(o);
			}
			return (Object[]) Database.close(stm, res, list.toArray());
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return (Object[]) Database.close(stm, res, new Object[0]);
	}
}
