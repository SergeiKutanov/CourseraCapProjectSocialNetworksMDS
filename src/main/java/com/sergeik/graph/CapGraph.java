package com.sergeik.graph;

import java.util.*;

public class CapGraph implements Graph {

    private HashMap<Integer, HashSet<Integer>> graphData = new HashMap<>();

    public void addVertex(int num) {
        if (!graphData.containsKey(num)) {
            graphData.put(num, new HashSet<>());
        }
    }

    public void addEdge(int from, int to) {
        //non existent edges
        if (!graphData.containsKey(from) || !graphData.containsKey(to)) return;

        Set<Integer> neighbours = graphData.get(from);
        if (!neighbours.contains(to)) {
            neighbours.add(to);
        }

    }

    @Override
    public Graph getEgonet(int center) {
        Graph egoGraph = new CapGraph();

        if (graphData.containsKey(center)) {
            Set<Integer> centerNeighbours = graphData.get(center);
            egoGraph.addVertex(center);
            //verify center neighbours
            for (Integer centerNeighbour: centerNeighbours) {
                //add vertex only if there's two way connection between the center and the examined vertex
                if (graphData.get(centerNeighbour).contains(center)) {
                    egoGraph.addVertex(centerNeighbour);
                    egoGraph.addEdge(center, centerNeighbour);
                    egoGraph.addEdge(centerNeighbour, center);
                }
            }
            //add edges between neighbours
            Set<Integer> egoGraphCenterNeighbours = egoGraph.exportGraph().get(center);
            for (Integer vertex: egoGraphCenterNeighbours) {
                for (Integer vertexNeighbour : graphData.get(vertex)) {
                    // add edge only if two-way connection exists between the two and if they are both neighbours with the center
                    if (egoGraphCenterNeighbours.contains(vertexNeighbour) && graphData.get(vertexNeighbour).contains(vertex)) {
                        egoGraph.addEdge(vertex, vertexNeighbour);
                        egoGraph.addEdge(vertexNeighbour, vertex);
                    }
                }
            }
        }
        return egoGraph;
    }

    @Override
    public List<Graph> getSCCs() {
        List<Graph> sccGraphs = new ArrayList<>();

        Set<Integer> visited = new HashSet<>();
        Stack<Integer> finishedStack = new Stack<>();

        //step 1
        for (Integer vertex: graphData.keySet()) {
            if (!visited.contains(vertex)) {
                dfsVisit(this, vertex, visited, finishedStack);
            }
        }

        //step 2 - transpose graph
        Graph transposedGraph = transposeGraph();

        //second DFS pass
        visited.clear();
        Stack<Integer> stepTwoFinished = new Stack<>();
        while (!finishedStack.isEmpty()) {
            Integer vertex = finishedStack.pop();
            if (!visited.contains(vertex)) {
                dfsVisit(transposedGraph, vertex, visited, stepTwoFinished);
                Graph sccGraph = new CapGraph();
                while (!stepTwoFinished.isEmpty()) {
                    Integer sccVertex = stepTwoFinished.pop();
                    sccGraph.addVertex(sccVertex);
                }
                sccGraphs.add(sccGraph);
            }
        }

        //rebuild SCC edges
        for (Graph sccGraph: sccGraphs) {
            Set<Integer> sccVertexes = sccGraph.exportGraph().keySet();
            for (Integer sccVertex: sccVertexes) {
                for (Integer sccVertexNeighbour: graphData.get(sccVertex)) {
                    if (sccVertexes.contains(sccVertexNeighbour)) {
                        sccGraph.addEdge(sccVertex, sccVertexNeighbour);
                        if (graphData.get(sccVertexNeighbour).contains(sccVertex)) {
                            sccGraph.addEdge(sccVertexNeighbour, sccVertex);
                        }
                    }
                }
            }
        }

        return sccGraphs;
    }

    private Graph transposeGraph() {
        Graph graph = new CapGraph();
        for (Integer vertex: graphData.keySet()) {
            graph.addVertex(vertex);
            for (Integer neighbour: graphData.get(vertex)) {
                graph.addVertex(neighbour);
                graph.addEdge(neighbour, vertex);
            }
        }
        return graph;
    }

    private void dfsVisit(Graph graph, Integer vertex, Set<Integer> visited, Stack<Integer> finished) {
        visited.add(vertex);
        for (Integer neighbour: graph.exportGraph().get(vertex)) {
            if (!visited.contains(neighbour)) {
                dfsVisit(graph, neighbour, visited, finished);
            }
        }
        finished.push(vertex);
    }

    /* (non-Javadoc)
     * @see graph.Graph#exportGraph()
     */
    @Override
    public HashMap<Integer, HashSet<Integer>> exportGraph() {
        return graphData;
    }

}
