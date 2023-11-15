package util.algorithms;

import java.util.ArrayList;
import java.util.function.Function;

//@FunctionalInterface
//interface Function<T, Integer> {
//    int apply(T t);
//}

class BattleGridNode {
    public int x;
    public int y;
    public int weight;

    public int f;
    public int g;
    public double h;
    public boolean visited;
    public boolean closed;
    public BattleGridNode parent;

    BattleGridNode(int x, int y, int weight) {
        this.x = x;
        this.y = y;
        this.weight = weight;
    }

    public double getCost(BattleGridNode fromNeighbor) {
        //get id of
        // Take diagonal weight into consideration.
        if (fromNeighbor != null && fromNeighbor.x != this.x && fromNeighbor.y != this.y) {
            return 1.41421;
        }
        return 1;
    }
}

class BattleGraph {
    public ArrayList<BattleGridNode> nodes;
    public ArrayList<BattleGridNode> dirtyNodes;
    public BattleGridNode[][] grid;

    BattleGraph(int[][] gridIn) {
        this.nodes = new ArrayList<>();
        this.dirtyNodes = new ArrayList<>();
        this.grid = new BattleGridNode[gridIn.length][];

        for (int x = 0; x < gridIn.length; x++) {
            int[] row = gridIn[x];
            this.grid[x] = new BattleGridNode[row.length];
            for (int y = 0; y < row.length; y++) {
                BattleGridNode node = new BattleGridNode(x, y, row[y]);
                this.grid[x][y] = node;
                this.nodes.add(node);
            }
        }
        this.init();
    }

    public void init() {
        this.dirtyNodes.clear();
        for (int i = 0; i < this.nodes.size(); i++) {
//            BattleAStar.cleanNode(this.nodes[i]);
        }
    }

    public void cleanDirty() {
        for (int i = 0; i < this.dirtyNodes.size(); i++) {
//            BattleAStar.cleanNode(this.dirtyNodes[i]);
        }
        this.dirtyNodes.clear();
    }

    public void markDirty(BattleGridNode node) {
        this.dirtyNodes.add(node);
    }

    public ArrayList<BattleGridNode> neighbors(BattleGridNode node) {
        ArrayList<BattleGridNode> ret = new ArrayList<>();
        int x = node.x;
        int y = node.y;

        // West
        if (this.grid[x - 1] != null && this.grid[x - 1][y] != null) {
            ret.add(this.grid[x - 1][y]);
        }

        // East
        if (this.grid[x + 1] != null && this.grid[x + 1][y] != null) {
            ret.add(this.grid[x + 1][y]);
        }

        // South
        if (this.grid[x] != null && this.grid[x][y - 1] != null) {
            ret.add(this.grid[x][y - 1]);
        }

        // North
        if (this.grid[x] != null && this.grid[x][y + 1] != null) {
            ret.add(this.grid[x][y + 1]);
        }

        // Southwest
        if (this.grid[x - 1] != null && this.grid[x - 1][y - 1] != null) {
            ret.add(this.grid[x - 1][y - 1]);
        }

        // Southeast
        if (this.grid[x + 1] != null && this.grid[x + 1][y - 1] != null) {
            ret.add(this.grid[x + 1][y - 1]);
        }

        // Northwest
        if (this.grid[x - 1] != null && this.grid[x - 1][y + 1] != null) {
            ret.add(this.grid[x - 1][y + 1]);
        }

        // Northeast
        if (this.grid[x + 1] != null && this.grid[x + 1][y + 1] != null) {
            ret.add(this.grid[x + 1][y + 1]);
        }
        return ret;
    }

    public void changeNodeWeight(int x, int y, int weight) {
        this.grid[x][y].weight = weight;
    }

    public BattleGridNode getNode(int x, int y) {
        return this.grid[x][y];
    }
}


class BinaryHeap<T> {
    public ArrayList<T> content;
    public Function<T, Integer> scoreFunction;

    public BinaryHeap(Function<T, Integer> scoreFunction) {
        this.content = new ArrayList<>();
        this.scoreFunction = scoreFunction;
    }

    public void push(T element) {
        // Add the new element to the end of the array.
        this.content.add(element);

        // Allow it to sink down.
        this.sinkDown(this.content.size() - 1);
    }

    public T pop() {
        // Store the first element so we can return it later.
        T result = this.content.get(0);
        // Get the element at the end of the array.
        T end = this.content.remove(this.content.size() - 1);
        // If there are any elements left, put the end element at the
        // start, and let it bubble up.
        if (this.content.size() > 0) {
            this.content.set(0, end);
            this.bubbleUp(0);
        }
        return result;
    }

    public void remove(T node) {
        int i = this.content.indexOf(node);

        // When it is found, the process seen in 'pop' is repeated
        // to fill up the hole.
        T end = this.content.remove(this.content.size() - 1);

        if (i != this.content.size() - 1) {
            this.content.set(i, end);

            if (this.scoreFunction.apply(end) < this.scoreFunction.apply(node)) {
                this.sinkDown(i);
            } else {
                this.bubbleUp(i);
            }
        }
    }

    public int size() {
        return this.content.size();
    }

    public void rescoreElement(T node) {
        this.sinkDown(this.content.indexOf(node));
    }

    public void sinkDown(int n) {
        // Fetch the element that has to be sunk.
        T element = this.content.get(n);

        // When at 0, an element can not sink any further.
        while (n > 0) {

            // Compute the parent element's index, and fetch it.
            int parentN = ((n + 1) >> 1) - 1;
            T parent = this.content.get(parentN);
            // Swap the elements if the parent is greater.
            if (this.scoreFunction.apply(element) < this.scoreFunction.apply(parent)) {
                this.content.set(parentN, element);
                this.content.set(n, parent);
                // Update 'n' to continue at the new position.
                n = parentN;
            }
            // Found a parent that is less, no need to sink any further.
            else {
                break;
            }
        }
    }

    public void bubbleUp(int n) {
        // Look up the target element and its score.
        int length = this.content.size();
        T element = this.content.get(n);
        int elemScore = this.scoreFunction.apply(element);

        while (true) {
            // Compute the indices of the child elements.
            int child2N = (n + 1) << 1;
            int child1N = child2N - 1;
            // This is used to store the new position of the element, if any.
            int swap = -1;//init null
            int child1Score = 0;
            // If the first child exists (is inside the array)...
            if (child1N < length) {
                // Look it up and compute its score.
                T child1 = this.content.get(child1N);
                child1Score = this.scoreFunction.apply(child1);

                // If the score is less than our element's, we need to swap.
                if (child1Score < elemScore) {
                    swap = child1N;
                }
            }

            // Do the same checks for the other child.
            if (child2N < length) {
                T child2 = this.content.get(child2N);
                int child2Score = this.scoreFunction.apply(child2);
                if (child2Score < (swap == -1 ? elemScore : child1Score)) {
                    swap = child2N;
                }
            }

            // If the element needs to be moved, swap it, and continue.
            if (swap != -1) {
                this.content.set(n, this.content.get(swap));
                this.content.set(swap, element);
                n = swap;
            }
            // Otherwise, we are done.
            else {
                break;
            }
        }
    }
}

public class BattleAStar {
    /**
     * Perform an A* Search on a graph given a start and end node.
     **/
    public ArrayList<BattleGridNode> search(BattleGraph graph, BattleGridNode start, BattleGridNode end, int[][] mapGrid) {
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
            int endId = mapGrid[end.x][end.y];
            int currentId = mapGrid[currentNode.x][currentNode.y];
            if (currentId == endId) {
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
                double gScore = currentNode.g + neighbor.getCost(currentNode) + neighbor.weight;
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

    public void cleanNode(BattleGridNode node) {
        node.f = 0;
        node.g = 0;
        node.h = 0;
        node.visited = false;
        node.closed = false;
        node.parent = null;
    }

    //if currentNode neighbor grid value == end grid value return true. else return false
    public void isEndCase(BattleGridNode currentNode, BattleGridNode endNode) {

    }

    public ArrayList<BattleGridNode> pathTo(BattleGridNode node) {
        BattleGridNode curr = node;
        ArrayList<BattleGridNode> path = new ArrayList<>();
        while (curr.parent != null) {
            path.add(0, curr);
            curr = curr.parent;
        }
        return path;
    }

    public BinaryHeap getHeap() {
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
