package util.algorithms;
import battle_models.BattleBuilding;
import util.Common;
import util.log.LogUtils;

import java.util.ArrayList;

//@FunctionalInterface
//interface Function<T, Integer> {
//    int apply(T t);
//}


public class BattleAStar {
    /**
     * Perform an A* Search on a graph given a start and end node.
     **/


//    //đi từ dọc theo trục x, sau đó đi dọc theo trục y đên đích
    public static ArrayList<BattleGridNode> searchSimple(BattleGraph graph, BattleGridNode start, BattleGridNode end) {
        ArrayList<BattleGridNode> path = new ArrayList<>();
        if(start.x != end.x) {
            //go to end x
            int x = start.x;
            int y = start.y;
            while (x != end.x) {
                if (x < end.x) {
                    x++;
                } else {
                    x--;
                }
                path.add(graph.grid[x][y]);
            }
        }
        if(start.y != end.y) {
            //go to end y
            int x = start.x;
            int y = start.y;
            while (y != end.y) {
                if (y < end.y) {
                    y++;
                } else {
                    y--;
                }
                path.add(graph.grid[x][y]);
            }
        }
        return path;
    }

    public static ArrayList<BattleGridNode> search(BattleGraph graph, BattleGridNode start, BattleGridNode end) {

        graph.cleanDirty();
        BinaryHeap<BattleGridNode> openHeap = getHeap();
        BattleGridNode closestNode = start; // set the start node to be the closest if required

        start.h = Heuristics.diagonal(start, end);
        start.g = 0;
        graph.markDirty(start);

        openHeap.push(start);

        while (openHeap.size() > 0) {
            // Grab the lowest f(x) to process next.  Heap keeps this sorted for us.
            BattleGridNode currentNode = openHeap.pop();
            LogUtils.writeLog("currentNode: "+currentNode.x+" "+currentNode.y);

            // End case -- result has been found, return the traced path.
            if(currentNode.x == end.x && currentNode.y == end.y) {
                return pathTo(currentNode);
            }


            // Normal case -- move currentNode from open to closed, process each of its neighbors.
            currentNode.closed = true;

            // Find all neighbors for the current node.
            ArrayList<BattleGridNode> neighbors = graph.neighbors(currentNode);

            for(BattleGridNode neighbor: neighbors)
            {
                LogUtils.writeLog("neighbor: "+neighbor.x+" "+neighbor.y);
            }

            for (BattleGridNode neighbor : neighbors) {
                if (neighbor.closed) {
                    // Not a valid node to process, skip to next neighbor.
                    continue;
                }

                // The g score is the shortest distance from start to current node.
                // We need to check if the path we have arrived at this neighbor is the shortest one we have seen yet.
                double gScore;
                if (neighbor.buildingId.equals(end.buildingId)) {
                    gScore = Common.roundFloat(currentNode.g + neighbor.getCost(currentNode), 4);
                } else {
                    gScore = Common.roundFloat(currentNode.g + neighbor.getCost(currentNode) + neighbor.weight, 4);
                }
                LogUtils.writeLog("gScore: " + gScore);
                boolean beenVisited = neighbor.visited;

                if (!beenVisited || gScore < neighbor.g) {

                    // Found an optimal (so far) path to this node.  Take score for node to see how good it is.
                    neighbor.visited = true;
                    neighbor.parent = currentNode;
                    neighbor.h = Heuristics.diagonal(neighbor, end);
                    neighbor.g = gScore;
                    neighbor.f = neighbor.g + neighbor.h;
                    graph.markDirty(neighbor);

                    if (!beenVisited) {
                        // Pushing to heap will put it in proper place based on the 'f' value.
                        openHeap.push(neighbor);
                    } else {
                        // Already seen the node, but since it has been rescored we need to reorder it in the heap
                        openHeap.rescoreElement(neighbor);
                    }
                }
            }
        }

        // No result was found - empty array signifies failure to find path.
        return null;
    }

    public static void cleanNode(BattleGridNode node) {
        node.f = 0;
        node.g = 0;
        node.h = 0;
        node.visited = false;
        node.closed = false;
        node.parent = null;
    }

    public static ArrayList<BattleGridNode> pathTo(BattleGridNode node) {
        BattleGridNode curr = node;
        ArrayList<BattleGridNode> path = new ArrayList<>();
        while (curr.parent != null) {
            path.add(0, curr);
            curr = curr.parent;
        }
        return path;
    }

    public static BinaryHeap<BattleGridNode> getHeap() {
        return new BinaryHeap<BattleGridNode>((node) -> node.f);
    }

    // See list of heuristics: http://theory.stanford.edu/~amitp/GameProgramming/Heuristics.html
    public static class Heuristics {
        public static double manhattan(BattleGridNode pos0, BattleGridNode pos1) {
            double d1 = Math.abs(pos1.x - pos0.x);
            double d2 = Math.abs(pos1.y - pos0.y);
            return d1 + d2;
        }

        public static double diagonal(BattleGridNode pos0, BattleGridNode pos1) {
            double D = 1;
            double D2 = Math.sqrt(2);
            double d1 = Math.abs(pos1.x - pos0.x);
            double d2 = Math.abs(pos1.y - pos0.y);
            return (D * (d1 + d2)) + ((D2 - (2 * D)) * Math.min(d1, d2));
        }
    }

}
