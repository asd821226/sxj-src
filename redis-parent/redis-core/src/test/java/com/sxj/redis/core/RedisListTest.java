package com.sxj.redis.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sxj.redis.core.collections.RedisCollections;

public class RedisListTest
{
    RedisCollections collections;
    
    private static final String LIST_NAME = "test-list";
    
    @Before
    public void setUp()
    {
        collections = new RedisCollections(
                "config/redis-collections.properties");
    }
    
    @Test
    public void testAdd()
    {
        RList<String> list = collections.getList(LIST_NAME);
        list.add("A");
        System.out.println(list.get(0));
    }
    
    @Test
    public void testAddAll()
    {
        RList<String> list = collections.getList(LIST_NAME);
        List<String> sub = new ArrayList<String>();
        sub.add("B");
        sub.add("C");
        list.addAll(sub);
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext())
            System.err.println(iterator.next());
    }
    
}
