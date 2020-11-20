package dev.lb.simplebase.loader;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a type as a class loader plugin. Must implement the {@link ClassLoaderPlugin} interface and implement all
 * methods.
 * <p>
 * The plugin path will be scanned for classes with this annotation and those classes will be loaded as class loader plugins
 * </p>
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface LoaderPlugin {

	/**
	 * The unique name of this loader plugin.
	 * @return The unique name of this loader plugin
	 */
	public String id();
	/**
	 * The name of a static method in the annotated class that takes either no parameters or a single String parameter
	 * and returns an instance of the annotated class. If the String parameter is present, it will have the 
	 * plugins id name as the value.
	 * @return The instance provider method
	 */
	public String instance();
}
