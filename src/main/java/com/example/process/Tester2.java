package com.example.process;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by i311821 on 16/08/2017.
 */
public class Tester2 {


    public static void main(String[] args) throws FileNotFoundException {
        CompilationData compilationData = new CompilationData("/Users/i311821/dev/openu/security/src/main/java/com/example/process/BankAccount.java");
        List<Variable> variableList = compilationData.getAll();
        variableList.forEach(System.out::println);
    }
}
