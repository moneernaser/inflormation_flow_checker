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

        public Variable setLine(int line) {
            this.line = line;
            return this;
        }

        private VarType varType;
        private int line;
        private String securityLevel;
        private int lineDefined;


    public int getLineDefined() {
        return lineDefined;
    }

    public void setLineDefined(int lineDefined) {
        this.lineDefined = lineDefined;
    }

    public Variable(){

        }
    public Variable(String identifier, VarType varType, int lineDefined, int line) {
        this.identifier = identifier;
        this.varType = varType;
        this.lineDefined = lineDefined;
        this.line = line;
    }

    public Variable(String identifier, VarType varType, int lineDefined, String securityLevel) {
        this.identifier = identifier;
        this.varType = varType;
        this.lineDefined = lineDefined;
        this.securityLevel = securityLevel;
    }

    public Variable(String identifier, String varType, int lineDefined, String securityLevel) {
        this.identifier = identifier;
        this.varType = VarType.valueOf(varType);
        this.lineDefined = lineDefined;
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
            return "{identifier:" + identifier + ", varType: " + varType  + ", lineDefined: " + lineDefined +"}";
        }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            return ((Variable) obj).getIdentifier().equals(this.getIdentifier())
                    && ((Variable) obj).lineDefined == this.lineDefined
                    && ((Variable) obj).getVarType() == this.varType;
        }
        return false;
    }



    @Override
    public int hashCode() {
        int result = identifier.hashCode();
        result = 31 * result + varType.hashCode();
        result = 31 * result + lineDefined;
        return result;
    }
}
