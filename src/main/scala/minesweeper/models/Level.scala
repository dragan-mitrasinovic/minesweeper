package minesweeper.models

import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import scala.io.Source

class Level(fileName: String) {
    private val source = Source.fromFile(fileName)
    private var tempFields = source.getLines().map {
        line =>
            line.map {
                case '#' => -1
                case '-' => 0
            }.toArray
    }.toArray

    source.close()

    tempFields = this.tempFields.zipWithIndex.map {
        case (row, i) =>
            row.zipWithIndex.map {
                case (field, j) =>
                    if (field == 0) surroundingMines(this.tempFields, i, j) else field
            }
    }

    private val fieldNumber = tempFields.length * tempFields.length
    private val mineNumber = tempFields.map(_.count(_ == -1)).sum
    
    var fields: Array[Array[Int]] = tempFields
    var difficulty: Difficulty =
        if (fieldNumber <= 100 && mineNumber <= 10) Easy
        else if (fieldNumber <= 300 && mineNumber <= 50) Medium
        else Hard
    var name: String = fileName.split("[\\\\/]").last.split('.').head

    // Functions
    private def surroundingMines(matrix: Array[Array[Int]], i: Int, j: Int): Int = {
        val directions = List((-1, -1), (-1, 0), (-1, 1), (0, -1), (0, 1), (1, -1), (1, 0), (1, 1))
        directions.count { case (x, y) =>
            val newX = i + x
            val newY = j + y
            newX >= 0 && newY >= 0 && newX < matrix.length && newY < matrix(newX).length && matrix(newX)(newY) == -1
        }
    }

    override def toString: String = this.name
}

object Level {
    var allLevels: Array[Level] = Array()
    var allScores: Array[Score] = Array()
    loadLevels()
    loadScores()

    // Functions
    private def loadLevels(): Unit = {
        val levelDir = Paths.get("gameData", "levels")
        val levelFiles = Files.list(levelDir)

        levelFiles.forEach { levelPath =>
            allLevels :+= new Level(levelPath.toString)
        }
    }

    private def loadScores(): Unit = {
        val source = Source.fromFile(Paths.get("gameData", "scores.txt").toString)

        for (line <- source.getLines()) {
            val Array(levelName, playerName, timeInSeconds, moves) = line.split(",")
            allScores :+= new Score(levelName, playerName, timeInSeconds.toInt, moves.toInt)
        }

        source.close()
    }

    def saveScores(): Unit = {
        val pw = new java.io.PrintWriter(Paths.get("gameData", "scores.txt").toFile)
        allScores.foreach { score =>
            pw.write(s"${score.levelName},${score.playerName},${score.time},${score.moves}\n")
        }
        pw.close()
    }

    def saveLevels(): Unit = {
        allLevels.foreach { level =>
            val pw = new PrintWriter(Paths.get("gameData", "levels", s"${level.name}.txt").toFile)
            level.fields.foreach { row =>
                pw.write(row.map {
                    case -1 => '#'
                    case n => '-'
                }.mkString)
                pw.write("\n")
            }
            pw.close()
        }
    }
}

class Score(val levelName: String, val playerName: String, val time: Int, val moves: Int)

sealed trait Difficulty
case object Easy extends Difficulty
case object Medium extends Difficulty
case object Hard extends Difficulty
