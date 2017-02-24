/*
This class implements model and all its methods
All methods are defined in Model.java
Also need to implent graph stuff
*/
import java.util.HashMap;
import java.util.ArrayList;

public class KripkeModel implements Model{

  public HashMap<Integer, ArrayList<Integer>> NodeMap;
  public int numberOfStates;
  //ipmlemnt hashmap
  public KripkeModel(){
    //System.out.println("New Model!");
    NodeMap = new HashMap<Integer, ArrayList<Integer>>();
  }


  public boolean finish()
  {
    //System.out.println("Finish Called");
    //System.out.println(NodeMap.get(3));
    return true;
  }

  public void setNumStates(int n)
  {
    numberOfStates = n;
    return;
  }

  public boolean isValidState(int s)
  {
    if(s >= 0 || s < numberOfStates){
      return true;
    }
    return false;
  }

  public void addArc(int s1, int s2)
  {
    //TODO
    if(!NodeMap.containsKey(s1)){
      ArrayList<Integer> hashVal = new ArrayList<Integer>();
      hashVal.add(s2);
      NodeMap.put(s1, hashVal);
    }
    else{
      ArrayList<Integer> val = NodeMap.get(s1);
      if(!val.contains(s2)){
        val.add(s2);
        NodeMap.put(s1,val);
      }
    }
    //System.out.println("AddArc called");

    return;
  }

  public StateSet makeEmptySet()
  {
    StateSet emptySet = new StateSetTest();
    return emptySet;
  }

  public void deleteSet(StateSet sset)
  {
    //TODO
    return;
  }

  public void addState(int s, StateSet sset)
  {
    sset.AddState(s);
    return;
  }

  public void copy(StateSet sset, StateSet rset)
  {
    rset.clear();
    ArrayList<Integer> toCopy = sset.getStateSet();
    for(int i = 0; i < toCopy.size(); i++){
      rset.AddState(toCopy.get(i));
    }
    return;
  }

  public void NOT(StateSet sset, StateSet rset)
  {
    StateSet temp = new StateSetTest();
    temp.clear();

    for(int i = 0; i < numberOfStates; i++){
      if(!sset.contains(i)){
        temp.AddState(i);
      }
    }
    copy(temp, rset);
    return;
  }

  public void EX(StateSet sset, StateSet rset){
  //  System.out.println("SSET");
    //sset.print();
    StateSet temp = new StateSetTest(); // may need to simplify this
    //copy(rset, temp);
    temp.clear();
    //rset = temp;
    //Begin
    //mark with AX p iff all sucessors are labeled with p
    for(int i = 0; i < numberOfStates; i++){
      ArrayList<Integer> outEdge = NodeMap.get(i);
      //System.out.println(outEdge);
      for(int j = 0; j < outEdge.size(); j++){
        if(sset.contains(outEdge.get(j))){
          temp.AddState(i);
        }
      }

    }
    //System.out.println();
    //temp.print();
    copy(temp, rset);
    //sset.print();
    //rset.print();
    return;
  }

  public void EF(StateSet sset, StateSet rset)
  {
    //EF t == E tt U t
    StateSet temp = new StateSetTest();
    temp.clear();
    StateSet temp1 = new StateSetTest();
    temp1.clear();

    //Adds all states
    for(int i = 0; i < numberOfStates; i++){
      temp.AddState(i);
    }

    EU(temp, sset, temp1);
    copy(temp1, rset);

    return;
  }

  public void EG(StateSet sset, StateSet rset)
  {
    StateSet temp = new StateSetTest();
    StateSet temp1 = new StateSetTest();
    StateSet temp2 = new StateSetTest();
    temp.clear();
    temp1.clear();
    temp2.clear();

    NOT(sset, temp);
    AF(temp, temp1);
    NOT(temp1, temp2);

    copy(temp2, rset);

    return;
  }

  public void AX(StateSet sset, StateSet rset)
  {
    StateSet temp = new StateSetTest(); // may need to simplify this
    temp.clear();
    //Begin
    //mark with AX p iff all sucessors are labeled with p
    for(int i = 0; i < numberOfStates; i++){
      ArrayList<Integer> outEdge = NodeMap.get(i);
      int test = 0;
      for(int j = 0; j < outEdge.size(); j++){
        if(sset.contains(outEdge.get(j))){
          test++;
        }
      }
      if(outEdge.size() == test){
        temp.AddState(i);
      }
      test = 0;

    }
    //System.out.println();
    //temp.print();
    copy(temp, rset);
    //sset.print();
    rset.print();
    return;
  }

  public void AF(StateSet sset, StateSet rset){
    //1. Any State labeled with t1 is also labeled with t
    //2. If all successsors of state s are labbled with t then label s with t
    //3. Repeat 2 intill no changes are possible

    StateSet temp = new StateSetTest(); // may need to simplify this
    temp.clear();
    //1.)
    copy(sset, temp);

    boolean change = true;
    while(change){
      change = false;
      for(int i = 0; i < numberOfStates; i++){
        ArrayList<Integer> outEdge = NodeMap.get(i);
        int size = outEdge.size();
        int test = 0;
        for(int j = 0; j < size; j++){
          if(temp.contains(outEdge.get(j))){
            test++;
          }
        }
        if(test == size){ //all sucessors are marked
          //System.out.println(i);
          if(!temp.contains(i)){
            temp.AddState(i);
            change = true;
            //System.out.println(i);
          }
        }
      }
      //change = false;
    }
    copy(temp, rset);
    return;
  }

  public void AG(StateSet sset, StateSet rset){
    // !EF t == AG !t
    StateSet temp = new StateSetTest();
    temp.clear();
    StateSet temp1 = new StateSetTest();
    temp1.clear();
    StateSet temp2 = new StateSetTest();
    temp2.clear();

    NOT(sset, temp); // !t
    EF(temp, temp1);
    NOT(temp1, temp2);

    copy(temp2, rset);


    return;
  }

  public void AND(StateSet sset1, StateSet sset2, StateSet rset)
  {
    StateSet temp = new StateSetTest();
    temp.clear();

    ArrayList<Integer>set1States = sset1.getStateSet();
    //ArrayList<Integer>set2States = sset2.getStateSet();

    for(int i = 0; i < set1States.size(); i++){
      int state = set1States.get(i);
      if(sset2.contains(state)){
        temp.AddState(state);
      }
    }

    copy(temp, rset);
    //display(rset);
    return;
  }

  public void OR(StateSet sset1, StateSet sset2, StateSet rset)
  {
    StateSet temp = new StateSetTest();
    temp.clear();

    ArrayList<Integer> p = sset1.getStateSet();
    ArrayList<Integer> q = sset2.getStateSet();

    for(int i = 0; i < p.size(); i++){
      int state = p.get(i);
      if(!temp.contains(state)) {
        temp.AddState(state);
      }
    }

    for(int j = 0; j < q.size(); j++){
      int state = q.get(j);
      if(!temp.contains(state)) {
        temp.AddState(state);
      }
    }

    copy(temp, rset);

    return;
  }

  public void IMPLIES(StateSet sset1, StateSet sset2, StateSet rset)
  {
    StateSet temp = new StateSetTest();
    temp.clear();

    for(int i = 0; i < numberOfStates; i++){
      if(!sset1.contains(i)){
        temp.AddState(i);
      }
      else if(sset1.contains(i) && sset2.contains(i)){
        temp.AddState(i);
      }
    }

    return;
  }

  public void EU(StateSet sset1, StateSet sset2, StateSet rset)
  {
    //t == E t1 U t2
    //1. Any state labeled with t2 is labeled with t
    //2. if state s is labeled with t1 and some sucessors of s is labeled with t then label s with t
    //3. Repeat untill no changes are possible
    StateSet temp = new StateSetTest(); // may need to simplify this
    temp.clear();

    ArrayList<Integer> t2 = sset2.getStateSet();
    int size = t2.size();
    //Step 1.
    for(int i = 0; i < size; i++){
      temp.AddState(t2.get(i));
    }

    //step 2.
    boolean change = true;
    while(change){
      change = false;

      for(int i = 0; i < numberOfStates; i++){
        if(sset1.contains(i)){ //state is labeled with t1
          ArrayList<Integer> outEdges = NodeMap.get(i);
          int outSize = outEdges.size();
          for(int j = 0; j < outSize; j++){
            int d = outEdges.get(j);
            if(temp.contains(d) && !temp.contains(i)){
              temp.AddState(i);
              change = true;
              //System.out.println("TRUE");
            }
          }
        }
      }
    }
    copy(temp, rset);
    display(rset);

    return;
  }

  public void AU(StateSet sset1, StateSet sset2, StateSet rset)
  {
    //t == A t1 U t2
    // !A t1 U t2 == EG !t2
    StateSet temp = new StateSetTest();
    temp.clear();
    StateSet temp1 = new StateSetTest();
    temp1.clear();
    StateSet temp2 = new StateSetTest();
    temp2.clear();

    //temp = !t2
    NOT(sset2, temp);
    EG(temp, temp1);
    NOT(temp1, temp2);

    copy(temp2, rset);

    return;
  }

  public boolean elementOf(int s, StateSet sset)
  {
    return(sset.contains(s));
  }

  public void display(StateSet sset)
  {
    ArrayList<Integer> toDisplay = sset.getStateSet();
    int size = toDisplay.size();
    //print begining brace
    System.out.print("{");

    //for loop to print the innerds
    for(int i = 0; i < size; i++){
      if(i != size - 1){
        System.out.print("S" + toDisplay.get(i) + ", ");
      }
      else{
        System.out.print("S" + toDisplay.get(i));
      }
    }

    System.out.print("}");
    System.out.println(); //mayneed to delete
    return;
  }


}
