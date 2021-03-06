package com.sxj.cache.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.sxj.cache.core.Cache;
import com.sxj.cache.core.CacheException;
import com.sxj.spring.modules.util.Serializers;
import com.sxj.spring.modules.util.serializer.Serializer;

/**
 * Redis 缓存实现
 * 
 */
public class RedisCache implements Cache
{
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RedisCache.class);
    
    private static final Serializer K_SERIALIZER = Serializers.getJsonSerializer();
    
    private static final Serializer V_SERIALIZER = Serializers.getJdkSerializer();
    
    private static final int OFFSET = 3;
    
    private String region;
    
    private static final String DELETE_SCRIPT_IN_LUA = "local keys = redis.call('keys', '%s')"
            + "  for i,k in ipairs(keys) do"
            + "    local res = redis.call('del', k)" + "  end";
    
    public RedisCache(String region)
    {
        this.region = region;
    }
    
    private String serializeObject(Object object)
    {
        
        return V_SERIALIZER.serialize(object);
    }
    
    private Object deserializeObject(String str)
    {
        return V_SERIALIZER.deserialize(str);
    }
    
    private String serializeKey(Object key)
    {
        return region + ":" + K_SERIALIZER.serialize(key);
    }
    
    @Override
    public Object get(Object key)
    {
        Object obj = null;
        boolean broken = false;
        Jedis cache = RedisCacheProvider.getResource();
        try
        {
            if (null == key)
                return null;
            byte[] b = cache.get(serializeKey(key).getBytes());
            if (b != null)
                obj = deserializeObject(new String(b));
        }
        catch (Exception e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
            broken = true;
        }
        finally
        {
            RedisCacheProvider.returnResource(cache, broken);
        }
        return obj;
    }
    
    @Override
    public void put(Object key, Object value)
    {
        if (value == null)
            evict(key);
        else
        {
            boolean broken = false;
            Jedis cache = RedisCacheProvider.getResource();
            try
            {
                cache.set(serializeKey(key).getBytes(),
                        serializeObject(value).getBytes());
            }
            catch (Exception e)
            {
                broken = true;
                LOGGER.error("Error occured when get data from L2 cache", e);
            }
            finally
            {
                RedisCacheProvider.returnResource(cache, broken);
            }
        }
    }
    
    @Override
    public void update(Object key, Object value)
    {
        put(key, value);
    }
    
    @Override
    public void evict(Object key)
    {
        boolean broken = false;
        Jedis cache = RedisCacheProvider.getResource();
        try
        {
            cache.del(serializeKey(key));
        }
        catch (Exception e)
        {
            broken = true;
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        finally
        {
            RedisCacheProvider.returnResource(cache, broken);
        }
    }
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#batchRemove(java.util.List)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void evict(List keys)
    {
        if (CollectionUtils.isEmpty(keys))
            return;
        boolean broken = false;
        Jedis cache = RedisCacheProvider.getResource();
        try
        {
            String[] okeys = new String[keys.size()];
            for (int i = 0; i < okeys.length; i++)
            {
                okeys[i] = serializeKey(keys.get(i));
            }
            cache.del(okeys);
        }
        catch (Exception e)
        {
            broken = true;
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        finally
        {
            RedisCacheProvider.returnResource(cache, broken);
        }
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public List keys()
    {
        Jedis cache = RedisCacheProvider.getResource();
        boolean broken = false;
        
        try
        {
            List<String> keys = new ArrayList<String>();
            keys.addAll(cache.keys(region + ":*"));
            for (int i = 0; i < keys.size(); i++)
            {
                keys.set(i, keys.get(i).substring(region.length() + OFFSET));
            }
            return keys;
        }
        catch (Exception e)
        {
            broken = true;
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        finally
        {
            RedisCacheProvider.returnResource(cache, broken);
        }
        return Collections.EMPTY_LIST;
    }
    
    public void deleteKeys(String pattern)
    {
        boolean broken = false;
        Jedis cache = RedisCacheProvider.getResource();
        try
        {
            
            if (cache == null)
            {
                throw new CacheException("Unable to get jedis resource!");
            }
            
            cache.eval(String.format(DELETE_SCRIPT_IN_LUA, pattern));
        }
        catch (Exception e)
        {
            broken = true;
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        
        finally
        {
            if (cache != null)
            {
                RedisCacheProvider.returnResource(cache, broken);
            }
        }
    }
    
    @Override
    public void clear()
    {
        Jedis cache = RedisCacheProvider.getResource();
        boolean broken = false;
        try
        {
            deleteKeys(region + ":*");
        }
        catch (Exception e)
        {
            broken = true;
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        finally
        {
            RedisCacheProvider.returnResource(cache, broken);
        }
    }
    
    @Override
    public void destroy()
    {
        this.clear();
    }
    
    @Override
    public Long size()
    {
        
        return (long) keys().size();
    }
    
    @Override
    public List values()
    {
        throw new CacheException("Operation not supported!!!");
    }
    
    @Override
    public void put(Object key, Object value, int seconds)
    
    {
        if (value == null)
            evict(key);
        else
        {
            boolean broken = false;
            Jedis cache = RedisCacheProvider.getResource();
            try
            {
                Transaction multi = cache.multi();
                byte[] bytes = serializeKey(key).getBytes();
                multi.set(bytes, serializeObject(value).getBytes());
                multi.expire(bytes, seconds);
                multi.exec();
            }
            catch (Exception e)
            {
                broken = true;
                LOGGER.error("Error occured when get data from L2 cache", e);
            }
            finally
            {
                RedisCacheProvider.returnResource(cache, broken);
            }
        }
        
    }
    
    @Override
    public Boolean exists(Object key)
    {
        if (key == null)
            return false;
        else
        {
            boolean broken = false;
            Jedis cache = RedisCacheProvider.getResource();
            try
            {
                return cache.exists(serializeKey(key).getBytes());
            }
            catch (Exception e)
            {
                broken = true;
                LOGGER.error("Error occured when get data from L2 cache", e);
            }
            finally
            {
                RedisCacheProvider.returnResource(cache, broken);
            }
            
        }
        return false;
    }
}
