package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    TextView equationTextView;
    TextView answerTextView;
    String equationStr,formula,changedFormula,tempFormula;
    Boolean exponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setInitTextViews();
    }

    public void setInitTextViews(){
        equationTextView = findViewById(R.id.equationTV);
        equationTextView.setText("");
        answerTextView = findViewById(R.id.resultsTV);
        answerTextView.setText("");
        equationStr= "";
        formula = "";
        changedFormula = "";
        tempFormula = "";
        exponent = false;
    }

    /* setEquation takes a string from button pressed,
       append it to a string and set equation TextView */
    public void setEquationTextView(String buttonStr) {
        equationTextView = findViewById(R.id.equationTV);
        String equationDisplay = equationTextView.getText().toString();
        // equationDisplay is for display purpose only. (contains string unreadable by script engine)
        equationDisplay = equationDisplay + buttonStr;
        // equationStr is a copy of formula
        equationStr = equationStr + buttonStr;
        Log.i(TAG, "setEquationTextView:equationStr: "+equationStr);
        equationTextView.setText(equationDisplay);
        // formula is for script engine use only. (contains only script engine readable string)
        formula = equationStr;
    }

    //Special Operations onClick (clear(C),negate(+/-), decimal point(.), equal(=))
    public void clearOnClick(View view) {
        equationTextView = findViewById(R.id.equationTV);
        equationTextView.setText("");
        answerTextView = findViewById(R.id.resultsTV);
        answerTextView.setText("");
        equationStr = "";
        formula = "";
        changedFormula = "";
        tempFormula = "";
        exponent = false;
    }

    public void negateOnClick(View view) {
        String lastNum = "";
        tempFormula = equationStr;
        boolean exit = false;
        //Find the last numeric number before negate button was pressed.
        for(int i= equationStr.length()-1; i >=0 ; i--){
            char c = equationStr.charAt(i);
            if(Character.isDigit(c)||c=='.') {
                lastNum = c+lastNum;
            }else {
                  if(!lastNum.equals("")){checkSymbol(i, lastNum);}
                  exit = true;
                break;
            }
        }
        if(!exit && !lastNum.equals("")){ checkSymbol(-1, lastNum); }
        formula = tempFormula;
        Log.i(TAG, "negateOnClick:formula: "+formula);
    }


    private void checkSymbol(int symbolIndex, String lastNum){
      char symbol;
      String workings = "";
      //eg: 9 (negate 9) => -9
      if(symbolIndex == -1) {
          tempFormula = "-"+tempFormula;
          workings = equationStr+" (negate "+lastNum+")";
      }
      if(symbolIndex == 0) {
          symbol = equationStr.charAt(symbolIndex);
          String symbolStr = String.valueOf(symbol);
          //eg: -9 (negate -9) => 9
          if (symbol == '-') {
              tempFormula = tempFormula.replace(symbolStr, "");
          }
          //eg: +9 (negate +9) => 9
          if (symbol == '+') {
              tempFormula = tempFormula.replace(symbolStr, "-");
          }
          workings = equationStr+" (negate "+symbol+lastNum+")";
      }
      if(symbolIndex > 0){
          symbol = equationStr.charAt(symbolIndex);
          char preSymbol = equationStr.charAt(symbolIndex-1);
          String doubleSymbols;
          if(preSymbol == '-'){
              //eg: 9--3 (negate -3) => 9-+3 => 9-3
              if (symbol == '-') {
                  doubleSymbols="--";
                  tempFormula = tempFormula.replace(doubleSymbols, "-");
              }
              //eg: 9-+3 (negate +3) => 9--3 => 9+3
              if (symbol == '+') {
                  doubleSymbols="-+";
                  tempFormula = tempFormula.replace(doubleSymbols, "+");
              }
              workings = equationStr+" (negate "+symbol+lastNum+")";
          }
          if(preSymbol=='+'){
              //eg: 9+-3 (negate -3) => 9++3 => 9+3
              if (symbol == '-') {
                  doubleSymbols="+-";
                  tempFormula = tempFormula.replace(doubleSymbols, "+");
              }
              //eg: 9++3 (negate +3) => 9+-3 => 9-3
              if (symbol == '+') {
                  doubleSymbols="++";
                  tempFormula = tempFormula.replace(doubleSymbols, "-");
              }
              workings = equationStr+" (negate "+symbol+lastNum+")";
          }
          if(Character.isDigit(preSymbol)){
              String symbolStr = String.valueOf(symbol);
              //eg: 9-3 (negate -3) => 9+3
              if (symbol == '-') {
                  tempFormula = tempFormula.replace(symbolStr, "+");
              }
              //eg: 9+3 (negate 3) => 9-3
              if (symbol == '+') {
                  tempFormula = tempFormula.replace(symbolStr, "-");
              }
              workings = equationStr+" (negate "+symbolStr+lastNum+")";
          }
      }
      String fullWorkings = workings+" = "+tempFormula;
      equationTextView = findViewById(R.id.equationTV);
      equationTextView.setText(fullWorkings);
      equationStr = tempFormula;
      formula = equationStr;
    }

    public void dotOnClick(View view) {
        setEquationTextView(".");
    }

    public void equalOnClick(View view) {
        Double answer = null;
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");
        /*When there is at least 1 exponent in the equation,
          call helper function to reformulate formula*/
        if(exponent){
            findExponent();
        }
        try {
            if(!formula.isEmpty()) {
                Log.i(TAG, "equalOnClick:formula: "+formula);
                answer = (double) engine.eval(formula);
            }
        } catch (ScriptException e) {
            e.printStackTrace();
            //a quick pop up message to notify user
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "equalOnClick:ScriptException");
        }
        if(answer!=null) {
            answerTextView = findViewById(R.id.resultsTV);
            answerTextView.setText(String.valueOf(answer));
            equationStr = answerTextView.getText().toString();
            formula = equationStr;
            equationTextView = findViewById(R.id.equationTV);
            equationTextView.setText(equationStr);
        }
    }

    // findExponent finds position of '^' in equationStr and call parseExponent function
    public void findExponent(){
        ArrayList<Integer>  exponentPosition = new ArrayList<>();
        // stores index of each occurrence of '^' in equationStr into exponentPosition<>
        for(int i=0;i<equationStr.length();i++){
            if(equationStr.charAt(i) == '^'){
                exponentPosition.add(i);
            }
        }
        formula = equationStr;
        changedFormula = equationStr;
        //call parseExponent on one '^' per iteration to rewrite
        for(Integer expoIndex : exponentPosition){
            parseExponent(expoIndex);
        }
        formula = changedFormula;
        exponent = false;
        Log.i(TAG, "findExponent:formula: "+formula);
    }

    /* parseExponent takes the index where '^' is, finds "leftNum ^ rightNum" and replace
       the notation with Math.pow(leftNum, rightNum) so rhino script engine can evaluate it.*/
    private void parseExponent(int index) {
        String leftNum="";
        String rightNum="";
        //This loop looks for the number right of ^
        for(int i = index + 1 ; i < equationStr.length(); i++){
            char c = equationStr.charAt(i);
            if(Character.isDigit(c) || c == '.'){
                rightNum = rightNum + c;
            }else{
                break;
            }
        }
        //This loop looks for the number left of ^
        for(int i = index - 1 ; i >= 0; i--){
            char c = equationStr.charAt(i);
            if(Character.isDigit(c) || c == '.'){
                leftNum = c+leftNum;
            }else{
                break;
            }
        }
        //Replace original ^ notation with math.pow notation
        String originalEq = leftNum +"^"+ rightNum;
        String newEq = "Math.pow("+leftNum+","+rightNum+")";
        changedFormula = changedFormula.replace(originalEq, newEq);
        formula = changedFormula;
        Log.i(TAG, "parseExponent:formula: "+formula);
    }

    // Operations onClick (+, -, *, /, ^)
    public void plusOnClick(View view) {
        setEquationTextView("+");
    }

    public void minusOnClick(View view) {
        setEquationTextView("-");
    }

    public void multiplyOnClick(View view) {
        setEquationTextView("*");
    }

    public void divideOnClick(View view) {
        setEquationTextView("/");
    }

    public void exponentOnClick(View view) {
        setEquationTextView("^");
        // marks when there is one or more exponent(s) in the equation
        exponent = true;
    }

    //Numerals onClick (0-9)
    public void zeroOnClick(View view) {
        setEquationTextView("0");
    }

    public void oneOnClick(View view) {
        setEquationTextView("1");
    }

    public void twoOnClick(View view) {
        setEquationTextView("2");
    }

    public void threeOnClick(View view) {
        setEquationTextView("3");
    }

    public void fourOnClick(View view) {
        setEquationTextView("4");
    }

    public void fiveOnClick(View view) {
        setEquationTextView("5");
    }

    public void sixOnClick(View view) {
        setEquationTextView("6");
    }

    public void sevenOnClick(View view) {
        setEquationTextView("7");
    }

    public void eightOnClick(View view) {
        setEquationTextView("8");
    }

    public void nineOnClick(View view) {
        setEquationTextView("9");
    }

}