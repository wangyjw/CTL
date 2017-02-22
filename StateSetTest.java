//implements StateSet
import java.util.*;

public class StateSetTest implements StateSet{
  public ArrayList<Integer> setStates = new ArrayList<Integer>();

  public StateSetTest(){
    setStates.clear();
  }

  public void AddState(int s) //change to hashmap with key s and value being labesl?
  {
    if (!setStates.contains(s))
      {
        setStates.add(s);
        //System.out.println(setStates);
      }
    //else{System.out.println("Set Already contains State " + s);}
  }

  public void print(){
    System.out.println(setStates);
  }

  public void clear(){
    setStates.clear();
  }

  public ArrayList<Integer> getStateSet(){
    return setStates;
  }

  public boolean contains(int s){
    if(setStates.contains(s)){
      return true;
    }
    return false;
  }
}
