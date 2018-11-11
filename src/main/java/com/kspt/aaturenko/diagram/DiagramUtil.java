package com.kspt.aaturenko.diagram;

import com.kspt.aaturenko.parser.VersionResolver;
import com.kspt.aaturenko.ssa_tree.SSABlock;
import com.kspt.aaturenko.ssa_tree.SSAExpression;
import io.github.livingdocumentation.dotdiagram.DotGraph;
import io.github.livingdocumentation.dotdiagram.DotGraph.Digraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kspt.aaturenko.ssa_tree.SSABlock.SSASyntaxBlockType.CONDITION;
import static com.kspt.aaturenko.ssa_tree.SSABlock.SSASyntaxBlockType.PHI;
import static io.github.livingdocumentation.dotdiagram.DotStyles.CLASS_NODE_OPTIONS;
import static io.github.livingdocumentation.dotdiagram.DotStyles.STUB_NODE_OPTIONS;

/**
 * Created by Anastasia on 05.11.2018.
 */
public class DiagramUtil {

    public static void generateGraph(SSABlock block, VersionResolver versionResolver, String fileName) {
        final DotGraph graph = new DotGraph("SSA Tree");
        final Digraph digraph = graph.getDigraph();
        createGraph(block, versionResolver, digraph);
        writeToFile(digraph, fileName);
    }

    private static void createGraph(SSABlock block, VersionResolver versionResolver, Digraph digraph) {
        digraph.addNode(block.getId()).setLabel(block.getExpression().toString()).setOptions(STUB_NODE_OPTIONS);
        if (block.getChildren().stream().anyMatch(ch -> ch.getType() == CONDITION)) {
            processWithCondition(block, versionResolver, digraph);
        } else {
            processSimpleBlock(block, versionResolver, digraph);
        }
    }

    private static void processSimpleBlock(SSABlock block, VersionResolver versionResolver, Digraph digraph) {
        block.getChildren().forEach(
                child -> {
                    digraph.addNode(child.getId()).setLabel(block.getExpression().toString()).setOptions(STUB_NODE_OPTIONS);
                    digraph.addAssociation(block.getId(), child.getId());
                    createGraph(child, versionResolver, digraph);
                }
        );
    }

    private static void processWithCondition(SSABlock block, VersionResolver versionResolver, Digraph digraph) {
        SSABlock conditionBlock = block.getChildren().stream().filter(ch -> ch.getType() == CONDITION).findFirst().get();
        digraph.addNode(conditionBlock.getId()).setLabel(conditionBlock.getExpression().toString()).setOptions(CLASS_NODE_OPTIONS);
        digraph.addAssociation(block.getId(), conditionBlock.getId());

        processSimpleBlock(conditionBlock, versionResolver, digraph);

        List<SSABlock> lists = getListsFromBranch(conditionBlock);
        SSAExpression phiExpr = new SSAExpression();
        phiExpr.setVarName(versionResolver.getPhiExpr(conditionBlock.getId()));
        SSABlock phiBlock = new SSABlock(PHI, phiExpr);
        digraph.addNode(phiBlock.getId()).setLabel(phiBlock.getExpression().toString()).setOptions(STUB_NODE_OPTIONS);
        lists.forEach(list -> digraph.addAssociation(list.getId(), phiBlock.getId()));

        block.getChildren().stream().filter(ch -> ch.getType() != CONDITION).forEach(
                child -> {
                    digraph.addNode(child.getId()).setLabel(child.getExpression().toString()).setOptions(STUB_NODE_OPTIONS);
                    digraph.addAssociation(phiBlock.getId(), child.getId());
                    createGraph(child, versionResolver, digraph);
                }
        );
    }

    private static List<SSABlock> getListsFromBranch(SSABlock block) {
        List<SSABlock> lists = new ArrayList<>();
        if (block.getChildren().isEmpty()) lists.add(block);
        block.getChildren().forEach(child -> lists.addAll(getListsFromBranch(child)));
        return lists;
    }


    private static void writeToFile(Digraph digraph, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            writer.write(digraph.render());
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
