package com.sxj.redis.core.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.sxj.redis.core.RList;
import com.sxj.redis.core.exception.RedisException;
import com.sxj.redis.core.impl.RedisExpirable;
import com.sxj.redis.core.provider.RedisProvider;

public class RedisList<V> extends RedisExpirable implements RList<V>
{
    private int batchSize = 50;
    
    public RedisList(RedisProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public int size()
    {
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            return jedis.llen(name).intValue();
        }
        catch (Exception e)
        {
            broken = true;
            throw new RedisException("", e);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }
    
    @Override
    public boolean contains(Object o)
    {
        return indexOf(o) != -1;
    }
    
    @Override
    public Iterator<V> iterator()
    {
        return listIterator();
    }
    
    @Override
    public Object[] toArray()
    {
        List<V> list = subList(0, size());
        return list.toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] a)
    {
        List<V> list = subList(0, size());
        return list.toArray(a);
    }
    
    @Override
    public boolean add(V e)
    {
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            jedis.rpush(name, V_SERIALIZER.serialize(e));
        }
        catch (Exception ex)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        return false;
    }
    
    @Override
    public boolean remove(Object o)
    {
        return remove(o, 1);
    }
    
    /**
     * @param o
     * @param count
     * 
    count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
    count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
    count = 0 : 移除表中所有与 value 相等的值。

     * @return
     */
    protected boolean remove(Object o, int count)
    {
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            return jedis.lrem(name, count, V_SERIALIZER.serialize(o)) > 0;
        }
        catch (Exception e)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        return false;
    }
    
    @Override
    public boolean containsAll(Collection<?> c)
    {
        if (isEmpty())
        {
            return false;
        }
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            Collection<String> copy = new ArrayList<String>();
            for (Object o : c)
            {
                copy.add(V_SERIALIZER.serialize(o));
            }
            int pages = pages(size(), batchSize);
            for (int i = 0; i < pages; i++)
            {
                List<String> lrange = jedis.lrange(name, i * batchSize, i
                        * batchSize + batchSize - 1);
                
                for (Iterator<String> iterator = copy.iterator(); iterator.hasNext();)
                {
                    String obj = iterator.next();
                    int index = lrange.indexOf(obj);
                    if (index != -1)
                    {
                        iterator.remove();
                    }
                }
            }
            return copy.isEmpty();
        }
        catch (Exception e)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        return false;
    }
    
    @Override
    public boolean addAll(Collection<? extends V> c)
    {
        String[] values = new String[c.size()];
        int index = 0;
        for (V value : c)
        {
            values[index] = V_SERIALIZER.serialize(value);
            index++;
        }
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            jedis.rpush(name, values);
            return true;
        }
        catch (Exception e)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        
        return false;
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends V> c)
    {
        checkPosition(index);
        String[] values = new String[c.size()];
        int i = 0;
        for (V value : c)
        {
            values[i] = V_SERIALIZER.serialize(value);
            i++;
        }
        if (index < size())
        {
            Jedis jedis = provider.getResource();
            boolean broken = false;
            try
            {
                List<String> tail = jedis.lrange(name, index, size());
                Transaction multi = jedis.multi();
                multi.ltrim(name, 0, index - 1);
                multi.rpush(name, values);
                multi.rpush(name, tail.toArray(new String[tail.size()]));
                return multi.exec().size() == 3;
            }
            catch (Exception e)
            {
                broken = true;
            }
            finally
            {
                provider.returnResource(jedis, broken);
            }
            return false;
            
        }
        else
        {
            return addAll(c);
        }
    }
    
    private void checkPosition(int index)
    {
        int size = size();
        if (!isPositionInRange(index, size))
            throw new IndexOutOfBoundsException("index: " + index
                    + " but current size: " + size);
    }
    
    private boolean isPositionInRange(int index, int size)
    {
        return index >= 0 && index <= size;
    }
    
    @Override
    public boolean removeAll(Collection<?> c)
    {
        Jedis jedis = provider.getResource();
        boolean broken = false;
        boolean result = false;
        try
        {
            boolean deleted = false;
            for (Object value : c)
            {
                
                deleted = jedis.lrem(name, 0, V_SERIALIZER.serialize(value)) > 0;
                if (!result)
                    result = deleted;
            }
        }
        catch (Exception e)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        return result;
    }
    
    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean changed = false;
        for (Iterator<V> iterator = iterator(); iterator.hasNext();)
        {
            V object = iterator.next();
            if (!c.contains(object))
            {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }
    
    @Override
    public void clear()
    {
        delete();
    }
    
    @Override
    public V get(int index)
    {
        checkIndex(index);
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            String lindex = jedis.lindex(name, index);
            return (V) V_SERIALIZER.deserialize(lindex);
        }
        catch (Exception e)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        return null;
    }
    
    private void checkIndex(int index)
    {
        int size = size();
        if (!isInRange(index, size))
            throw new IndexOutOfBoundsException("index: " + index
                    + " but current size: " + size);
    }
    
    private boolean isInRange(int index, int size)
    {
        return index >= 0 && index < size;
    }
    
    @Override
    public V set(int index, V element)
    {
        checkIndex(index);
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            String lindex = jedis.lindex(name, index);
            Transaction multi = jedis.multi();
            multi.lset(name, index, V_SERIALIZER.serialize(element));
            if (multi.exec().size() == 1)
                return (V) V_SERIALIZER.deserialize(lindex);
        }
        catch (Exception e)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        return null;
    }
    
    @Override
    public void add(int index, V element)
    {
        addAll(index, Collections.singleton(element));
    }
    
    @Override
    public V remove(int index)
    {
        checkIndex(index);
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            if (index == 0)
            {
                String lpop = jedis.lpop(getName());
                return (V) V_SERIALIZER.deserialize(lpop);
            }
            while (true)
            {
                String lindex = jedis.lindex(name, index);
                List<String> tail = jedis.lrange(name, index + 1, size());
                Transaction multi = jedis.multi();
                multi.ltrim(name, 0, index - 1);
                multi.rpush(name, tail.toArray(new String[tail.size()]));
                if (multi.exec().size() == 2)
                    return (V) V_SERIALIZER.deserialize(lindex);
            }
        }
        catch (Exception e)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        return null;
    }
    
    private int pages(int p, int q)
    {
        int div = p / q;
        int rem = p - q * div; // equal to p % q
        
        if (rem == 0)
        {
            return div;
        }
        
        return div + 1;
    }
    
    @Override
    public int indexOf(Object o)
    {
        if (isEmpty())
        {
            return -1;
        }
        Jedis jedis = provider.getResource();
        boolean broken = false;
        int pages = pages(size(), batchSize);
        String value = V_SERIALIZER.serialize(o);
        try
        {
            for (int i = 0; i < pages; i++)
            {
                List<String> lrange = jedis.lrange(name, i * batchSize, i
                        * batchSize + batchSize - 1);
                int index = lrange.indexOf(value);
                if (index != -1)
                {
                    return index + i * batchSize;
                }
            }
            return -1;
        }
        catch (Exception e)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        return -1;
    }
    
    @Override
    public int lastIndexOf(Object o)
    {
        if (isEmpty())
        {
            return -1;
        }
        
        final int size = size();
        int pages = pages(size, batchSize);
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            
            for (int i = 1; i <= pages; i++)
            {
                int startIndex = -i * batchSize;
                List<String> lrange = jedis.lrange(name, startIndex, size
                        - (i - 1) * batchSize);
                int index = lrange.lastIndexOf(V_SERIALIZER.serialize(o));
                if (index != -1)
                {
                    return Math.max(size + startIndex, 0) + index;
                }
            }
        }
        catch (Exception e)
        {
            broken = true;
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        
        return -1;
    }
    
    @Override
    public ListIterator<V> listIterator()
    {
        return listIterator(0);
    }
    
    @Override
    public ListIterator<V> listIterator(final int index)
    {
        return new ListIterator<V>()
        {
            
            private int currentIndex = index - 1;
            
            private boolean removeExecuted;
            
            @Override
            public boolean hasNext()
            {
                int size = size();
                return currentIndex + 1 < size && size > 0;
            }
            
            @Override
            public V next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException(
                            "No such element at index " + currentIndex);
                }
                currentIndex++;
                removeExecuted = false;
                return RedisList.this.get(currentIndex);
            }
            
            @Override
            public void remove()
            {
                if (removeExecuted)
                {
                    throw new IllegalStateException(
                            "Element been already deleted");
                }
                RedisList.this.remove(currentIndex);
                currentIndex--;
                removeExecuted = true;
            }
            
            @Override
            public boolean hasPrevious()
            {
                int size = size();
                return currentIndex - 1 < size && size > 0 && currentIndex >= 0;
            }
            
            @Override
            public V previous()
            {
                if (!hasPrevious())
                {
                    throw new NoSuchElementException(
                            "No such element at index " + currentIndex);
                }
                removeExecuted = false;
                V res = RedisList.this.get(currentIndex);
                currentIndex--;
                return res;
            }
            
            @Override
            public int nextIndex()
            {
                return currentIndex + 1;
            }
            
            @Override
            public int previousIndex()
            {
                return currentIndex;
            }
            
            @Override
            public void set(V e)
            {
                if (currentIndex >= size() - 1)
                {
                    throw new IllegalStateException();
                }
                RedisList.this.set(currentIndex, e);
            }
            
            @Override
            public void add(V e)
            {
                RedisList.this.add(currentIndex + 1, e);
                currentIndex++;
            }
        };
    }
    
    @Override
    public List<V> subList(int fromIndex, int toIndex)
    {
        int size = size();
        if (fromIndex < 0 || toIndex > size)
        {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex
                    + " toIndex: " + toIndex + " size: " + size);
        }
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("fromIndex: " + fromIndex
                    + " toIndex: " + toIndex);
        }
        
        Jedis jedis = provider.getResource();
        boolean broken = false;
        try
        {
            List<String> lrange = jedis.lrange(name, fromIndex, toIndex);
            List<V> retValue = new ArrayList<V>();
            for (String value : lrange)
            {
                retValue.add((V) V_SERIALIZER.deserialize(value));
            }
            return retValue;
        }
        catch (Exception e)
        {
            broken = true;
            throw new RedisException("", e);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
        
    }
    
}
