package minesweeper.scenes

import minesweeper.Minesweeper.stage
import minesweeper.models.Level
import play.api.libs.json.Json
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.nio.charset.StandardCharsets
import java.nio.file.Files

object StartScene extends Scene {
    private val welcomeText = new Label("Welcome to Minesweeper!") {
        style = "-fx-font-size: 24pt"
        alignmentInParent = Pos.Center
        padding = Insets(75, 0, 0, 0)
    }

    private val newGameButton = new Button("New Game") {
        minWidth = 200
        minHeight = 50
    }

    private val resumeGameButton = new Button("Resume Game") {
        minWidth = 200
        minHeight = 50
    }

    private val loadLevelButton = new Button("Load Level") {
        minWidth = 200
        minHeight = 50
    }

    private val leaderboardButton = new Button("Leaderboard") {
        minWidth = 200
        minHeight = 50
    }

    private val editLevelButton = new Button("Edit Level") {
        minWidth = 200
        minHeight = 50
    }

    private val buttonBox = new VBox(newGameButton, resumeGameButton, loadLevelButton, editLevelButton, leaderboardButton) {
        spacing = 25
        alignment = Pos.Center
    }

    private val topLevelPane = new BorderPane {
        top = welcomeText
        center = buttonBox
    }

    root = topLevelPane
    fill = Color.LightGray

    // Functions
    private def loadLevel(): Unit = {
        val fileChooser = new FileChooser {
            title = "Open Level File"
            extensionFilters.add(new ExtensionFilter("Text Files", "*.txt"))
        }

        val file = fileChooser.showOpenDialog(stage)
        if (file != null) {
            val newLevel = new Level(file.getAbsolutePath)
            Level.allLevels :+= newLevel

            val alert = new Alert(Alert.AlertType.Information) {
                initOwner(stage)
                title = "Success"
                headerText = "Level loaded successfully!"
                contentText = s"Successfully loaded level ${newLevel.name}"
            }.showAndWait()
        }
    }

    private def resumeGame(): Unit = {
        val fileChooser = new FileChooser {
            title = "Open Saved Game File"
            extensionFilters.add(new ExtensionFilter("JSON Files", "*.json"))
        }

        val file = fileChooser.showOpenDialog(stage)
        if (file != null) {
            val json = new String(Files.readAllBytes(file.toPath), StandardCharsets.UTF_8)
            val saveData = Json.parse(json).as[SaveData]

            val gameScene = new GameScene(Level.allLevels.find(_.name == saveData.levelName).get)
            gameScene.updateState(saveData.fields, saveData.elapsedTime, saveData.moves)

            stage.scene = gameScene
        }
    }

    // Behavior
    newGameButton.onAction = _ => {
        stage.scene = new LevelSelectionScene(false)
    }

    resumeGameButton.onAction = _ => {
        resumeGame()
    }

    loadLevelButton.onAction = _ => {
        loadLevel()
    }

    leaderboardButton.onAction = _ => {
        stage.scene = LeaderboardScene
    }

    editLevelButton.onAction = _ => {
        stage.scene = new LevelSelectionScene(true)
    }
}
