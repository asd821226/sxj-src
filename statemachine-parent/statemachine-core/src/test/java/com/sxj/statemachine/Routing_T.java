package com.sxj.statemachine;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.sxj.statemachine.exception.TransitionException;
import com.sxj.statemachine.impl.EnumStateMachine;
import com.sxj.statemachine.interfaces.StateHandler;
import com.sxj.statemachine.interfaces.StateRouter;

/**
 * @author Ben Fagin
 * @version 2013-04-02
 */
public class Routing_T
{
    
    @Test
    public void testSimpleRedirect()
    {
        final AtomicInteger c1 = new AtomicInteger(0);
        final AtomicInteger c2 = new AtomicInteger(0);
        final AtomicInteger c3 = new AtomicInteger(0);
        
        EnumStateMachine<TestStates> esm = new EnumStateMachine<TestStates>(
                TestStates.One);
        esm.addAll(TestStates.class, true);
        
        esm.onEntering(TestStates.One, new StateHandler<TestStates>()
        {
            public void onState(TestStates state)
            {
                c1.incrementAndGet();
            }
        });
        
        esm.onEntering(TestStates.Two, new StateHandler<TestStates>()
        {
            public void onState(TestStates state)
            {
                c2.incrementAndGet();
            }
        });
        
        esm.onEntering(TestStates.Three, new StateHandler<TestStates>()
        {
            public void onState(TestStates state)
            {
                c3.incrementAndGet();
            }
        });
        
        esm.routeBeforeEntering(TestStates.Three, new StateRouter<TestStates>()
        {
            public TestStates route(TestStates current, TestStates next)
            {
                assertEquals(next, TestStates.Three);
                return TestStates.Two;
            }
        });
        
        esm.transition(TestStates.One);
        esm.transition(TestStates.Two);
        esm.transition(TestStates.Three);
        
        assertEquals(1, c1.get());
        assertEquals(2, c2.get());
        assertEquals(0, c3.get());
    }
    
    @Test(expected = TransitionException.class)
    public void testInvalidRoutingResult()
    {
        EnumStateMachine<TestStates> esm = new EnumStateMachine<TestStates>();
        esm.addTransition(null, TestStates.One);
        esm.addTransition(TestStates.One, TestStates.Two);
        
        // this should cause a failure, One -> Three not valid
        esm.routeAfterExiting(TestStates.One, new StateRouter<TestStates>()
        {
            public TestStates route(TestStates current, TestStates next)
            {
                return TestStates.Three;
            }
        });
        
        esm.transition(TestStates.One);
        esm.transition(TestStates.Two);
    }
    
    enum TestStates
    {
        One, Two, Three
    }
}
