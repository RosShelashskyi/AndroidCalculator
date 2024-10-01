package edu.tcu.rossshelashskyi.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Deque

class MainActivity : AppCompatActivity() {

    private var output: String = ""
    private var afterOperator = false
    private var firstDigitZero = false
    private var hasDot = false
    private var valueStartIndex = 0
    private val inputList: MutableList<String> = mutableListOf()
    private var divideZero = false
    private val opStk: ArrayDeque<String> = ArrayDeque()
    private val valStk: ArrayDeque<BigDecimal> = ArrayDeque()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val resultTv = findViewById<TextView>(R.id.result_tv)

        findViewById<Button>(R.id.zero_btn).setOnClickListener{onDigit(resultTv, "0")}
        findViewById<Button>(R.id.one_btn).setOnClickListener{onDigit(resultTv, "1")}
        findViewById<Button>(R.id.two_btn).setOnClickListener{onDigit(resultTv, "2")}
        findViewById<Button>(R.id.three_btn).setOnClickListener{onDigit(resultTv, "3")}
        findViewById<Button>(R.id.four_btn).setOnClickListener{onDigit(resultTv, "4")}
        findViewById<Button>(R.id.five_btn).setOnClickListener{onDigit(resultTv, "5")}
        findViewById<Button>(R.id.six_btn).setOnClickListener{onDigit(resultTv, "6")}
        findViewById<Button>(R.id.seven_btn).setOnClickListener{onDigit(resultTv, "7")}
        findViewById<Button>(R.id.eight_btn).setOnClickListener{onDigit(resultTv, "8")}
        findViewById<Button>(R.id.nine_btn).setOnClickListener{onDigit(resultTv, "9")}

        findViewById<Button>(R.id.add_btn).setOnClickListener{onOperator(resultTv, "+")}
        findViewById<Button>(R.id.subtract_btn).setOnClickListener{onOperator(resultTv, "-")}
        findViewById<Button>(R.id.multiply_btn).setOnClickListener{onOperator(resultTv, "*")}
        findViewById<Button>(R.id.divide_btn).setOnClickListener{onOperator(resultTv, "/")}

        findViewById<Button>(R.id.equal_btn).setOnClickListener{onEqual(resultTv)}
        findViewById<Button>(R.id.dot_btn).setOnClickListener{onDot(resultTv)}
        findViewById<Button>(R.id.clear_btn).setOnClickListener{onClear(resultTv)}

    }
    private fun onDigit(resultTv: TextView, digit: String){
        if(digit == "0"){
            if(firstDigitZero || divideZero) return
            if(output.isEmpty() || afterOperator){
                firstDigitZero = true
            }
            output += digit
            resultTv.text = output
            afterOperator = false
        }else{
            if(firstDigitZero){
                output = output.dropLast(1)
            }
            firstDigitZero = false
            output += digit
            resultTv.text = output
            afterOperator = false
        }
    }

    private fun onOperator(resultTv: TextView, operator: String){
        if(afterOperator || divideZero) return
        inputList.add(output.substring(valueStartIndex))
        inputList.add(operator)
        output += operator
        resultTv.text = output
        afterOperator = true
        firstDigitZero = false
        hasDot = false;
        valueStartIndex = output.length;
    }

    private fun onDot(resultTv: TextView){
        if(hasDot || divideZero) return
        if(afterOperator){
            output += "0"
            afterOperator = false
        }
        output += "."
        resultTv.text = output
        hasDot = true
        firstDigitZero = false
    }

    private fun onEqual(resultTv: TextView){
        if(afterOperator) return
        inputList.add(output.substring(valueStartIndex))
        output = evalExp().setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toString()
        if(divideZero) {
            output = "Error!\nTap CLR to continue."
        }
        resultTv.text = output

    }

    private fun onClear(resultTv: TextView){
        divideZero = false
        output = "0"
        resultTv.text = output
        afterOperator = false
        firstDigitZero = true
        hasDot = false
        inputList.clear()
        valueStartIndex = 0
        valStk.clear()
        opStk.clear()
    }

    private fun precedence(op: String): Int{
        if(op == "$") return 0
        if(op == "+" || op == "-") return 1
        if(op == "*" || op == "/") return 2
        return -1
    }

    private fun doOp(){
        val x: BigDecimal = valStk.removeLast()
        val y: BigDecimal = valStk.removeLast()
        val op = opStk.removeLast()
        if(op == "+"){
            valStk.addLast(y.add(x))
        }else if (op == "-"){
            valStk.addLast(y.subtract(x))
        }else if (op == "*"){
            valStk.addLast(y.multiply(x))
        }else{
            if (x == BigDecimal.ZERO){
                divideZero = true
                return
            }
            valStk.addLast(y.divide(x, 8, RoundingMode.HALF_UP))
        }
    }

    private fun repeatOps(refOp: String){
        while(valStk.size > 1 && precedence(opStk.last()) >= precedence(refOp)){
            doOp()
            if(divideZero) return
        }
    }

    private fun evalExp(): BigDecimal{
        for(tok in inputList){
            println(tok);
        }
        for(tok in inputList){
            if(tok.toBigDecimalOrNull() != null){
                valStk.addLast(tok.toBigDecimal())
            }else{
                repeatOps(tok)
                if(divideZero) return BigDecimal.ZERO
                opStk.addLast(tok)
            }
        }
        repeatOps("$")
        if(divideZero) return BigDecimal.ZERO
        return valStk.last()
    }
}