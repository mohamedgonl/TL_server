package util.algorithms;

import java.util.ArrayList;

public class BattleGraph
{
    public ArrayList<BattleGridNode> nodes;
    public ArrayList<BattleGridNode> dirtyNodes;
    public BattleGridNode[][] grid;

    public BattleGraph(int[][] gridIn, int[][] idIn)
    {
        this.nodes = new ArrayList<>();
        this.dirtyNodes = new ArrayList<>();
        this.grid = new BattleGridNode[gridIn.length][];

        int xLength = gridIn.length;
        int yLength = gridIn[0].length;

        for (int x = 0; x < xLength; x++)
        {
            this.grid[x] = new BattleGridNode[yLength];
            for (int y = 0; y < yLength; y++)
            {
                BattleGridNode node = new BattleGridNode(x, y, gridIn[x][y], idIn[x][y]);
                this.grid[x][y] = node;
                this.nodes.add(node);
            }
        }
        this.init();
    }

    public void init()
    {
        this.dirtyNodes.clear();
        for (int i = 0; i < this.nodes.size(); i++)
        {
            BattleAStar.cleanNode(this.nodes.get(i));
        }
    }

    public void cleanDirty()
    {
        for (int i = 0; i < this.dirtyNodes.size(); i++)
        {
            BattleAStar.cleanNode(this.dirtyNodes.get(i));
        }
        this.dirtyNodes.clear();
    }

    public void markDirty(BattleGridNode node)
    {
        this.dirtyNodes.add(node);
    }

    public ArrayList<BattleGridNode> neighbors(BattleGridNode node)
    {
        ArrayList<BattleGridNode> ret = new ArrayList<>();
        int x = node.x;
        int y = node.y;

        // West
        if (this.grid[x - 1] != null && this.grid[x - 1][y] != null)
        {
            ret.add(this.grid[x - 1][y]);
        }

        // East
        if (this.grid[x + 1] != null && this.grid[x + 1][y] != null)
        {
            ret.add(this.grid[x + 1][y]);
        }

        // South
        if (this.grid[x] != null && this.grid[x][y - 1] != null)
        {
            ret.add(this.grid[x][y - 1]);
        }

        // North
        if (this.grid[x] != null && this.grid[x][y + 1] != null)
        {
            ret.add(this.grid[x][y + 1]);
        }

        // Southwest
        if (this.grid[x - 1] != null && this.grid[x - 1][y - 1] != null)
        {
            ret.add(this.grid[x - 1][y - 1]);
        }

        // Southeast
        if (this.grid[x + 1] != null && this.grid[x + 1][y - 1] != null)
        {
            ret.add(this.grid[x + 1][y - 1]);
        }

        // Northwest
        if (this.grid[x - 1] != null && this.grid[x - 1][y + 1] != null)
        {
            ret.add(this.grid[x - 1][y + 1]);
        }

        // Northeast
        if (this.grid[x + 1] != null && this.grid[x + 1][y + 1] != null)
        {
            ret.add(this.grid[x + 1][y + 1]);
        }
        return ret;
    }

    public void changeNodeWeight(int x, int y, int weight)
    {
        this.grid[x][y].weight = weight;
    }

    public BattleGridNode getNode(int x, int y)
    {
        return this.grid[x][y];
    }
}
