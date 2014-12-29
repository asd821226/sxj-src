package com.sxj.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sxj.statemachine.exceptions.StateMachineException;
import com.sxj.statemachine.interfaces.IStateMachine;
import com.sxj.statemachine.interfaces.StateMachineDefinition;
import com.sxj.statemachine.interfaces.StateMachineStrategy;

/**
 * Basic state machine implementation. Implements a non-reentrant transition
 * strategy
 */
public class StateMachineImpl<S extends Enum<?>> implements IStateMachine<S>
{
    protected Logger l = LoggerFactory.getLogger(getClass());
    
    protected S currentState;
    
    protected StateMachineDefinition<S> definition;
    
    protected StateMachineStrategy<S> strategy;
    
    public StateMachineImpl(StateMachineDefinition<S> definition,
            StateMachineStrategy<S> strategy) throws StateMachineException
    {
        this.definition = definition;
        this.strategy = strategy;
        this.currentState = definition.getStartState();
        
        if (currentState == null)
            throw new StateMachineException(
                    "Start state has not been defined for the state machine");
    }
    
    /**
     * The state machine object is the entry point to the state management
     * world. The state machine is defined by the {@link StateMachineDefinition}
     * and the execution strategy is defined by {@link StateMachineStrategy}
     * 
     * <p>
     * This method delegates completely on the strategy.
     */
    public void fire(String event, Object object) throws StateMachineException
    {
        strategy.processEvent(this, event, object);
    }
    
    public StateMachineDefinition<S> getDefinition()
    {
        return this.definition;
    }
    
    public StateMachineStrategy<S> getStrategy()
    {
        return this.strategy;
    }
    
    public S getCurrentState()
    {
        return currentState;
    }
    
    public void setCurrentState(S currentState)
    {
        l.debug("#setCurrentState: " + currentState);
        this.currentState = currentState;
    }
    
    /**
     * Returns the state machine definition in a XML format. This is not a cheap
     * operation.
     */
    public String toString()
    {
        return this.definition.toString();
    }
}
