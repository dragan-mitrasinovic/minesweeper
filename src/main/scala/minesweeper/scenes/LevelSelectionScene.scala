package minesweeper.scenes

import minesweeper.*
import minesweeper.models.*
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, Separator}
import scalafx.scene.layout.{BorderPane, FlowPane, HBox, VBox}
import scalafx.scene.{Node, Scene}

import scala.util.Random

class LevelSelectionScene(val editMode: Boolean) extends Scene {
    private val selectDifficultyLabel = new Label("Select Difficulty") {
        style = "-fx-font-size: 24pt"
    }

    private val backButton = new Button("⬅️") {
        minWidth = 50
        minHeight = 30
    }

    private val spacer = new HBox {
        minWidth = 50
        minHeight = 30
    }

    private val topPane = new BorderPane {
        left = backButton
        center = selectDifficultyLabel
        right = spacer
        padding = Insets(20, 20, 0, 20)
        BorderPane.setAlignment(backButton, Pos.Center)
    }

    private val easyButton = new Button("Easy") {
        minWidth = 200
        minHeight = 50
    }

    private val mediumButton = new Button("Medium") {
        minWidth = 200
        minHeight = 50
    }

    private val hardButton = new Button("Hard") {
        minWidth = 200
        minHeight = 50
    }

    private val buttonBox = new HBox(easyButton, mediumButton, hardButton) {
        spacing = 55
        alignment = Pos.Center
    }

    private val levelButtonsPane = new FlowPane {
        hgap = 30
        vgap = 30
        padding = Insets(0, 30, 30, 30)
        minHeight = 100
    }

    private val mainPane = new VBox(topPane, buttonBox, new Separator(), levelButtonsPane) {
        spacing = 30
        alignment = Pos.Center
        padding = Insets(-350, 0, 0, 0)
    }

    private val confirmButton = new Button("Confirm") {
        minWidth = 200
        minHeight = 50
        alignmentInParent = Pos.Center
        disable = true
    }

    private val levelLabel = new Label("Select a level") {
        style = "-fx-font-size: 20pt"
        alignmentInParent = Pos.Center
        minWidth = 150
    }

    private val confirmHBox = new HBox(levelLabel, confirmButton) {
        spacing = 30
        alignment = Pos.Center
    }

    private val bottomBox = new VBox(new Separator(), confirmHBox) {
        spacing = 30
        alignment = Pos.Center
    }

    private val borderPane = new BorderPane {
        center = mainPane
        bottom = bottomBox
        padding = Insets(0, 0, 20, 0)
    }

    root = borderPane
    
    // State
    private var selectedLevel: Option[Level] = None
    private var levelButtons = Array[Button]()

    // Functions
    private def updateLevels(difficulty: Difficulty): Unit = {
        val levels = Level.allLevels.filter(_.difficulty == difficulty)
        levelButtons = Array()
        levels.foreach { level =>
            val levelButton = new Button(level.name) {
                minWidth = 80
                minHeight = 35
                style = if level == selectedLevel.orNull then "-fx-background-color: lightblue" else ""
                onAction = _ => {
                    handleLevelSelected(level, this)
                }
            }

            levelButtons :+= levelButton
        }

        val randomLevelButton = new Button("random") {
            minWidth = 80
            minHeight = 35
            onAction = _ => {
                val randomLevelIndex = Random.nextInt(levels.length)
                val levelButton = levelButtons(randomLevelIndex)
                handleLevelSelected(levels(randomLevelIndex), levelButton)
            }
        }

        levelButtons :+= randomLevelButton
        levelButtonsPane.children = levelButtons
    }

    private def handleConfirm(): Unit = {
        selectedLevel match {
            case Some(level) => 
                Minesweeper.stage.scene = if editMode then new EditScene(level) else new GameScene(level)
            case None => levelLabel.text = "Please select a level"
        }
    }

    private def handleLevelSelected(level: Level, levelButton: Button): Unit = {
        confirmButton.disable = false
        selectedLevel = Some(level)
        levelLabel.text = level.name
        levelButtons.foreach { button =>
            button.style = ""
        }
        levelButton.style = "-fx-background-color: lightblue"
    }

    // Behavior
    backButton.onAction = _ => {
        Minesweeper.stage.scene = StartScene
    }

    easyButton.onAction = _ => {
        updateLevels(Easy)
    }

    mediumButton.onAction = _ => {
        updateLevels(Medium)
    }

    hardButton.onAction = _ => {
        updateLevels(Hard)
    }

    confirmButton.onAction = _ => {
        handleConfirm()
    }
}