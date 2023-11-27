package battle_models;

import util.algorithms.BattleAStar;
import util.algorithms.BattleGraph;
import util.algorithms.BattleGridNode;
import util.log.LogUtils;

import java.awt.*;
import java.util.ArrayList;

public class Bomber extends BattleTroop{
    public Bomber(int level, int posX, int posY) {
        super("ARM_6", level, posX, posY);
    }
    @Override
    public void findPath() {
        try
        {
            //get path
            BattleGraph graph = this.match.getBattleGraphWithoutWall();
            BattleGridNode start = new BattleGridNode(this.posX, this.posY, graph.getNode(this.posX, this.posY).weight, null);


            Point nearestPoint = this.target.getNearestPoint(this.posX, this.posY, this.id, true);

            BattleGridNode end = new BattleGridNode(
                    nearestPoint.x, nearestPoint.y,
                    graph.getNode(nearestPoint.x, nearestPoint.y).weight, this.target.id);

            ArrayList<BattleGridNode> path = BattleAStar.search(graph, start, end);

            //change to Point
            ArrayList<Point> ret = new ArrayList<>();
            if (path == null)
            {
                this.path = ret;
                return;
            }

            for (BattleGridNode node : path)
            {
                ret.add(new Point(node.x, node.y));
            }

            this.path = ret;
            return;
        } catch (Exception exception)
        {
            exception.printStackTrace();
            System.out.println(exception.getMessage());
        }
        this.path = null;
    }
    @Override
    public void checkPath() {
        for (int i = 0; i < this.path.size(); i++)
        {
            int x = this.path.get(i).x;
            int y = this.path.get(i).y;

            // if path go through WAL, this.target = WAL
            BattleBuilding building = this.match.getBattleBuildingByPos(x, y);
            if (building != null)
            {
                LogUtils.writeLog("building: " + building.type);
                LogUtils.writeLog("x , y" + x + " " + y);
                LogUtils.writeLog("building pos: " + building.posX + " " + building.posY);
                LogUtils.writeLog("building width: " + building.width + " height: " + building.height);
            }


            //if (x,y) is in range attack, path is valid, return;
            if (this.isInAttackRange(this.target, x, y))
            {
                return;
            }
        }
    }
}