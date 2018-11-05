package com.kspt.aaturenko;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.kspt.aaturenko.diagram.DiagramUtil;
import com.kspt.aaturenko.parser.MethodSSAParser;
import com.kspt.aaturenko.ssa_tree.SSABlock;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Anastasia on 28.10.2018.
 */
public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        FileInputStream file = new FileInputStream("C:\\Users\\Anastasia\\IdeaProjects\\com.kspt.aaturenko\\src\\main\\java\\com\\kspt\\aaturenko\\Test.java");
        CompilationUnit cu = JavaParser.parse(file);
        Node rootNode = cu.findRootNode();
        ClassOrInterfaceDeclaration classDeclaration = rootNode.findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new IllegalArgumentException("Class declaration was not found"));

        MethodSSAParser parser = new MethodSSAParser();
        classDeclaration.findAll(MethodDeclaration.class).forEach(
            method -> {
                SSABlock block = parser.processMethod(method);
                System.out.println(block);
                DiagramUtil.generateGraph(block, method.getNameAsString());
//                parser.print(method, 0);
            }
        );
    }
}
