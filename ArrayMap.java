import java.util.*;

public class ArrayMap<K, V> extends AbstractMap<K, V>{
	private ArrayList<ArrayMapEntry> A = new ArrayList<>();
	public V put(K key, V value) {
		Iterator<ArrayMapEntry> it = A.iterator();
		ArrayMapEntry entry;
		while(it.hasNext()) {
			entry=it.next();
			if(entry.getKey()==key)
				return entry.setValue(value);
		}
		A.add(new ArrayMapEntry(key, value));
		return null;
	}
	public boolean containsKey(Object key) {
		Iterator<ArrayMapEntry> it = A.iterator();
		ArrayMapEntry entry;
		while(it.hasNext()) {
			entry=it.next();
			if(entry.getKey()==key)
				return true;
		}
		return false;
	}
	public V get(Object key) {
		Iterator<ArrayMapEntry> it = A.iterator();
		ArrayMapEntry entry;
		while(it.hasNext()) {
			entry=it.next();
			if(entry.getKey().equals(key))
				return entry.getValue();
		}
		return null;
	}
	public int size() {
		return entrySet().size();
	}
	public Set<Entry<K,V>> entrySet() {
		return new HashSet<>(A);
	}
	public class ArrayMapEntry implements Map.Entry<K, V>{
		private K key;
		private V value;
		public ArrayMapEntry(K key, V value) {
			this.key=key;
			this.value=value;
		}
		public K getKey() {
			return key;
		}
		public V getValue() {
			return value;
		}
		public V setValue(V value) {
			V val=this.value;
			this.value=value;
			return val;
		}
		public String toString() {
			return key+" "+value;
		}
		public boolean equals(Object o) {
			if(!(o instanceof ArrayMap.ArrayMapEntry))
				return false;
			@SuppressWarnings("unchecked")
			ArrayMapEntry entry = (ArrayMapEntry) o;
			if(key==entry.getKey()&&value==entry.getValue())
				return true;
			return false;
		}
		public int hashCode() {
			return (getKey()==null ? 0 : getKey().hashCode()) ^
					(getValue()==null ? 0 : getValue().hashCode());
		}
	}
	
}
