package util.algorithms;
import java.util.ArrayList;

//@FunctionalInterface
//interface Function<T, Integer> {
//    int apply(T t);
//}


public class BattleAStar {
    /**
     * Perform an A* Search on a graph given a start and end node.
     **/
    public static void main(String[] args) {
        //grid 1, 132x132 value 0
        //grid 2, 132x132 value 1

        int [][] grid1,grid2;
        grid1 = new int[132][132];
        grid2 = new int[132][132];
        for(int i=0;i<132;i++)
        {
            for(int j=0;j<132;j++)
            {
                grid1[i][j] = 0;
                grid2[i][j] = 1;
            }
        }

        BattleGraph graph = new BattleGraph(grid1, grid2);
        BattleGridNode start = graph.getNode(0, 0);
        BattleGridNode end = graph.getNode(9, 9);
        ArrayList<BattleGridNode> path = search(graph, start, end);
        System.out.println(path);
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

            // End case -- result has been found, return the traced path.
            if(currentNode.x == end.x && currentNode.y == end.y) {
                return pathTo(currentNode);
            }


            // Normal case -- move currentNode from open to closed, process each of its neighbors.
            currentNode.closed = true;

            // Find all neighbors for the current node.
            ArrayList<BattleGridNode> neighbors = graph.neighbors(currentNode);

            for (int i = 0, il = neighbors.size(); i < il; ++i) {
                BattleGridNode neighbor = neighbors.get(i);

                if (neighbor.closed) {
                    // Not a valid node to process, skip to next neighbor.
                    continue;
                }

                // The g score is the shortest distance from start to current node.
                // We need to check if the path we have arrived at this neighbor is the shortest one we have seen yet.
                double gScore;
                if(neighbor.buildingId.equals(end.buildingId))
                {
                    gScore = currentNode.g + neighbor.getCost(currentNode);
                }
                else
                {
                    gScore = currentNode.g + neighbor.getCost(currentNode)+neighbor.weight;
                }

                boolean beenVisited = neighbor.visited;

                if (!beenVisited || gScore < neighbor.g) {

                    // Found an optimal (so far) path to this node.  Take score for node to see how good it is.
                    neighbor.visited = true;
                    neighbor.parent = currentNode;
                    neighbor.h = Heuristics.diagonal(neighbor, end);
                    neighbor.g = (int) gScore;

                    neighbor.f = (int) (neighbor.g + neighbor.h);
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
