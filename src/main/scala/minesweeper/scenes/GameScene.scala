package minesweeper.scenes

import minesweeper.Minesweeper
import minesweeper.Minesweeper.stage
import minesweeper.models.{Level, Score}
import play.api.libs.json.{Json, OFormat}
import scalafx.animation.{KeyFrame, Timeline}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, Label, TextInputDialog}
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.{BorderPane, GridPane, HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.util.Duration

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.io.Source
import scala.util.Random

class GameScene(level: Level) extends Scene {
    private val timerLabel = new Label("Time: 00:00") {
        style = "-fx-font-size: 20pt;"
        alignmentInParent = Pos.Center
        minWidth = 150
        minHeight = 50
    }

    private val movesLabel = new Label("Moves: 0") {
        style = "-fx-font-size: 20pt;"
        alignmentInParent = Pos.Center
        minWidth = 150
        minHeight = 50
    }

    private val timer = new Timeline {
        cycleCount = Timeline.Indefinite
        keyFrames = Seq(
            KeyFrame(Duration(1000), onFinished = _ => {
                elapsedTime += 1
                val minutes = elapsedTime / 60
                val seconds = elapsedTime % 60
                timerLabel.text = f"Time: $minutes%02d:$seconds%02d"
            })
        )
    }

    private val levelTitle = new Label() {
        style = "-fx-font-size: 24pt;"
        alignmentInParent = Pos.Center
    }

    private val gameBoard = new GridPane {
        alignment = Pos.Center
        gridLinesVisible = true
    }

    private val endMessageLabel = new Label {
        style = "-fx-font-size: 20pt;"
        alignmentInParent = Pos.Center
        visible = false
    }

    private val restartButton = new Button("Restart") {
        minWidth = 200
        minHeight = 50
        alignmentInParent = Pos.Center
        visible = false
    }

    private val mainMenuButton = new Button("Main Menu") {
        minWidth = 200
        minHeight = 50
        alignmentInParent = Pos.Center
        visible = false
    }

    private val endGamePane = new HBox(endMessageLabel, restartButton, mainMenuButton) {
        spacing = 40
        alignment = Pos.Center
        visible = false
    }

    private val helpButton = new Button("Help") {
        minWidth = 100
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val saveButton = new Button("Save") {
        minWidth = 100
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val loadMoveButton = new Button("Load Moves") {
        minWidth = 100
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val quitButton = new Button("Quit") {
        minWidth = 100
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val saveHelpLoadBox = new VBox(saveButton, helpButton, loadMoveButton, quitButton) {
        spacing = 20
        alignment = Pos.Center
    }

    private val leftSpacer = new HBox {
        minWidth = 100
    }

    private val borderPane = new BorderPane {
        top = new BorderPane {
            left = timerLabel
            center = levelTitle
            right = movesLabel
        }
        center = gameBoard
        bottom = endGamePane
        right = saveHelpLoadBox
        left = leftSpacer
        padding = Insets(20, 40, 40, 40)
    }

    root = borderPane

    // State
    private var elapsedTime: Int = 0
    private var moves: Int = 0
    private val fields: Array[Array[Field]] = level.fields.map(_.map(mines => new Field(mines)))
    private val fieldButtons: Array[Array[Button]] = Array.ofDim[Button](fields.length, fields(0).length)
    private var gameEnded: Boolean = false

    // Initialization
    levelTitle.text = level.name
    populateBoard()

    // Functions
    private def revealField(row: Int, col: Int): Unit = {
        if (row < 0 || col < 0 || row >= fields.length || col >= fields(row).length || fields(row)(col).isRevealed || fields(row)(col).isMine) {
            return
        }

        updateFieldRevealed(row, col)

        if (fields(row)(col).surroundingMines == 0) {
            revealField(row - 1, col - 1)
            revealField(row - 1, col)
            revealField(row - 1, col + 1)
            revealField(row, col - 1)
            revealField(row, col + 1)
            revealField(row + 1, col - 1)
            revealField(row + 1, col)
            revealField(row + 1, col + 1)
        }
    }

    private def updateFieldButtonStyle(row: Int, col: Int): Unit = {
        fieldButtons(row)(col).text = fields(row)(col).getText
        fieldButtons(row)(col).style = fields(row)(col).getStyle
        fieldButtons(row)(col).effect = fields(row)(col).getEffect
        fieldButtons(row)(col).disable = fields(row)(col).isRevealed
    }

    private def updateFieldRevealed(row: Int, col: Int): Unit = {
        fields(row)(col).isRevealed = true
        fields(row)(col).isHelped = false

        updateFieldButtonStyle(row, col)
    }

    private def updateFieldHelped(row: Int, col: Int): Unit = {
        fields(row)(col).isHelped = true

        updateFieldButtonStyle(row, col)
    }

    private def revealAllMines(): Unit = {
        fields.foreach(row => row.foreach(field => {
            if (field.isMine) {
                updateFieldRevealed(fields.indexOf(row), row.indexOf(field))
            }
        }
        ))
    }

    private def help(): Unit = {
        val unrevealedNonMineFields = fields.flatten.filter(field => !field.isRevealed && !field.isMine)

        if (unrevealedNonMineFields.isEmpty) {
            return
        }

        val randomField = unrevealedNonMineFields(Random.nextInt(unrevealedNonMineFields.length))
        updateFieldHelped(fields.indexOf(fields.find(row => row.contains(randomField)).get), fields.find(row => row.contains(randomField)).get.indexOf(randomField))
        moves += 2
    }

    private def save(): Unit = {
        val fieldsData = fields.map(_.map(field => FieldData(field.surroundingMines, field.isMine, field.isFlagged, field.isRevealed, field.isHelped)).toList).toList

        val saveData = SaveData(level.name, elapsedTime, moves, fieldsData)

        val json = Json.toJson(saveData).toString()

        val fileChooser = new FileChooser {
            title = "Save Game"
            extensionFilters.addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
            )
        }

        val file = fileChooser.showSaveDialog(Minesweeper.stage)
        if (file == null) {
            return
        }

        Files.write(file.toPath, json.getBytes(StandardCharsets.UTF_8))

        new Alert(AlertType.Information) {
            initOwner(Minesweeper.stage)
            title = "Save Game"
            headerText = "Level saved successfully"
            contentText = s"Your game progress has been saved to ${file.getName}."
        }.showAndWait()
    }

    private def loadMoves(): Unit = {
        val fileChooser = new FileChooser {
            title = "Open Moves File"
            extensionFilters.add(new ExtensionFilter("Text Files", "*.txt"))
        }

        val file = fileChooser.showOpenDialog(stage)
        if (file == null) {
            return
        }

        val source = Source.fromFile(file.getPath)
        val lines = source.getLines().toList
        source.close()

        for (line <- lines) {
            val clickType = line.charAt(0)
            val coordinates = line.substring(2, line.length - 1).split(",").map(_.trim.toInt - 1)
            val row = coordinates(0)
            val col = coordinates(1)

            clickType match {
                case 'L' => handleLeftClick(row, col)
                case 'R' => handleRightClick(row, col)
            }
        }

    }

    private def endGame(won: Boolean): Unit = {
        revealAllMines()
        gameEnded = true
        timer.stop()
        endMessageLabel.text = if (won) "You Won!" else "Game Over"

        // Show end game pane
        endGamePane.visible = true
        endMessageLabel.visible = true
        restartButton.visible = true
        mainMenuButton.visible = true

        // Disable buttons
        saveButton.disable = true
        helpButton.disable = true
        loadMoveButton.disable = true
        quitButton.disable = true

        if (!won) {
            return
        }

        // Show dialog to enter name
        val dialog = new TextInputDialog() {
            title = "Congratulations!"
            headerText = "Enter your name:"
        }

        val result: Option[String] = dialog.showAndWait()

        result match {
            case Some(name) => Level.allScores :+= new Score(level.name, name, elapsedTime, moves)
            case None => Level.allScores :+= new Score(level.name, "Unknown Warrior", elapsedTime, moves)
        }

    }

    private def checkWin(): Unit = {
        if (fields.flatten.count(field => field.isRevealed || field.isMine) == fields.length * fields(0).length) {
            endGame(true)
        }
    }

    private def handleLeftClick(row: Int, col: Int): Unit = {
        if (gameEnded || fields(row)(col).isFlagged) return

        if (elapsedTime == 0) {
            timer.play()
        }

        if (fields(row)(col).isMine) {
            endGame(false)
        } else {
            revealField(row, col)
            moves += 1
            movesLabel.text = s"Moves: $moves"
            checkWin()
        }
    }

    private def handleRightClick(row: Int, col: Int): Unit = {
        if (gameEnded) {
            return
        }

        fields(row)(col).isFlagged = !fields(row)(col).isFlagged
        updateFieldButtonStyle(row, col)
    }

    private def populateBoard(): Unit = {
        gameBoard.children.clear()
        for (row <- fields.indices) {
            for (col <- fields(row).indices) {
                val button = new Button {
                    minWidth = 30
                    minHeight = 30
                    style = "-fx-background-radius: 0; -fx-font-weight: bold; -fx-font-size: 9pt;"
                    effect = new DropShadow {
                        color = Color.Gray
                        spread = 0.3
                    }
                    onAction = _ => {
                        handleLeftClick(row, col)
                    }
                    onContextMenuRequested = event => {
                        handleRightClick(row, col)
                    }
                }

                fieldButtons(row)(col) = button
                gameBoard.add(button, col, row)
            }
        }
    }

    def updateState(fields: List[List[FieldData]], time: Int, moves: Int): Unit = {
        for (row <- fields.indices) {
            for (col <- fields(row).indices) {
                this.fields(row)(col).surroundingMines = fields(row)(col).surroundingMines
                this.fields(row)(col).isMine = fields(row)(col).isMine
                this.fields(row)(col).isFlagged = fields(row)(col).isFlagged
                this.fields(row)(col).isRevealed = fields(row)(col).isRevealed
                this.fields(row)(col).isHelped = fields(row)(col).isHelped
                updateFieldButtonStyle(row, col)
            }
        }

        elapsedTime = time
        this.moves = moves
        timerLabel.text = f"Time: ${elapsedTime / 60}%02d:${elapsedTime % 60}%02d"
        timer.play()
        movesLabel.text = s"Moves: $moves"
    }

    // Behavior
    restartButton.onAction = _ => {
        Minesweeper.stage.scene = new GameScene(level)
    }

    mainMenuButton.onAction = _ => {
        Minesweeper.stage.scene = StartScene
    }

    helpButton.onAction = _ => {
        help()
    }

    saveButton.onAction = _ => {
        save()
    }

    loadMoveButton.onAction = _ => {
        loadMoves()
    }

    quitButton.onAction = _ => {
        Minesweeper.stage.scene = StartScene
    }
}


class Field(var surroundingMines: Int) {
    var isMine: Boolean = surroundingMines == -1
    var isFlagged: Boolean = false
    var isRevealed: Boolean = false
    var isHelped: Boolean = false

    def getText: String = {
        if (isRevealed) {
            if (isMine) {
                "💣"
            } else {
                if (surroundingMines == 0) "" else surroundingMines.toString
            }
        } else if (isFlagged) {
            "🚩"
        } else {
            ""
        }
    }

    def getEffect: DropShadow = {
        if (isRevealed) {
            null
        } else {
            new DropShadow {
                color = Color.Gray
                spread = 0.3
            }
        }
    }

    def getStyle: String = {
        var style: String = "-fx-background-radius: 0; -fx-font-weight: bold; -fx-font-size: 9pt;"
        if (isHelped) style += "-fx-background-color: lightblue;"
        if (!isRevealed) {
            style
        } else {
            surroundingMines match
                case 1 => style + " -fx-text-fill: blue;"
                case 2 => style + " -fx-text-fill: green;"
                case 3 => style + " -fx-text-fill: red;"
                case 4 => style + " -fx-text-fill: darkblue;"
                case 5 => style + " -fx-text-fill: brown;"
                case 6 => style + " -fx-text-fill: cyan;"
                case 7 => style + " -fx-text-fill: black;"
                case 8 => style + " -fx-text-fill: grey;"
                case _ => style
        }
    }
}

// Saving to JSON
case class FieldData(surroundingMines: Int, isMine: Boolean, isFlagged: Boolean, isRevealed: Boolean, isHelped: Boolean)

object FieldData {
    implicit val format: OFormat[FieldData] = Json.format[FieldData]
}

case class SaveData(levelName: String, elapsedTime: Int, moves: Int, fields: List[List[FieldData]])

object SaveData {
    implicit val format: OFormat[SaveData] = Json.format[SaveData]
}
