package com.sergeik.graph;

import java.util.*;

public class MDS {

    /**
     * Computes optimal cover set for the graph
     *
     * @param graph - contains graph data
     * @return cover set approximation
     */
    public List<Integer> vertexCoverGreedy(Graph graph) {

        HashMap<Integer, HashSet<Integer>> graphData = graph.exportGraph();
        List<Integer> coverList = new LinkedList<>();

        //Important to make a copy of key set.
        //Otherwise the actual graph data will be affected upon deletion of an uncovered vertex
        Set<Integer> uncovered = new HashSet<>(graphData.keySet());

        //iterate over uncovered vertices until all are covered
        while (!uncovered.isEmpty()) {
            Integer vertex = getMaxUncoveredVertex(uncovered, graphData);
            uncovered.remove(vertex);
            uncovered.removeAll(graphData.get(vertex));
            coverList.add(vertex);
        }

        return coverList;
    }

    /**
     * Looks up next vertex which covers the most uncovered vertices
     *
     * @param uncovered - set of uncovered vertices
     * @param graph - graph data
     * @return
     */
    private Integer getMaxUncoveredVertex(Set<Integer> uncovered, HashMap<Integer, HashSet<Integer>> graph) {
        Integer maxVertex = null;
        Integer covers = null;

        for (Integer v: uncovered) {
            Set<Integer> neighbours = new HashSet<>(graph.get(v));
            //Keeps union of two sets. This way will get how many uncovered vertices will be covered by v
            neighbours.retainAll(uncovered);
            if (maxVertex == null && covers == null) {
                maxVertex = v;
                covers = neighbours.size();
            } else if (neighbours.size() > covers) {
                maxVertex = v;
                covers = neighbours.size();
            }
        }
        return maxVertex;
    }
}
