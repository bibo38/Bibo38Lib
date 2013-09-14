package me.bibo38.Bibo38Lib.database;

import java.util.HashMap;

public class WhereQuery
{
	private HashMap<String, String> where = new HashMap<String, String>();
	private DatabaseQuery dbq;
	
	protected WhereQuery(DatabaseQuery dbq)
	{
		this.dbq = dbq;
	}
	
	public WhereQuery eq(String arg, Object val)
	{
		where.put(arg, "<=>'"+val.toString()+"'");
		return this;
	}
	
	public WhereQuery ne(String arg, Object val)
	{
		where.put(arg, "!='"+val.toString()+"'");
		return this;
	}
	
	public WhereQuery gt(String arg, Object val)
	{
		where.put(arg, ">'"+val.toString()+"'");
		return this;
	}
	
	public WhereQuery lt(String arg, Object val)
	{
		where.put(arg, "<'"+val.toString()+"'");
		return this;
	}
	
	public WhereQuery ge(String arg, Object val)
	{
		where.put(arg, ">='"+val.toString()+"'");
		return this;
	}
	
	public WhereQuery le(String arg, Object val)
	{
		where.put(arg, "<='"+val.toString()+"'");
		return this;
	}
	
	public DatabaseQuery finish()
	{
		
		return dbq;
	}
	
	@Override
	public String toString()
	{
		String q = "";
		for(String akt : where.keySet().toArray(new String[0]))
			q += " AND `"+akt+"`"+where.get(akt);
		return q.substring(5);
	}
}
