package minesweeper.isometrics

import minesweeper.scenes.EditField

abstract class Isometry(val name: String) {
    override def toString: String = name

    def applyInChain(sector: Sector, isometryArgs: List[IsometryArgs]): Sector

    def getChain: List[Isometry]

    def applyIsometry(sector: Sector, board: Array[Array[EditField]], isometryArgs: List[IsometryArgs], expanding: IsometryExpansionType, transparencyType: IsometryTransparencyType): (Sector, Array[Array[EditField]]) = {
        var inputBoard: Array[Array[EditField]] = board
        var inputSector: Sector = sector

        if (expanding == Expanding) {
            val finalSector: Sector = applyInChain(sector, isometryArgs)
            val expanded = expandBoard(finalSector, board, inputSector)
            inputBoard = expanded._1
            inputSector = expanded._2
        }

        val finalSector: Sector = applyInChain(inputSector, isometryArgs)
        val newBoard = mapToFinalBoard(inputSector, finalSector, inputBoard, transparencyType)

        val adjustedSector = cutOffSector(finalSector, newBoard)

        (adjustedSector, newBoard)
    }

    private def expandBoard(sector: Sector, board: Array[Array[EditField]], inputSector: Sector): (Array[Array[EditField]], Sector) = {
        var newBoard = board
        var newSector = inputSector

        if (sector.topLeftPoint.row < 0) {
            val rowsToAdd = Math.abs(sector.topLeftPoint.row)
            val addedRows = Array.fill(rowsToAdd, board(0).length)(new EditField(false))
            newBoard = addedRows ++ board
            newSector = new Sector(new Point(newSector.topLeftPoint.row + rowsToAdd, newSector.topLeftPoint.col), newSector.bottomRightPoint)
        }

        if (sector.bottomRightPoint.row >= board.length) {
            val rowsToAdd = sector.bottomRightPoint.row - board.length + 1
            val addedRows = Array.fill(rowsToAdd, board(0).length)(new EditField(false))
            newBoard = newBoard ++ addedRows
        }

        if (sector.topLeftPoint.col < 0) {
            val colsToAdd = Math.abs(sector.topLeftPoint.col)
            val addedCols = Array.fill(newBoard.length, colsToAdd)(new EditField(false))
            newBoard = for ((row, idx) <- newBoard.zipWithIndex) yield addedCols(idx) ++ row
            newSector = new Sector(new Point(newSector.topLeftPoint.row, newSector.topLeftPoint.col + colsToAdd), newSector.bottomRightPoint)
        }

        if (sector.bottomRightPoint.col >= board(0).length) {
            val colsToAdd = sector.bottomRightPoint.col - board(0).length + 1
            val addedCols = Array.fill(newBoard.length, colsToAdd)(new EditField(false))
            newBoard = for ((row, idx) <- newBoard.zipWithIndex) yield row ++ addedCols(idx)
        }

        (newBoard, newSector)
    }

    private def cutOffSector(sector: Sector, board: Array[Array[EditField]]): Sector = {
        val startRow = sector.topLeftPoint.row
        val startCol = sector.topLeftPoint.col
        val endRow = sector.bottomRightPoint.row
        val endCol = sector.bottomRightPoint.col

        val newTopLeftRow = Math.max(0, startRow)
        val newTopLeftCol = Math.max(0, startCol)
        val newBottomRightRow = Math.min(board.length - 1, endRow)
        val newBottomRightCol = Math.min(board(0).length - 1, endCol)

        if (newTopLeftRow >= board.length || newTopLeftCol >= board(0).length || newBottomRightRow < 0 || newBottomRightCol < 0) {
            return null
        }

        new Sector(new Point(newTopLeftRow, newTopLeftCol), new Point(newBottomRightRow, newBottomRightCol))
    }

    private def mapToFinalBoard(inputSector: Sector, finalSector: Sector, inputBoard: Array[Array[EditField]], transparencyType: IsometryTransparencyType): Array[Array[EditField]] = {
        val newBoard = Array.fill(inputBoard.length, inputBoard(0).length)(new EditField(false))

        for (i <- inputSector.indices.indices; j <- inputSector.indices(0).indices) {
            val (oldRow, oldCol) = inputSector.indices(i)(j)
            val (newRow, newCol) = finalSector.indices(i)(j)

            if (newRow >= 0 && newRow < newBoard.length && newCol >= 0 && newCol < newBoard(0).length) {
                if (transparencyType == NonTransparent) {
                    newBoard(newRow)(newCol).isMine = inputBoard(oldRow)(oldCol).isMine
                    newBoard(newRow)(newCol).isSector = true
                } else {
                    newBoard(newRow)(newCol).isMine = inputBoard(oldRow)(oldCol).isMine || inputBoard(newRow)(newCol).isMine
                    newBoard(newRow)(newCol).isSector = true

                }
            }
        }

        newBoard
    }
}

object Isometry {
    private val rotation = Rotation("Rotation")
    private val axialReflection = Reflection("Axial Reflection")
    private val centralReflection = CentralSymmetry("Central Symmetry")
    var allIsometries: Array[Isometry] = Array[Isometry](rotation, axialReflection, centralReflection)
}

class IsometryArgs(val direction: IsometryDirection, val rotationDirection: RotationDirection, val pivotPoint: Point, val inverted: Boolean) {}

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

trait Expanding extends Isometry {
    
}

class Sector(pointOne: Point, pointTwo: Point) {
    private val topLeftRow = Math.min(pointOne.row, pointTwo.row)
    private val topLeftCol = Math.min(pointOne.col, pointTwo.col)
    private val bottomRightRow = Math.max(pointOne.row, pointTwo.row)
    private val bottomRightCol = Math.max(pointOne.col, pointTwo.col)

    val topLeftPoint: Point = new Point(topLeftRow, topLeftCol)
    val bottomRightPoint: Point = new Point(bottomRightRow, bottomRightCol)

    var indices: Array[Array[(Int, Int)]] = Array.tabulate(bottomRightRow - topLeftRow + 1, bottomRightCol - topLeftCol + 1) {
        (i, j) => (topLeftRow + i, topLeftCol + j)
    }
}

class Point(val row: Int, val col: Int) {}