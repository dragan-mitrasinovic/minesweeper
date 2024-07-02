package minesweeper.isometrics

class Composition(override val name: String, val isometries: List[Isometry]) extends Isometry(name) {

    override def applyInChain(sector: Sector, isometryArgs: List[IsometryArgs]): Sector = {
        var resultingSector = sector
        var args = isometryArgs

        for (isometry <- isometries) {
            resultingSector = isometry.applyInChain(resultingSector, args)
            for (i <- isometry.getChain.indices) {
                args = args.tail
            }
        }

        resultingSector
    }

    override def getChain: List[Isometry] = {
        isometries.flatMap(_.getChain)
    }
}
