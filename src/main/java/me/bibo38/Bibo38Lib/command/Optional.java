package me.bibo38.Bibo38Lib.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Für optionale Argumente gedacht
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME) // Es soll auch während der Laufzeit funktionieren
public @interface Optional
{
}
