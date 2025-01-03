import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main{
    public static void main(String[] args){
        String regex = "AT*(TA|(AA*C))";
        ThompsonClass.ThompsonAlgorithm(regex).print();
    }
}

class ThompsonClass{
    private static final char NULL_CHAR = Character.MIN_VALUE;
    
    public static Automaton ThompsonAlgorithm(String regex){
        Automaton NFA = ThompsonRecursion(regex);
        return NFA;
    }

    private static Automaton ThompsonRecursion(String v){
        Automaton Thvl = null, Thvr = null;
        if(v.charAt(0) == 40){
            v = RegexHandler.getInsideParentheses(v);
        }
        String[] parts = RegexHandler.SplitAroundOperator(v);
        String vl = null, vr = null;
        if(parts[0]!=null){
            vl = RegexHandler.getInsideParentheses(parts[0]);
        }
        if(parts[2]!=null){
            vr = RegexHandler.getInsideParentheses(parts[2]);
        }
        if(parts[1].equals("|")|| parts[1].equals(".")){
            Thvl = ThompsonRecursion(vl);
            Thvr = ThompsonRecursion(vr);
        }else if(parts[1].equals("*")){
            Thvl = ThompsonRecursion(vl);
            Thvr = ThompsonRecursion(vr);
        }

        if(v.equals(NULL_CHAR+"")){
            return emptyWord();
        }if(parts[1].equals(".")){
            return concatenate(Thvl, Thvr);
        }if(parts[1].equals("|")){
            return union(Thvl, Thvr);
        }if(parts[1].equals("*")){
            if(Thvr == null){
                return star(Thvl);
            }else{
                return concatenate(star(Thvl), Thvr);
            }
        }if(v.length() > 0){
            Automaton chain = new Automaton();
            for(int i=0; i<v.length(); i++){
                chain.pushState(chain.lastStateReached, v.charAt(i), chain.createState());
            }
            chain.markAsFinal(chain.lastStateReached);
            return chain;
        }
        System.out.println("Se alcanzó el critico en la recursión con:\n"+vl+"\n"+parts[1]+"\n"+vr);
        return null;
    }

    public static Automaton emptyWord(){
        Automaton automaton = new Automaton();
        State state1 = automaton.createState();
        automaton.markAsFinal(state1);
        automaton.pushState(automaton.initial, NULL_CHAR, state1);
        return automaton;
    }

    public static Automaton singleCharacter(char character){
        Automaton automaton = new Automaton();
        State state1 = automaton.createState();
        automaton.markAsFinal(state1);
        automaton.pushState(automaton.initial, character, state1);
        return automaton;
    }

    public static Automaton concatenate(Automaton first, Automaton push){
        push.addID((first.maxID-1));
        first.concatenateAutomaton(push);
        return first;
    }

    public static Automaton union(Automaton a1, Automaton a2){
        Automaton result = new Automaton();
        State finalState = result.createState();
        a1.addID(result.maxID);
        a2.addID(a1.maxID);
        // Connect the initial state of result with the initial state of a1 and a2
        result.pushState(result.initial, NULL_CHAR, a1.initial);
        result.pushState(result.initial, NULL_CHAR, a2.initial);
        // connect the final states of a1 and a2 with the final state of result
        for (State a1FinalStates : a1.finalStates) {
            result.pushState(a1FinalStates, NULL_CHAR, finalState);
        }
        a1.finalStates.clear();
        for (State a2FinalStates : a2.finalStates) {
            result.pushState(a2FinalStates, NULL_CHAR, finalState);
        }
        a2.finalStates.clear();
        result.markAsFinal(finalState);
        result.maxID = a2.maxID;
        return result;
    }

    public static Automaton star(Automaton automaton){
        Automaton result = new Automaton();
        State finalState = result.createState();
        result.markAsFinal(finalState);
        automaton.addID(result.maxID);
        // Connect the initial state of result with the initial state of automaton
        result.pushState(result.initial, NULL_CHAR, automaton.initial);
        // Connect the initial state of result with his final state
        result.pushState(result.initial, NULL_CHAR, finalState);
        // Connect the final states of automaton with the final state of result
        for(State automatonFinalState : automaton.finalStates){
            result.pushState(automatonFinalState, NULL_CHAR, finalState);
        }
        // Connect the final state of automaton with his initial state
        for(State automatonFinalState : automaton.finalStates){
            automaton.pushState(automatonFinalState, NULL_CHAR, automaton.initial);
        }
        result.maxID = automaton.maxID;
        return result;
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
        String[] result = {v, "", ""};
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
    int maxID = 0;
    List<State> finalStates = new ArrayList<State>();
    // δ(state, character) = stateReached
    public void pushState(State state, char character, State stateReached){
        state.setTransition(character, stateReached);
        this.lastStateReached = stateReached;
    }
    public State createState(){
        this.maxID++;
        return new State((this.maxID));
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

    public void addID(int min){
        int accumulated = min;
        List<State> alreadyView = new ArrayList<State>();
        List<State> bifurcations = new ArrayList<State>();
        State actual = this.initial;
        while(actual != null){
            accumulated++;
            State newState = null;
            actual.id = accumulated;
            for (Map.Entry<State, Character> entry : actual.transitions.entrySet()) {
                State value = entry.getKey();
                // Using foreach to find the next state
                if(!(alreadyView.contains(value))){
                    newState = value;
                }
            }
            // Mark as viewed
            alreadyView.add(actual);
            // Check if have two or more transitions
            if(actual.transitions.size() > 1){
                bifurcations.add(actual);
            }
            /* If a new state is not found above, then it returns to the last state with more 
             * than one transition and looks for a possible branch to traverse. */
            State candidate = null;
            while(!(bifurcations.size()==0) && newState == null){
                candidate = bifurcations.get(bifurcations.size()-1);
                for(Map.Entry<State, Character> entry : candidate.transitions.entrySet()){
                    State value = entry.getKey();
                    if(!(alreadyView.contains(value) || finalStates.contains(value))){
                        newState = value;
                    }
                }
                if(newState == null){
                    bifurcations.remove(candidate);
                }
            }
            actual = newState;
        }
        this.maxID = accumulated;
    }

    public void print(){
        List<State> alreadyView = new ArrayList<State>();
        List<State> bifurcations = new ArrayList<State>();
        State actual = this.initial;
        while(actual != null){
            State newState = null;
            // Show actual state
            System.out.println("TRANSICIONES DEL NODO "+actual.id+":");
            for (Map.Entry<State, Character> entry : actual.transitions.entrySet()) {
                Character key = entry.getValue();
                State value = entry.getKey();
                System.out.println("("+actual.id+", " + key + ", " + value.id+")");
                // Using foreach to find the next state
                if(!(alreadyView.contains(value) || this.finalStates.contains(value))){
                    newState = value;
                }
            }
            // Mark as viewed
            alreadyView.add(actual);
            // Check if have two or more transitions
            if(actual.transitions.size() > 1){
                bifurcations.add(actual);
            }
            /* If a new state is not found above, then it returns to the last state with more 
             * than one transition and looks for a possible branch to traverse. */
            State candidate = null;
            while(!(bifurcations.size()==0) && newState == null){
                candidate = bifurcations.get(bifurcations.size()-1);
                for(Map.Entry<State, Character> entry : candidate.transitions.entrySet()){
                    State value = entry.getKey();
                    if(!(alreadyView.contains(value) || finalStates.contains(value))){
                        newState = value;
                    }
                }
                if(newState == null){
                    bifurcations.remove(candidate);
                }
            }
            actual = newState;
        }
    }
}

class State{
    int id;
    Map<State, Character> transitions = new HashMap<>();
    public State(int id){
        this.id = id;
    }
    // Use char LF (10) to represent a transition with any character.
    public void setTransition(char character, State state){
        this.transitions.put(state, character);
    }
}