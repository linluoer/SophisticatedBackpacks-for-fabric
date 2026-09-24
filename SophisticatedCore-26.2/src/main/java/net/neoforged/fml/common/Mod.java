package net.neoforged.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking a class as a mod entry point.
 * In NeoForge this triggers mod loading; in Fabric the mod id comes from fabric.mod.json,
 * but we keep the annotation for source compatibility.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
	String value();

	String dist() default "";
}
