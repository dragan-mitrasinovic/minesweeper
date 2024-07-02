package minesweeper.isometrics

case class Reflection(override val name: String) extends Isometry(name) {

    override def applyInChain(sector: Sector, isometryArgs: List[IsometryArgs]): Sector = {
        val pivotRow = isometryArgs.head.pivotPoint.row
        val pivotCol = isometryArgs.head.pivotPoint.col

        val (startRow, startCol) = (sector.topLeftPoint.row, sector.topLeftPoint.col)
        val (endRow, endCol) = (sector.bottomRightPoint.row, sector.bottomRightPoint.col)

        var minRow = Int.MaxValue
        var minCol = Int.MaxValue
        var maxRow = Int.MinValue
        var maxCol = Int.MinValue

        val indices = Array.ofDim[(Int, Int)](sector.indices.length, sector.indices(0).length)

        for (row <- startRow to endRow) {
            for (col <- startCol to endCol) {
                val newRow = isometryArgs.head.direction match {
                    case Row => 2 * pivotRow - row
                    case Column => row
                    case Diagonal => pivotRow + pivotCol - col
                    case Antidiagonal => pivotRow - pivotCol + col
                    case Field => row
                }
                val newCol = isometryArgs.head.direction match {
                    case Row => col
                    case Column => 2 * pivotCol - col
                    case Diagonal => pivotRow + pivotCol - row
                    case Antidiagonal => pivotCol - pivotRow + row
                    case Field => col
                }

                minRow = Math.min(minRow, newRow)
                minCol = Math.min(minCol, newCol)
                maxRow = Math.max(maxRow, newRow)
                maxCol = Math.max(maxCol, newCol)

                for (i <- indices.indices; j <- indices(0).indices) {
                    if (sector.indices(i)(j) == (row, col)) {
                        indices(i)(j) = (newRow, newCol)
                    }
                }
            }
        }

        val newSector = new Sector(new Point(minRow, minCol), new Point(maxRow, maxCol))
        newSector.indices = indices

        newSector
    }

    override def getChain: List[Isometry] = List(this)
}
