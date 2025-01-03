import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main{
    public static void main(String[] args){
        
    }
}

class ThompsonClass{
    private final char NULL_CHAR = Character.MIN_VALUE;

    public Automaton emptyWord(){
        Automaton automaton = new Automaton();
        State state1 = automaton.createState();
        automaton.markAsFinal(state1);
        automaton.pushState(automaton.initial, NULL_CHAR, state1);
        return automaton;
    }

    public Automaton singleCharacter(char character){
        Automaton automaton = new Automaton();
        State state1 = automaton.createState();
        automaton.markAsFinal(state1);
        automaton.pushState(automaton.initial, character, state1);
        return automaton;
    }

    public Automaton concatenate(Automaton first, Automaton push){
        first.concatenateAutomaton(push);
        return first;
    }

    public Automaton union(Automaton a1, Automaton a2){
        Automaton result = new Automaton();
        // Connect the initial state of result with the initial state of a1 and a2
        result.pushState(result.initial, NULL_CHAR, a1.initial);
        result.pushState(result.initial, NULL_CHAR, a2.initial);
        // connect the final states of a1 and a2 with the final state of result
        State finalState = result.createState();
        for (State a1FinalStates : a1.finalStates) {
            result.pushState(finalState, NULL_CHAR, a1FinalStates);
        }
        for (State a2FinalStates : a2.finalStates) {
            result.pushState(finalState, NULL_CHAR, a2FinalStates);
        }
        result.finalStates.add(finalState);
        return result;
    }

    public Automaton star(Automaton automaton){
        Automaton result = new Automaton();
        // Connect the initial state of result with the initial state of automaton
        result.pushState(result.initial, NULL_CHAR, automaton.initial);
        // Connect the initial state of result with his final state
        State finalState = result.createState();
        result.finalStates.add(finalState);
        // Connect the final states of automaton with the final state of result
        for(State automatonFinalState : automaton.finalStates){
            result.pushState(automatonFinalState, NULL_CHAR, finalState);
        }
        // Connect the final state of automaton with his initial state
        for(State automatonFinalState : automaton.finalStates){
            automaton.pushState(automatonFinalState, NULL_CHAR, automaton.initial);
        }
        return automaton;
    }
}

/* class to handle regular expression and extract information from it in a centralized way */
class RegexHandler{
    /* | : OR 
     * * : Zero or more
     * . : Any character
     * Character.MIN_VALUE : No character (Epsilon).
     * (, ) : Grouping parentheses.
    */

    /* Split String around an operator. 
     * returns
     *      null: no operator found
     *      String[3]: {v1, operator, v2};
    */
    public static String[] SplitAroundOperator(String v){ // TESTED ☑
        String[] result = {null, null, null};
        // Search for an operator
        for(int i=0; i<v.length(); i++){
            char actual = v.charAt(i);
            if(     (actual == 124) // |
                ||  (actual == 42)  // *
                ||  (actual == 46)  // .
            ){
                result = new String[3];
                result[0] = v.substring(0, i);
                result[1] = ""+actual;
                result[2] = v.substring((i+1), v.length());
                break;
            }
        }
        return result;
    }
    public static String getInsideParentheses(String v){ // TESTED ☑
        int start = 0, fin = v.length();
        // Search (
        for(int i=0; i<v.length(); i++){
            if(v.charAt(i) == 40){
                start = i+1;
                break;
            }
        }
        // Search )
        for(int i=(v.length()-1); i>=start; i--){
            if(v.charAt(i) == 41){
                fin = i;
                break;
            }
        }
        return v.substring(start, fin);
    }
}

class Automaton{
    State initial = new State(0);
    State lastStateReached = initial;
    List<State> finalStates = new ArrayList<State>();
    // δ(state, character) = stateReached
    public void pushState(State state, char character, State stateReached){
        state.setTransition(character, stateReached);
        this.lastStateReached = stateReached;
    }
    public State createState(){
        return new State((this.lastStateReached.id+1));
    }
    public void markAsFinal(State state){
        this.finalStates.add(state);
    }
    public void concatenateAutomaton(Automaton automaton){
        State bridgeState = this.finalStates.get(0);
        this.finalStates.remove(bridgeState);
        this.finalStates.addAll(automaton.finalStates);
        bridgeState.transitions = automaton.initial.transitions;
    }
}

class State{
    int id;
    Map<Character, State> transitions = new HashMap<>();
    public State(int id){
        this.id = id;
    }
    // Use char LF (10) to represent a transition with any character.
    public void setTransition(char character, State state){
        this.transitions.put(character, state);
    }
}