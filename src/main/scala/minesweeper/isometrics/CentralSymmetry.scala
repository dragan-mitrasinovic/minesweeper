package minesweeper.isometrics

case class CentralSymmetry(override val name: String) extends Isometry(name) {

    private val innerChain: List[Isometry] = List(Rotation("Rotation1"), Rotation("Rotation2"))

    override def applyInChain(sector: Sector, isometryArgs: List[IsometryArgs]): Sector = {
        var resultingSector = sector
        val args = List(IsometryArgs(Field, Clockwise, isometryArgs.head.pivotPoint, false))

        for (isometry <- innerChain) {
            resultingSector = isometry.applyInChain(resultingSector, args)
        }

        resultingSector
    }

    override def getChain: List[Isometry] = List(this)

    override def cloneIsometry(expanding: Boolean, transparent: Boolean): Isometry = {
        if (expanding && transparent) {
            new CentralSymmetry(name) with Transparent with Expanding
        } else if (expanding) {
            new CentralSymmetry(name) with Expanding
        } else if (transparent) {
            new CentralSymmetry(name) with Transparent
        } else {
            CentralSymmetry(name)
        }
    }
}
