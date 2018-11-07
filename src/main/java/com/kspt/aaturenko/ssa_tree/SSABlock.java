package com.kspt.aaturenko.ssa_tree;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Anastasia on 03.11.2018.
 */
public class SSABlock {
    private SSASyntaxBlockType type;
    private SSAExpression expression;
    private LinkedList<SSABlock> children;

    public SSABlock(SSASyntaxBlockType type, SSAExpression expression) {
        children = new LinkedList<>();
        this.type = type;
        this.expression = expression;
    }

    public enum SSASyntaxBlockType {
        ASSIGNMENT, CONDITION
    }

    public SSASyntaxBlockType getType() {
        return type;
    }

    public SSAExpression getExpression() {
        return expression;
    }

    public void addChild(SSABlock block){
        children.add(block);
    }

    public LinkedList<SSABlock> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(" ---> " + expression + "\n");
        for (SSABlock child : children) {
            stringBuilder.append(child);
        }
        return stringBuilder.toString();
    }
}
