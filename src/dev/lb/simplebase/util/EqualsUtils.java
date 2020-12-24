package dev.lb.simplebase.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import dev.lb.simplebase.util.annotation.StaticType;

/**
 * Contains utility equals methods for types that don't implement them.
 */
@StaticType
public final class EqualsUtils {
	private EqualsUtils() {}
	
	/**
	 * Checks whether two {@link AtomicReference}s are equal (by value).
	 * <p>
	 * This method considers both references equal if they either are both {@code null},
	 * or if the value accessed with the {@link AtomicReference#get()} method for both references
	 * is equal according to {@link Objects#equals(Object, Object)}.
	 * </p>
	 * @param ref1 The first refenence to check for equality
	 * @param ref2 The second reference to check for equality
	 * @return Whether both refereces can be considered equal by the criteria described above
	 */
	public static boolean refEquals(AtomicReference<?> ref1, AtomicReference<?> ref2) {
		if(ref1 == null || ref2 == null) return ref1 == ref2;
		
		final var v1 = ref1.get();
		final var v2 = ref2.get();
		
		return Objects.equals(v1, v2);
	}
	
	/**
	 * Calculates a hash code for an {@link AtomicReference} that is based on the refeneced value.
	 * <p>
	 * If the reference is {@code null}, this method returns a value of {@code 0}.
	 * Otherwise, the hash code of the value accessed by {@link AtomicReference#get()} will be
	 * calculated using {@link Objects#hashCode(Object)} and returned.s 
	 * </p>
	 * @param ref The reference for which the content hash code should be calculated
	 * @return The hash code of the reference. calcuated as described above
	 */
	public static int refHashCode(AtomicReference<?> ref) {
		if(ref == null) return 0;
		return Objects.hashCode(ref.get());
	}
	
}
