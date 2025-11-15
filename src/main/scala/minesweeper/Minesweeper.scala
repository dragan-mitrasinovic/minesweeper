package minesweeper

import minesweeper.Minesweeper.stage
import minesweeper.models.Level
import minesweeper.scenes.StartScene
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.image.Image

object Minesweeper extends JFXApp3 {

    override def start(): Unit = {

        Runtime.getRuntime.addShutdownHook(new Thread {
            override def run(): Unit = {
                Level.saveScores()
                Level.saveLevels()
            }
        })

        stage = new PrimaryStage {
            title = "Minesweeper"
            width = 1200
            height = 800
            resizable = false
            scene = StartScene
            // Add icon if resource is available
            val iconStream = getClass.getResourceAsStream("/mine.png")
            if (iconStream != null) {
                icons += new Image(iconStream)
            }
        }
    }
}