package oracle.communications.ordermanagement.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map which reduces concurrency and potential memory leaks for non-static ThreadLocals.
 * http://www.me.umn.edu/~shivane/blogs/cafefeed/2004/06/of-non-static-threadlocals-and-memory.html
 */
public class ThreadLocalMap<K, V> implements Map<K, V> {

    private final ThreadLocal<Map<K, V>> threadLocal = new ThreadLocal<Map<K, V>>() {

        @Override
        protected synchronized Map<K, V> initialValue() {
            return new HashMap<K, V>();
        }
    };

    private Map<K, V> getThreadLocalMap() {
        return threadLocal.get();
    }

    public V put(final K key, final V value) {
        return getThreadLocalMap().put(key, value);
    }

    public V get(final Object key) {
        return getThreadLocalMap().get(key);
    }

    public V remove(final Object key) {
        return getThreadLocalMap().remove(key);
    }

    public int size() {
        return getThreadLocalMap().size();
    }

    public void clear() {
        getThreadLocalMap().clear();
    }

    public boolean isEmpty() {
        return getThreadLocalMap().isEmpty();
    }

    public boolean containsKey(final Object arg0) {
        return getThreadLocalMap().containsKey(arg0);
    }

    public boolean containsValue(final Object arg0) {
        return getThreadLocalMap().containsValue(arg0);
    }

    public Collection<V> values() {
        return getThreadLocalMap().values();
    }

    public void putAll(final Map<? extends K, ? extends V> arg0) {
        getThreadLocalMap().putAll(arg0);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return getThreadLocalMap().entrySet();
    }

    public Set<K> keySet() {
        return getThreadLocalMap().keySet();
    }

}
