/*******************************************************************************
 The MIT License (MIT)

 Copyright (c) 2013 Benjamin Fagin

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package com.sxj.statemachine.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import com.sxj.statemachine.StateHandler;
import com.sxj.statemachine.exception.TransitionException;
import com.sxj.statemachine.interfaces.HandlerRegistration;
import com.sxj.statemachine.interfaces.SequenceHandler;
import com.sxj.statemachine.interfaces.State;
import com.sxj.statemachine.interfaces.StateMachine;
import com.sxj.statemachine.interfaces.StateRouter;
import com.sxj.statemachine.interfaces.TransitionHandler;

/**
 * Note that null is a valid state
 * in this system. The initial starting point is null by default.
 *
 * @author  Benjamin Fagin
 * @version 12-23-2010
 */
public class GenericStateMachine<T extends State> implements StateMachine<T>
{
    
    // states, and the routers that route them
    private final Map<StateWrapper, StateContainer> states = new HashMap<StateWrapper, StateContainer>();
    
    private final List<StateRouter<T>> routers = new ArrayList<StateRouter<T>>();
    
    // sequence matching
    private int maxRecent = 0;
    
    private final Queue<State> recentStates = new ArrayDeque<State>();
    
    private final Set<PatternMatcher> matchers = new HashSet<PatternMatcher>();
    
    // global handlers
    private final Set<StateHandler<T>> globalOnEntryHandlers = new HashSet<StateHandler<T>>();
    
    private final Set<StateHandler<T>> globalOnExitHandlers = new HashSet<StateHandler<T>>();
    
    private final Set<TransitionHandler<T>> globalOnTransitionHandlers = new HashSet<TransitionHandler<T>>();
    
    // backing data
    private StateContainer initial;
    
    private StateContainer current;
    
    private long transitions;
    
    public GenericStateMachine()
    {
        this(null);
    }
    
    public GenericStateMachine(T initial)
    {
        setInitialState(initial);
        reset();
    }
    
    public synchronized void reset()
    {
        transitions = 0;
        current = initial;
        recentStates.clear();
    }
    
    @SuppressWarnings("unchecked")
    public synchronized boolean transition(final T next)
    {
        final StateContainer requestedState = getState(next);
        final StateContainer nextState = route(next, requestedState);
        
        if (!current.transitions.containsKey(nextState))
        {
            throw new TransitionException("No transition exists between "
                    + current + " and " + requestedState);
        }
        
        onExit();
        onTransition(nextState);
        onEntry(nextState);
        
        transitions += 1;
        doPatternMatching(nextState);
        
        if (current == nextState)
        {
            return false;
        }
        else
        {
            current = nextState;
            return true;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void doPatternMatching(StateContainer nextState)
    {
        if (maxRecent != 0)
        {
            recentStates.add(nextState.state);
        }
        
        if (recentStates.size() > maxRecent)
        {
            recentStates.remove();
        }
        
        final List<State> recent = Collections.unmodifiableList(new ArrayList<State>(
                recentStates));
        
        for (PatternMatcher matcher : matchers)
        {
            if (matcher.matches(recent.iterator(), recent.size()))
            {
                matcher.handler.onMatch(matcher.pattern);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private StateContainer route(T next, StateContainer requestedState)
    {
        StateContainer nextState = null;
        
        // routing
        for (StateRouter<T> router : routers)
        {
            T decision = router.route((T) current.state, next);
            
            if (decision != null)
            {
                
                // if it's the same, bypass lookup
                if (decision == next)
                {
                    nextState = requestedState;
                }
                
                // otherwise lookup the new state
                else
                {
                    nextState = getState(decision);
                }
            }
        }
        
        // default to the originally requested state
        if (nextState == null)
        {
            nextState = requestedState;
        }
        
        return nextState;
    }
    
    @SuppressWarnings("unchecked")
    private void onEntry(StateContainer nextState)
    {
        for (StateHandler handler : globalOnEntryHandlers)
        {
            handler.onState(nextState.state);
        }
        
        for (StateHandler entryAction : nextState.entryActions)
        {
            entryAction.onState(nextState.state);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void onTransition(StateContainer nextState)
    {
        for (TransitionHandler handler : globalOnTransitionHandlers)
        {
            handler.onTransition(current.state, nextState.state);
        }
        
        Transition transition = current.transitions.get(nextState);
        
        for (TransitionHandler handler : transition.callbacks)
        {
            handler.onTransition(current.state, nextState.state);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void onExit()
    {
        for (StateHandler handler : globalOnExitHandlers)
        {
            handler.onState(current.state);
        }
        
        for (StateHandler handler : current.exitActions)
        {
            handler.onState(current.state);
        }
    }
    
    @SuppressWarnings("unchecked")
    public synchronized T currentState()
    {
        return (T) current.state;
    }
    
    public synchronized long transitionCount()
    {
        return transitions;
    }
    
    @SuppressWarnings("unchecked")
    public synchronized T initialState()
    {
        return (T) initial.state;
    }
    
    public synchronized void setInitialState(T state)
    {
        initial = getState(state);
    }
    
    public synchronized void addAllTransitions(List<T> states,
            boolean includeSelf)
    {
        for (T state : states)
        {
            List<T> _toStates;
            
            if (includeSelf)
            {
                _toStates = states;
            }
            else
            {
                _toStates = new ArrayList<T>(states);
                _toStates.remove(state);
            }
            
            addTransitions(state, _toStates);
        }
    }
    
    public HandlerRegistration onEntering(final StateHandler<T> callback)
    {
        globalOnEntryHandlers.add(callback);
        
        return new HandlerRegistration()
        {
            public void unregister()
            {
                globalOnEntryHandlers.remove(callback);
            }
        };
    }
    
    public synchronized HandlerRegistration onEntering(T state,
            final StateHandler<T> callback)
    {
        final StateContainer s = getState(state);
        s.entryActions.add(callback);
        
        return new HandlerRegistration()
        {
            public void unregister()
            {
                s.entryActions.remove(callback);
            }
        };
    }
    
    public HandlerRegistration onExiting(final StateHandler<T> callback)
    {
        globalOnExitHandlers.add(callback);
        
        return new HandlerRegistration()
        {
            public void unregister()
            {
                globalOnExitHandlers.remove(callback);
            }
        };
    }
    
    public synchronized HandlerRegistration onExiting(T state,
            final StateHandler<T> callback)
    {
        final StateContainer s = getState(state);
        s.exitActions.add(callback);
        
        return new HandlerRegistration()
        {
            public void unregister()
            {
                s.exitActions.remove(callback);
            }
        };
    }
    
    public HandlerRegistration onTransition(final TransitionHandler<T> callback)
    {
        globalOnTransitionHandlers.add(callback);
        
        return new HandlerRegistration()
        {
            public void unregister()
            {
                globalOnTransitionHandlers.remove(callback);
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    public synchronized HandlerRegistration onTransition(final T from,
            final T to, final TransitionHandler<T> callback)
    {
        addTransitions(callback, false, from, Arrays.asList(to));
        
        return new HandlerRegistration()
        {
            public void unregister()
            {
                removeCallback(callback, from, to);
            }
        };
    }
    
    public synchronized HandlerRegistration routeOnTransition(
            final StateRouter<T> router)
    {
        if (router == null)
        {
            throw new IllegalArgumentException("router cannot be null");
        }
        
        routers.add(router);
        
        return new HandlerRegistration()
        {
            public void unregister()
            {
                routers.remove(router);
            }
        };
    }
    
    public synchronized HandlerRegistration routeOnTransition(final T from,
            final T to, final StateRouter<T> router)
    {
        return routeOnTransition(new StateRouter<T>()
        {
            public T route(T current, T next)
            {
                
                // only route if it matches the pattern
                if (current == from && next == to)
                {
                    return router.route(current, next);
                }
                else
                {
                    return null;
                }
            }
        });
    }
    
    public synchronized HandlerRegistration routeBeforeEntering(final T to,
            final StateRouter<T> router)
    {
        return routeOnTransition(new StateRouter<T>()
        {
            public T route(T current, T next)
            {
                
                // only route if it matches the pattern
                if (next == to)
                {
                    return router.route(current, next);
                }
                else
                {
                    return null;
                }
            }
        });
    }
    
    public synchronized HandlerRegistration routeAfterExiting(final T from,
            final StateRouter<T> router)
    {
        return routeOnTransition(new StateRouter<T>()
        {
            public T route(T current, T next)
            {
                
                // only route if it matches the pattern
                if (current == from)
                {
                    return router.route(current, next);
                }
                else
                {
                    return null;
                }
            }
        });
    }
    
    public synchronized HandlerRegistration onSequence(List<T> pattern,
            SequenceHandler<T> handler)
    {
        List<State> _pattern = Collections.<State> unmodifiableList(pattern);
        final PatternMatcher matcher = new PatternMatcher(_pattern, handler);
        matchers.add(matcher);
        maxRecent = Math.max(maxRecent, pattern.size());
        
        return new HandlerRegistration()
        {
            public void unregister()
            {
                matchers.remove(matcher);
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    public synchronized boolean addTransition(T fromState, T toState)
    {
        return addTransitions(null, true, fromState, Arrays.asList(toState));
    }
    
    @SuppressWarnings("unchecked")
    public synchronized boolean addTransition(T fromState, T toState,
            TransitionHandler<T> callback)
    {
        return addTransitions(callback, true, fromState, Arrays.asList(toState));
    }
    
    public synchronized boolean addTransitions(T fromState, T... toStates)
    {
        return addTransitions(null, true, fromState, Arrays.asList(toStates));
    }
    
    public synchronized boolean addTransitions(T fromState, List<T> toStates,
            TransitionHandler<T> callback)
    {
        return addTransitions(callback, true, fromState, toStates);
    }
    
    public boolean addTransitions(T fromState, List<T> toStates)
    {
        return addTransitions(null, true, fromState, toStates);
    }
    
    public boolean addTransitions(TransitionHandler<T> callback, T fromState,
            T... toStates)
    {
        return addTransitions(callback,
                true,
                fromState,
                Arrays.asList(toStates));
    }
    
    private boolean addTransitions(TransitionHandler<T> callback,
            boolean create, T fromState, List<T> toStates)
    {
        Set<T> set = new HashSet<T>(toStates);
        StateContainer from = getState(fromState);
        boolean modified = false;
        
        for (T state : set)
        {
            StateContainer to = getState(state);
            Transition transition = null;
            
            if (from.transitions.containsKey(to))
            {
                transition = from.transitions.get(to);
            }
            else if (create)
            {
                transition = new Transition(to);
                from.transitions.put(to, transition);
                modified = true;
            }
            
            if (transition != null && callback != null)
            {
                transition.callbacks.add(callback);
            }
        }
        
        if (modified)
        {
            reset();
        }
        return modified;
    }
    
    public synchronized boolean removeTransitions(T fromState, T... toStates)
    {
        return removeTransitions(fromState, Arrays.asList(toStates));
    }
    
    public boolean removeTransitions(T fromState, List<T> toStates)
    {
        Set<T> set = new HashSet<T>(toStates);
        StateContainer from = getState(fromState);
        boolean modified = false;
        
        for (T state : set)
        {
            StateContainer to = getState(state);
            if (from.transitions.remove(to) != null)
            {
                modified = true;
            }
        }
        
        if (modified)
        {
            reset();
        }
        return modified;
    }
    
    private void removeCallback(TransitionHandler callback, T fromState,
            T... toStates)
    {
        Set<T> set = new HashSet<T>(Arrays.asList(toStates));
        StateContainer from = getState(fromState);
        
        for (T state : set)
        {
            StateContainer to = getState(state);
            Transition transition = from.transitions.get(to);
            
            if (transition != null)
            {
                transition.callbacks.remove(callback);
            }
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        if (initial != null)
        {
            sb.append(fullString(initial.state)).append(" | \n");
        }
        
        int i = 1;
        Map<StateWrapper, StateContainer> sortedStates = new TreeMap<StateWrapper, StateContainer>(
                states);
        
        for (Map.Entry<StateWrapper, StateContainer> entry : sortedStates.entrySet())
        {
            sb.append("\t")
                    .append(fullString(entry.getKey().state))
                    .append(" : {");
            
            int j = 1;
            Map<StateContainer, Transition> sortedTransitions = new TreeMap<StateContainer, Transition>(
                    entry.getValue().transitions);
            
            for (Transition t : sortedTransitions.values())
            {
                sb.append(fullString(t.next.state));
                
                if (j++ != sortedTransitions.size())
                {
                    sb.append(", ");
                }
            }
            
            sb.append("}");
            
            if (i++ != states.size())
                sb.append(" | \n");
        }
        
        return sb.toString();
    }
    
    private StateContainer getState(T token)
    {
        StateWrapper wrapped = new StateWrapper(token);
        
        if (states.containsKey(wrapped))
        {
            return states.get(wrapped);
        }
        
        StateContainer s = new StateContainer(token);
        states.put(wrapped, s);
        return s;
    }
    
    private static class StateContainer implements Comparable<StateContainer>
    {
        final State state;
        
        final Map<StateContainer, Transition> transitions = new HashMap<StateContainer, Transition>();
        
        final Set<StateHandler> entryActions = new HashSet<StateHandler>();
        
        final Set<StateHandler> exitActions = new HashSet<StateHandler>();
        
        StateContainer(State state)
        {
            this.state = state;
        }
        
        @Override
        public String toString()
        {
            return state != null ? state.name() : "null";
        }
        
        public int compareTo(StateContainer other)
        {
            String n1 = state == null ? "" : state.name();
            String n2 = other == null ? "" : other.state.name();
            return n1.compareTo(n2);
        }
    }
    
    private static class StateWrapper implements Comparable<StateWrapper>
    {
        private final State state;
        
        StateWrapper(State state)
        {
            this.state = state;
        }
        
        public int compareTo(StateWrapper other)
        {
            String n1 = state == null ? "" : state.name();
            String n2 = other.state == null ? "" : other.state.name();
            return n1.compareTo(n2);
        }
        
        @Override
        public boolean equals(Object obj)
        {
            StateWrapper that = (StateWrapper) obj;
            
            String s1 = this.state != null ? this.state.name().trim() : null;
            String s2 = that.state != null ? that.state.name().trim() : null;
            
            if (s1 == null ^ s2 == null)
            {
                return false;
            }
            
            if (s1 == null)
            {
                return true;
            }
            
            return s1.equals(s2);
        }
        
        @Override
        public int hashCode()
        {
            return state != null ? state.name().trim().hashCode() : 0;
        }
    }
    
    private static class Transition
    {
        final StateContainer next;
        
        final Set<TransitionHandler> callbacks = new HashSet<TransitionHandler>();
        
        Transition(StateContainer next)
        {
            this.next = next;
        }
        
        //		public @Override boolean equals(Object obj) {
        //			if (!(obj instanceof Transition)) { return false; }
        //			Transition other = (Transition) obj;
        //
        //			if (this.next.theEnum == null) {
        //				return other.next.theEnum == null;
        //			} else {
        //				return this.next.theEnum.equals(other.next.theEnum);
        //			}
        //		}
    }
    
    private static class PatternMatcher
    {
        private final List<State> pattern;
        
        private final SequenceHandler handler;
        
        PatternMatcher(List<State> pattern, SequenceHandler handler)
        {
            this.pattern = pattern;
            this.handler = handler;
        }
        
        boolean matches(Iterator<State> it, int size)
        {
            if (size < pattern.size())
            {
                return false;
            }
            
            for (State state : pattern)
            {
                if (!state.equals(it.next()))
                {
                    return false;
                }
            }
            
            return true;
        }
    }
    
    private static String fullString(State state)
    {
        return state != null ? state.name() : null;
    }
}