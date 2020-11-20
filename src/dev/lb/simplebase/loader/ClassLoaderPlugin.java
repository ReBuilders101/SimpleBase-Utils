package dev.lb.simplebase.loader;

public interface ClassLoaderPlugin {

	public void registerMethodTransformers(MethodList methods);
	
	public void registerAccessTransformers(TransformerList transforms);
	
	public void transformMethod();
	
	public TransformConflictResponse resolveConfilct(long handle, Iterable<String> requestingPlugins);
	
	/**
	 * A set of options on how to resolve a situation where two plugins want to transform the same
	 * method.
	 */
	public static enum TransformConflictResponse {
		/**
		 * The transformation is required for this plugin to work, and the entire plugin will not
		 * be loaded if the conflict cannot be resolved.
		 */
		REQUIRE,
		/**
		 * Signal that other plugins may transform this method if they like.
		 * If all plugins {@link #YIELD} or {@link #REFUSE}, a random one of the yielding plugins will be selected to run.
		 * The 'randomly' selected plugin will be consistent for every launch with the same set of plugins.
		 */
		YIELD,
		/**
		 * Signal that an attempt to run all transformers on the method should be made. All plugins that return this 
		 * value for a conflict will run in arbitrary but consistent order.
		 */
		COMPOSE,
		/**
		 * The transformation will not be applied by this plugin, but the plugin can continue loading.
		 */
		REFUSE;
	}
}
