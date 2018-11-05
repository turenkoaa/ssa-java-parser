package com.kspt.aaturenko.diagram;

import com.kspt.aaturenko.ssa_tree.SSABlock;
import io.github.livingdocumentation.dotdiagram.DotGraph;

import java.io.FileWriter;
import java.io.IOException;

import static io.github.livingdocumentation.dotdiagram.DotStyles.ASSOCIATION_EDGE_STYLE;
import static io.github.livingdocumentation.dotdiagram.DotStyles.STUB_NODE_OPTIONS;

/**
 * Created by Anastasia on 05.11.2018.
 */
public class DiagramUtil {

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
}
