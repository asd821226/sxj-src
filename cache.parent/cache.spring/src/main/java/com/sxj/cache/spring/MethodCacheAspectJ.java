package com.sxj.cache.spring;

import java.lang.reflect.Method;
import java.util.Date;

import net.sf.ehcache.CacheException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.sxj.cache.manager.CacheLevel;
import com.sxj.cache.manager.HierarchicalCacheManager;
import com.sxj.cache.spring.annotation.Cached;

@Aspect
public class MethodCacheAspectJ
{
    @Pointcut("@annotation(com.sxj.cache.spring.annotation.Cached)")
    public void methodCachePointCut()
    {
        return;
    }
    
    @Around("methodCachePointCut()")
    public Object methodCacheHold(ProceedingJoinPoint joinPoint)
    {
        try
        {
            String targetName = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            Object[] arguments = joinPoint.getArgs();
            Method method = findCachedMethod(joinPoint.getTarget().getClass(),
                    methodName,
                    arguments);
            Cached annotation = method.getAnnotation(Cached.class);
            CacheLevel level = annotation.level();
            int timeToLive = annotation.timeToLive();
            String name = annotation.name();
            Object result = null;
            String cacheKey = getCacheKey(targetName, methodName, arguments);
            Object object = HierarchicalCacheManager.get(level.ordinal(),
                    name,
                    cacheKey);
            if (object != null)
                return object;
            else
            {
                result = joinPoint.proceed();
                
                if (result != null)
                {
                    HierarchicalCacheManager.set(level.ordinal(),
                            name,
                            cacheKey,
                            result,
                            timeToLive);
                }
            }
            return result;
        }
        catch (Throwable t)
        {
            throw new CacheException(t);
        }
    }
    
    private Method findCachedMethod(Class<?> clazz, String methodName,
            Object[] arguments) throws NoSuchMethodException, SecurityException
    {
        Class<?>[] parameterTypes = new Class<?>[arguments.length];
        for (int i = 0; i < arguments.length; i++)
        {
            parameterTypes[i] = arguments[i].getClass();
        }
        Method method = clazz.getMethod(methodName, parameterTypes);
        if (method.isAnnotationPresent(Cached.class))
            return method;
        return null;
    }
    
    private String getCacheKey(String targetName, String methodName,
            Object[] arguments)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(targetName).append(".").append(methodName);
        if ((arguments != null) && (arguments.length != 0))
        {
            for (int i = 0; i < arguments.length; i++)
            {
                if (arguments[i] instanceof Date)
                {
                    sb.append(".").append(((Date) arguments[i]).getTime());
                }
                else
                {
                    sb.append(".").append(arguments[i]);
                }
            }
        }
        return sb.toString();
    }
}
