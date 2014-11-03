package com.sxj.redis.advance.collections;

import io.netty.util.concurrent.Future;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.sxj.redis.advance.RedisCollections;
import com.sxj.redis.advance.core.RList;
import com.sxj.redis.advance.core.RMap;

public class RedisMapTest
{
    RedisCollections collections;
    
    @Before
    public void before()
    {
        collections = RedisCollections.create();
    }
    
    @After
    public void after()
    {
        try
        {
            //            redis.flushdb();
        }
        finally
        {
            //redis.shutdown();
        }
    }
    
    public static class SimpleKey implements Serializable
    {
        
        private String key;
        
        public SimpleKey()
        {
        }
        
        public SimpleKey(String field)
        {
            this.key = field;
        }
        
        public String getKey()
        {
            return key;
        }
        
        public void setKey(String key)
        {
            this.key = key;
        }
        
        @Override
        public String toString()
        {
            return "key: " + key;
        }
        
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleKey other = (SimpleKey) obj;
            if (key == null)
            {
                if (other.key != null)
                    return false;
            }
            else if (!key.equals(other.key))
                return false;
            return true;
        }
        
    }
    
    public static class SimpleValue implements Serializable
    {
        
        private String value;
        
        public SimpleValue()
        {
        }
        
        public SimpleValue(String field)
        {
            this.value = field;
        }
        
        public void setValue(String field)
        {
            this.value = field;
        }
        
        public String getValue()
        {
            return value;
        }
        
        @Override
        public String toString()
        {
            return "value: " + value;
        }
        
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleValue other = (SimpleValue) obj;
            if (value == null)
            {
                if (other.value != null)
                    return false;
            }
            else if (!value.equals(other.value))
                return false;
            return true;
        }
        
    }
    
    public void testMap()
    {
        RMap<String, RList<String>> map = collections.getMap("甲方");
        
    }
    
    public void testAddAndGet() throws InterruptedException
    {
        RMap<Integer, Integer> map = collections.getMap("getAll");
        map.put(1, 100);
        
        Integer res = map.addAndGet(1, 12);
        Assert.assertEquals(112, (int) res);
    }
    
    public void testGetAll()
    {
        RMap<Integer, Integer> map = collections.getMap("getAll");
        map.put(1, 100);
        map.put(2, 200);
        map.put(3, 300);
        map.put(4, 400);
        System.out.println(map.get(1));
        Map<Integer, Integer> filtered = map.getAll(new HashSet<Integer>(
                Arrays.asList(2, 3)));
        
        Map<Integer, Integer> expectedMap = new HashMap<Integer, Integer>();
        expectedMap.put(2, 200);
        expectedMap.put(3, 300);
        Assert.assertEquals(expectedMap, filtered);
    }
    
    //    @Test
    //    public void testFilterKeys()
    //    {
    //        RMap<Integer, Integer> map = redisson.getMap("filterKeys");
    //        map.put(1, 100);
    //        map.put(2, 200);
    //        map.put(3, 300);
    //        map.put(4, 400);
    //        
    //        Map<Integer, Integer> filtered = map.filterKeys(new Predicate<Integer>()
    //        {
    //            @Override
    //            public boolean apply(Integer input)
    //            {
    //                return input >= 2 && input <= 3;
    //            }
    //        });
    //        
    //        Map<Integer, Integer> expectedMap = new HashMap<Integer, Integer>();
    //        expectedMap.put(2, 200);
    //        expectedMap.put(3, 300);
    //        Assert.assertEquals(expectedMap, filtered);
    //    }
    
    public void testInteger()
    {
        Map<Integer, Integer> map = collections.getMap("test_int");
        map.put(1, 2);
        map.put(3, 4);
        
        Assert.assertEquals(2, map.size());
        
        Integer val = map.get(1);
        Assert.assertEquals(2, val.intValue());
        Integer val2 = map.get(3);
        Assert.assertEquals(4, val2.intValue());
    }
    
    public void testLong()
    {
        Map<Long, Long> map = collections.getMap("test_long");
        map.put(1L, 2L);
        map.put(3L, 4L);
        
        Assert.assertEquals(2, map.size());
        
        Long val = map.get(1L);
        Assert.assertEquals(2L, val.longValue());
        Long val2 = map.get(3L);
        Assert.assertEquals(4L, val2.longValue());
    }
    
    public void testNull()
    {
        Map<Integer, String> map = collections.getMap("simple12");
        map.put(1, null);
        map.put(2, null);
        map.put(3, "43");
        
        Assert.assertEquals(3, map.size());
        
        String val = map.get(2);
        Assert.assertNull(val);
        String val2 = map.get(1);
        Assert.assertNull(val2);
        String val3 = map.get(3);
        Assert.assertEquals("43", val3);
    }
    
    public void testSimpleTypes()
    {
        Map<Integer, String> map = collections.getMap("simple12");
        map.put(1, "12");
        map.put(2, "33");
        map.put(3, "43");
        
        String val = map.get(2);
        Assert.assertEquals("33", val);
    }
    
    public void testRemove()
    {
        Map<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        map.put(new SimpleKey("33"), new SimpleValue("44"));
        map.put(new SimpleKey("5"), new SimpleValue("6"));
        
        map.remove(new SimpleKey("33"));
        map.remove(new SimpleKey("5"));
        
        Assert.assertEquals(1, map.size());
    }
    
    public void testKeySet()
    {
        Map<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        map.put(new SimpleKey("33"), new SimpleValue("44"));
        map.put(new SimpleKey("5"), new SimpleValue("6"));
        
        Assert.assertTrue(map.keySet().contains(new SimpleKey("33")));
        Assert.assertFalse(map.keySet().contains(new SimpleKey("44")));
    }
    
    public void testContainsValue()
    {
        Map<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        map.put(new SimpleKey("33"), new SimpleValue("44"));
        map.put(new SimpleKey("5"), new SimpleValue("6"));
        
        Assert.assertTrue(map.containsValue(new SimpleValue("2")));
        Assert.assertFalse(map.containsValue(new SimpleValue("441")));
        Assert.assertFalse(map.containsValue(new SimpleKey("5")));
    }
    
    public void testContainsKey()
    {
        Map<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        map.put(new SimpleKey("33"), new SimpleValue("44"));
        map.put(new SimpleKey("5"), new SimpleValue("6"));
        
        Assert.assertTrue(map.containsKey(new SimpleKey("33")));
        Assert.assertFalse(map.containsKey(new SimpleKey("34")));
    }
    
    public void testRemoveValue()
    {
        ConcurrentMap<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        
        boolean res = map.remove(new SimpleKey("1"), new SimpleValue("2"));
        Assert.assertTrue(res);
        
        SimpleValue val1 = map.get(new SimpleKey("1"));
        Assert.assertNull(val1);
        
        Assert.assertEquals(0, map.size());
    }
    
    public void testRemoveValueFail()
    {
        ConcurrentMap<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        
        boolean res = map.remove(new SimpleKey("2"), new SimpleValue("1"));
        Assert.assertFalse(res);
        
        boolean res1 = map.remove(new SimpleKey("1"), new SimpleValue("3"));
        Assert.assertFalse(res1);
        
        SimpleValue val1 = map.get(new SimpleKey("1"));
        Assert.assertEquals("2", val1.getValue());
    }
    
    public void testReplaceOldValueFail()
    {
        ConcurrentMap<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        
        boolean res = map.replace(new SimpleKey("1"),
                new SimpleValue("43"),
                new SimpleValue("31"));
        Assert.assertFalse(res);
        
        SimpleValue val1 = map.get(new SimpleKey("1"));
        Assert.assertEquals("2", val1.getValue());
    }
    
    public void testReplaceOldValueSuccess()
    {
        ConcurrentMap<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        
        boolean res = map.replace(new SimpleKey("1"),
                new SimpleValue("2"),
                new SimpleValue("3"));
        Assert.assertTrue(res);
        
        boolean res1 = map.replace(new SimpleKey("1"),
                new SimpleValue("2"),
                new SimpleValue("3"));
        Assert.assertFalse(res1);
        
        SimpleValue val1 = map.get(new SimpleKey("1"));
        Assert.assertEquals("3", val1.getValue());
    }
    
    public void testReplaceValue()
    {
        ConcurrentMap<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        
        SimpleValue res = map.replace(new SimpleKey("1"), new SimpleValue("3"));
        Assert.assertEquals("2", res.getValue());
        
        SimpleValue val1 = map.get(new SimpleKey("1"));
        Assert.assertEquals("3", val1.getValue());
    }
    
    public void testReplace()
    {
        Map<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        map.put(new SimpleKey("33"), new SimpleValue("44"));
        map.put(new SimpleKey("5"), new SimpleValue("6"));
        
        SimpleValue val1 = map.get(new SimpleKey("33"));
        Assert.assertEquals("44", val1.getValue());
        
        map.put(new SimpleKey("33"), new SimpleValue("abc"));
        SimpleValue val2 = map.get(new SimpleKey("33"));
        Assert.assertEquals("abc", val2.getValue());
    }
    
    public void testPutGet()
    {
        Map<SimpleKey, SimpleValue> map = collections.getMap("simple");
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        map.put(new SimpleKey("33"), new SimpleValue("44"));
        map.put(new SimpleKey("5"), new SimpleValue("6"));
        
        SimpleValue val1 = map.get(new SimpleKey("33"));
        Assert.assertEquals("44", val1.getValue());
        
        SimpleValue val2 = map.get(new SimpleKey("5"));
        Assert.assertEquals("6", val2.getValue());
    }
    
    public void testSize()
    {
        Map<SimpleKey, SimpleValue> map = collections.getMap("simple");
        
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        map.put(new SimpleKey("3"), new SimpleValue("4"));
        map.put(new SimpleKey("5"), new SimpleValue("6"));
        Assert.assertEquals(3, map.size());
        
        map.put(new SimpleKey("1"), new SimpleValue("2"));
        map.put(new SimpleKey("3"), new SimpleValue("4"));
        Assert.assertEquals(3, map.size());
        
        map.put(new SimpleKey("1"), new SimpleValue("21"));
        map.put(new SimpleKey("3"), new SimpleValue("41"));
        Assert.assertEquals(3, map.size());
        
        map.put(new SimpleKey("51"), new SimpleValue("6"));
        Assert.assertEquals(4, map.size());
        
        map.remove(new SimpleKey("3"));
        Assert.assertEquals(3, map.size());
    }
    
    public void testPutAsync() throws InterruptedException, ExecutionException
    {
        RMap<Integer, Integer> map = collections.getMap("simple");
        Future<Integer> future = map.putAsync(2, 3);
        Assert.assertNull(future.get());
        
        Assert.assertEquals((Integer) 3, map.get(2));
        
        Future<Integer> future1 = map.putAsync(2, 4);
        Assert.assertEquals((Integer) 3, future1.get());
        
        Assert.assertEquals((Integer) 4, map.get(2));
    }
    
    public void testRemoveAsync() throws InterruptedException,
            ExecutionException
    {
        RMap<Integer, Integer> map = collections.getMap("simple");
        map.put(1, 3);
        map.put(3, 5);
        map.put(7, 8);
        
        Assert.assertEquals((Integer) 3, map.removeAsync(1).get());
        Assert.assertEquals((Integer) 5, map.removeAsync(3).get());
        Assert.assertNull(map.removeAsync(10).get());
        Assert.assertEquals((Integer) 8, map.removeAsync(7).get());
    }
    
    public void testFastRemoveAsync() throws InterruptedException,
            ExecutionException
    {
        RMap<Integer, Integer> map = collections.getMap("simple");
        map.put(1, 3);
        map.put(3, 5);
        map.put(4, 6);
        map.put(7, 8);
        
        Assert.assertEquals((Long) 3L, map.fastRemoveAsync(1, 3, 7).get());
        Thread.sleep(1);
        Assert.assertEquals(1, map.size());
    }
    
}
