package dev.lb.simplebase.util.value;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.lb.simplebase.util.annotation.Internal;

@Internal
class ValueLazy<T> implements Lazy<T> {

	private Supplier<? extends T> supplier;
	private T value;
	
	private final Object lock = new Object();
	
	ValueLazy(Supplier<? extends T> supplier) {
		Objects.requireNonNull(supplier, "Lazy value supplier must not be null");
		this.supplier = supplier;
		this.value = null;
	}
	
	@Override
	public T get() {
		//Supplier exists, must retrieve value
		if(supplier != null) {
			synchronized (lock) {
				//Re-check whether another thread already read the value
				if(supplier != null) {
					//Writing is synchronized, reading not
					value = supplier.get();
					supplier = null;
				}
			}
		}
		return value;
	}
	
	@Override
	public <V> Lazy<V> map(Function<T, V> mapper) {
		return new DelegateLazy<>(this, mapper);
	}

	@Override
	public void ifPresent(Consumer<? super T> action) {
		if(supplier == null) { //This will never change as there is no unGet() or sth
			action.accept(value);
		}
	}

	@Override
	public boolean isPresent() {
		return supplier == null;
	}
}
