package com.example.process;



public class Variable {

        String identifier;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public VarType getVarType() {
            return varType;
        }

        public void setVarType(VarType varType) {
            this.varType = varType;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        private VarType varType;
        private int line;
        private String securityLevel;


        public Variable(String identifier, VarType varType) {
            this.identifier = identifier;
            this.varType = varType;
        }

        public Variable(){

        }
    public Variable(String identifier, VarType varType, int line) {
        this.identifier = identifier;
        this.varType = varType;
        this.line = line;
        this.securityLevel = "CONFIDENTIAL";
    }

    public Variable(String identifier, VarType varType, int line, String securityLevel) {
        this.identifier = identifier;
        this.varType = varType;
        this.line = line;
        this.securityLevel = securityLevel;
    }

    public Variable(String identifier, String varType, int line, String securityLevel) {
        this.identifier = identifier;
        this.varType = VarType.valueOf(varType);
        this.line = line;
        this.securityLevel = securityLevel;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    @Override
        public String toString() {
            return "{identifier:" + identifier + ", varType: " + varType  + ", line: " + line +"}";
        }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            return ((Variable) obj).getIdentifier().equals(this.getIdentifier()) && ((Variable) obj).line == this.line;
        }
        return false;
    }
}
