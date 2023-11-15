//package util.algorithms;
//
//public class AlgorithmImplement {
//    public static AlgorithmImplement instance = null;
////    _gridMapAStar:null,
////    _results,
////    _amcSize:5,
//
//    public static AlgorithmImplement getInstance() {
//        if (AlgorithmImplement.instance == null) {
//            AlgorithmImplement.instance = new AlgorithmImplement();
//        }
//        return AlgorithmImplement.instance;
//    }
//
//    public void setGridMapStar(gridMapGame) {
//
//        let barracks = ArmyManager.getInstance().getBarrackList();
//        let barrackIds = barracks.map(e = > e.getId())
//        let armyCamps = ArmyManager.getInstance().getArmyCampList();
//        let armyCampIds = armyCamps.map(e = > e.getId())
//        this._results = {};
//        let gridMap = JSON.parse(JSON.stringify(gridMapGame));
//        for (let i = 0; i < gridMap.length; i++) {
//            for (let j = 0; j < gridMap[i].length; j++) {
//                if (gridMap[i][j] == = 0 || armyCampIds.indexOf(gridMap[i][j]) != = -1) {
//                    gridMap[i][j] = 1;
//                } else if ((gridMap[i][j] != = 0 && (gridMap[i + 1] ? gridMap[i + 1][j] : 0.1) != = gridMap[i][j])
//                        || (gridMap[i][j] != = 0 && gridMap[i][j + 1] != = gridMap[i][j])) {
//                    gridMap[i][j] = 1;
//                } else {
//                    gridMap[i][j] = 0;
//                }
//            }
//        }
//        this._gridMapAStar = new Graph(gridMap, {diagonal:true})
//    }
//
//    public void searchPathByAStar(start, end) {
//        let key = start.toString() + end.toString();
//        if (this._results[key]) {
//            // cc.log("OLD SEARCH")
//            return this._results[key];
//        } else {
//            let _start = this._gridMapAStar.grid[start[0]][start[1]];
//            let _end = this._gridMapAStar.grid[end[0]][end[1]];
//            let result = a_star.search(this._gridMapAStar, _start, _end, {closest:true})
//            this._results[key] = result;
//            return result;
//        }
//    },
//
//    public void getDiagonalPoints(startX, startY, endX, endY) {
//        // Make startX <= endX, if you don't need to check, remove this block
//        if (startX > endX) {
//            [startX, startY, endX, endY] = [endX, endY, startX, startY]
//        }
//
//        const result = []
//        const slope = Math.floor((endY - startY) / (endX - startX));
//
//        for (let i = startX, j = startY; i < endX; i++, j += slope) {
//            result.push({x:i, y :j})
//        }
//
//        cc.log("RESULT ::: " + JSON.stringify(result))
//        return result;
//    },
//
//    public void printGridMap(grid) {
//        for (let i = 0; i < grid.length; i++) {
//            for (let j = 0; j < grid[i].length; j++) {
//                cc.log(grid[i][j] + "|");
//            }
//            cc.log("\n");
//        }
//    }
//
//}
//
//
//
