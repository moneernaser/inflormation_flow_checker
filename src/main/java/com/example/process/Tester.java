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

    static List<Variable> fields = new ArrayList<>();
    static List<Variable> variables = new ArrayList<>();
    static List<Variable> parameters = new ArrayList<>();
    static List<Variable> all = new ArrayList<>();

    static Map<String, Set<String>> varaibleDependencies = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {


        FileInputStream in = new FileInputStream("/Users/i311821/dev/openu/security/src/main/java/com/example/process/BankAccount.java");
        CompilationUnit compilationUnit = JavaParser.parse(in);
      //  System.out.println(compilationUnit);
        compilationUnit.getNodesByType(FieldDeclaration.class).stream().
                forEach(f -> addField(f));

//
        compilationUnit.getNodesByType(VariableDeclarator.class).stream().
                forEach(v -> addVariable(v));


        compilationUnit.getNodesByType(Parameter.class).stream().
                forEach(p -> addParameter(p));




        System.out.println("=====fields ====");
        fields.forEach(System.out::println);
        System.out.println("=====variables ====");
        variables.forEach(System.out::println);
        System.out.println("=====parameters ====");
        parameters.forEach(System.out::println);

      //  new MethodVisitor().visit(compilationUnit, null);
//        List<AssignExpr> assignExprs = new ArrayList<>();
//        compilationUnit.accept(new AssignmentVisitor(), assignExprs);
//        List<Statement> stmts = Navigator.findAllNodesOfGivenClass(compilationUnit, Statement.class);
//        buildVariableDependencyGraph(stmts);
      //  stmts.forEach(Tester::printStmt);
     //   printDependencies();
        // fields.forEach(System.out::println);
    }



    private static void addParameter(Parameter p) {
        parameters.add(new Variable(p.getNameAsString(), VarType.PARAMETER, p.getRange().get().begin.line));
    }

    private static void printDependencies() {
        System.out.println("variable dependencies");
        for (Map.Entry<String, Set<String>> entry : varaibleDependencies.entrySet()) {
            Set<String> depList = entry.getValue();
            String l = "[";
            for (String s: depList) {
                l+= s + ", ";
            }
            l += "]";
            System.out.println(entry.getKey() + "\t----->\t" + l);
        }
    }

    private static void buildVariableDependencyGraph(List<Statement> stmts) {
        for (Statement statement : stmts) {
            if (statement instanceof IfStmt) {
                Expression condition = ((IfStmt) statement).getCondition();
                if (condition instanceof BinaryExpr) {
                    Expression left = ((BinaryExpr) condition).getLeft();
                    if (left instanceof NameExpr) {
                        String variableName = ((NameExpr) left).getNameAsString();
                        Set<String> childrenVariables = new TreeSet<>();
                        processIfStatement(((IfStmt) statement).getThenStmt(), variableName, childrenVariables);
                        if (((IfStmt) statement).getElseStmt().isPresent()) {
                            processIfStatement( ((IfStmt) statement).getElseStmt().get(), variableName, childrenVariables);
                        }
                        updateDependencies(variableName, childrenVariables);
                    }
                }
            } else if (statement instanceof ExpressionStmt) {
                Expression expression = ((ExpressionStmt) statement).getExpression();
                if (expression instanceof AssignExpr) {
                    AssignmentStructure assignmentStructure = processAssignment((AssignExpr) expression);
                    if (assignmentStructure.right != null) {
                        updateDependencies(assignmentStructure.left.identifier, toList(assignmentStructure.left.identifier, assignmentStructure.right));
                    }
                } else if(expression instanceof VariableDeclarationExpr) {
                    String var = ((VariableDeclarationExpr) expression).getVariables().get(0).getName().toString();
                    Optional<Expression> initializer = ((VariableDeclarationExpr) expression).getVariables().get(0).getInitializer();
                    if (initializer.isPresent()) {
                        Expression expression1 = initializer.get();
                        if (expression1 instanceof NameExpr) {
                            List<Variable> rvars = getVariables(expression1);
                            updateDependencies(var, toList(var, rvars));
                        } else if (expression1 instanceof BinaryExpr) {
                            List<Variable> rvars = getVariables(expression1);
                            updateDependencies(var, toList(var, rvars));
                        }
                    }
                }

            }
        }
    }

    private static void updateDependencies(String variableName, Set<String> childrenVariables) {
        Set<String> children = varaibleDependencies.get(variableName);
        if (children == null) {
            varaibleDependencies.put(variableName, childrenVariables);
        } else {
            children.addAll(childrenVariables);
        }
    }

    private static void processIfStatement(Statement stmt, String variableName, Set<String> childrenVariables) {
        List<AssignExpr> assignExprs = new ArrayList<>();
        stmt.accept(new AssignmentVisitor(), assignExprs);
        for (AssignExpr assignExpr : assignExprs) {
            AssignmentStructure assignmentStructure = processAssignment(assignExpr);
            if (!assignmentStructure.left.identifier.equals(variableName))
                childrenVariables.add(assignmentStructure.left.identifier);
           if (assignmentStructure.right != null) {
               updateDependencies(assignmentStructure.left.identifier, toList(assignmentStructure.left.identifier, assignmentStructure.right));
           }
        }
    }

    private static Set<String> toList(String excluded, List<Variable> right) {
        Set<String> names = new TreeSet<>();
        right.forEach(x -> {
            if (!x.identifier.equals(excluded))
                 names.add(x.identifier);
        });
        return names;
    }

    private static AssignmentStructure processAssignment(AssignExpr assignExpr) {
        AssignmentStructure assignmentStructure = new AssignmentStructure();
        String leftIdentifier = null;
        if (assignExpr.getTarget() instanceof FieldAccessExpr) {
            leftIdentifier = ((FieldAccessExpr) assignExpr.getTarget()).getName().getIdentifier();
            assignmentStructure.left = new Variable(leftIdentifier, VarType.FIELD);
        }else if (assignExpr.getTarget() instanceof NameExpr) {
            leftIdentifier = ((NameExpr) assignExpr.getTarget()).getNameAsString();
            assignmentStructure.left = new Variable(leftIdentifier, VarType.LOCAL);
        }
        Expression assignExprValue = assignExpr.getValue();
        if (assignExprValue instanceof BinaryExpr) {
            Expression left = ((BinaryExpr) assignExprValue).getLeft();
            Expression right = ((BinaryExpr) assignExprValue).getRight();
            List<Variable> lvariable = getVariables(left);
            List<Variable> rvariable = getVariables(right);
            List<Variable> rightVars = new ArrayList<>();
            if (lvariable != null) {
                rightVars.addAll(lvariable);
            }
            if (rvariable != null) {
                rightVars.addAll(rvariable);
            }
            assignmentStructure.right = rightVars;
        } else if (assignExprValue instanceof MethodCallExpr) {
            NodeList<Expression> arguments = ((MethodCallExpr) assignExprValue).getArguments();
            List<Variable> args = getArgumentsString(assignmentStructure.left.identifier, arguments);
            assignmentStructure.right = args;
        }
        return assignmentStructure;
    }

    private static List<Variable> getArgumentsString(String identifier, NodeList<Expression> arguments) {
        List<Variable> args = new ArrayList<Variable>();
        for (Expression expression : arguments){
            if (expression instanceof NameExpr) {
                if (!identifier.equals(((NameExpr) expression).getNameAsString()))
                    args.add(new Variable(((NameExpr) expression).getNameAsString(), VarType.PARAMETER));
            }
            else if (expression instanceof FieldAccessExpr) {
                if (!identifier.equals(((FieldAccessExpr) expression).getNameAsString()))
                    args.add(new Variable(((FieldAccessExpr) expression).getNameAsString(), VarType.FIELD));
            }
        }
        return args;
    }

    private static List<Variable> getVariables(Expression expression) {
        List<Variable> vars = new ArrayList<>();
        if (expression instanceof FieldAccessExpr) {
            Variable v = new Variable(((FieldAccessExpr) expression).getName().getIdentifier(), VarType.FIELD);
            vars.add(v);
            return vars;
        } else if (expression instanceof NameExpr){
            Variable v = new Variable(((NameExpr) expression).getName().getIdentifier(), VarType.PARAMETER);
            vars.add(v);
            return vars;
        } else if (expression instanceof BinaryExpr) {
            Expression left = ((BinaryExpr) expression).getLeft();
            Expression right = ((BinaryExpr) expression).getRight();
            vars.addAll(getVariables(left));
            vars.addAll(getVariables(right));
            return vars;
        }
        return null;
    }



    private static void printStmt(Statement stmt) {
        System.out.println(stmt.getClass().toString() + ":");
        System.out.println("\t " +stmt);
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            /* here you can access the attributes of the method.
             this method will be called for all methods in this
             CompilationUnit, including inner class methods */
            System.out.println(n.getName());
            super.visit(n, arg);
        }
    }




    private static void addVariable(VariableDeclarator v) {
      //  System.out.println("variable: " + v.getName());
        if (!(v.getParentNode().get() instanceof FieldDeclaration))
        variables.add(new Variable(v.getName().getIdentifier(), VarType.LOCAL, v.getRange().get().begin.line));
    }


    private static void addField(FieldDeclaration f) {
      //  System.out.println("field: " + f.getVariable(0).getName() + ". modifiers: " + f.getModifiers());;
        if (!(f.getModifiers().contains(Modifier.STATIC) && f.getModifiers().contains(Modifier.FINAL))) {
            fields.add(new Variable(f.getVariable(0).getName().toString(), VarType.FIELD, f.getRange().get().begin.line));
        }
    }





    private static class AssignmentVisitor extends VoidVisitorAdapter<List<AssignExpr>> {

        @Override
        public void visit(AssignExpr md, List<AssignExpr> collector) {
            super.visit(md, collector);
            collector.add(md);
        }
    }

    public static class AssignmentStructure {
        private Variable left;
        private List<Variable> right;
    }


}
