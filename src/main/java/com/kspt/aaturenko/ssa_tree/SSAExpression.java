package com.kspt.aaturenko.ssa_tree;

/**
 * Created by Anastasia on 03.11.2018.
 */
public class SSAExpression {
    private SSAExpression left;
    private SSAExpression right;
    private String varName;
    private String numericValue;
    private String sign;

    public enum SSAExpressionType { numeric, identifier, expression };

    public void setLeft(SSAExpression left) {
        this.left = left;
    }

    public void setRight(SSAExpression right) {
        this.right = right;
    }

    public void setNumericValue(String numericValue) {
        this.numericValue = numericValue;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public SSAExpression getLeft() {
        return left;
    }

    public SSAExpression getRight() {
        return right;
    }

    public String getNumericValue() {
        return numericValue;
    }

    public String getSign() {
        return sign;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    @Override
    public String toString() {
        if (numericValue != null) return numericValue;
        if (varName != null) return varName;

        StringBuilder sb = new StringBuilder();
        return sb.append(left.toString())
            .append(" ")
            .append(sign)
            .append(" ")
            .append(right.toString())
            .toString();
    }
}
