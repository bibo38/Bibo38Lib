package me.bibo38.Bibo38Lib.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // Es soll auch während der Laufzeit funktionieren
public @interface Command
{
	String usage() default "";
	String description() default "";
	String permissions() default "";
	boolean umlaut() default false;
}
