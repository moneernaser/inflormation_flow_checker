package com.example.process;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class CompilationData {

    private String filePath;
    private CompilationUnit compilationUnit;

     List<Variable> fields = new ArrayList<>();
     List<Variable> variables = new ArrayList<>();
     List<Variable> parameters = new ArrayList<>();
     List<Variable> all = new ArrayList<>();


    public CompilationData(String filePath) throws FileNotFoundException {
        this.filePath = filePath;
        FileInputStream in = new FileInputStream(filePath);
        this.compilationUnit = JavaParser.parse(in);
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
            variables.add(new Variable(v.getName().getIdentifier(), VarType.LOCAL, v.getRange().get().begin.line));
        }
    }

    private void addField(FieldDeclaration f) {
        if (!(f.getModifiers().contains(Modifier.STATIC) && f.getModifiers().contains(Modifier.FINAL))) {
            fields.add(new Variable(f.getVariable(0).getName().toString(), VarType.FIELD, f.getRange().get().begin.line));
        }
    }

    private void addParameter(Parameter p) {
        parameters.add(new Variable(p.getNameAsString(), VarType.PARAMETER, p.getRange().get().begin.line));
    }
}



