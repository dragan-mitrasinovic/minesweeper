package minesweeper.scenes

import minesweeper.*
import minesweeper.isometrics.*
import minesweeper.models.{Easy, Hard, Level, Medium}
import scalafx.Includes.*
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.layout.{BorderPane, GridPane, HBox, VBox}

import java.io.PrintWriter
import scala.language.postfixOps

class EditScene(level: Level) extends Scene {
    private val levelTitle = new Label() {
        style = "-fx-font-size: 24pt;"
        alignmentInParent = Pos.Center
    }

    private val levelDifficulty = new Label() {
        style = "-fx-font-size: 24pt;"
        alignmentInParent = Pos.Center
    }

    private val topBox = new HBox(levelTitle, levelDifficulty) {
        spacing = 50
        alignment = Pos.Center
    }

    private val editBoard = new GridPane {
        alignment = Pos.Center
        gridLinesVisible = true
    }

    private val saveButton = new Button("Save") {
        minWidth = 100
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val quitButton = new Button("Quit") {
        minWidth = 100
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val saveQuitBox = new VBox(saveButton, quitButton) {
        spacing = 20
        alignment = Pos.Center
    }

    private val leftSpacer = new HBox {
        minWidth = 100
    }

    private val addButton = new Button("Add") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val removeButton = new Button("Remove") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val boardUpButton = new Button("⬆") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val boardDownButton = new Button("⬇") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val boardLeftButton = new Button("⬅") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val boardRightButton = new Button("➡") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val boardCancelButton: Button = new Button("Cancel") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val selectModeButton: Button = new Button("Select Mode") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val selectModeClearButton = new Button("Clear") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val selectModeCancelButton = new Button("Cancel") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val isometricsButton = new Button("Isometrics") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val chooseIsometry = new ComboBox[Isometry] {
        items = ObservableBuffer(Isometry.allIsometries: _*)
        promptText = "Choose Isometry"
    }

    private val chooseExpansion = new CheckBox("Expanding")

    private val chooseTransparency = new CheckBox("Transparent")

    private val invert = new CheckBox("Invert")

    private val chooseRotationDirection = new ComboBox[RotationDirection] {
        items = ObservableBuffer(Clockwise, Counterclockwise)
        promptText = "Choose Rotation Direction"
    }

    private val chooseIsometryDirection = new ComboBox[IsometryDirection] {
        items = ObservableBuffer(Row, Column, Diagonal, Antidiagonal)
        promptText = "Choose Isometry Direction"
    }

    private val chooseSection = new Button("Choose Section") {
        minWidth = 80
        minHeight = 30
        alignmentInParent = Pos.Center
    }

    private val chooseSectionDoneButton = new Button("Done") {
        minWidth = 80
        minHeight = 50
        alignmentInParent = Pos.Center
    }

    private val applyIsometryButton = new Button("Apply") {
        minWidth = 80
        minHeight = 30
        alignmentInParent = Pos.Center
    }

    private val composeButton = new Button("Compose") {
        minWidth = 80
        minHeight = 30
        alignmentInParent = Pos.Center
    }

    private val composeCancelButton = new Button("Cancel") {
        minWidth = 80
        minHeight = 30
        alignmentInParent = Pos.Center
    }

    private val compositionName = new TextField {
        promptText = "Composition Name"
    }

    private val composeChooseIsometry = new ComboBox[Isometry] {
        items = ObservableBuffer(Isometry.allIsometries: _*)
        promptText = "Choose Isometry"
        maxWidth = 130
    }

    private val composeChainBox = new HBox(composeChooseIsometry) {
        spacing = 20
        alignment = Pos.Center
    }

    private val composeConfirmButtom = new Button("Confirm") {
        minWidth = 80
        minHeight = 30
        alignmentInParent = Pos.Center
    }

    private val cancelIsometryButton = new Button("Cancel") {
        minWidth = 80
        minHeight = 30
        alignmentInParent = Pos.Center
    }

    private val cancelApplyingIsometry = new Button("Cancel") {
        minWidth = 80
        minHeight = 30
        alignmentInParent = Pos.Center
    }

    private val confirmIsometryButton = new Button("Confirm") {
        minWidth = 80
        minHeight = 30
        alignmentInParent = Pos.Center
    }

    private val nextIsometryButton = new Button("Next") {
        minWidth = 80
        minHeight = 30
        alignmentInParent = Pos.Center
    }

    private val bottomButtons: HBox = new HBox(addButton, removeButton, selectModeButton, isometricsButton) {
        spacing = 40
        minHeight = 100
        alignment = Pos.Center
    }

    private val errorLabel = new Label("") {
        style = "-fx-font-size: 16pt; -fx-text-fill: red;"
        alignmentInParent = Pos.Center
    }

    private val bottomPane = new VBox(errorLabel, bottomButtons) {
        spacing = 20
        alignment = Pos.Center
    }

    private val borderPane = new BorderPane {
        top = topBox
        center = editBoard
        right = saveQuitBox
        left = leftSpacer
        bottom = bottomPane
        padding = Insets(20, 40, 40, 40)
    }

    root = borderPane

    // State
    private var editFields: Array[Array[EditField]] = level.fields.map(_.map(mines => new EditField(mines == -1)))
    private var editFieldButtons: Array[Array[Button]] = Array.ofDim(editFields.length, editFields(0).length)
    private val minMines = level.difficulty match {
        case Easy => 0
        case Medium => 11
        case Hard => 51
    }
    private val maxMines = level.difficulty match {
        case Easy => 10
        case Medium => 50
        case Hard => Int.MaxValue
    }
    private val minSize = level.difficulty match {
        case Easy => 1
        case Medium => 101
        case Hard => 301
    }
    private val maxSize = level.difficulty match {
        case Easy => 100
        case Medium => 300
        case Hard => Int.MaxValue
    }
    private var selectMode = false
    private var addMode = false

    private var isometryDirection: IsometryDirection = Field
    private var applyingIsometry = false
    private var isometryPivot: Option[Point] = None
    private var chooseSectorMode = false
    private var sectorFirstPoint: Option[Point] = None
    private var sectorSecondPoint: Option[Point] = None

    private var isometryTypeList: Option[List[Isometry]] = None
    private var isometryArgsList: List[IsometryArgs] = List()

    private var composeChain: List[Isometry] = List()
    private var lastComposeComboBox: ComboBox[Isometry] = composeChooseIsometry

    // Initialization
    levelTitle.text = s"Base level: ${level.name}"
    levelDifficulty.text = s"Difficulty: ${level.difficulty}"
    populateBoard()

    // Functions
    private def save(): Unit = {
        if (editFields.flatten.count(_.isMine) < minMines || editFields.flatten.count(_.isMine) > maxMines) {
            errorLabel.text = s"Invalid number of mines! Must be between $minMines and $maxMines."
            return
        }

        if (editFields.length * editFields(0).length < minSize || editFields.length * editFields(0).length > maxSize) {
            errorLabel.text = s"Invalid number of fields! Must be between $minSize and $maxSize."
            return
        }

        val dialog = new TextInputDialog() {
            title = "Save Level"
            headerText = "Enter the name for the level:"
        }

        val result = dialog.showAndWait()

        result match {
            case Some(levelName) =>
                val pw = new PrintWriter(s"gameData/levels/$levelName.txt")
                for (row <- editFields.indices) {
                    for (col <- editFields(row).indices) {
                        pw.print(if editFields(row)(col).isMine then "#" else "-")
                    }
                    pw.println()
                }
                pw.close()
                Level.allLevels :+= new Level(s"gameData\\levels\\$levelName.txt")
            case None =>
        }
    }

    private def handleLeftClick(row: Int, col: Int): Unit = {
        if (chooseSectorMode) {
            if (sectorFirstPoint.isEmpty && sectorSecondPoint.isEmpty) {
                sectorFirstPoint = Some(Point(row, col))
                editFields(row)(col).isSector = true
                updateFieldButtonStyle(row, col)
            } else if (sectorFirstPoint.isDefined && sectorSecondPoint.isEmpty) {
                sectorSecondPoint = Some(Point(row, col))
                val topLeftRow = Math.min(sectorFirstPoint.get.row, sectorSecondPoint.get.row)
                val topLeftCol = Math.min(sectorFirstPoint.get.col, sectorSecondPoint.get.col)
                val bottomRightRow = Math.max(sectorFirstPoint.get.row, sectorSecondPoint.get.row)
                val bottomRightCol = Math.max(sectorFirstPoint.get.col, sectorSecondPoint.get.col)
                for (i <- topLeftRow to bottomRightRow) {
                    for (j <- topLeftCol to bottomRightCol) {
                        editFields(i)(j).isSector = true
                        updateFieldButtonStyle(i, j)
                    }
                }
            } else {
                sectorFirstPoint = Some(Point(row, col))
                sectorSecondPoint = None
                removeSectors()
                editFields(row)(col).isSector = true
                updateFieldButtonStyle(row, col)
            }
            return
        }

        if (selectMode) {
            editFields(row)(col).isSelected = !editFields(row)(col).isSelected
        } else {
            editFields(row)(col).isMine = !editFields(row)(col).isMine
            checkNumberOfMines()
        }

        updateFieldButtonStyle(row, col)
    }

    private def handleRightClick(row: Int, col: Int): Unit = {
        if (!applyingIsometry || isometryDirection == null) {
            return
        }

        isometryPivot = Some(Point(row, col))
        val sameDirectionSelected = editFields(row)(col).isSelected
        deselectAllFields()
        if (sameDirectionSelected) {
            return
        }

        isometryDirection match
            case Row =>
                for (i <- editFields(row).indices) {
                    editFields(row)(i).isSelected = true
                    updateFieldButtonStyle(row, i)
                }
            case Column =>
                for (i <- editFields.indices) {
                    editFields(i)(col).isSelected = true
                    updateFieldButtonStyle(i, col)
                }
            case Diagonal =>
                val sum = row + col
                for (i <- editFields.indices) {
                    for (j <- editFields(i).indices) {
                        if (i + j == sum) {
                            editFields(i)(j).isSelected = true
                            updateFieldButtonStyle(i, j)
                        }
                    }
                }
            case Antidiagonal =>
                val diff = row - col
                for (i <- editFields.indices) {
                    for (j <- editFields(i).indices) {
                        if (i - j == diff) {
                            editFields(i)(j).isSelected = true
                            updateFieldButtonStyle(i, j)
                        }
                    }
                }
            case Field =>
                editFields(row)(col).isSelected = true
                updateFieldButtonStyle(row, col)
    }

    private def clearAllSelectedFields(): Unit = {
        for (row <- editFields.indices) {
            for (col <- editFields(row).indices) {
                if (editFields(row)(col).isSelected) {
                    editFields(row)(col).isMine = false
                    editFields(row)(col).isSelected = false
                    updateFieldButtonStyle(row, col)
                }
            }
        }

        checkNumberOfMines()
    }

    private def checkNumberOfMines(): Unit = {
        val mines = editFields.flatten.count(_.isMine)
        if (mines < minMines) {
            errorLabel.text = s"Too few mines! Must be at least $minMines."
        } else if (mines > maxMines) {
            errorLabel.text = s"Too many mines! Must be at most $maxMines."
        } else {
            errorLabel.text = ""
        }
    }

    private def checkBoardSize(): Unit = {
        val size = editFields.length * editFields(0).length
        if (size < minSize) {
            errorLabel.text = s"Too few fields! Must be at least $minSize."
        } else if (size > maxSize) {
            errorLabel.text = s"Too many fields! Must be at most $maxSize."
        } else {
            errorLabel.text = ""
        }
    }

    private def deselectAllFields(): Unit = {
        for (row <- editFields.indices) {
            for (col <- editFields(row).indices) {
                editFields(row)(col).isSelected = false
                updateFieldButtonStyle(row, col)
            }
        }
    }

    private def clearAllMines(): Unit = {
        for (row <- editFields.indices) {
            for (col <- editFields(row).indices) {
                editFields(row)(col).isMine = false
            }
        }
    }

    private def removeSectors(): Unit = {
        for (row <- editFields.indices) {
            for (col <- editFields(row).indices) {
                editFields(row)(col).isSector = false
                updateFieldButtonStyle(row, col)
            }
        }
    }

    private def updateFieldButtonStyle(row: Int, col: Int): Unit = {
        editFieldButtons(row)(col).text = editFields(row)(col).getText
        editFieldButtons(row)(col).style = editFields(row)(col).getStyle
    }

    private def populateBoard(): Unit = {
        editBoard.children.clear()
        for (row <- editFields.indices) {
            for (col <- editFields(row).indices) {
                val button = new Button {
                    minWidth = 30
                    minHeight = 30
                    text = editFields(row)(col).getText
                    style = editFields(row)(col).getStyle
                    onAction = _ => {
                        handleLeftClick(row, col)
                    }
                    onContextMenuRequested = _ => {
                        handleRightClick(row, col)
                    }
                }

                editBoard.add(button, col, row)
                editFieldButtons(row)(col) = button
            }
        }
    }

    private def repopulateBoard(): Unit = {
        editFieldButtons = Array.ofDim(editFields.length, editFields(0).length)
        editBoard.children.clear()
        for (row <- editFields.indices) {
            for (col <- editFields(row).indices) {
                val button = new Button {
                    minWidth = 30
                    minHeight = 30
                    text = editFields(row)(col).getText
                    style = editFields(row)(col).getStyle
                    onAction = _ => {
                        handleLeftClick(row, col)
                    }
                    onContextMenuRequested = _ => {
                        handleRightClick(row, col)
                    }
                }

                editBoard.add(button, col, row)
                editFieldButtons(row)(col) = button
            }
        }
    }

    private def clearAllButNewSector(newSector: Sector): Unit = {
        if (newSector == null) {
            return
        }

        for (row <- editFields.indices) {
            for (col <- editFields(row).indices) {
                val rowInNewSector = row >= newSector.topLeftPoint.row && row <= newSector.bottomRightPoint.row
                val colInNewSector = col >= newSector.topLeftPoint.col && col <= newSector.bottomRightPoint.col

                if (!(rowInNewSector && colInNewSector)) {
                    editFields(row)(col).isMine = false
                    editFields(row)(col).isSector = false
                }
            }
        }
    }

    private def setBottomText(text: String): Unit = {
        errorLabel.text = text
        errorLabel.style = "-fx-font-size: 16pt;"
    }

    private def clearBottomText(): Unit = {
        errorLabel.text = ""
        errorLabel.style = "-fx-font-size: 16pt; -fx-text-fill: red;"
    }

    private def clearIsometryView(clearSector: Boolean): Unit = {
        chooseIsometry.value = null
        chooseExpansion.selected = false
        chooseTransparency.selected = false
        if (clearSector) {
            sectorFirstPoint = None
            sectorSecondPoint = None
            removeSectors()
        }
    }

    private def clearApplyIsometryView(clearSector: Boolean): Unit = {
        isometryTypeList = None
        isometryArgsList = List()
        isometryPivot = None
        applyingIsometry = false
        invert.selected = false

        clearAllSelectedFields()
        clearIsometryView(clearSector)
    }

    private def onComposeChain(): Unit = {
        if (lastComposeComboBox.value == null || lastComposeComboBox.value.value == null) {
            return
        }

        composeChain :+= lastComposeComboBox.value.value

        val newComboBox = new ComboBox[Isometry] {
            items = ObservableBuffer(Isometry.allIsometries: _*)
            promptText = "Choose Isometry"
            maxWidth = 130
            onAction = _ => {
                onComposeChain()
            }
        }

        composeChainBox.children.add(newComboBox)
        lastComposeComboBox = newComboBox
    }

    private def clearComposeView(): Unit = {
        composeChain = List()
        lastComposeComboBox = composeChooseIsometry
        compositionName.text = ""
        composeChooseIsometry.value = null
    }

    private def updateIsometries(): Unit = {
        chooseIsometry.items = ObservableBuffer(Isometry.allIsometries: _*)
        composeChooseIsometry.items = ObservableBuffer(Isometry.allIsometries: _*)
    }

    // View change functions
    private def addRemoveView(addMode: Boolean): Unit = {
        this.addMode = addMode
        bottomButtons.children.clear()
        bottomButtons.children.addAll(boardUpButton, boardDownButton, boardLeftButton, boardRightButton, boardCancelButton)
    }

    private def mainView(): Unit = {
        bottomButtons.children.clear()
        bottomButtons.children.addAll(addButton, removeButton, selectModeButton, isometricsButton)
    }

    private def selectView(): Unit = {
        bottomButtons.children.clear()
        bottomButtons.children.addAll(selectModeClearButton, selectModeCancelButton)
    }

    private def isometryView(): Unit = {
        chooseIsometry.items = ObservableBuffer(Isometry.allIsometries: _*)
        bottomButtons.children.clear()

        val expansionTransparencyVBox = new VBox(chooseExpansion, chooseTransparency) {
            spacing = 10
            alignment = Pos.CenterLeft
        }

        bottomButtons.children.addAll(
            chooseIsometry, expansionTransparencyVBox, chooseSection,
            applyIsometryButton, composeButton, cancelIsometryButton
        )
    }

    private def applyingIsometryView(isometry: Isometry): Unit = {
        val isLast = isometryTypeList.get.length == 1
        bottomButtons.children.clear()

        isometry match {
            case Rotation(_) =>
                bottomButtons.children.add(chooseRotationDirection)
            case Reflection(_) =>
                bottomButtons.children.add(chooseIsometryDirection)
            case CentralSymmetry(_) =>
        }

        if (isLast) {
            bottomButtons.children.addAll(invert, confirmIsometryButton, cancelApplyingIsometry)
        } else {
            bottomButtons.children.addAll(invert, nextIsometryButton, cancelApplyingIsometry)
        }
    }

    private def chooseSectorView(): Unit = {
        bottomButtons.children.clear()
        bottomButtons.children.addAll(chooseSectionDoneButton)
    }

    private def composeView(): Unit = {
        bottomButtons.children.clear()
        composeChainBox.children.clear()
        composeChainBox.children.add(composeChooseIsometry)
        bottomButtons.children.addAll(compositionName, composeChainBox, composeConfirmButtom, composeCancelButton)
    }

    // Behavior
    saveButton.onAction = _ => {
        save()
    }

    quitButton.onAction = _ => {
        Minesweeper.stage.scene = StartScene
    }

    addButton.onAction = _ => {
        addRemoveView(true)
    }

    removeButton.onAction = _ => {
        addRemoveView(false)
    }

    boardUpButton.onAction = _ => {
        if (addMode) {
            val newRow = Array.fill(editFields(0).length)(new EditField(false))
            editFields = newRow +: editFields
        } else if (editFields.length > 1) {
            editFields = editFields.tail
        }

        checkBoardSize()
        repopulateBoard()
    }

    boardDownButton.onAction = _ => {
        if (addMode) {
            val newRow = Array.fill(editFields(0).length)(new EditField(false))
            editFields = editFields :+ newRow
        } else if (editFields.length > 1) {
            editFields = editFields.init
        }

        checkBoardSize()
        repopulateBoard()
    }

    boardLeftButton.onAction = _ => {
        if (addMode) {
            for (row <- editFields.indices) {
                editFields(row) = new EditField(false) +: editFields(row)
            }
        } else if (editFields(0).length > 1) {
            for (row <- editFields.indices) {
                editFields(row) = editFields(row).tail
            }
        }

        checkBoardSize()
        repopulateBoard()
    }

    boardRightButton.onAction = _ => {
        if (addMode) {
            for (row <- editFields.indices) {
                editFields(row) = editFields(row) :+ new EditField(false)
            }
        } else if (editFields(0).length > 1) {
            for (row <- editFields.indices) {
                editFields(row) = editFields(row).init
            }
        }

        checkBoardSize()
        repopulateBoard()
    }

    boardCancelButton.onAction = _ => {
        mainView()
    }

    selectModeButton.onAction = _ => {
        selectMode = true
        selectView()
    }

    selectModeClearButton.onAction = _ => {
        clearAllSelectedFields()
    }

    selectModeCancelButton.onAction = _ => {
        selectMode = false
        clearAllSelectedFields()
        mainView()
    }

    isometricsButton.onAction = _ => {
        isometryView()
    }

    applyIsometryButton.onAction = _ => {
        if (chooseIsometry.value != null) {
            applyingIsometry = true
            isometryTypeList = Some(chooseIsometry.value.value.getChain)

            applyingIsometryView(isometryTypeList.get.head)
        }
    }

    chooseSection.onAction = _ => {
        chooseSectorMode = true
        chooseSectorView()
    }

    chooseSectionDoneButton.onAction = _ => {
        chooseSectorMode = false
        isometryView()
    }

    cancelIsometryButton.onAction = _ => {
        clearIsometryView(clearSector = true)
        mainView()
    }

    cancelApplyingIsometry.onAction = _ => {
        clearApplyIsometryView(clearSector = true)
        isometryView()
    }

    nextIsometryButton.onAction = _ => {
        isometryTypeList.get.head match {
            case Rotation(_) =>
                isometryArgsList = isometryArgsList :+ new IsometryArgs(
                    Field,
                    chooseRotationDirection.value.value,
                    isometryPivot.get,
                    invert.selected.value
                )
            case Reflection(_) =>
                isometryArgsList = isometryArgsList :+ new IsometryArgs(
                    isometryDirection,
                    Unused,
                    isometryPivot.get,
                    invert.selected.value
                )
            case CentralSymmetry(_) =>
                isometryArgsList = isometryArgsList :+ new IsometryArgs(
                    Field,
                    Unused,
                    isometryPivot.get,
                    invert.selected.value
                )
        }

        isometryTypeList = Some(isometryTypeList.get.tail)
        isometryPivot = None
        invert.selected = false
        clearAllSelectedFields()

        isometryTypeList.get.head match
            case Rotation(_) =>
                isometryDirection = Field
            case Reflection(_) =>
                isometryDirection = Row
            case CentralSymmetry(_) =>
                isometryDirection = Field

        applyingIsometryView(isometryTypeList.get.head)
    }

    confirmIsometryButton.onAction = _ => {
        isometryTypeList.get.head match {
            case Rotation(_) =>
                isometryArgsList = isometryArgsList :+ new IsometryArgs(
                    Field,
                    chooseRotationDirection.value.value,
                    isometryPivot.get,
                    invert.selected.value
                )
            case Reflection(_) =>
                isometryArgsList = isometryArgsList :+ new IsometryArgs(
                    isometryDirection,
                    Unused,
                    isometryPivot.get,
                    invert.selected.value
                )
            case CentralSymmetry(_) =>
                isometryArgsList = isometryArgsList :+ new IsometryArgs(
                    Field,
                    Unused,
                    isometryPivot.get,
                    invert.selected.value
                )
        }

        val (newSector: Sector, newBoard: Array[Array[EditField]]) = chooseIsometry.value.value.applyIsometry(
            new Sector(sectorFirstPoint.get, sectorSecondPoint.get),
            editFields,
            isometryArgsList,
            if (chooseExpansion.isSelected) Expanding else NonExpanding,
            if (chooseTransparency.isSelected) Transparent else NonTransparent
        )

        editFields = newBoard

        if (newSector != null) {
            sectorFirstPoint = Some(newSector.topLeftPoint)
            sectorSecondPoint = Some(newSector.bottomRightPoint)
        } else {
            sectorFirstPoint = None
            sectorSecondPoint = None
            clearAllMines()
        }

        repopulateBoard()
        clearBottomText()
        checkBoardSize()
        checkNumberOfMines()
        clearApplyIsometryView(clearSector = false)
        isometryView()
    }

    chooseIsometryDirection.onAction = _ => {
        isometryDirection = chooseIsometryDirection.value.value
        if (isometryPivot.nonEmpty) {
            handleRightClick(isometryPivot.get.row, isometryPivot.get.col)
            handleRightClick(isometryPivot.get.row, isometryPivot.get.col)
        }
    }

    composeButton.onAction = _ => {
        clearIsometryView(clearSector = true)
        composeView()
    }

    composeChooseIsometry.onAction = _ => {
        onComposeChain()
    }

    composeConfirmButtom.onAction = _ => {
        val composition = new Composition(compositionName.text.value, composeChain)
        Isometry.allIsometries :+= composition
        clearComposeView()
        updateIsometries()
        isometryView()
    }

    composeCancelButton.onAction = _ => {
        isometryView()
    }
}

class EditField(var isMine: Boolean) {
    var isSelected = false
    var isSector = false

    def this(isMine: Boolean, isSelected: Boolean, isSector: Boolean) = {
        this(isMine)
        this.isSelected = isSelected
        this.isSector = isSector
    }

    def getText: String = {
        if isMine then "💣" else ""
    }

    def getStyle: String = {
        val style = "-fx-background-radius: 0; -fx-font-weight: bold; -fx-font-size: 9pt;"
        if (isSelected) style + "-fx-background-color: lightblue;"
        else if (isSector) style + "-fx-background-color: rgba(145, 14, 28, 0.5);"
        else style
    }
}


