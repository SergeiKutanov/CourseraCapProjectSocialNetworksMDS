package com.sergeik;

import com.sergeik.graph.CapGraph;
import com.sergeik.graph.MDS;
import com.sergeik.util.GraphLoader;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.view.camera.Camera;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Visualiser {

    private static String INFLUENCER_CLASS = "influencer";
    private static String INFLUENCED_CLASS = "influenced";

    private static long ZOOM_SPEED = 20;
    private static long NODE_DELAY = 1000;
    private static double ZOOM_FACTOR = 0.25;
    private static int PAN_FACTOR = 100;
    private static int ANIMATION_DELAY = 5000;
    private static String DATA_FILE = "data/facebook_1000.txt";

    /**
     * Launches animation
     * @param args
     */
    public static void main(String[] args) {

        System.setProperty("org.graphstream.ui", "swing");

        CapGraph capGraph = new CapGraph();
        Graph graph = loadGraphs(
                DATA_FILE,
                capGraph
        );
        graph.setAttribute("ui.stylesheet", readCSS());

        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        Viewer viewer = graph.display();
        ViewerPipe graphViewer = viewer.newViewerPipe();
        graphViewer.addSink(graph);
        View graphView = viewer.getDefaultView();

        new Thread(() -> {

            MDS mds = new MDS();
            List<Integer> cover = mds.vertexCoverGreedy(capGraph);

            int totalCoverSet = cover.size();
            int covered = 0;

            try {
                Thread.sleep(ANIMATION_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                //Main loop iterates over cover set and highlights the affected nodes at each iteration
                for (Integer coverNode : cover) {
                    graphViewer.pump();

                    Camera cam = graphView.getCamera();
                    Node node = graph.getNode(coverNode.toString());
                    zoomInto(node, cam);

                    graph.getNode(coverNode.toString())
                            .setAttribute("ui.class", INFLUENCER_CLASS);
                    node.edges().forEach((edge -> {
                        edge.setAttribute("ui.class", INFLUENCED_CLASS);
                        edge.getOpposite(node)
                                .setAttribute("ui.class", INFLUENCED_CLASS);
                    }));

                    covered++;
                    System.out.println("Covered " + covered + " out of " + totalCoverSet + ". +" + node.edges().count());

                    Thread.sleep(NODE_DELAY);
                    zoomOut(cam);

                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();

    }

    private static String readCSS() {
        StringBuilder css = new StringBuilder();
        InputStream inputStream = Visualiser.class.getClassLoader().getResourceAsStream("style.css");
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                css.append((char) c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return css.toString();
    }

    private static Graph loadGraphs(String dataFilePath, CapGraph capGraph) {
        GraphLoader.loadGraph(capGraph, dataFilePath);
        Graph vGraph = new SingleGraph("Graph", false, true);

        HashMap<Integer, HashSet<Integer>> exportedGraph = capGraph.exportGraph();
        for (Integer node: exportedGraph.keySet()) {
            vGraph.addNode(node.toString());
        }
        for (Integer node: exportedGraph.keySet()) {
            for (Integer neighbour: exportedGraph.get(node)) {
                String edgeId = node.toString() + "." + neighbour.toString();
                vGraph.addEdge(edgeId, node.toString(), neighbour.toString());
            }
        }

        return vGraph;
    }

    private static void zoomInto(Node node, Camera cam) throws InterruptedException {
        double[] pos =  Toolkit.nodePosition(node);

        double viewPercent = cam.getViewPercent();
        boolean reachedDestination = false;
        Point3 currPosition = cam.getViewCenter();
        double xStep = (pos[0] - currPosition.x) / PAN_FACTOR;
        double yStep = (pos[1] - currPosition.y) / PAN_FACTOR;
        double zStep = (pos[2] - currPosition.z) / PAN_FACTOR;
        double delta = 0.01;

        double x = currPosition.x;
        double y = currPosition.y;
        double z = currPosition.z;

        while (!reachedDestination) {
            if (viewPercent > ZOOM_FACTOR)
                viewPercent -= 0.01;

            if (!getCompareWithDelta(pos[0], x, delta)) {
                x += xStep;
            }
            if (!getCompareWithDelta(pos[1], y, delta)) {
                y += yStep;
            }
            if (!getCompareWithDelta(pos[2], z, delta)) {
                z += zStep;
            }

            cam.setViewCenter(x, y, z);
            cam.setViewPercent(viewPercent);
            Thread.sleep(ZOOM_SPEED);

            if (getCompareWithDelta(pos[0], x, delta)
                    && getCompareWithDelta(pos[1], y, delta)
                    && getCompareWithDelta(pos[2], z, delta)
                    && viewPercent <= ZOOM_FACTOR) {
                reachedDestination = true;
            }

        }
    }

    private static boolean getCompareWithDelta(double v, double v1, double delta) {
        double diff = Math.abs(v - v1);
        return diff <= delta;
    }

    private static void zoomOut(Camera cam) throws InterruptedException {
        double viewPercent = cam.getViewPercent();

        Point3 currentPoint = cam.getViewCenter();
        double x = currentPoint.x;
        double y = currentPoint.y;
        double z = currentPoint.z;

        double stepX = (0 - x) / PAN_FACTOR;
        double stepY = (0 - y) / PAN_FACTOR;
        double stepZ = (0 - z) / PAN_FACTOR;

        double delta = 0.01;

        boolean reachedDestination = false;
        while (!reachedDestination) {
            if (viewPercent < 1)
                viewPercent += 0.01;
            if (!getCompareWithDelta(x, 0, delta))
                x += stepX;
            if (!getCompareWithDelta(y, 0, delta))
                y += stepY;
            if (!getCompareWithDelta(z, 0, delta))
                z += stepZ;
            cam.setViewPercent(viewPercent);
            cam.setViewCenter(x, y, z);
            Thread.sleep(ZOOM_SPEED);
            if (getCompareWithDelta(0, x, delta)
                    && getCompareWithDelta(0, y, delta)
                    && getCompareWithDelta(0, z, delta)
                    && viewPercent >= 1) {
                reachedDestination = true;
                cam.setViewCenter(0, 0, 0);
                cam.setViewPercent(1);
            }
        }
    }

}
