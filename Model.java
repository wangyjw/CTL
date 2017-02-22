import java.util.ArrayList;
// state_id is an int
class ModelFactory {
  /**
    Returns a new and empty model.

    Students must provide this method, and it should
    return a new instance of a concrete class
    tha implements the model interface.
    */
  static Model makeEmptyModel(int debug_level) {
    KripkeModel newEmptyModel = new KripkeModel();
    return newEmptyModel;
  }
}

/**
  Interface for class used to store subsets of states of a Kripke structure.

  Students must define this as they see fit, to support the required
  model checking operations.
*/
interface StateSet {

  void AddState(int s);

  void print();

  void clear();

  ArrayList<Integer> getStateSet();

  boolean contains(int s);

}

/**

  Interface for models and model checking operations.

  Students must create a class that implements this interface.
  These are called as needed by the parser.
  */
interface Model {

  /**
    Finalize the Kripke structure.
    The parser will call this after all arcs have been processed,
    in case any post-processing must be done on the data structure
    used to store the Kripke structure.
    This method will be called exactly once by the parser.
    */
  boolean finish();

  /**
    Set the number of states in the model.
    This method is called once by the parser,
    before any arcs are added to the model,
    and before the call to finish().

    @param  n   Number of states in the Kripke structure.
    Once set, valid states are from 0 to n-1.
    */
  void setNumStates(int n);

  /**
    Check if the given state is valid.
    Will be called after setNumStates().

    @param  s   State id to check
    @return     True, iff s is within range 0..n-1.
    */
  boolean isValidState(int s);

  /**
    Add an arc in the Kripke structure,
    from state s1 to state s2.
    Will not be called after finish() has been called.

    @param  s1    Source state id
    @param  s2    Destination state id
    */
  void addArc(int s1, int s2);

  /**
    Create a new, empty, StateSet for this model.
    */
  StateSet makeEmptySet();

  /**
    Destroy a StateSet for this model.
    */
  void deleteSet(StateSet sset);

  /**
    Add a state to a StateSet.
    The parser calls this for state labels.

    @param  s     State id to add
    @param  sset  State_set to add s into
    */
  void addState(int s, StateSet sset);

  /**
    Copy a set of states.

    @param  sset    Source set of states.
    @param  rset    Destination set of states;
    on return of this method, set rset
    should contain the same states as set sset.
    */
  void copy(StateSet sset, StateSet rset);

  //
  // Unary operations
  //

  /**
    Take the complement of a set of states.
    @param  sset  On input: a set of states Y.
    @param  rset  On output: complement of Y is stored here.
    Note that rset and sset may point to the
    same object!
    */
  void NOT(StateSet sset, StateSet rset);

  /**
    Labeling for EX.
    @param  sset  On input: a set of states satisfying p.
    @param  rset  On output: the set of states satisfying EX p
    is stored here.
    Note that rset and sset may point to the
    same object!
    */
  void EX(StateSet sset, StateSet rset);

  /**
    Labeling for EF.
    @param  sset  On input: a set of states satisfying p.
    @param  rset  On output: the set of states satisfying EF p
    is stored here.
    Note that rset and sset may point to the
    same object!
    */
  void EF(StateSet sset, StateSet rset);

  /**
    Labeling for EG.
    @param  sset  On input: a set of states satisfying p.
    @param  rset  On output: the set of states satisfying EG p
    is stored here.
    Note that rset and sset may point to the
    same object!
    */
  void EG(StateSet sset, StateSet rset);

  /**
    Labeling for AX.
    @param  sset  On input: a set of states satisfying p.
    @param  rset  On output: the set of states satisfying AX p
    is stored here.
    Note that rset and sset may point to the
    same object!
    */
  void AX(StateSet sset, StateSet rset);

  /**
    Labeling for AF.
    @param  sset  On input: a set of states satisfying p.
    @param  rset  On output: the set of states satisfying AF p
    is stored here.
    Note that rset and sset may point to the
    same object!
    */
  void AF(StateSet sset, StateSet rset);

  /**
    Labeling for AG.
    @param  sset  On input: a set of states satisfying p.
    @param  rset  On output: the set of states satisfying AG p
    is stored here.
    Note that rset and sset may point to the
    same object!
    */
  void AG(StateSet sset, StateSet rset);

  //
  // Binary operations
  //

  /**
    Labeling for ^ (and).

    @param  sset1   On input: a set of states satisfying p.
    @param  sset2   On input: a set of states satisfying q.
    @param  rset    On output: the set of states satisfying p ^ q
    is stored here.
    Note that pointers sset1, sset2, and rset
    may be the same!
    */
  void AND(StateSet sset1, StateSet sset2, StateSet rset);

  /**
    Labeling for v (or).

    @param  sset1   On input: a set of states satisfying p.
    @param  sset2   On input: a set of states satisfying q.
    @param  rset    On output: the set of states satisfying p v q
    is stored here.
    Note that pointers sset1, sset2, and rset
    may be the same!
    */
  void OR(StateSet sset1, StateSet sset2, StateSet rset);

  /**
    Labeling for -> (implies).

    @param  sset1   On input: a set of states satisfying p.
    @param  sset2   On input: a set of states satisfying q.
    @param  rset    On output: the set of states satisfying p -> q
    is stored here.
    Note that pointers sset1, sset2, and rset
    may be the same!
    */
  void IMPLIES(StateSet sset1, StateSet sset2, StateSet rset);

  /**
    Labeling for EU.

    @param  sset1   On input: a set of states satisfying p.
    @param  sset2   On input: a set of states satisfying q.
    @param  rset    On output: the set of states satisfying E p U q
    is stored here.
    Note that pointers sset1, sset2, and rset
    may be the same!
    */
  void EU(StateSet sset1, StateSet sset2, StateSet rset);

  /**
    Labeling for AU.

    @param  sset1   On input: a set of states satisfying p.
    @param  sset2   On input: a set of states satisfying q.
    @param  rset    On output: the set of states satisfying A p U q
    is stored here.
    Note that pointers sset1, sset2, and rset
    may be the same!
    */
  void AU(StateSet sset1, StateSet sset2, StateSet rset);

  /**
    Check if a state is contained in a set.

    @param  s     State id to check
    @param  sset  Set of states

    @return true  iff state s is contained in set sset.
    */
  boolean elementOf(int s, StateSet sset);

  /**
    Display all states contained in a set to standard output.
    Output should be a comma separated list of state ids, in order,
    contained in the set.
    For example, output should be
    "{}"      for an empty set,
    "{S42}"   for a set containing state id 42,
    "{S0, S1, S7}"  for a set containing state ids 0, 1, and 7.

    @param  sset    Set to display.
    */
  void display(StateSet sset);
}
