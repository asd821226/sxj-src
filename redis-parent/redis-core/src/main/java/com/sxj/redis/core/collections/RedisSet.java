package com.sxj.redis.core.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanResult;

import com.sxj.redis.core.RSet;
import com.sxj.redis.core.exception.RedisException;
import com.sxj.redis.core.impl.RedisExpirable;
import com.sxj.redis.core.provider.RedisProvider;

public class RedisSet<V> extends RedisExpirable implements RSet<V> {

	public RedisSet(RedisProvider provider, String name) {
		super(provider, name);
	}

	@Override
	public int size() {
		Jedis jedis = provider.getResource();
		boolean broken = false;
		try {
			return jedis.scard(name).intValue();
		} catch (Exception e) {
			broken = true;
			throw new RedisException("", e);
		} finally {
			provider.returnResource(jedis, broken);
		}
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		Jedis jedis = provider.getResource();
		boolean broken = false;
		try {
			return jedis.sismember(name, V_SERIALIZER.serialize(o));
		} catch (Exception e) {
			broken = true;
			throw new RedisException("", e);
		} finally {
			provider.returnResource(jedis, broken);
		}
	}

	private Iterator<V> scanIterator(Jedis jedis, final int start) {
		ScanResult<String> sscan = jedis.sscan(name, start);
		List<String> results = sscan.getResult();
		List<V> retValue = new ArrayList<V>();
		for (String result : results) {
			retValue.add((V) V_SERIALIZER.deserialize(result));
		}
		return retValue.iterator();
	}

	@Override
	public Iterator<V> iterator() {

		return new Iterator<V>() {
			Iterator<V> iterator;

			private V value;

			private boolean removed;

			@Override
			public boolean hasNext() {
				scan();
				return iterator.hasNext();
			}

			private void scan() {
				final Jedis jedis = provider.getResource();
				boolean broken = false;
				try {
					if (iterator == null)
						iterator = scanIterator(jedis, 0);
				} catch (Exception e) {
					broken = true;
				} finally {
					provider.returnResource(jedis, broken);
				}
			}

			@Override
			public V next() {
				if (!hasNext()) {
					throw new NoSuchElementException("No such element at index");
				}
				removed = false;
				return value = iterator.next();
			}

			@Override
			public void remove() {
				scan();
				iterator.remove();
				RedisSet.this.remove(value);
				removed = true;
			}

		};
	}

	@Override
	public Object[] toArray() {
		Jedis jedis = provider.getResource();
		boolean broken = false;
		try {
			Set<String> smembers = jedis.smembers(name);
			return smembers.toArray();
		} catch (Exception e) {
			broken = true;
			throw new RedisException("", e);
		} finally {
			provider.returnResource(jedis, broken);
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		Jedis jedis = provider.getResource();
		boolean broken = false;
		try {
			Set<String> smembers = jedis.smembers(name);
			return smembers.toArray(a);
		} catch (Exception e) {
			broken = true;
			throw new RedisException("", e);
		} finally {
			provider.returnResource(jedis, broken);
		}
	}

	@Override
	public boolean add(V e) {
		Jedis jedis = provider.getResource();
		boolean broken = false;
		try {
			int sadd = jedis.sadd(name, V_SERIALIZER.serialize(e)).intValue();
			switch (sadd) {
			case 1:
				return true;
			default:
				return false;
			}
		} catch (Exception ex) {
			broken = true;
			throw new RedisException("", ex);
		} finally {
			provider.returnResource(jedis, broken);
		}
	}

	@Override
	public boolean remove(Object o) {
		Jedis jedis = provider.getResource();
		boolean broken = false;
		try {
			int sadd = jedis.srem(name, V_SERIALIZER.serialize(o)).intValue();
			switch (sadd) {
			case 1:
				return true;
			default:
				return false;
			}
		} catch (Exception ex) {
			broken = true;
			throw new RedisException("", ex);
		} finally {
			provider.returnResource(jedis, broken);
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object object : c) {
			if (!contains(object)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends V> c) {
		Jedis jedis = provider.getResource();
		boolean broken = false;
		String[] values = array(c);
		try {
			int sadd = jedis.sadd(name, values).intValue();
			switch (sadd) {
			case 1:
				return true;
			default:
				return false;
			}
		} catch (Exception ex) {
			broken = true;
			throw new RedisException("", ex);
		} finally {
			provider.returnResource(jedis, broken);
		}
	}

	private String[] array(Collection<?> c) {
		String[] values = new String[c.size()];
		int index = 0;
		for (Object value : c) {
			values[index] = V_SERIALIZER.serialize(value);
			index++;
		}
		return values;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		for (Object object : this) {
			if (!c.contains(object)) {
				remove(object);
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Jedis jedis = provider.getResource();
		boolean broken = false;
		String[] values = array(c);
		try {
			int sadd = jedis.srem(name, values).intValue();
			switch (sadd) {
			case 1:
				return true;
			default:
				return false;
			}
		} catch (Exception ex) {
			broken = true;
			throw new RedisException("", ex);
		} finally {
			provider.returnResource(jedis, broken);
		}
	}

	@Override
	public void clear() {
		delete();
	}

}
