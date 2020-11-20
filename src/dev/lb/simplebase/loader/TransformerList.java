package dev.lb.simplebase.loader;

public interface TransformerList {

	public long addClass(String fullClassName);
	public long addField(String fullClassName, String fieldDescriptor);
	public long addMethod(String fullClassName, String methodDescriptor);
	
}
