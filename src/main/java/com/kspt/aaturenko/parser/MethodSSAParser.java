package com.kspt.aaturenko.parser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.kspt.aaturenko.ssa_tree.SSABlock;
import com.kspt.aaturenko.ssa_tree.SSAExpression;

import java.util.*;

import static com.kspt.aaturenko.ssa_tree.SSABlock.SSASyntaxBlockType.ASSIGNMENT;
import static com.kspt.aaturenko.ssa_tree.SSABlock.SSASyntaxBlockType.CONDITION;

public class MethodSSAParser {
    private VersionResolver versionResolver;

    public SSABlock processMethod(Node node, VersionResolver versionResolver) {
        BlockStmt block = node.findFirst(BlockStmt.class)
                .orElseThrow(() -> new IllegalArgumentException("Abstract functions are not supported"));
        SSABlock ssaBlock = processBlock(block);
        versionResolver.resolveVersions(ssaBlock);
        return ssaBlock;
    }


    private SSABlock process(Node node) {
        if (node instanceof ExpressionStmt) return processExpression((ExpressionStmt) node);
        if (node instanceof BlockStmt) return processBlock((BlockStmt) node);
        if (node instanceof Statement) return processStatement((Statement) node);
        else throw new IllegalArgumentException("Type of Statement was nor recognized: " + node.getMetaModel());
    }

    private SSABlock processBlock(BlockStmt block) {
        SSABlock entry = null;
        SSABlock parent = null;

        List<Node> childNodes = block.getChildNodes();
        for (Node node : childNodes) {
            SSABlock processedSSABlock = process(node);

            if (parent != null) parent.addChild(processedSSABlock);
            else entry = processedSSABlock;

            if (processedSSABlock.getType() != CONDITION) parent = processedSSABlock;
        }

        return entry;
    }

    private SSABlock processExpression(ExpressionStmt node) {
        Expression expression = node.getExpression();
        if (expression.isVariableDeclarationExpr())
            return processVariableDeclarationExpr((VariableDeclarationExpr) expression);
        if (expression.isAssignExpr())
            return processAssignExpr((AssignExpr) expression);
        else
            throw new NoSuchElementException("Node is not a VariableDeclarationExpr or an AssignExpr: " + node.getMetaModel());
    }

    private SSABlock processStatement(Statement statement) {
        if (statement.isIfStmt()) return processIfStatement((IfStmt) statement);
        else throw new IllegalArgumentException("Type of Statement was nor recognized: " + statement.getMetaModel());
    }

    private SSABlock processIfStatement(IfStmt ifStmt) {
        BinaryExpr condition = ifStmt.getCondition().asBinaryExpr();
        SSAExpression conditionExpression = new SSAExpression();
        conditionExpression.setLeft(parseExpression(condition.getLeft()));
        conditionExpression.setRight(parseExpression(condition.getRight()));
        conditionExpression.setSign(condition.getOperator().asString());
        SSABlock conditionBlock = new SSABlock(CONDITION, conditionExpression);

        SSABlock thenBlock = process(ifStmt.getThenStmt());
        conditionBlock.addChild(thenBlock);

        Optional<SSABlock> elseBlock = ifStmt.getElseStmt().map(this::process);
        elseBlock.ifPresent(conditionBlock::addChild);

        return conditionBlock;
    }

    private SSABlock processVariableDeclarationExpr(VariableDeclarationExpr variableDeclarationExpr) {
        VariableDeclarator declarator = variableDeclarationExpr
                .findFirst(VariableDeclarator.class)
                .orElseThrow(NoSuchElementException::new);

        String varName = declarator.findFirst(SimpleName.class).orElseThrow(NoSuchElementException::new).asString();
        SSAExpression left = new SSAExpression();
        left.setVarName(varName);

        SSAExpression right = parseExpression(declarator);
        if (right == null) throw new NoSuchElementException("Not found structure: " + declarator.toString());

        SSAExpression ssaExpression = new SSAExpression();
        ssaExpression.setLeft(left);
        ssaExpression.setRight(right);
        ssaExpression.setSign("=");

        return new SSABlock(ASSIGNMENT, ssaExpression);
    }

    private SSAExpression parseExpression(Node declarator) {
        SSAExpression ssaExpression = null;

        Optional<SSAExpression> binary = declarator.findFirst(BinaryExpr.class).map(this::parseBinaryExpression);
        if (binary.isPresent()) ssaExpression = binary.get();

        Optional<SSAExpression> literal = declarator.findFirst(LiteralExpr.class).map(this::parseLiteralExpression);
        if (ssaExpression == null && literal.isPresent()) ssaExpression = literal.get();

        Optional<SSAExpression> name = declarator.findFirst(NameExpr.class).map(this::parseNameExpression);
        if (ssaExpression == null && name.isPresent()) ssaExpression = name.get();

        return ssaExpression;
    }

    private SSAExpression parseNameExpression(NameExpr nameExpr) {
        SSAExpression ssaExpression = new SSAExpression();
        String value = nameExpr.getName().asString();
        ssaExpression.setVarName(value);
        return ssaExpression;
    }

    private SSAExpression parseLiteralExpression(LiteralExpr literalExpr) {
        SSAExpression ssaExpression = new SSAExpression();
        ssaExpression.setNumericValue(literalExpr.toString());
        return ssaExpression;
    }

    private SSAExpression parseBinaryExpression(BinaryExpr binaryExpr) {
        SSAExpression ssaExpression = new SSAExpression();
        ssaExpression.setLeft(parseExpression(binaryExpr.getLeft()));
        ssaExpression.setRight(parseExpression(binaryExpr.getRight()));
        ssaExpression.setSign(binaryExpr.getOperator().asString());
        return ssaExpression;
    }

    private SSABlock processAssignExpr(AssignExpr assignExpr) {
        SSAExpression ssaExpression = new SSAExpression();

        String sign = assignExpr.getOperator().asString();
        ssaExpression.setSign(sign);

        Expression target = assignExpr.getTarget();
        if (target.isNameExpr()) {
            NameExpr nameExpr = (NameExpr) target;
            SSAExpression left = new SSAExpression();
            left.setVarName(nameExpr.getName().asString());
            ssaExpression.setLeft(left);
        } else {
            throw new NoSuchElementException("Target from AssignExpr is not NameExpr.");
        }

        SSAExpression right = parseExpression(assignExpr.getValue());
        ssaExpression.setRight(right);

        return new SSABlock(ASSIGNMENT, ssaExpression);
    }

    private void prettyPrint(Node node, int level) {
        String tab = "";
        for (int i = 0; i <= level; i++) {
            tab += " ";
        }
        System.out.println(tab + node.getMetaModel() + ": " + node);
    }

    public void print(Node node, int level) {
        prettyPrint(node, level);
        node.getChildNodes().forEach(c -> print(c, level + 1));
    }
}