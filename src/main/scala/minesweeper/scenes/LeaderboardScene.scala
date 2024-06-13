package minesweeper.scenes

import minesweeper.Minesweeper
import minesweeper.models.Level
import scalafx.Includes.*
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, TableColumn, TableView}
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.{Node, Scene}

object LeaderboardScene extends Scene {
    private val levelNameLabel = new Label("") {
        style = "-fx-font-size: 24pt"
        alignmentInParent = Pos.Center
    }

    private val backButton = new Button("⬅️") {
        minWidth = 50
        minHeight = 30
    }

    private val spacer = new HBox {
        minWidth = 50
        minHeight = 30
    }

    private val leftButton = new Button("<") {
        style = "-fx-font-size: 16pt"
    }

    private val rightButton = new Button(">") {
        style = "-fx-font-size: 16pt"
    }

    private val nameColumn = new TableColumn[TableScore, String] {
        text = "Name"
        cellValueFactory = {
            _.value.playerName
        }
    }

    private val timeColumn = new TableColumn[TableScore, String] {
        text = "Time"
        cellValueFactory = { cellDataFeatures =>
            val time = cellDataFeatures.value.time.value
            val minutes = time / 60
            val seconds = time % 60
            new StringProperty(this, "Time", f"$minutes%02d:$seconds%02d")
        }
    }

    private val movesColumn = new TableColumn[TableScore, Int] {
        text = "Moves"
        cellValueFactory = {
            _.value.moves
        }
    }

    private val tableView = new TableView[TableScore]() {
        maxWidth = 800
        minWidth = 800
        alignmentInParent = Pos.Center
        columns ++= List(nameColumn, timeColumn, movesColumn)
    }

    private val tableViewBox = new HBox(tableView) {
        alignment = Pos.Center
    }

    private val navigationBox = new HBox(leftButton, levelNameLabel, rightButton) {
        alignment = Pos.Center
        spacing = 30
    }

    private val topBorderPane = new BorderPane() {
        left = backButton
        center = navigationBox
        right = spacer
        padding = Insets(20)
        BorderPane.setAlignment(backButton, Pos.Center)
    }

    private val rootBox = new VBox(topBorderPane, tableViewBox) {
        spacing = 50
    }

    root = rootBox

    // State
    private var levelIndex = 0
    private val observableBuffer = ObservableBuffer[TableScore]()

    // Initialization
    tableView.items = observableBuffer
    updateScores()
    nameColumn.prefWidth <== tableView.width / 3
    timeColumn.prefWidth <== tableView.width / 3
    movesColumn.prefWidth <== tableView.width / 3

    // Functions
    private def updateScores(): Unit = {
        observableBuffer.clear()
        val sortedScores = Level.allScores
          .filter(score => score.levelName == Level.allLevels(levelIndex).name)
          .sortWith((score1, score2) =>
              if (score1.time != score2.time) score1.time < score2.time
              else score1.moves < score2.moves
          )

        observableBuffer.addAll(
            sortedScores.map(score => new TableScore(score.playerName, score.time, score.moves))
        )

        levelNameLabel.text = Level.allLevels(levelIndex).name
    }

    // Behavior
    backButton.onAction = _ => {
        Minesweeper.stage.scene = StartScene
    }

    leftButton.onAction = _ => {
        if (levelIndex > 0) {
            levelIndex -= 1
            updateScores()
        }
    }

    rightButton.onAction = _ => {
        if (levelIndex < Level.allLevels.length - 1) {
            levelIndex += 1
            updateScores()
        }
    }
}

class TableScore(playerNameInput: String, timeInput: Int, movesInput: Int) {
    val playerName = new StringProperty(this, "playerName", playerNameInput)
    val time = new ObjectProperty(this, "time", timeInput)
    val moves = new ObjectProperty(this, "moves", movesInput)
}
