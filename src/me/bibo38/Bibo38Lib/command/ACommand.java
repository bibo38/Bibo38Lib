package me.bibo38.Bibo38Lib.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // Es soll auch während der Laufzeit funktionieren
public @interface ACommand
{
	int minArgs() default 0;
	int maxArgs() default -1;
	String usage() default "";
	String description() default "";
	boolean playerNeed() default false; // Wird ein Spieler benötigt
	String permissions() default "op";
}
