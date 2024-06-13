package minesweeper.isometrics

import minesweeper.scenes.EditField

class Isometry(val name: String, val expanding: IsometryExpansionType, val transparent: IsometryTransparencyType, val isometryType: IsometryType, val isometryDirection: IsometryDirection, rotationDirection: RotationDirection = Unused) {
    private var chain = Array[Isometry]()

    def chain(nextIsometry: Isometry): Unit = {
        chain :+= nextIsometry
    }

    def getInverse: Isometry = {
        val inverseRotationDirection = rotationDirection match {
            case Clockwise => Counterclockwise
            case Counterclockwise => Clockwise
            case _ => Unused
        }

        val inverse = new Isometry(name + "-INV", expanding, transparent, isometryType, isometryDirection, inverseRotationDirection)

        inverse.chain = chain.reverse.map(_.getInverse)

        inverse
    }

    def applyIsometry(sector: Sector, board: Array[Array[EditField]], pivotPoint: Point): (Array[Array[EditField]], Sector, Point) = {
        var newSector: Sector = null

        // Expand the board if needed
        var (xBoard: Array[Array[EditField]], xSector: Sector, xPivotPoint: Point) = expandBoardIfNeeded(board, sector, pivotPoint, isometryDirection)

        // Copy the board
        val tempBoard: Array[Array[EditField]] = Array.ofDim[EditField](xBoard.length, xBoard(0).length)
        for (row <- tempBoard.indices) {
            for (col <- tempBoard(0).indices) {
                tempBoard(row)(col) = new EditField(false)
                tempBoard(row)(col).isMine = xBoard(row)(col).isMine
                tempBoard(row)(col).isSelected = xBoard(row)(col).isSelected
            }
        }

        if (isometryType == AxialReflection) {
            newSector = getNewSector(xSector, xPivotPoint, isometryDirection, Unused)

            if (expanding == NonExpanding) {
                // Cut off the sector if it goes out of bounds
                val newTopLeftRow = Math.max(0, newSector.topLeftPoint.row)
                val newTopLeftCol = Math.max(0, newSector.topLeftPoint.col)
                val newBottomRightRow = Math.min(tempBoard.length - 1, newSector.bottomRightPoint.row)
                val newBottomRightCol = Math.min(tempBoard(0).length - 1, newSector.bottomRightPoint.col)

                if (newTopLeftRow >= tempBoard(0).length || newTopLeftCol >= tempBoard.length || newBottomRightRow < 0 || newBottomRightCol < 0) {
                    return (tempBoard, null, xPivotPoint)
                }

                newSector = new Sector(new Point(newTopLeftRow, newTopLeftCol), new Point(newBottomRightRow, newBottomRightCol))

                // Map cut off new sector to old sector
                xSector = getNewSector(newSector, xPivotPoint, isometryDirection, Unused)
            }

            val startRow = xSector.topLeftPoint.row
            val startCol = xSector.topLeftPoint.col
            val endRow = xSector.bottomRightPoint.row
            val endCol = xSector.bottomRightPoint.col

            for (row <- startRow to endRow) {
                for (col <- startCol to endCol) {
                    isometryDirection match {
                        case Row =>
                            val rowDistance = row - xPivotPoint.row
                            tempBoard(xPivotPoint.row - rowDistance)(col).isMine = xBoard(row)(col).isMine
                            tempBoard(xPivotPoint.row - rowDistance)(col).isSector = true
                        case Column =>
                            val colDistance = col - xPivotPoint.col
                            tempBoard(row)(xPivotPoint.col - colDistance).isMine = xBoard(row)(col).isMine
                            tempBoard(row)(xPivotPoint.col - colDistance).isSector = true
                        case Diagonal =>
                            val rowDistance = row - xPivotPoint.row
                            val colDistance = col - xPivotPoint.col
                            tempBoard(xPivotPoint.row - colDistance)(xPivotPoint.col - rowDistance).isMine = xBoard(row)(col).isMine
                            tempBoard(xPivotPoint.row - colDistance)(xPivotPoint.col - rowDistance).isSector = true
                            tempBoard(row)(col).isMine = false
                        case Antidiagonal =>
                            val rowDistance = row - xPivotPoint.row
                            val colDistance = col - xPivotPoint.col
                            tempBoard(xPivotPoint.row + colDistance)(xPivotPoint.col + rowDistance).isMine = xBoard(row)(col).isMine
                            tempBoard(xPivotPoint.row + colDistance)(xPivotPoint.col + rowDistance).isSector = true
                            tempBoard(row)(col).isMine = false
                        case Field =>
                    }
                }
            }
        } else {
            newSector = getNewSector(xSector, xPivotPoint, isometryDirection, rotationDirection)

            if (expanding == NonExpanding) {
                // Cut off the sector if it goes out of bounds
                val newTopLeftRow = Math.max(0, newSector.topLeftPoint.row)
                val newTopLeftCol = Math.max(0, newSector.topLeftPoint.col)
                val newBottomRightRow = Math.min(tempBoard.length - 1, newSector.bottomRightPoint.row)
                val newBottomRightCol = Math.min(tempBoard(0).length - 1, newSector.bottomRightPoint.col)

                if (newTopLeftRow >= tempBoard(0).length || newTopLeftCol >= tempBoard.length || newBottomRightRow < 0 || newBottomRightCol < 0) {
                    return (tempBoard, null, xPivotPoint)
                }

                newSector = new Sector(new Point(newTopLeftRow, newTopLeftCol), new Point(newBottomRightRow, newBottomRightCol))

                // Map cut off new sector to old sector
                val reverseRotationDirection = rotationDirection match {
                    case Clockwise => Counterclockwise
                    case Counterclockwise => Clockwise
                    case _ => Unused
                }
                xSector = getNewSector(newSector, xPivotPoint, isometryDirection, reverseRotationDirection)
            }

            val pivotRow = xPivotPoint.row
            val pivotCol = xPivotPoint.col

            val (startRow, startCol) = (xSector.topLeftPoint.row, xSector.topLeftPoint.col)
            val (endRow, endCol) = (xSector.bottomRightPoint.row, xSector.bottomRightPoint.col)

            var minRow = Int.MaxValue
            var minCol = Int.MaxValue
            var maxRow = Int.MinValue
            var maxCol = Int.MinValue

            for (row <- startRow to endRow) {
                for (col <- startCol to endCol) {
                    val newRow = rotationDirection match {
                        case Counterclockwise => pivotRow - (col - pivotCol)
                        case Clockwise => pivotRow + (col - pivotCol)
                        case _ => row
                    }
                    val newCol = rotationDirection match {
                        case Counterclockwise => pivotCol + (row - pivotRow)
                        case Clockwise => pivotCol - (row - pivotRow)
                        case _ => col
                    }

                    minRow = Math.min(minRow, newRow)
                    minCol = Math.min(minCol, newCol)
                    maxRow = Math.max(maxRow, newRow)
                    maxCol = Math.max(maxCol, newCol)

                    tempBoard(newRow)(newCol).isMine = tempBoard(row)(col).isMine
                    tempBoard(newRow)(newCol).isSector = true
                }
            }
        }

        if (transparent == Transparent) {
            for (row <- newSector.topLeftPoint.row to newSector.bottomRightPoint.row) {
                for (col <- newSector.topLeftPoint.col to newSector.bottomRightPoint.col) {
                    tempBoard(row)(col).isMine = tempBoard(row)(col).isMine || xBoard(row)(col).isMine
                }
            }

        }

        var (chainBoard, chainSector, chainPivot) = (tempBoard, newSector, xPivotPoint)

        for (newxtIsometry <- chain) {
            val (nextChainBoard: Array[Array[EditField]], nextChainSector: Sector, nextChainPivot: Point) = newxtIsometry.applyIsometry(chainSector, chainBoard, chainPivot)
            chainBoard = nextChainBoard
            chainSector = nextChainSector
            chainPivot = nextChainPivot
        }

        (chainBoard, chainSector, chainPivot)
    }

    private def expandBoardIfNeeded(board: Array[Array[EditField]], sector: Sector, pivotPoint: Point, direction: IsometryDirection): (Array[Array[EditField]], Sector, Point) = {
        if (expanding == NonExpanding) {
            return (board, sector, pivotPoint)
        }

        val (startRow, startCol) = (sector.topLeftPoint.row, sector.topLeftPoint.col)
        val (endRow, endCol) = (sector.bottomRightPoint.row, sector.bottomRightPoint.col)

        var newSector: Sector = sector
        var newPivotPoint: Point = pivotPoint
        var tempBoard: Array[Array[EditField]] = board

        var potentialNewSector: Sector = null

        if (isometryType == AxialReflection) {
            direction match {
                case Row =>
                    val rowDistanceTopLeft = startRow - pivotPoint.row
                    val rowDistanceBottomRight = endRow - pivotPoint.row
                    potentialNewSector = new Sector(new Point(pivotPoint.row - rowDistanceTopLeft, startCol), new Point(pivotPoint.row - rowDistanceBottomRight, endCol))
                case Column =>
                    val colDistanceTopLeft = startCol - pivotPoint.col
                    val colDistanceBottomRight = endCol - pivotPoint.col
                    potentialNewSector = new Sector(new Point(startRow, pivotPoint.col - colDistanceTopLeft), new Point(endRow, pivotPoint.col - colDistanceBottomRight))
                case Diagonal =>
                    val rowDistanceTopLeft = startRow - pivotPoint.row
                    val rowDistanceBottomRight = endRow - pivotPoint.row
                    val colDistanceTopLeft = startCol - pivotPoint.col
                    val colDistanceBottomRight = endCol - pivotPoint.col
                    potentialNewSector = new Sector(new Point(pivotPoint.row - colDistanceTopLeft, pivotPoint.col - rowDistanceTopLeft), new Point(pivotPoint.row - colDistanceBottomRight, pivotPoint.col - rowDistanceBottomRight))
                case Antidiagonal =>
                    val rowDistanceTopLeft = startRow - pivotPoint.row
                    val rowDistanceBottomRight = endRow - pivotPoint.row
                    val colDistanceTopLeft = startCol - pivotPoint.col
                    val colDistanceBottomRight = endCol - pivotPoint.col
                    potentialNewSector = new Sector(new Point(pivotPoint.row + colDistanceTopLeft, pivotPoint.col + rowDistanceTopLeft), new Point(pivotPoint.row + colDistanceBottomRight, pivotPoint.col + rowDistanceBottomRight))
                case Field =>
            }
        } else {
            val pivotRow = pivotPoint.row
            val pivotCol = pivotPoint.col

            val (startRow, startCol) = (sector.topLeftPoint.row, sector.topLeftPoint.col)
            val (endRow, endCol) = (sector.bottomRightPoint.row, sector.bottomRightPoint.col)

            var minRow = Int.MaxValue
            var minCol = Int.MaxValue
            var maxRow = Int.MinValue
            var maxCol = Int.MinValue

            for (row <- startRow to endRow) {
                for (col <- startCol to endCol) {
                    val newRow = rotationDirection match {
                        case Counterclockwise => pivotRow - (col - pivotCol)
                        case Clockwise => pivotRow + (col - pivotCol)
                        case _ => row
                    }
                    val newCol = rotationDirection match {
                        case Counterclockwise => pivotCol + (row - pivotRow)
                        case Clockwise => pivotCol - (row - pivotRow)
                        case _ => col
                    }

                    minRow = Math.min(minRow, newRow)
                    minCol = Math.min(minCol, newCol)
                    maxRow = Math.max(maxRow, newRow)
                    maxCol = Math.max(maxCol, newCol)
                }
            }

            potentialNewSector = new Sector(new Point(minRow, minCol), new Point(maxRow, maxCol))
        }

        if (potentialNewSector.topLeftPoint.row < 0) {
            val newBoard = Array.ofDim[EditField](tempBoard.length + Math.abs(potentialNewSector.topLeftPoint.row), tempBoard(0).length)
            for (row <- newBoard.indices) {
                for (col <- newBoard(0).indices) {
                    newBoard(row)(col) = new EditField(false)
                }
            }
            for (row <- tempBoard.indices) {
                for (col <- tempBoard(0).indices) {
                    newBoard(row + Math.abs(potentialNewSector.topLeftPoint.row))(col) = tempBoard(row)(col)
                }
            }

            tempBoard = newBoard
            newSector = new Sector(new Point(newSector.topLeftPoint.row + Math.abs(potentialNewSector.topLeftPoint.row), newSector.topLeftPoint.col), new Point(newSector.bottomRightPoint.row + Math.abs(potentialNewSector.topLeftPoint.row), newSector.bottomRightPoint.col))
            newPivotPoint = new Point(pivotPoint.row + Math.abs(potentialNewSector.topLeftPoint.row), pivotPoint.col)
        }

        if (potentialNewSector.topLeftPoint.col < 0) {
            val newBoard = Array.ofDim[EditField](tempBoard.length, tempBoard(0).length + Math.abs(potentialNewSector.topLeftPoint.col))
            for (row <- newBoard.indices) {
                for (col <- newBoard(0).indices) {
                    newBoard(row)(col) = new EditField(false)
                }
            }
            for (row <- tempBoard.indices) {
                for (col <- tempBoard(0).indices) {
                    newBoard(row)(col + Math.abs(potentialNewSector.topLeftPoint.col)) = tempBoard(row)(col)
                }
            }

            tempBoard = newBoard
            newSector = new Sector(new Point(newSector.topLeftPoint.row, newSector.topLeftPoint.col + Math.abs(potentialNewSector.topLeftPoint.col)), new Point(newSector.bottomRightPoint.row, newSector.bottomRightPoint.col + Math.abs(potentialNewSector.topLeftPoint.col)))
            newPivotPoint = new Point(pivotPoint.row, pivotPoint.col + Math.abs(potentialNewSector.topLeftPoint.col))
        }

        if (potentialNewSector.bottomRightPoint.row >= tempBoard.length) {
            val newBoard = Array.ofDim[EditField](tempBoard.length + (potentialNewSector.bottomRightPoint.row - tempBoard.length + 1), tempBoard(0).length)
            for (row <- newBoard.indices) {
                for (col <- newBoard(0).indices) {
                    newBoard(row)(col) = new EditField(false)
                }
            }
            for (row <- tempBoard.indices) {
                for (col <- tempBoard(0).indices) {
                    newBoard(row)(col) = tempBoard(row)(col)
                }
            }
            tempBoard = newBoard
        }

        if (potentialNewSector.bottomRightPoint.col >= tempBoard(0).length) {
            val newBoard = Array.ofDim[EditField](tempBoard.length, tempBoard(0).length + (potentialNewSector.bottomRightPoint.col - tempBoard(0).length + 1))
            for (row <- newBoard.indices) {
                for (col <- newBoard(0).indices) {
                    newBoard(row)(col) = new EditField(false)
                }
            }
            for (row <- tempBoard.indices) {
                for (col <- tempBoard(0).indices) {
                    newBoard(row)(col) = tempBoard(row)(col)
                }
            }
            tempBoard = newBoard
        }

        (tempBoard, newSector, newPivotPoint)
    }

    private def getNewSector(xSector: Sector, xPivotPoint: Point, direction: IsometryDirection, rotationDirection: RotationDirection): Sector = {
        if (isometryType == AxialReflection) {
            var newSector: Sector = null
            val (startRow, startCol) = (xSector.topLeftPoint.row, xSector.topLeftPoint.col)
            val (endRow, endCol) = (xSector.bottomRightPoint.row, xSector.bottomRightPoint.col)

            // Adjust the new sector
            direction match {
                case Row =>
                    val rowDistanceTopLeft = startRow - xPivotPoint.row
                    val rowDistanceBottomRight = endRow - xPivotPoint.row
                    new Sector(new Point(xPivotPoint.row - rowDistanceTopLeft, startCol), new Point(xPivotPoint.row - rowDistanceBottomRight, endCol))
                case Column =>
                    val colDistanceTopLeft = startCol - xPivotPoint.col
                    val colDistanceBottomRight = endCol - xPivotPoint.col
                    new Sector(new Point(startRow, xPivotPoint.col - colDistanceTopLeft), new Point(endRow, xPivotPoint.col - colDistanceBottomRight))
                case Diagonal =>
                    val rowDistanceTopLeft = startRow - xPivotPoint.row
                    val rowDistanceBottomRight = endRow - xPivotPoint.row
                    val colDistanceTopLeft = startCol - xPivotPoint.col
                    val colDistanceBottomRight = endCol - xPivotPoint.col
                    new Sector(new Point(xPivotPoint.row - colDistanceTopLeft, xPivotPoint.col - rowDistanceTopLeft), new Point(xPivotPoint.row - colDistanceBottomRight, xPivotPoint.col - rowDistanceBottomRight))
                case Antidiagonal =>
                    val rowDistanceTopLeft = startRow - xPivotPoint.row
                    val rowDistanceBottomRight = endRow - xPivotPoint.row
                    val colDistanceTopLeft = startCol - xPivotPoint.col
                    val colDistanceBottomRight = endCol - xPivotPoint.col
                    new Sector(new Point(xPivotPoint.row + colDistanceTopLeft, xPivotPoint.col + rowDistanceTopLeft), new Point(xPivotPoint.row + colDistanceBottomRight, xPivotPoint.col + rowDistanceBottomRight))
                case Field =>
                    null
            }
        } else {
            val pivotRow = xPivotPoint.row
            val pivotCol = xPivotPoint.col

            val (startRow, startCol) = (xSector.topLeftPoint.row, xSector.topLeftPoint.col)
            val (endRow, endCol) = (xSector.bottomRightPoint.row, xSector.bottomRightPoint.col)

            var minRow = Int.MaxValue
            var minCol = Int.MaxValue
            var maxRow = Int.MinValue
            var maxCol = Int.MinValue

            for (row <- startRow to endRow) {
                for (col <- startCol to endCol) {
                    val newRow = rotationDirection match {
                        case Counterclockwise => pivotRow - (col - pivotCol)
                        case Clockwise => pivotRow + (col - pivotCol)
                        case _ => row
                    }
                    val newCol = rotationDirection match {
                        case Counterclockwise => pivotCol + (row - pivotRow)
                        case Clockwise => pivotCol - (row - pivotRow)
                        case _ => col
                    }

                    minRow = Math.min(minRow, newRow)
                    minCol = Math.min(minCol, newCol)
                    maxRow = Math.max(maxRow, newRow)
                    maxCol = Math.max(maxCol, newCol)
                }
            }

            new Sector(new Point(minRow, minCol), new Point(maxRow, maxCol))
        }
    }

    override def toString: String = name
}

object Isometry {
    var allIsometries: Array[Isometry] = Array[Isometry]()

    private val AxialRowReflection = new Isometry("Axial Row", NonExpanding, NonTransparent, AxialReflection, Row)
    private val AxialColumnReflection = new Isometry("Axial Column", NonExpanding, NonTransparent, AxialReflection, Column)
    private val AxialDiagonalReflection = new Isometry("Axial Diagonal", NonExpanding, NonTransparent, AxialReflection, Diagonal)
    private val AxialAntidiagonalReflection = new Isometry("Axial Antidiagonal", NonExpanding, NonTransparent, AxialReflection, Antidiagonal)

    private val RotationClockwise = new Isometry("Rotation Clockwise", NonExpanding, NonTransparent, Rotation, Field, Clockwise)
    private val RotationCounterclockwise = new Isometry("Rotation Counterclockwise", NonExpanding, NonTransparent, Rotation, Field, Counterclockwise)

    allIsometries = Array(AxialRowReflection, AxialColumnReflection, AxialDiagonalReflection, AxialAntidiagonalReflection, RotationClockwise, RotationCounterclockwise)
}

sealed trait IsometryType
case object Rotation extends IsometryType
case object AxialReflection extends IsometryType

sealed trait IsometryExpansionType
case object Expanding extends IsometryExpansionType
case object NonExpanding extends IsometryExpansionType

sealed trait IsometryTransparencyType
case object Transparent extends IsometryTransparencyType
case object NonTransparent extends IsometryTransparencyType

sealed trait IsometryDirection
case object Row extends IsometryDirection
case object Column extends IsometryDirection
case object Diagonal extends IsometryDirection
case object Antidiagonal extends IsometryDirection
case object Field extends IsometryDirection

sealed trait RotationDirection
case object Clockwise extends RotationDirection
case object Counterclockwise extends RotationDirection
case object Unused extends RotationDirection

class Sector(pointOne: Point, pointTwo: Point) {
    private val topLeftRow = Math.min(pointOne.row, pointTwo.row)
    private val topLeftCol = Math.min(pointOne.col, pointTwo.col)
    private val bottomRightRow = Math.max(pointOne.row, pointTwo.row)
    private val bottomRightCol = Math.max(pointOne.col, pointTwo.col)

    val topLeftPoint: Point = new Point(topLeftRow, topLeftCol)
    val bottomRightPoint: Point = new Point(bottomRightRow, bottomRightCol)
}
class Point(val row: Int, val col: Int) {}