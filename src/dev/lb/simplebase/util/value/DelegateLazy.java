package dev.lb.simplebase.util.value;

import java.util.function.Consumer;
import java.util.function.Function;

import dev.lb.simplebase.util.annotation.Internal;

@Internal
class DelegateLazy<V, D> implements Lazy<V> {

	private Lazy<D> delegate;
	private final Lazy<D> finalDelegate;
	private V value;
	
	private final Function<D, V> mapper;
	private final Object lock = new Object();
	
	DelegateLazy(Lazy<D> delegate, Function<D, V> mapper) {
		this.delegate = delegate;
		this.finalDelegate = delegate;
		this.value = null;
		this.mapper = mapper;
	}
	
	@Override
	public V get() {
		//Supplier exists, must retrieve value
		if(delegate != null) {
			synchronized (lock) {
				//Re-check whether another thread already read the value
				if(delegate != null) {
					//Writing is synchronized, reading not
					value = mapper.apply(delegate.get());
					delegate = null;
				}
			}
		}
		return value;
	}

	@Override
	public <R> Lazy<R> map(Function<V, R> mapperFunction) {
		return new DelegateLazy<>(finalDelegate, mapperFunction.compose(this.mapper));
	}

	@Override
	public void ifPresent(Consumer<? super V> action) {
		if(delegate != null) { //This will never change as there is no unGet() or sth
			action.accept(value);
		}
	}

}
