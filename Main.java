import java.util.HashMap;
import java.util.Map;

public class Main{
    public static void main(String[] args){
        
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
    public static String[] SplitAroundOperator(String v){
        String[] result = null;
        // Search for an operator
        for(int i=0; i<v.length(); i++){
            char actual = v.charAt(i);
            if(     (actual == 124) // |
                ||  (actual == 42)  // *
                ||  (actual == 46)  // .
            ){
                result = new String[3];
                result[0] = v.substring(0, (i-1));
                result[1] = ""+actual;
                result[2] = v.substring((i+1), (v.length()-1));
            }
        }
        return result;
    }
}

class Automaton{
    State initial = new State(0, this);
    State lastStateReached = initial;
    // δ(state, character) = stateReached
    public void pushState(State state, char character, State stateReached){
        state.setTransition(character, stateReached);
        this.lastStateReached = stateReached;
    }
    public State createState(){
        return new State((this.lastStateReached.id+1), this);
    }
}

class State{
    int id;
    Map<Character, State> transitions = new HashMap<>();
    Automaton parent;
    public State(int id, Automaton parent){
        this.id = id;
        this.parent = parent;
    }
    // Use char LF (10) to represent a transition with any character.
    public void setTransition(char character, State state){
        this.transitions.put(character, state);
    }
}