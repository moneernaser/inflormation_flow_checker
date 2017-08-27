package com.example.process;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparser.Navigator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class CompilationData {

     private CompilationUnit compilationUnit;
     private Map<Variable, Set<Variable>> variableDependencies = new HashMap<>();


     private List<Variable> fields = new ArrayList<>();
     private List<Variable> variables = new ArrayList<>();
     private List<Variable> parameters = new ArrayList<>();
     private List<Variable> all = new ArrayList<>();


    public CompilationData(String filePath) throws FileNotFoundException {
        FileInputStream in = new FileInputStream(filePath);
        this.compilationUnit = JavaParser.parse(in);
    }

    public CompilationData(InputStream inputStream) throws FileNotFoundException {
        this.compilationUnit = JavaParser.parse(inputStream);
    }

    public  List<Variable> getFields() {
        if (fields.isEmpty()) {
            compilationUnit.getNodesByType(FieldDeclaration.class).stream().
                    forEach(f -> addField(f));
        }
        return fields;
    }

    public  List<Variable> getParameters() {
        if (parameters.isEmpty()){
            compilationUnit.getNodesByType(Parameter.class).stream().
                    forEach(p -> addParameter(p));
        }
        return parameters;
    }

    public  List<Variable> getVariables() {
        if (variables.isEmpty()) {
            compilationUnit.getNodesByType(VariableDeclarator.class).stream().
                    forEach(v -> addVariable(v));
        }
        return variables;
    }

    public  List<Variable> getAll() {
        if (all.isEmpty()) {
            all.addAll(getFields());
            all.addAll(getParameters());
            all.addAll(getVariables());
        }
        return all;
    }


    private void addVariable(VariableDeclarator v) {
        if (!(v.getParentNode().get() instanceof FieldDeclaration)) {
            int lineDefined = v.getRange().get().begin.line;
            int line = v.getRange().get().begin.line;
            variables.add(new Variable(v.getName().getIdentifier(), VarType.LOCAL,lineDefined, line));
        }
    }

    private void addField(FieldDeclaration f) {
        if (!(f.getModifiers().contains(Modifier.STATIC) && f.getModifiers().contains(Modifier.FINAL))) {
            int lineDefined = f.getRange().get().begin.line;
            int line = f.getRange().get().begin.line;
            fields.add(new Variable(f.getVariable(0).getName().toString(), VarType.FIELD, lineDefined, line));
        }
    }

    private void addParameter(Parameter p) {
        int lineDefined = p.getRange().get().begin.line;
        int line = p.getRange().get().begin.line;
        parameters.add(new Variable(p.getNameAsString(), VarType.PARAMETER, lineDefined, line));
    }


    public Map<Variable, Set<Variable>> buildGraphDependencies() {
        getAll();
        List<Statement> stmts = Navigator.findAllNodesOfGivenClass(compilationUnit, Statement.class);
        buildVariableDependencyGraph(stmts);
        return variableDependencies;
    }

    private void buildVariableDependencyGraph(List<Statement> stmts) {
        variableDependencies.clear();
        for (Statement statement : stmts) {
            if (statement instanceof IfStmt) {
                Expression condition = ((IfStmt) statement).getCondition();
                if (condition instanceof BinaryExpr) {
                    Expression left = ((BinaryExpr) condition).getLeft();
                    if (left instanceof NameExpr) {
                        Variable var1 = getVariables(left).get(0);
                        Set<Variable> childrenVariables = new LinkedHashSet<>();
                        processIfStatement(((IfStmt) statement).getThenStmt(), var1, childrenVariables);
                        if (((IfStmt) statement).getElseStmt().isPresent()) {
                            processIfStatement( ((IfStmt) statement).getElseStmt().get(), var1, childrenVariables);
                        }
                        updateDependencies(var1, childrenVariables);
                    }
                }
            } else if (statement instanceof ExpressionStmt) {
                Expression expression = ((ExpressionStmt) statement).getExpression();
                if (expression instanceof AssignExpr) {
                    AssignmentStructure assignmentStructure = processAssignment((AssignExpr) expression);
                    if (assignmentStructure.right != null) {
                        updateDependencies(assignmentStructure.left, toList(assignmentStructure.left, assignmentStructure.right));
                    }
                } else if(expression instanceof VariableDeclarationExpr) {
                    String var = ((VariableDeclarationExpr) expression).getVariables().get(0).getName().toString();
                    Variable v = createVariable(var, getLine(((VariableDeclarationExpr) expression).getVariables().get(0)));
                    Optional<Expression> initializer = ((VariableDeclarationExpr) expression).getVariables().get(0).getInitializer();
                    if (initializer.isPresent()) {
                        Expression expression1 = initializer.get();
                        List<Variable> rvars = getVariables(expression1);
                        if (rvars != null) {
                            updateDependencies(v, toList(v, rvars));
                        }
                    }
                }

            }
        }
    }

    private int getLine(VariableDeclarator variableDeclarator) {
        return variableDeclarator.getRange().get().begin.line;
    }


    private void processIfStatement(Statement stmt, Variable variable, Set<Variable> childrenVariables) {
        List<AssignExpr> assignExprs = new ArrayList<>();
        stmt.accept(new AssignmentVisitor(), assignExprs);
        for (AssignExpr assignExpr : assignExprs) {
            AssignmentStructure assignmentStructure = processAssignment(assignExpr);
            if (!assignmentStructure.left.equals(variable))
                childrenVariables.add(assignmentStructure.left);
            if (assignmentStructure.right != null) {
                updateDependencies(assignmentStructure.left, toList(assignmentStructure.left, assignmentStructure.right));
            }
        }
    }



    private void updateDependencies(Variable variableName, Set<Variable> childrenVariables) {
        if (childrenVariables == null || childrenVariables.isEmpty()) {
            return;
        }
        Set<Variable> children = variableDependencies.get(variableName);
        if (children == null) {
            variableDependencies.put(variableName, childrenVariables);
        } else {
            children.addAll(childrenVariables);
        }
    }


    private List<Variable> getVariables(Expression expression) {
        List<Variable> vars = new ArrayList<>();
        if (expression instanceof FieldAccessExpr) {
            Variable v = createVariable(((FieldAccessExpr) expression).getName().getIdentifier(), VarType.FIELD, getLine(expression));
            vars.add(v);
            return vars;
        } else if (expression instanceof NameExpr){
            Variable v = createVariable(((NameExpr) expression).getName().getIdentifier(), getLine(expression));
            vars.add(v);
            return vars;
        } else if (expression instanceof BinaryExpr) {
            Expression left = ((BinaryExpr) expression).getLeft();
            Expression right = ((BinaryExpr) expression).getRight();
            List<Variable> lvariables = getVariables(left);
            List<Variable> rvariables = getVariables(right);
            if (rvariables!=null) vars.addAll(rvariables);
            if (lvariables!=null) vars.addAll(lvariables);
            return vars;
        } else if (expression instanceof MethodCallExpr) {
        NodeList<Expression> arguments = ((MethodCallExpr) expression).getArguments();
        List<Variable> variables = new ArrayList<>();
        for (Expression expression1 : arguments) {
            List<Variable> variables1 = getVariables(expression1);
            if (variables1 != null)
                variables.addAll(variables1);
        }
        return variables;
    }
        return null;
    }

    private class AssignmentVisitor extends VoidVisitorAdapter<List<AssignExpr>> {

        @Override
        public void visit(AssignExpr md, List<AssignExpr> collector) {
            super.visit(md, collector);
            collector.add(md);
        }
    }

    private AssignmentStructure processAssignment(AssignExpr assignExpr) {
        AssignmentStructure assignmentStructure = new AssignmentStructure();
        String leftIdentifier = null;
        if (assignExpr.getTarget() instanceof FieldAccessExpr) {
            leftIdentifier = ((FieldAccessExpr) assignExpr.getTarget()).getName().getIdentifier();
            assignmentStructure.left = createVariable(leftIdentifier, VarType.FIELD, getLine(assignExpr.getTarget()));
        }else if (assignExpr.getTarget() instanceof NameExpr) {
            leftIdentifier = ((NameExpr) assignExpr.getTarget()).getNameAsString();
            assignmentStructure.left = createVariable(leftIdentifier, getLine(assignExpr.getTarget()));;
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

    private Variable createVariable(String identifier, VarType type, int line) {
        int lineDefined = getLineDefined(identifier, type, line);
        return new Variable(identifier, type, lineDefined, line);
    }

    private Variable createVariable(String identifier, int line) {
        VarType type = getType(identifier, line);
        int lineDefined = getLineDefined(identifier, type, line);
        return new Variable(identifier, type, lineDefined, line);
    }

    private VarType getType(String identifier, int line) {
        List<Variable> vars = getAll();
        List<Variable> relevant = new ArrayList<>();
        for (Variable v : vars) {
            if (v.getIdentifier().equals(identifier) && v.getLineDefined() <= line) {
                relevant.add(v);
            }
        }
        Variable selected = null;
        for (Variable v : relevant) {
            if (selected == null) {
                selected = v;
            } else {
                if (selected.getLineDefined() < v.getLineDefined()) {
                    selected = v;
                }
            }
        }
        return selected.getVarType();
    }

    private int getLineDefined(String identifier, VarType type, int line) {
        List<Variable> vars = getAll();
        Variable selected = null;
        for (Variable v : vars) {
            if (v.getIdentifier().equals(identifier) && v.getVarType() == type && v.getLineDefined() <= line) {
                if (selected == null) {
                    selected = v;
                } else {
                    if (selected.getLineDefined() < v.getLineDefined()) {
                        selected = v;
                    }
                }
            }
        }
        return selected.getLineDefined();
    }

    private List<Variable> getArgumentsString(String identifier, NodeList<Expression> arguments) {
        List<Variable> args = new ArrayList<Variable>();
        for (Expression expression : arguments){
            if (expression instanceof NameExpr) {
                if (!identifier.equals(((NameExpr) expression).getNameAsString())) {
                    Variable v = createVariable(((NameExpr) expression).getNameAsString(), getLine(expression));
                    args.add(v);
                }
            }
            else if (expression instanceof FieldAccessExpr) {
                if (!identifier.equals(((FieldAccessExpr) expression).getNameAsString())) {
                    Variable v = createVariable(((FieldAccessExpr) expression).getNameAsString(), VarType.FIELD, getLine(expression));
                    args.add(v);
                }
            }
        }
        return args;
    }
    public class AssignmentStructure {
        private Variable left;
        private List<Variable> right;
    }


    private Set<Variable> toList(Variable excluded, List<Variable> right) {
        Set<Variable> names =new LinkedHashSet<>();
        right.forEach(x -> {
            if (!x.equals(excluded))
                names.add(x);
        });
        return names;
    }

    private int getLine(Expression expression) {
        return expression.getRange().get().begin.line;
    }
    public static void main(String[] args) throws FileNotFoundException {
        CompilationData compilationData = new CompilationData("/Users/i311821/dev/openu/security/src/main/java/com/example/process/BankAccount.java");
        Map<Variable, Set<Variable>> variableSetMap = compilationData.buildGraphDependencies();
        printDependencies(variableSetMap);
    }

    private static void printDependencies(Map<Variable, Set<Variable>> variableSetMap) {
        System.out.println("variable dependencies");
        for (Map.Entry<Variable, Set<Variable>> entry : variableSetMap.entrySet()) {
            Set<Variable> depList = entry.getValue();
            String l = "[";
            for (Variable s: depList) {
                l+= s.getIdentifier() + ", ";
            }
            l += "]";
            System.out.println(entry.getKey().getIdentifier() + "\t----->\t" + l);
        }
    }
}



