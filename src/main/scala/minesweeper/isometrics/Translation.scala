package minesweeper.isometrics

//import minesweeper.scenes.EditField
//
//case class Translation(override val name: String) extends Isometry(name) {
//    def applyIsometry(sector: Sector, board: Array[Array[EditField]], isometryArgs: List[IsometryArgs], expanding: IsometryExpansionType, transparencyType: IsometryTransparencyType): (Sector, Array[Array[EditField]]) = {
//        var inputBoard: Array[Array[EditField]] = board
//        var inputSector: Sector = sector
//
//        if (expanding == Expanding) {
//            val finalSector: Sector = getResultingSector(sector, isometryArgs)
//            val expanded = expandBoard(finalSector, board, inputSector)
//            inputBoard = expanded._1
//            inputSector = expanded._2
//        }
//
//        val finalSector: Sector = applyInChain(sector, isometryArgs)
//        val newBoard = mapToFinalBoard(inputSector, finalSector, inputBoard, transparencyType)
//
//        val adjustedSector = cutOffSector(finalSector, newBoard)
//
//        (adjustedSector, newBoard)
//    }
//
//    def applyInChain(sector: Sector, isometryArgs: List[IsometryArgs]): Sector = {
//        
//    }
//
//    def getResultingSector(sector: Sector, isometryArgs: List[IsometryArgs]): Sector = {
//        
//    }
//
//    def getChain: List[Isometry] = List(this)
//}
