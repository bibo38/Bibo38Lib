package me.bibo38.Bibo38Lib.database;

import java.lang.reflect.Field;
import java.util.TreeMap;

public class DatabaseTable
{
	TreeMap<String, Field> colums;
	Field id = null;
	Class<?> mainClass;
	String name;
}
