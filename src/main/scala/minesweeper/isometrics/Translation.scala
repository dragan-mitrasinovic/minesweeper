package minesweeper.isometrics

case class Translation(override val name: String) extends Isometry(name) {
    override def applyInChain(sector: Sector, isometryArgs: List[IsometryArgs]): Sector = {
        val indices = Array.ofDim[(Int, Int)](sector.indices.length, sector.indices(0).length)

        val pivotX = isometryArgs.head.pivotPoint.row
        val pivotY = isometryArgs.head.pivotPoint.col

        val sectorHeight = sector.bottomRightPoint.row - sector.topLeftPoint.row
        val sectorWidth = sector.bottomRightPoint.col - sector.topLeftPoint.col

        var deltaX = pivotX - sector.bottomRightPoint.row
        var deltaY = pivotY - sector.topLeftPoint.col

        if (isometryArgs.head.inverted) {
            deltaX = -deltaX
            deltaY = -deltaY
        }

        val newSector = new Sector(
            new Point(sector.topLeftPoint.row + deltaX, sector.topLeftPoint.col + deltaY),
            new Point(sector.bottomRightPoint.row + deltaX, sector.bottomRightPoint.col + deltaY)
        )

        for (i <- sector.indices.indices; j <- sector.indices(0).indices) {
            indices(i)(j) = (sector.indices(i)(j)._1 + deltaX, sector.indices(i)(j)._2 + deltaY)
        }

        newSector.indices = indices
        newSector
    }

    override def getChain: List[Isometry] = List(this)

    override def cloneIsometry(expanding: Boolean, transparent: Boolean): Isometry = {
        if (expanding && transparent) {
            new Translation(name) with Transparent with Expanding
        } else if (expanding) {
            new Translation(name) with Expanding
        } else if (transparent) {
            new Translation(name) with Transparent
        } else {
            Translation(name)
        }
    }
}
