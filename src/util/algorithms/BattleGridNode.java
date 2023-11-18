package util.algorithms;

public class BattleGridNode
{
    public int x;
    public int y;
    public int weight;

    public Integer buildingId;

    public double f;
    public double g;
    public double h;
    public boolean visited;
    public boolean closed;
    public BattleGridNode parent;

    public BattleGridNode(int x, int y, int weight, Integer id)
    {
        this.x = x;
        this.y = y;
        this.weight = weight;
        this.buildingId = id;

    }

    public double getCost(BattleGridNode fromNeighbor)
    {
        //get id of
        // Take diagonal weight into consideration.
        if (fromNeighbor != null && fromNeighbor.x != this.x && fromNeighbor.y != this.y)
        {
            return 1.41421;
        }
        return 1;
    }
    public String toString(){
        return "x: "+x+" y: "+y+" weight: "+weight+" id: "+buildingId;
    }
}
