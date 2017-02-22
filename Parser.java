
/**
  Parser for the model checking tool.

  Abandon all hope, ye who enter here.
  */

import java.io.*;
import java.lang.String;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;


enum ParserState {
  INIT, KRIPKE, STATES, INTEGER,
  ARCS, ARCS_S1, ARCS_ARROW, ARCS_S2,
  LABELS, LABELS_L, LABELS_COLON, LABELS_S, LABELS_COMMA,
  CTL, CTL_L, CTL_ASSIGN, CTL_FORMULA,
  CTL_S, CTL_MODELS, CTL_S_L,
  CTL_SET_OPEN, CTL_SET_L, CTL_SET_CLOSE,
  DONE
}

class Parser {

  public static void main(String[] args) {
    String fn = null;
    Model m = null;
    int debugLevel = 0;

    // debug levels:
    // 100: trace all states of parser
    // 101: trace the ctl formula evaluation
    Parser parser = new Parser();

    parser.setPrintStream(System.out);
    CtlFormula.setPrintStream(System.out);

    //
    // Process arguments, if any
    //
    for (int i=0; i<args.length; i++) {

      if (args[i].equals("-h")) { parser.usage(); }

      if (args[i].equals("-d")) {
        i++;
        try {
          debugLevel = Integer.parseInt(args[i]);
        } catch (NumberFormatException|IndexOutOfBoundsException e) {
          parser.usage();
        }
        continue;
      }

      if (fn != null) { parser.usage(); }
      fn = args[i];
    }

    if (debugLevel > 0) {
      System.out.println("Using debug level " + debugLevel);
      parser.setDebugLevel(debugLevel);
    }

    //
    // Decide if input is file, or stdin
    //

    InputStreamReader source = null;
    BufferedReader buffered_source = null;

    try {
      source = (fn == null)
        ? new InputStreamReader(System.in)
        : new FileReader(fn);
      buffered_source = new BufferedReader(source);
      m = parser.parseTokens(buffered_source);
      buffered_source.close();
      source.close();
    } catch (IOException e) {
      System.out.println("An error has occurred whilst opening " + fn);
      System.exit(0);
    } catch (Exception e) {
      System.out.println("Error while parsing " + fn);
      System.exit(0);
    }

    if (m != null) {
      m = null;
    } else {
      System.out.println("Error, null model - did you rewrite function makeEmptyModel()?");
    }
  }

  void usage() {
    StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
    StackTraceElement main = stack[stack.length - 1];
    String mainClass = main.getClassName ();

    out.println("\nUsage: " + mainClass + " [-h] [-d debugLevel] [input-file]\n");
    out.println("\t-h: display this help screen\n");
    out.println("\t-d: specify the debug level; a level of 0 (the default)");
    out.println("\t    should not display any debugging information\n");
    out.println("\tIf an input file is not specified, then the input file is");
    out.println("\tread from standard input.\n");
    System.exit(0);
  }

  // parse the input source
  Model parseTokens(BufferedReader source_stream)
    throws IOException {
      Model m = null;
      int numStates = 0;
      int s1 = 0;
      int s2 = 0;
      Vector<CtlFormula> ctlFormulas = new Vector<CtlFormula>();
      CtlFormula currentFormula = null;
      StateSet sset = null;

      ParserState currentState;
      currentState = ParserState.INIT;
      label = null;
      lineNumber = 0;
      for (line = source_stream.readLine(); line != null;
          line = source_stream.readLine()) {
        lineNumber++;
        //out.println(lineNumber + ": " + line);
        for (i = 0; i < line.length(); i++) {
          if (line.charAt(i) == '#') break;        // comment; ignore rest of line
          if (Character.isSpaceChar(line.charAt(i))) continue;   // whitespace; skip

          switch (currentState) {
            case INIT:
              // expecting "KRIPKE"
              if (!readString("KRIPKE")) {
                syntax_error("keyword KRIPKE");
                System.exit(1);
              }
              if (trace) {
                out.println( "KRIPKE" );
              }
              currentState = ParserState.KRIPKE;
              m = ModelFactory.makeEmptyModel(debugLevel);
              if (null == m) return m;
              break;

            case KRIPKE:
              // expecting "STATES"
              if (!readString("STATES")) {
                syntax_error("keyword STATES");
                System.exit(1);
              }
              if (trace) {
                out.print( "STATES" );
              } 
              currentState = ParserState.STATES;
              break;

            case STATES:
              // expecting an integer
              if (!readInteger()) {
                syntax_error("an integer");
                System.exit(1);
              }
              numStates = integer;
              if (trace) {
                out.print( " " + numStates);
              }
              currentState = ParserState.INTEGER;
              m.setNumStates(numStates);
              break;

            case INTEGER:
              // expecting ARCS
              if (!readString("ARCS")) {
                syntax_error("keyword ARCS");
                System.exit(1);
              }
              if (trace) {
                out.println( "\n" + "ARCS" );
              }
              currentState = ParserState.ARCS;
              break;

            case ARCS:
              // expecting LABELS or a state (s1)
              if (readString("LABELS")) {
                currentState = ParserState.LABELS;
                if (trace) {
                  out.print( "LABELS" + "\n");
                }
              } else if (readStateId()) {
                s1 = stateId;
                currentState = ParserState.ARCS_S1;
                if (trace) {
                  out.print( "  " + s1);
                }
              } else  {
                syntax_error("keyword LABELS or a state (S*)");
                System.exit(1);
              }
              break;

            case ARCS_S1:
              // expecting '->'
              if (!readString("->")) {
                syntax_error("->");
                System.exit(1);
              }
              if (trace) {
                out.print( " ->");
              }
              currentState = ParserState.ARCS_ARROW;
              break;

            case ARCS_ARROW:
              // expecting a state (s2)
              if (!readStateId()) {
                syntax_error("state (S*)");
                System.exit(1);
              }
              s2 = stateId;
              if (trace) {
                out.print( "  " + s2);
              }
              currentState = ParserState.ARCS_S2;
              break;

            case ARCS_S2:
              // expecting ';'
              if (!readString(";")) {
                syntax_error(";");
                System.exit(1);
              }
              if (trace) {
                out.print( ";" + "\n");
              }
              currentState = ParserState.ARCS;
              m.addArc(s1, s2);
              break;

            case LABELS:
              // expecting CTL or a label
              if (readString("CTL")) {
                currentState = ParserState.CTL;
                if (trace) {
                  out.print( "CTL" + "\n");
                }
                if (!m.finish()) {
                  out.println("Error: Kripke structure failed to finish\n");
                  System.exit(1);
                }
              } else if (readLabel()) {
                currentState = ParserState.LABELS_L;
                if (trace) {
                  out.print( "  " + label);
                }
              } else  {
                syntax_error("keyword CTL or a label");
                System.exit(1);
              }
              break;

            case LABELS_L:
              // expecting ':'
              if (!readString(":")) {
                syntax_error(":");
                System.exit(1);
              }
              if (trace) {
                out.print( ":");
              }
              currentState = ParserState.LABELS_COLON;
              sset = CtlFormula.getSet(label);
              CtlFormula.eraseSet(label);
              if (sset == null) {
                sset = m.makeEmptySet();
                CtlFormula.setSet(label, sset);
              }
              break;

            case LABELS_COLON:
              // expecting a state or ';'
              if (readStateId()) {
                s1 = stateId;
                currentState = ParserState.LABELS_S;
                if (trace) {
                  out.print( " " + s1);
                }
                m.addState(s1, sset);
              } else if (readString(";")) {
                currentState = ParserState.LABELS;
                if (trace) {
                  out.print( " ;" + "\n");
                }
                sset = null;
              } else  {
                syntax_error("a state (S*) or ;");
                System.exit(1);
              }
              break;

            case LABELS_S:
              // expecting a ',' or ';'
              if (readString(",")) {
                currentState = ParserState.LABELS_COMMA;
                if (trace) {
                  out.print( " ,");
                }
              } else if (readString(";")) {
                currentState = ParserState.LABELS;
                if (trace) {
                  out.print( " ;" + "\n");
                }
                sset = null;
              } else  {
                syntax_error(", or ;");
                System.exit(1);
              }
              break;

            case LABELS_COMMA:
              // expecting a state
              if (!readStateId()) {
                syntax_error("state (S*)");
                System.exit(1);
              }
              s1 = stateId;
              currentState = ParserState.LABELS_S;
              if (trace) {
                out.print( " " + s1);
              }
              m.addState(s1, sset);
              break;

            case CTL:
              // expecting state, label or '[[' 

              if (traceMax) {
                out.println("case CTL: ");
                out.println("\t i     : " + i);
                out.println("\t line  : " + line);
                out.print("\t i     : ");
                for (int k = 0; k < i; k++) {
                  out.print(" ");
                }
                out.println("^");
              }

              formula = new Vector<String>();

              if (readStateId()) {
                s1 = stateId;
                currentState = ParserState.CTL_S;
                if (trace) {
                  out.print( "  " + s1);
                }
                if (!m.isValidState(s1)) {
                  syntax_error("a valid state");
                  System.exit(1);
                }
                CtlFormulaModels temp = new CtlFormulaModels();
                temp.setModel(m);
                temp.setState(s1);
                currentFormula = temp;
              } else if (readLabel()) {
                currentState = ParserState.CTL_L;
                if (trace) {
                  out.print( "  " + label);
                }
                CtlFormulaLabels temp = new CtlFormulaLabels();
                temp.setModel(m);
                temp.setLabel(label);
                currentFormula = temp;
              } else if (readString("[[")) {
                currentState = ParserState.CTL_SET_OPEN;
                if (trace) {
                  out.print( "  [[");
                }
                CtlFormulaDisplays temp = new CtlFormulaDisplays();
                temp.setModel(m);
                currentFormula = temp;
              } else  {
                syntax_error("a state, a label, or [[");
                System.exit(1);
              }
              break;

            case CTL_S:
              // expecting '|='
              if (!readString("|=")) {
                syntax_error("|=");
                System.exit(1);
              }
              currentState = ParserState.CTL_MODELS;
              if (trace) {
                out.print( " !=");
              }
              break;

            case CTL_MODELS:
              // expecting label
              if (!readLabel()) {
                syntax_error("a label");
                System.exit(1);
              }
              currentState = ParserState.CTL_S_L;
              if (trace) {
                out.print( " " + label);
              }
              if (CtlFormula.getSet(label) == null) {
                i = i + 1 - label.length();
                syntax_error("a previously declared label");
                System.exit(1);
              }
              ((CtlFormulaModels)currentFormula).setLabel(label);
              break;

            case CTL_S_L:
              // expecting ';'
              if (!readString(";")) {
                syntax_error(";");
                System.exit(1);
              }
              currentState = ParserState.CTL;
              if (trace) currentFormula.show();
              ctlFormulas.add(currentFormula);
              currentFormula = null;
              if (trace) {
                out.print( ";" + "\n");
              }
              break;

            case CTL_L:
              // expecting ':='
              if (!readString(":=")) {
                syntax_error(":=");
                System.exit(1);
              }
              currentState = ParserState.CTL_ASSIGN;
              if (trace) {
                out.print( " :=");
              }
              break;

            case CTL_ASSIGN:
              // expecting formula
              if (!readCtlFormula()) {
                syntax_error("CTL formula");
                if (traceFormula) {
                  out.print( "\n");
                  for (int j = 0; j < formula.size(); j++) out.print( " " + formula.elementAt(j));
                  out.print( "\n");
                }
                System.exit(1);
              }
              if (traceFormula) {
                out.print( "Finished reading formula from: " + line + "\n");
                out.print( "Currently at:                  ");
                for (int j = 0; j < i; j++) out.print( " ");
                out.print( "^" + "\n");
              }
              currentState = ParserState.CTL_FORMULA;
              if (traceFormula) {
                for (int j = 0; j < formula.size(); j++) out.print( " " + formula.elementAt(j));
              }
              ((CtlFormulaLabels)currentFormula).setFormula(formula);
              break;

            case CTL_FORMULA:
              // expecting ';'
              if (!readString(";")) {
                syntax_error(";");
                System.exit(1);
              }
              currentState = ParserState.CTL;
              if (trace) currentFormula.show();
              ctlFormulas.add(currentFormula);
              currentFormula = null;
              if (trace) {
                out.print( ";" + "\n");
              }
              break;

            case CTL_SET_OPEN:
              // expecting label
              if (!readLabel()) {
                syntax_error("a label");
                System.exit(1);
              }
              currentState = ParserState.CTL_SET_L;
              if (trace) {
                out.print( " " + label);
              }
              if (CtlFormula.getSet(label) == null) {
                i = i + 1 - label.length();
                syntax_error("a previously declared label");
                System.exit(1);
              }
              ((CtlFormulaDisplays)currentFormula).setLabel(label);
              break;

            case CTL_SET_L:
              // expecting ']]'
              if (!readString("]]")) {
                syntax_error("]]");
                System.exit(1);
              }
              currentState = ParserState.CTL_SET_CLOSE;
              if (trace) {
                out.print( "]]");
              }
              break;

            case CTL_SET_CLOSE:
              // expecting ';'
              if (!readString(";")) {
                syntax_error(";");
                System.exit(1);
              }
              currentState = ParserState.CTL;
              if (trace) currentFormula.show();
              ctlFormulas.add(currentFormula);
              currentFormula = null;
              if (trace) {
                out.print( ";" + "\n");
              }
              break;

            default:
              System.exit(1);
          }

        }
      }

      if (currentState == ParserState.CTL) {
        currentState = ParserState.DONE;
      } else {
        if (trace) {
          out.print( "Error: reached end of file in state #" + currentState + "\n");
        }
        System.exit(1);
      }

      // evaluate the CTL formulas
      for (int j = 0; j < ctlFormulas.size(); j++) {
        if (ctlFormulas.elementAt(j) instanceof CtlFormulaLabels) {
          CtlFormulaLabels f = (CtlFormulaLabels) ctlFormulas.elementAt(j);
          f.getResult();
        }
      }
      for (int j = 0; j < ctlFormulas.size(); j++) {
        ctlFormulas.elementAt(j).show();
      }

      return m;
    }


  boolean readInteger() {
    assert line != null;
    if (i >= line.length()) return false;
    if (!Character.isDigit(line.charAt(i))) return false;

    int start = i;
    for (i++; i < line.length() && Character.isDigit(line.charAt(i)); i++);
    integer = Integer.parseInt(line.substring(start, i));
    i--;
    return true;
  }


  boolean readStateId() {
    if (traceMax) {
      out.println("In readStateId: ");
      out.println("\t line: " + line);
    }
    assert line != null;
    if (line.charAt(i) == 's' || line.charAt(i) == 'S') {
      ++i;
      if (readInteger()) {
        stateId = integer;
        return true;
      }
    }
    return false;
  }


  boolean readString(String match) {
    if (traceMax) {
      out.println("In readString: ");
      out.println("\t match : " + match);
      out.println("\t line  : " + line);
      out.println("\t i     : " + i);
    }
    assert line != null;
    if (match.length() + i > line.length()) return false;
    if (!line.substring(i, i+match.length()).equals(match)) return false;
    i += match.length() - 1;
    return true;
  }


  boolean readLabel() {
    if (traceMax) {
      out.println("In readLabel: ");
      out.println("\t line  : " + line);
      out.println("\t i     : " + i);
    }
    assert line != null;

    if (i >= line.length()) return false;
    if (!Character.isLetterOrDigit(line.charAt(i))) return false;

    int start = i;
    i++;
    while (i < line.length() && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) i++;
    label = line.substring(start, i);
    if (traceMax) out.println("Read label: " + label);
    i--;    // i points to the last character of the string
    return true;
  }


  static int opToVal(String op) {
    return
      op.equals("->")? 0
      : op.equals("|")? 1
      : op.equals("&")? 2
      : (op.equals("AU") || op.equals("EU"))? 3
      : (op.equals("!") || op.equals("AX") || op.equals("AF") || op.equals("AG")
          || op.equals("EX") || op.equals("EF") || op.equals("EG"))? 4
      : -1;
  }


  static boolean isUnaryOperator(String op) {
    return (op.equals("!")
        || op.equals("AX") || op.equals("AF") || op.equals("AG")
        || op.equals("EX") || op.equals("EF") || op.equals("EG"));
  }

  static boolean isBinaryOperator(String op) {
    return (op.equals("&") || op.equals("|") || op.equals("->")
        || op.equals("EU") || op.equals("AU"));
  }


  // op1 is on the operator stack and op2 is the newly discovered operator
  static boolean isHigherPrecedence(String op1, String op2) {
    // Precedence: ascending order
    // ->
    // |
    // &
    // AU, EU
    // !, AX, AF, AG, EX, EF, EG
    // ()
    //
    // Note1: all unary ops have the same precedence,
    //        and from right to left in the CTL formula
    // Note2: the rest of the operations are sorted by precedence,
    //        and from left to right in the CTL formula
    //        (with the earlier operation having a higher precedence).
    return isUnaryOperator(op2)? false: opToVal(op1) >= opToVal(op2);
  }


  boolean readCtlOperator() {
    assert line != null;

    if (i >= line.length()) return false;
    if (line.charAt(i) == '!' || line.charAt(i) == '&' || line.charAt(i) == '|') {
      ctlOperator = line.substring(i, i+1); return true;
    }
    // for the rest of the ctl operators the line must have atleast two characters
    if (i+1 < line.length()) {
      if (line.charAt(i) == 'A' || line.charAt(i) == 'E') {
        if (line.charAt(i+1) == 'X'
            || line.charAt(i+1) == 'F'
            || line.charAt(i+1) == 'G') {
          ctlOperator = line.substring(i, i+2); return true;
        }
        if (Character.isSpaceChar(line.charAt(i+1))
            || line.charAt(i+1) == '(') {
          ctlOperator = line.substring(i, i+1); return true;
        }
      }
      if (line.charAt(i) == 'U'
          && (Character.isSpaceChar(line.charAt(i+1))
            || line.charAt(i+1) == '(')) {
        ctlOperator = line.substring(i, i+1); return true;
      }
      if (line.substring(i, i+2).equals("->")) {
        ctlOperator = "->"; return true;
      }
    }
    return false;
  }


  boolean readCtlFormula() {
    assert line != null;

    if (i >= line.length()) return false;
    /*
     * Parsing a formula:
     *
     * Convert infix to postfix (Based on Knuth's algorithm):
     * . From left to right:
     *        . If "(", push to operator stack.
     *        . If ")", pop all operators till "(", and append to postfix vector.
     *                . If no matching "(" is found, signal syntax error.
     *        . If operator,
     *                . while stack.peek() is a operator of equal or higher precedence,
     *                        . pop the operator and append to postfix vector.
     *                . push operator onto operator stack.
     *        . If operand, append to postfix vector.
     */

    if (traceFormula)
      out.print("Reading new formula" + "\n");

    ArrayDeque<String> operators = new ArrayDeque<String>();
    while (i < line.length() && line.charAt(i) != ';') {
      if (Character.isSpaceChar(line.charAt(i))) { i++; continue; }

      if (line.charAt(i) == '(') {
        operators.push("("); i++; continue;
      }
      if (line.charAt(i) == ')') {
        while (!operators.isEmpty() && !operators.peek().equals("("))  {
          formula.add(operators.pop());
        }
        if (operators.isEmpty()) {
          // Did not find a matching "(" in the operators stack, signal error
          out.print("Syntax error: ) found without a matching ( " + "\n");
          return false;
        }
        operators.pop(); i++; continue;
      }

      // could be a CTL operator
      if (readCtlOperator()) {
        if (traceFormula)
          out.print("\n" + "Found CTL operator: " + ctlOperator + "\n");
        if (ctlOperator.equals("A") || ctlOperator.equals("E")) {
          operators.push(ctlOperator); i++; continue;
        } else if (ctlOperator.equals("U")) {
          // special case: A U, E U
          while (!operators.isEmpty()
              && !operators.peek().equals("(")
              && !operators.peek().equals("A")
              && !operators.peek().equals("E")) {
            formula.add(operators.pop());
          }
          // operators.peek() must be either A or E
          if (operators.isEmpty()
              || (!operators.peek().equals("A") && !operators.peek().equals("E"))) {
            // Did not find a matching A or E in the operators stack, signal error
            out.print("Syntax error: U found without a matching A or E" + "\n");
            return false;
          }
          ctlOperator = operators.pop() + ctlOperator;
          operators.push(ctlOperator);
          i++; continue;
        } else {
          while (!operators.isEmpty()
              && !operators.peek().equals("(")
              && !operators.peek().equals("A")
              && !operators.peek().equals("E")
              && isHigherPrecedence(operators.peek(), ctlOperator)) {
            formula.add(operators.pop());
          }
          operators.push(ctlOperator); i += ctlOperator.length(); continue;
        }
      }

      // must be a label, i.e. operand
      if (!readLabel()) return false;

      if (CtlFormula.getSet(label) == null) {
        // error: unknown label
        out.print("Syntax error: previously undeclared label " + label + "\n");
        i = i+1-label.length();
        return false;
      }

      formula.add(label);
      i++;
    }

    while(!operators.isEmpty()) { formula.add(operators.pop()); }

    if (traceFormula) {
      for (int j = 0; j < formula.size(); j++) {
        out.print(formula.elementAt(j) + " ");
      }
      out.print("\n");
      out.print(i + ": " + line.charAt(i) + "\n");
    }

    i--;    // set the pointer at the last character of the formula

    return true;
  }


  void syntax_error(String str) {
    System.out.println("In syntax_error");
    out.println("Syntax error: expecting " + str
        + " at line: " + lineNumber
        + " and col: " + i);
    out.println(line);
    for (int j = 0; j < i; j++) out.print(" ");
    out.println("^");
  }

  void setDebugLevel(int level) {
    debugLevel = level;
    if (debugLevel >= 100) trace = true;
    if (debugLevel >= 101) traceFormula = true;
    if (debugLevel >= 102) traceMax = true;
    if (trace) CtlFormula.setDebug(true);
  }

  void setPrintStream(PrintStream ps) {
    out = ps;
  }

  Parser() {
    debugLevel = 0;
    trace = false;
    traceFormula = false;
    traceMax = false;
    out = System.out;
    line = new String();
    lineNumber = 0;
    i = 0;
    integer = 0;
    label = new String();
    stateId = 0;
    ctlOperator = new String();
    formula = new Vector<String>();
  }

  int debugLevel;
  boolean trace;
  boolean traceFormula;
  boolean traceMax;
  PrintStream out;

  String line;
  int lineNumber;
  int i;

  int integer;
  String label;
  int stateId;
  String ctlOperator;
  Vector<String> formula;
}

abstract class CtlFormula {

  static final HashMap<String, StateSet> labelToSet =
    new HashMap<String, StateSet>();
  static boolean debug = false;
  static PrintStream out = System.out;

  static StateSet getSet(String label) {
    return labelToSet.get(label);
  }

  static void eraseSet(String label) {
    labelToSet.remove(label);
  }

  static void setSet(String label, StateSet sset) {
    if (getSet(label) == null)
      labelToSet.put(label, sset);
  }

  static void setDebug(boolean flag) {
    debug = flag;
  }

  static void setPrintStream(PrintStream ps) {
    out = ps;
  }

  abstract void show();
};


class CtlFormulaModels extends CtlFormula {
  Model m;
  boolean evaluated;
  boolean result;
  int state;
  String label;

  CtlFormulaModels() {
    m = null;
    evaluated = false;
    result = false;
    state = 0;
    label = null;
  }

  void show() {
    out.println("S" + state + " |= " + label + "? " +
        (getResult()? "Yes": "No"));
  }

  boolean getResult() {
    if (!evaluated) {
      assert m != null;
      assert m.isValidState(state);
      StateSet sset = getSet(label);
      assert sset != null;
      result = m.elementOf(state, sset);
      evaluated = true;
    }
    return result;
  }

  void setModel(Model a_model) {
    m = a_model;
    evaluated = false;
  }

  void setState(int s) {
    state = s;
    evaluated = false;
  }

  void setLabel(String l) {
    label = l;
    evaluated = false;
  }
};

class CtlFormulaDisplays extends CtlFormula {
  Model m = null;
  String label = null;

  void show() {
    out.print("[[ " + label + " ]]:");
    assert m != null;
    StateSet sset = getSet(label);
    assert sset != null;
    m.display(sset);
    out.println();
  }

  void setModel(Model a_model) { m = a_model; }

  void setLabel(String l) {
    label = l;
    assert getSet(label) != null;
  }
};

class CtlFormulaLabels extends CtlFormula {
  Model m = null;
  String label = null;
  Vector<String> formula = null;
  boolean evaluated = false;
  StateSet result = null;

  void show() {
    if (debug) {
      out.print(label + " := ");
      for (int i = 0; i < formula.size(); i++) {
        out.print(" " + formula.elementAt(i));
      }
      out.println(" (postfix notation)");
    }
  }

  StateSet getResult() {
    if (!evaluated) {
      assert m != null;
      assert formula != null;
      // evaluate formula in postfix
      ArrayDeque<StateSet> operands = new ArrayDeque<StateSet>();
      if (debug) System.out.println("Formula.size(): " + formula.size());
      for (int i = 0; i < formula.size(); i++) {
        String token = formula.elementAt(i);
        if (debug) System.out.println("Token: " + token);
        if (Parser.isUnaryOperator(token)) {
          assert !operands.isEmpty();
          StateSet sset = operands.pop();
          // sset can be over-written with the result,
          // see operand case
          if (token.equals("!")) {
            m.NOT(sset, sset);
          } else if (token.equals("EX")) {
            m.EX(sset, sset);
          } else if (token.equals("EF")) {
            m.EF(sset, sset);
          } else if (token.equals("EG")) {
            m.EG(sset, sset);
          } else if (token.equals("AX")) {
            m.AX(sset, sset);
          } else if (token.equals("AF")) {
            m.AF(sset, sset);
          } else {
            assert token.equals("AG");
            m.AG(sset, sset);
          }
          operands.push(sset);
        } else if (Parser.isBinaryOperator(token)) {
          assert !operands.isEmpty();
          StateSet sset2 = operands.pop();
          assert !operands.isEmpty();
          StateSet sset1 = operands.pop();
          // sset1 and sset2 can be over-written with the result,
          // see operand case
          if (token.equals("&")) {
            m.AND(sset1, sset2, sset1);
          } else if (token.equals("|")) {
            m.OR(sset1, sset2, sset1);
          } else if (token.equals("->")) {
            m.IMPLIES(sset1, sset2, sset1);
          } else if (token.equals("EU")) {
            m.EU(sset1, sset2, sset1);
          } else {
            assert token.equals("AU");
            m.AU(sset1, sset2, sset1);
          }
          m.deleteSet(sset2);
          operands.push(sset1);
        } else {
          // operand, or label, put on top of stack
          if (debug) System.out.print("Token is an operand or label: ");
          StateSet sset = getSet(token);
          if (debug) m.display(sset);
          if (debug) System.out.println();
          StateSet rset = m.makeEmptySet();
          m.copy(sset, rset);
          if (debug) System.out.print("Copy of the above token:      ");
          if (debug) m.display(rset);
          if (debug) System.out.println();
          operands.push(rset);
        }
      }

      assert !operands.isEmpty();
      result = operands.pop();
      assert operands.isEmpty();

      // write result to label (over-write if necessary)
      StateSet label_sset = getSet(label);
      if (label_sset != result) {
        eraseSet(label);
        if (label_sset != null) m.deleteSet(label_sset);
        setSet(label, result);
      }
      evaluated = true;
    }
    return result;
  }

  void setModel(Model a_model) {
    if (result != null) {
      assert m != null;
      m.deleteSet(result);
      result = null;
    }
    m = a_model;
    evaluated = false;
  }

  void setLabel(String l) {
    assert m != null;
    if (result != null) { m.deleteSet(result); result = null; }
    label = l;
    evaluated = false;
    if (null == getSet(label)) {
      setSet(label, m.makeEmptySet());
    }
  }

  void setFormula(Vector<String> f) {
    if (result != null) { m.deleteSet(result); result = null; }
    assert f != null;
    assert f.size() > 0;
    formula = f;
    evaluated = false;
  }
};
