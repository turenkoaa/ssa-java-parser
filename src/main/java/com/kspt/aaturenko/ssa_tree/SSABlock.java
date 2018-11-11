package com.kspt.aaturenko.ssa_tree;

import com.kspt.aaturenko.IdGenerator;

import java.util.LinkedList;

/**
 * Created by Anastasia on 03.11.2018.
 */
public class SSABlock {
    private SSASyntaxBlockType type;
    private SSAExpression expression;
    private LinkedList<SSABlock> children;
    private long id;

    public SSABlock(SSASyntaxBlockType type, SSAExpression expression) {
        children = new LinkedList<>();
        this.type = type;
        this.expression = expression;
        this.id = IdGenerator.getNextId();
    }

    public enum SSASyntaxBlockType {
        ASSIGNMENT, CONDITION, PHI
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

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(id + " ---> " + expression + "\n");
        for (SSABlock child : children) {
            stringBuilder.append(child);
        }
        return stringBuilder.toString();
    }
}
