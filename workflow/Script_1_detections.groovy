// =====================================================
// SCRIPT 1 - ROI ID assignment + StarDist detection
// =====================================================
//
// For each processed image:
//   1. Every annotation is assigned a "Parent_ROI" class
//      and a sequential name (ROI_1, ROI_2, ...).
//   2. StarDist is run inside those annotations.
//
// Run Script 2 afterwards to convert any manually-added
// annotations into detections and generate measurements.
//
// Requires the QuPath StarDist extension.
// Tested on QuPath 0.6.x. A few GUI-related classes used
// here (GuiTools, FileChoosers) have moved between recent
// QuPath versions - if a method is reported as missing,
// check the class it now lives on for your QuPath version.
// =====================================================

import qupath.ext.stardist.StarDist2D
import qupath.lib.scripting.QP
import qupath.lib.plugins.parameters.ParameterList
import qupath.lib.gui.tools.GuiTools
import qupath.fx.dialogs.FileChoosers

import javafx.application.Platform
import javafx.stage.FileChooser
import javafx.scene.control.Dialog
import javafx.scene.control.ButtonType
import javafx.scene.control.ListView
import javafx.scene.control.ListCell
import javafx.scene.control.SelectionMode

import java.util.concurrent.CountDownLatch
import java.util.prefs.Preferences


// =====================================================
// STARDIST MODEL SELECTION (remembers the last path used)
// =====================================================

def prefs = Preferences.userRoot().node("QuPath/Script_1")
def savedModelPath = prefs.get("stardistModelPath", "")
def initialModel = (savedModelPath && new File(savedModelPath).exists()) ? new File(savedModelPath) : null

def modelFile = FileChoosers.promptForFile(
        null,
        "Select StarDist model",
        initialModel,
        [
                new FileChooser.ExtensionFilter("StarDist models", "*.pb"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        ] as FileChooser.ExtensionFilter[]
)

if (modelFile == null) {
    println "No StarDist model selected. Script cancelled."
    return
}

String modelPath = modelFile.getAbsolutePath()
prefs.put("stardistModelPath", modelPath)


// =====================================================
// SETTINGS DIALOG
// =====================================================

def params = new ParameterList()
        .addTitleParameter("Processing")
        .addChoiceParameter("processingScope", "Process",
                "Whole project - all annotations",
                [
                        "Whole project - all annotations",
                        "Whole project - selected images",
                        "Current image - all annotations",
                        "Current image - selected annotations"
                ])
        .addTitleParameter("StarDist settings")
        .addIntParameter("channel", "Input channel", 0)
        .addIntParameter("normalizeLow", "Normalization lower percentile", 1)
        .addIntParameter("normalizeHigh", "Normalization upper percentile", 99)
        .addStringParameter("threshold", "Detection threshold (e.g. 0.3)", "0.3")
        .addStringParameter("pixelSize", "Pixel size in \u00b5m (e.g. 0.15)", "0.15")
        .addBooleanParameter("constrainToParent", "Constrain detections to parent", false)

if (!GuiTools.showParameterDialog("StarDist Detection", params)) {
    println "Cancelled by user."
    return
}

String  processingScope  = params.getChoiceParameterValue("processingScope")
int     channel          = params.getIntParameterValue("channel")
int     normalizeLow     = params.getIntParameterValue("normalizeLow")
int     normalizeHigh    = params.getIntParameterValue("normalizeHigh")
double  threshold        = params.getStringParameterValue("threshold").toDouble()
double  pixelSize        = params.getStringParameterValue("pixelSize").toDouble()
boolean constrainToParent = params.getBooleanParameterValue("constrainToParent")


// =====================================================
// IMAGE PICKER (for "Whole project - selected images")
// =====================================================
//
// ParameterList dialogs don't support multi-select lists,
// so this is a standalone JavaFX dialog instead. Scripts
// run on a background thread, so showing it requires
// dispatching to the JavaFX Application Thread and
// blocking until it closes.
// =====================================================

def pickImages = { List projectEntries ->

    def chosen = null
    def latch = new CountDownLatch(1)

    Platform.runLater {
        try {
            def dialog = new Dialog<ButtonType>()
            dialog.setTitle("Select images to process")
            dialog.setHeaderText(
                    "Choose one or more images " +
                    "(Ctrl/Cmd-click or Shift-click for multiple)"
            )

            def listView = new ListView()
            listView.getItems().addAll(projectEntries)
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE)
            listView.setPrefHeight(350)
            listView.setPrefWidth(400)

            dialog.getDialogPane().setContent(listView)
            dialog.getDialogPane().getButtonTypes().addAll(
                    ButtonType.OK,
                    ButtonType.CANCEL
            )

            def result = dialog.showAndWait()

            if (result.isPresent() && result.get() == ButtonType.OK) {
                chosen = listView.getSelectionModel().getSelectedItems().toList()
            }

        } finally {
            latch.countDown()
        }
    }

    latch.await()

    return chosen
}

// =====================================================
// PROJECT / CURRENT IMAGE
// =====================================================

def project = QP.getProject()
def originalImageData = QP.getCurrentImageData()
String originalImageName = originalImageData != null ? QP.getCurrentImageName() : null
def originalEntry = QP.getProjectEntry()


// =====================================================
// RESOLVE "WHOLE PROJECT - SELECTED IMAGES"
// =====================================================

def selectedEntries = null

if (processingScope == "Whole project - selected images") {

    if (project == null) {
        println "ERROR: No QuPath project is currently open."
        println "\"Whole project - selected images\" requires a project."
        return
    }

    def imageList = project.getImageList()

    if (imageList.isEmpty()) {
        println "ERROR: The project contains no images."
        return
    }

    selectedEntries = pickImages(imageList)

    if (selectedEntries == null || selectedEntries.isEmpty()) {
        println "No images selected. Script cancelled."
        return
    }

    println "Selected images (${selectedEntries.size()}):"
    selectedEntries.each { println "    ${it.getImageName()}" }
}


// =====================================================
// STARDIST
// =====================================================
//
// Runs StarDist on the given annotations and reports the
// number of detections found in each.
// =====================================================

def runStarDist = { imageData, pathObjects, imageName ->

    if (pathObjects == null || pathObjects.isEmpty()) {
        println "${imageName}: no annotations to process."
        return
    }

    def stardist = StarDist2D.builder(modelPath)
            .channels(channel)
            .normalizePercentiles(normalizeLow, normalizeHigh)
            .threshold(threshold)
            .pixelSize(pixelSize)
            .constrainToParent(constrainToParent)
            .build()

    stardist.detectObjects(imageData, pathObjects)
    stardist.close()

    println "${imageName}: StarDist completed."
    for (def annotation in pathObjects) {
        def detections = annotation.getChildObjects().findAll { it.isDetection() }
        println "    ${annotation.getName()}: ${detections.size()} objects detected"
    }
}


// =====================================================
// PROCESS IMAGE
// =====================================================
//
// Assigns ROI IDs/classes to every annotation in the given
// image, then runs StarDist on the requested scope
// (all annotations, or only the selected ones).
// =====================================================

def processImage = { imageData, annotationScope, imageName ->

    if (imageData == null) {
        println "ERROR: No image data available for ${imageName}"
        return
    }

    if (project != null) {
        setBatchProjectAndImage(project, imageData)
    }

    def annotations = QP.getAnnotationObjects()

    if (annotations.isEmpty()) {
        println "${imageName}: no annotations found."
        return
    }

    annotations.eachWithIndex { annotation, i ->
        annotation.setPathClass(getPathClass("Parent_ROI"))
        annotation.setName("ROI_${i + 1}")
    }
    fireHierarchyUpdate()

    def pathObjects
    if (annotationScope == "selected") {
        pathObjects = QP.getSelectedObjects().findAll { it.isAnnotation() }
        if (pathObjects.isEmpty()) {
            println "WARNING: Selected annotations were requested, but none are currently selected."
            println "No StarDist detection was performed."
            return
        }
    } else {
        pathObjects = annotations
    }

    runStarDist(imageData, pathObjects, imageName)
    fireHierarchyUpdate()
}


// =====================================================
// DISPATCH BY PROCESSING SCOPE
// =====================================================

if (processingScope == "Current image - all annotations" ||
        processingScope == "Current image - selected annotations") {

    if (originalImageData == null) {
        println "ERROR: No image is currently open."
        return
    }

    String annotationScope = processingScope == "Current image - selected annotations" ? "selected" : "all"
    processImage(originalImageData, annotationScope, originalImageName)

    if (originalEntry != null) {
        originalEntry.saveImageData(originalImageData)
    }

} else if (processingScope == "Whole project - selected images") {

    // Same "current image first, then the rest" pattern as
    // the full whole-project mode, but restricted to
    // selectedEntries. If the currently open image isn't
    // part of the selection, it's left untouched entirely.

    println "========================================"
    println "WHOLE PROJECT PROCESSING (selected images)"
    println "========================================"
    println "Images to process: ${selectedEntries.size()}"

    boolean currentImageIncluded = originalEntry != null && selectedEntries.contains(originalEntry)

    if (currentImageIncluded) {
        println "----------------------------------------"
        println "Current image: ${originalImageName}"
        println "----------------------------------------"

        processImage(originalImageData, "all", originalImageName)
        originalEntry.saveImageData(originalImageData)
        println "Saved: ${originalImageName}"
    }

    selectedEntries.eachWithIndex { entry, imageIndex ->

        if (currentImageIncluded && entry == originalEntry) return

        println "----------------------------------------"
        println "Selected image ${imageIndex + 1} / ${selectedEntries.size()}: ${entry.getImageName()}"
        println "----------------------------------------"

        def imageData = entry.readImageData()
        processImage(imageData, "all", entry.getImageName())
        entry.saveImageData(imageData)
        println "Saved: ${entry.getImageName()}"
    }

} else {

    // Whole project - all annotations

    if (project == null) {
        println "ERROR: No QuPath project is currently open."
        println "Whole-project processing cannot be performed."
        return
    }

    def imageList = project.getImageList()

    if (imageList.isEmpty()) {
        println "ERROR: The project contains no images."
        return
    }

    println "========================================"
    println "WHOLE PROJECT PROCESSING"
    println "========================================"
    println "Images to process: ${imageList.size()}"

    if (originalImageData != null) {
        println "----------------------------------------"
        println "Current image: ${originalImageName}"
        println "----------------------------------------"

        processImage(originalImageData, "all", originalImageName)

        if (originalEntry != null) {
            originalEntry.saveImageData(originalImageData)
            println "Saved: ${originalImageName}"
        }
    }

    imageList.eachWithIndex { entry, imageIndex ->

        if (originalEntry != null && entry == originalEntry) return

        println "----------------------------------------"
        println "Project image ${imageIndex + 1} / ${imageList.size()}: ${entry.getImageName()}"
        println "----------------------------------------"

        def imageData = entry.readImageData()
        processImage(imageData, "all", entry.getImageName())
        entry.saveImageData(imageData)
        println "Saved: ${entry.getImageName()}"
    }
}


// =====================================================
// RESTORE ORIGINAL IMAGE
// =====================================================
//
// Only affects which image is displayed in QuPath - it
// does not undo any processing.
// =====================================================

if (originalEntry != null) {
    try {
        setBatchProjectAndImage(project, originalEntry.readImageData())
    } catch (Exception e) {
        println "WARNING: Could not restore the original image."
    }
}

println "========================================"
println "Script_1 finished"
println "========================================"