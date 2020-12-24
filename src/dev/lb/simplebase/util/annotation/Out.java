package dev.lb.simplebase.util.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as an out parameter; This means that the method will assign some internal
 * state of the parameter. The parameter might be required to be in a certain state when passed to the method.
 * This must be mentioned in the method documentation.
 */
@Retention(SOURCE)
@Target(PARAMETER)
public @interface Out {}
