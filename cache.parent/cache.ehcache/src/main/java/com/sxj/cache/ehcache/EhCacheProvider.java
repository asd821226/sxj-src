package com.sxj.cache.ehcache;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sxj.cache.core.CacheException;
import com.sxj.cache.core.CacheExpiredListener;
import com.sxj.cache.core.CacheProvider;
import com.sxj.spring.modules.util.ClassLoaderUtil;

public class EhCacheProvider implements CacheProvider
{
    
    private final static Logger LOGGER = LoggerFactory.getLogger(EhCacheProvider.class);
    
    private final static String CONFIG_XML = "/ehcache.xml";
    
    private CacheManager manager;
    
    private ConcurrentHashMap<String, EhCache> _CacheManager;
    
    @Override
    public String name()
    {
        return "ehcache";
    }
    
    /**
     * Builds a Cache.
     * <p>
     * Even though this method provides properties, they are not used.
     * Properties for EHCache are specified in the ehcache.xml file.
     * Configuration will be read from ehcache.xml for a cache declaration
     * where the name attribute matches the name parameter in this builder.
     *
     * @param name the name of the cache. Must match a cache configured in ehcache.xml
     * @param properties not used
     * @return a newly built cache will be built and initialised
     * @throws CacheException inter alia, if a cache of the same name already exists
     */
    public EhCache buildCache(String name, boolean autoCreate,
            CacheExpiredListener listener) throws CacheException
    {
        EhCache ehcache = _CacheManager.get(name);
        if (ehcache == null && autoCreate)
        {
            try
            {
                synchronized (_CacheManager)
                {
                    ehcache = _CacheManager.get(name);
                    if (ehcache == null)
                    {
                        net.sf.ehcache.Cache cache = manager.getCache(name);
                        if (cache == null)
                        {
                            LOGGER.warn("Could not find configuration [" + name
                                    + "]; using defaults.");
                            manager.addCache(name);
                            cache = manager.getCache(name);
                            LOGGER.debug("started EHCache region: " + name);
                        }
                        ehcache = new EhCache(cache, listener);
                        _CacheManager.put(name, ehcache);
                    }
                }
            }
            catch (net.sf.ehcache.CacheException e)
            {
                throw new CacheException(e);
            }
        }
        return ehcache;
    }
    
    /**
     * Callback to perform any necessary initialization of the underlying cache implementation
     * during SessionFactory construction.
     *
     * @param properties current configuration settings.
     * @throws FileNotFoundException 
     */
    public void start(Properties props) throws CacheException
    {
        if (manager != null)
        {
            LOGGER.warn("Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() "
                    + " between repeated calls to buildSessionFactory. Using previously created EhCacheProvider."
                    + " If this behaviour is required, consider using net.sf.ehcache.hibernate.SingletonEhCacheProvider.");
            return;
        }
        if (props != null)
            LOGGER.info("Properties file not supported in this version!!");
        try
        {
            InputStream resource = ClassLoaderUtil.getResource(CONFIG_XML);
            if (resource == null)
                throw new CacheException("cannot find ehcache.xml !!!");
            manager = new CacheManager(resource);
            _CacheManager = new ConcurrentHashMap<String, EhCache>();
        }
        catch (FileNotFoundException e)
        {
            throw new CacheException(e);
        }
        
    }
    
    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    public void stop()
    {
        if (manager != null)
        {
            manager.shutdown();
            manager = null;
        }
    }
    
}
