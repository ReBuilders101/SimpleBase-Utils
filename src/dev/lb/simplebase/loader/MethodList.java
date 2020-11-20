package dev.lb.simplebase.loader;

/**
 * A write-only view of a list of methods that should be transformed
 */
public interface MethodList {
	
	/**
	 * Registers a method for transformation.<br>
	 * Be careful not to refer to the class when finding its name, as this will trigger the class loading before the transformer can run.
	 * @param fullClassName The fully qualified class name of the class with that method.
	 * Several class name formats are accepted: The package and class names may be separated by slashes('/') or dots ('.'), and
	 * the '.class' suffix is may be included or omitted.
	 * @param methodDescriptor The full method descriptor of the method to transform
	 * @return A handle id for this specific transformation that is unique for this plugin
	 */
	public long add(String fullClassName, String methodDescriptor);
}