package com.example.process;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparser.Navigator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class Tester {



    public static void main(String[] args) throws FileNotFoundException {


        FileInputStream in = new FileInputStream("/Users/i311821/dev/openu/security/src/main/java/com/example/process/BankAccount.java");
      //  System.out.println(compilationUnit);
        CompilationData compilationData = new CompilationData(in);




      //  new MethodVisitor().visit(compilationUnit, null);
//        List<AssignExpr> assignExprs = new ArrayList<>();
//        compilationUnit.accept(new AssignmentVisitor(), assignExprs);
        Map<Variable, Set<Variable>> graphDependencies = compilationData.buildGraphDependencies();

      //  stmts.forEach(Tester::printStmt);
     //   printDependencies();
        // fields.forEach(System.out::println);
    }


}
