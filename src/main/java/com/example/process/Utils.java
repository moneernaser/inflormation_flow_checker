package com.example.process;


import com.github.javaparser.ast.expr.Expression;

public class Utils {

    public static int getLine(Expression expression) {
        return expression.getRange().get().begin.line;
    }
}
