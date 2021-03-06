/*
 * @(#)MyBatisTest.java 2013年12月23日 下午23:33:33
 *
 * Copyright (c) 2011-2013 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package com.sxj.mybatis.orm.unit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.sxj.mybatis.orm.mapper.UserMapper;
import com.sxj.mybatis.orm.model.User;

/**
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@ActiveProfiles("test")
public class MyBatisTest
{
    
    @Autowired(required = true)
    private UserMapper userMapper;
    
    private static User buildUser()
    {
        User user = new User();
        user.setUserName("makersoft");
        return user;
    }
    
    @Test
    @Transactional
    public void testInsert()
    {
        User user = buildUser();
        int rows = userMapper.insertUser(user);
        
        assertTrue("Insert entity not success!", rows > 0);
        assertNotNull("Id can not be null!", user.getId());
        
    }
    
    @Test
    @Transactional
    public void testUpdate()
    {
        User entity = userMapper.get(1L);
        assertNotNull("selected entity can not be null!", entity);
        
        User newUser = new User(entity.getId());
        newUser.setUserName("my_name");
        int rows = userMapper.update(newUser);
        
        assertTrue("Update entity not success!", rows == 1);
    }
    
    @Test
    @Transactional
    public void testGet()
    {
        User entity = userMapper.get(1L);
        assertNotNull("selected entity can not be null!", entity);
    }
    
    @Test
    @Transactional
    public void testDelete()
    {
        User user = userMapper.get(1L);
        user = userMapper.get(user.getId());
        
        int rows = userMapper.delete(user);
        
        assertTrue("Delete operation could not success!", rows == 1);
    }
    
}
