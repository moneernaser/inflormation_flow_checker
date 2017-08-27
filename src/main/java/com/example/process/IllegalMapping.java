package com.example.process;


import java.util.List;
import java.util.Set;

public class IllegalMapping {
    private Variable key;
    private Set<Variable> values;

    public Set<Variable> getValues() {
        return values;
    }

    public void setKey(Variable key) {
        this.key = key;
    }

    public void setValues(Set<Variable> values) {
        this.values = values;
    }

    public Variable getKey() {
        return key;
    }
    public IllegalMapping(Variable key, Set<Variable> values){
        this.key = key;
        this.values = values;
    }
}
