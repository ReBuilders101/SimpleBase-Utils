package dev.lb.simplebase.util.value;

import java.util.Arrays;

/**
 * Contains entries of three unique int values that can be addressed by any of the three values in constant time.
 * Not threadsafe.
 */
public class ThreeWayIntMap {
	
	private final Node[][] nodes;
	private int length; //nodes[?].length
	private int size; //Amount of elements
	
	/**
	 * Constructs a new Map with no elements
	 */
	public ThreeWayIntMap() {
		nodes = new Node[3][];
		length = 0;
		size = 0;
	}
	
	/**
	 * The amount of elements currently present in the map
	 * @return The size of the map
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Gets the map entry by looking up the first value.
	 * @param value The first value of the entry
	 * @return The map entry, or {@code null} if not found
	 */
	public Entry getBy1(int value) {
		return entry(getNode(value, 0));
	}
	
	/**
	 * Gets the map entry by looking up the second value.
	 * @param value The second value of the entry
	 * @return The map entry, or {@code null} if not found
	 */
	public Entry getBy2(int value) {
		return entry(getNode(value, 1));
	}

	/**
	 * Gets the map entry by looking up the third value.
	 * @param value The thrid value of the entry
	 * @return The map entry, or {@code null} if not found
	 */
	public Entry getBy3(int value) {
		return entry(getNode(value, 2));
	}
	
	/**
	 * Removes the map entry found by looking up the first value.
	 * @param value The first value of the entry
	 * @return The map entry that was removed, or {@code null} if not found
	 */
	public Entry removeBy1(int value) {
		return entry(removeNodes(value, 0));
	}
	
	/**
	 * Removes the map entry found by looking up the second value.
	 * @param value The second value of the entry
	 * @return The map entry that was removed, or {@code null} if not found
	 */
	public Entry removeBy2(int value) {
		return entry(removeNodes(value, 1));
	}

	/**
	 * Removes the map entry found by looking up the third value.
	 * @param value The third value of the entry
	 * @return The map entry that was removed, or {@code null} if not found
	 */
	public Entry removeBy3(int value) {
		return entry(removeNodes(value, 2));
	}
	
	/**
	 * Checks whether a map entry with that first value exists.<br>
	 * Equivalent to {@code getBy1(int) != null}.
	 * @param value The first value of the entry
	 * @return {@code true} if an entry was found, {@code false} otherwise
	 */
	public boolean containsValue1(int value) {
		return getNode(value, 0) != null;
	}
	
	/**
	 * Checks whether a map entry with that second value exists.<br>
	 * Equivalent to {@code getBy2(int) != null}.
	 * @param value The second value of the entry
	 * @return {@code true} if an entry was found, {@code false} otherwise
	 */
	public boolean containsValue2(int value) {
		return getNode(value, 1) != null;
	}

	/**
	 * Checks whether a map entry with that third value exists.<br>
	 * Equivalent to {@code getBy3(int) != null}.
	 * @param value The third value of the entry
	 * @return {@code true} if an entry was found, {@code false} otherwise
	 */
	public boolean containsValue3(int value) {
		return getNode(value, 2) != null;
	}
	
	/**
	 * Checks whether a map entry exists that has value1 as the first value, or value2 as the second value,
	 * or value3 as the third value. Because all 3 values can be used as a key and must be unique, 
	 * this method is used to check if a new entry cannot be inserted into the map.
	 * @param value1 The first value of the entry
	 * @param value2 The second value of the entry
	 * @param value3 The third value of the entry
	 * @return {@code true} if any of the values is already used as a key, {@code false} otherwise
	 */
	public boolean containsAny(int value1, int value2, int value3) {
		return containsValue1(value1) || containsValue2(value2) || containsValue3(value3);
	}
	
	/**
	 * Adds a new entry to the map
	 * @param value1 The first value of the entry
	 * @param value2 The second value of the entry
	 * @param value3 The third value of the entry
	 * @return The created entry, or {@code null} if it couldn't be inserted
	 */
	public Entry put(int value1, int value2, int value3) {
		//Can we insert
		if(containsAny(value1, value2, value3)) return null;
		
		assertCapacity(size+1);
		Entry e = new Entry(value1, value2, value3);
		putNode(e);
		return e;
	}
	
	private void putNode(Entry e) {

		//Insert by all 3 indices
		for(int i = 0; i < 3; i++) {
			int hash = hash2mask(length, e.values[i]);
			Node c = new Node(e);
			Node n = nodes[i][hash];
			if(n == null) {
				nodes[i][hash] = c;
			} else {
				while(n.next != null) {
					n = n.next;
				}
				n.next = c;
			}
		}
		
		size++;
	}
	
	private Node removeNodes(int value, int cmpi) {
		Node n = getNode(value, cmpi);
		if(n == null) return null;
		for(int i = 0; i < 3; i++) {
			removeNode(n.entry.values[i], i);
		}
		size--;
		return n;
	}
	
	private Node removeNode(int value, int cmpi) {
		final Node[] n = nodes[cmpi];
		if(n == null || length == 0) return null;
		final int hash = hash2mask(length, value);
		Node current = n[hash];
		if(current == null) return null;
		if(current.entry.values[cmpi] == value) {
			n[hash] = current.next;
			return current;
		} else if(current.next == null){
			return null;
		} else {
			Node last = current;
			current = current.next;
			while(current != null && current.entry.values[cmpi] != value) {
				last = current;
				current = current.next;
			}
			if(current == null) {
				return null;
			} else {
				last.next = current.next; //Unlink
				return current;
			}
		}
	}
	
	private Node getNode(int value, int cmpi) {
		final Node[] n = nodes[cmpi];
		if(n == null || length == 0) return null;
		Node current = n[hash2mask(length, value)];
		while(current != null && current.entry.values[cmpi] != value) {
			current = current.next;
		}
		return current;
	}
	
	private void assertCapacity(int count) {
		if(count > length) {
			final int cap = count > 0 ? Integer.highestOneBit(count) << 1 : 0;
			if(length == 0) {
				//Nothing to copy
				for(int i = 0; i < 3; i++) {
					nodes[i] = new Node[cap];
				}
			} else {
				//Copy all 3 arrays
				for(int i = 0; i < 3; i++) {
					Node[] newNodes = new Node[cap];
					//Different hash mask, copy every entry individually
					for(Node n : nodes[i]) { //Iterate old nodes
						if(n == null) continue;
						
						//New hash for ith value
						int hash = hash2mask(cap, n.entry.values[i]);
						newNodes[hash] = n;
					}
					nodes[i] = newNodes;
				}
			}
			length = cap;
		}
		//Otherwise it's fine, we have space
	}
	
	//Copied from HashMap
	private static final int hash2mask(int length, int value) {
        return (length - 1) & (value ^ (value >>> 16));
    }
	
	private static final Entry entry(Node node) {
		return node == null ? null : node.entry;
	}
	
	/**
	 * An entry to a {@link ThreeWayIntMap}.
	 */
	public class Entry {
		private final int[] values = new int[3]; 
		
		private Entry(int v1, int v2, int v3) {
			values[0] = v1;
			values[1] = v2;
			values[2] = v3;
		}
		
		/**
		 * The first value of the entry.
		 * @return The first value
		 */
		public int getValue1() {
			return values[0];
		}
		
		/**
		 * The second value of the entry.
		 * @return The second value
		 */
		public int getValue2() {
			return values[1];
		}
		
		/**
		 * The third value of the entry.
		 * @return The third value
		 */
		public int getValue3() {
			return values[2];
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(values);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry other = (Entry) obj;
			if (!Arrays.equals(values, other.values))
				return false;
			return true;
		}
	}
	
	private class Node {
		private Node next;
		private final Entry entry;
		
		private Node(Entry e) {
			this.entry = e;
		}
	}
}
