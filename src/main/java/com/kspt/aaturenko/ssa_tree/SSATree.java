package com.kspt.aaturenko.ssa_tree;

import io.github.livingdocumentation.dotdiagram.DotGraph;

import java.io.FileWriter;
import java.io.IOException;

import static io.github.livingdocumentation.dotdiagram.DotStyles.ASSOCIATION_EDGE_STYLE;
import static io.github.livingdocumentation.dotdiagram.DotStyles.STUB_NODE_OPTIONS;

/**
 * Created by Anastasia on 03.11.2018.
 */
public class SSATree {
//    private List<SSABlock> blocks = new LinkedList<>();
//
//    public void addBlock(SSABlock block) {
//        this.blocks.add(block);
//    }
//
//    public static void generateGraph(SSATree ssaTree) {
//        final DotGraph graph = new DotGraph("SSA Tree");
//        final DotGraph.Digraph digraph = graph.getDigraph();
//
//        List<SSABlock> blocks = ssaTree.getBlocks();
//        long id = 0;
//
//        for (SSABlock block : blocks) {
//            digraph.addNode(id).setLabel(block.getExpression().toString()).setOptions(STUB_NODE_OPTIONS);
//            if (id != 0) digraph.addAssociation(id, ++id).setOptions(ASSOCIATION_EDGE_STYLE);
//            else id++;
//        }
//
//        writeToFile(digraph);
//    }

    public static void generateGraph(SSABlock block, String fileName) {
        final DotGraph graph = new DotGraph("SSA Tree");
        final DotGraph.Digraph digraph = graph.getDigraph();
        createGraph(block, digraph, 1);
        writeToFile(digraph, fileName);
    }

    private static void createGraph(SSABlock block, DotGraph.Digraph digraph, long id) {
        digraph.addNode(id).setLabel(block.getExpression().toString()).setOptions(STUB_NODE_OPTIONS);
        digraph.addAssociation(id, id + 1).setOptions(ASSOCIATION_EDGE_STYLE);
        for (SSABlock child : block.getChildren()) {
            createGraph(child, digraph, ++id);
            digraph.addAssociation(id-1, id).setOptions(ASSOCIATION_EDGE_STYLE);
        }
    }

    private static void writeToFile(DotGraph.Digraph digraph, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            writer.write(digraph.render());
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
//
//    public List<SSABlock> getBlocks() {
//        return blocks;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder("SSATreeL: \n");
//        for (SSABlock block : blocks) {
//            sb.append(block.toString());
//        }
//        return sb.toString();
//    }
}


//    private void process(Node node) {
//        prettyPrint(node, level);
//        processChildren(node, level);


//    private void processChildren(Node node, int level) {
//        node.getChildNodes().forEach(child -> {
//            this.process(child, level + 1);
//        });
//    }

//        node.findAll(VariableDeclarationExpr.class).forEach(
//                expr -> expr.getVariables().forEach(
//                        var -> {
//                            String name = var.getNameAsString();
//                            variableNumbers.put(name, 1);
//                            var.setName(name + "_" + 1);
//                        }
//                )
//        );
//
//        node.findAll(AssignExpr.class).forEach(
//                expr -> {
//                    expr.findAll(SimpleName.class)
//                            .forEach(name -> {
//                                String string = name.toString();
//                                name.setIdentifier(string + "_" + variableNumbers.get(string));
//                            });
//
//                    NameExpr nameExpr = expr.findFirst(NameExpr.class).get();
//                    String name = nameExpr.getNameAsString();
//                    Integer currentNumber = variableNumbers.get(name);
//                    nameExpr.setName(name + "_" + currentNumber);
//
//                }
//        );

//        node.findAll(SimpleName.class)
//                .forEach(name -> name.setIdentifier(name.toString() + "+ololo"));