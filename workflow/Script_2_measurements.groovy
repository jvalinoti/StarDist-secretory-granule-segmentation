// ====================================================================
// SCRIPT 2 - Detection formatting + measurements generation and export
// ====================================================================
//
// Run this after manually curating detections produced by
// Script 1 (deleting incorrect ones, adding missed objects
// as new annotations).
//
// For every image processed, this script:
//   1. Converts manually-added annotations (anything not
//      classified "Parent_ROI") into detections, and
//      inserts them into the correct spatial hierarchy.
//   2. Optionally removes detections that fall on an
//      annotation border, and/or detections with no
//      parent ROI (orphans).
//   3. Generates shape / intensity / parent-ROI-area
//      measurements for EVERY detection in the image -
//      both pre-existing and newly-converted.
//   4. Optionally exports all measurements via QuPath's
//      own MeasurementExporter.
//
// Requires the annotation hierarchy set up by Script 1
// (parent annotations classified "Parent_ROI").
// Tested on QuPath 0.6.x. 
// =====================================================

import qupath.lib.objects.PathObjects
import qupath.lib.objects.PathDetectionObject
import qupath.lib.objects.PathObject
import qupath.lib.regions.ImageRegion
import qupath.lib.analysis.features.ObjectMeasurements
import qupath.lib.plugins.parameters.ParameterList
import qupath.lib.gui.tools.GuiTools
import qupath.lib.gui.tools.MeasurementExporter
import qupath.fx.dialogs.FileChoosers
import org.locationtech.jts.geom.util.LinearComponentExtracter

import java.util.stream.Collectors


// =====================================================
// SETTINGS DIALOG
// =====================================================

def params = new ParameterList()
        .addTitleParameter("Detections cleanup")
        .addBooleanParameter("removeOrphanDetections", "Remove detections without a parent ROI", true)
        .addBooleanParameter("removeBorderDetections", "Remove detections on annotation borders", true)
        .addDoubleParameter("borderDistancePixels", "Border distance", 1, "px",
                "Detections within this many pixels of an annotation boundary will be removed")

        .addTitleParameter("Detections measurements")
        .addBooleanParameter("addParentROIArea", "Add parent ROI area", true)
        .addBooleanParameter("addShapeMeasurements", "Add shape measurements", true)
        .addBooleanParameter("addIntensityMeasurements", "Add intensity measurements", false)
        .addDoubleParameter("intensityDownsample", "Intensity measurement downsample", 1.0, null,
                "1 = full (native) resolution - the most conservative option for calibrated images")

        .addTitleParameter("Measurements export")
        .addBooleanParameter("exportMeasurements", "Export measurements after processing", true)
        .addChoiceParameter("exportSeparator", "Column separator", "Tab", ["Tab", "Comma"], null)
        .addStringParameter("exportFilename", "Output filename", "measurements", null)
        .addChoiceParameter("exportFolder", "Output folder", "Project folder",
                ["Project folder", "Choose folder"], null)

if (!GuiTools.showParameterDialog("Annotation -> Detection Utility - Settings", params)) {
    println "Cancelled by user."
    return
}

boolean removeBorderDetections  = params.getBooleanParameterValue("removeBorderDetections")
double  borderDistancePixels    = params.getDoubleParameterValue("borderDistancePixels")
boolean removeOrphanDetections  = params.getBooleanParameterValue("removeOrphanDetections")

boolean doParentROIArea         = params.getBooleanParameterValue("addParentROIArea")
boolean doShapeMeasurements     = params.getBooleanParameterValue("addShapeMeasurements")
boolean doIntensityMeasurements = params.getBooleanParameterValue("addIntensityMeasurements")
double  intensityDownsample     = params.getDoubleParameterValue("intensityDownsample")

boolean doExportMeasurements    = params.getBooleanParameterValue("exportMeasurements")
String  exportSeparatorChoice   = params.getChoiceParameterValue("exportSeparator")
String  exportFilename          = params.getStringParameterValue("exportFilename")
String  exportFolderChoice      = params.getChoiceParameterValue("exportFolder")


// =====================================================
// PROJECT / EXPORT FOLDER
// =====================================================

def project = getProject()

String exportDirectory = null

if (doExportMeasurements) {
    if (exportFolderChoice == "Choose folder") {
        File initialDir = project?.getPath()?.getParent()?.toFile()
        File chosenDir = FileChoosers.promptForDirectory("Select output folder", initialDir)
        // Falls back to the project folder if the user cancels the picker
        exportDirectory = chosenDir?.getAbsolutePath() ?: project?.getPath()?.getParent()?.toString()
    } else {
        exportDirectory = project?.getPath()?.getParent()?.toString()
    }
}


// =====================================================
// COUNTERS
// =====================================================

int imagesProcessed = 0
int totalConverted   = 0
int totalRemoved     = 0


// =====================================================
// SHAPE-MEASUREMENT NAMING
// =====================================================

def shapeFeatureNames = [
        (ObjectMeasurements.ShapeFeatures.AREA)        : "area",
        (ObjectMeasurements.ShapeFeatures.LENGTH)      : "perimeter",
        (ObjectMeasurements.ShapeFeatures.CIRCULARITY) : "circularity",
        (ObjectMeasurements.ShapeFeatures.SOLIDITY)    : "solidity",
        (ObjectMeasurements.ShapeFeatures.MAX_DIAMETER): "max_diameter",
        (ObjectMeasurements.ShapeFeatures.MIN_DIAMETER): "min_diameter"
]

def addCleanShapeMeasurements = { Collection<PathObject> objects, cal ->

    if (objects.isEmpty()) return

    boolean calibrated = cal.hasPixelSizeMicrons() ?: false
    String unit = calibrated ? "um" : "px"
    def probe = objects[0]

    shapeFeatureNames.each { feature, baseName ->

        def before = probe.getMeasurementList().getNames().toList()
        ObjectMeasurements.addShapeMeasurements(objects, cal, feature)
        def after = probe.getMeasurementList().getNames().toList()
        def newKeys = after - before

        if (newKeys.isEmpty()) return
        String rawKey = newKeys[0]

        String cleanName
        if (feature == ObjectMeasurements.ShapeFeatures.AREA) {
            cleanName = "${baseName}_${unit}2"
        } else if (feature in [ObjectMeasurements.ShapeFeatures.LENGTH,
                                ObjectMeasurements.ShapeFeatures.MAX_DIAMETER,
                                ObjectMeasurements.ShapeFeatures.MIN_DIAMETER]) {
            cleanName = "${baseName}_${unit}"
        } else {
            cleanName = baseName
        }

        objects.each { obj ->
            def mlist = obj.getMeasurementList()
            if (mlist.getNames().contains(rawKey)) {
                double value = mlist.get(rawKey)
                mlist.put(cleanName, value)
                mlist.removeMeasurements(rawKey)
            }
        }
    }
}


// =====================================================
// INTENSITY-MEASUREMENT NAMING
// =====================================================

def addCleanIntensityMeasurements = { Collection<PathObject> objects, server, double downsample ->

    def stats = ObjectMeasurements.Measurements.values()
    def compartments = ObjectMeasurements.Compartments.values() as List

    objects.each { det ->
        def mlist = det.getMeasurementList()

        stats.each { stat ->
            def before = mlist.getNames().toList()
            ObjectMeasurements.addIntensityMeasurements(server, det, downsample, [stat], compartments)
            def after = mlist.getNames().toList()
            def newKeys = after - before

            newKeys.eachWithIndex { key, i ->
                double value = mlist.get(key)
                String cleanName = "ch${i + 1}_${stat.name().toLowerCase()}"
                mlist.put(cleanName, value)
                mlist.removeMeasurements(key)
            }
        }
    }
}


// =====================================================
// PROCESS CURRENT IMAGE
// =====================================================

def processCurrentImage = {

    def server = getCurrentServer()
    def cal = server.getPixelCalibration()
    def hierarchy = getCurrentHierarchy()

    double pixelWidth = cal.getPixelWidthMicrons()
    double pixelHeight = cal.getPixelHeightMicrons()

    // -------------------------------------------------
    // Convert manually-added annotations to detections
    // -------------------------------------------------

    def annotations = getAnnotationObjects().findAll {
        it.getPathClass()?.toString() != "Parent_ROI"
    }
    int converted = annotations.size()

    def newDetections = annotations.collect {
        def det = PathObjects.createDetectionObject(it.getROI(), it.getPathClass())
        det.setName(it.getName())
        return det
    }

    removeObjects(annotations, true)
    addObjects(newDetections)
    resolveHierarchy()

    // Combined count of border + orphan detections removed below
    int removed = 0

    // --------------------------------------------------------------------------------------------------------------
    // Optional: remove detections near annotation borders
    // - Original approach by Pete Bankhead (https://gist.github.com/petebankhead/aac937b112724ab1626b020b6cca87b4)-
    // --------------------------------------------------------------------------------------------------------------

    if (removeBorderDetections) {
        def toRemove = new HashSet<PathObject>()

        for (def annotation in hierarchy.getAnnotationObjects()) {
            def roi = annotation.getROI()
            if (roi == null) continue

            def detections = hierarchy.getObjectsForRegion(
                    PathDetectionObject.class, ImageRegion.createInstance(roi), null)
            def geometry = roi.getGeometry()

            for (def line in LinearComponentExtracter.getLines(geometry)) {
                toRemove.addAll(
                        detections.parallelStream()
                                .filter { d -> line.isWithinDistance(d.getROI().getGeometry(), borderDistancePixels) }
                                .collect(Collectors.toList())
                )
            }
        }

        removed += toRemove.size()
        hierarchy.removeObjects(toRemove, true)
        fireHierarchyUpdate()
    }

    // -------------------------------------------------
    // Optional: remove orphan detections (parent is Root)
    // -------------------------------------------------

    if (removeOrphanDetections) {
        def rootObject = hierarchy.getRootObject()
        def orphanDetections = getDetectionObjects().findAll { it.getParent() == rootObject }

        if (!orphanDetections.isEmpty()) {
            removed += orphanDetections.size()
            hierarchy.removeObjects(orphanDetections, true)
            fireHierarchyUpdate()
        }
    }

    // -------------------------------------------------
    // Generate measurements for every detection, so
    // pre-existing and newly-converted ones are measured
    // identically.
    // -------------------------------------------------

    def allDetections = getDetectionObjects()

    if (doShapeMeasurements) {
        addCleanShapeMeasurements(allDetections, cal)
    }

    if (doIntensityMeasurements) {
        addCleanIntensityMeasurements(allDetections, server, intensityDownsample)
    }

    if (doParentROIArea) {
        boolean calibrated = cal.hasPixelSizeMicrons() ?: false
        String unit = calibrated ? "um" : "px"

        allDetections.each { det ->
            def parent = det.getParent()
            if (parent != null && parent.isAnnotation()) {
                det.getMeasurementList().put(
                        "parent_roi_area_${unit}2",
                        parent.getROI().getArea() * pixelWidth * pixelHeight
                )
            }
        }
    }

    fireHierarchyUpdate()

    imagesProcessed++
    totalConverted += converted
    totalRemoved += removed

    println String.format(
            "Converted: %d | Removed: %d | Remaining detections: %d",
            converted, removed, getDetectionObjects().size()
    )
}


// =====================================================
// PROCESS PROJECT (current image first, then the rest)
// =====================================================

if (project == null) {
    processCurrentImage()
    println "No project detected - processed the open image only."
    return
}

processCurrentImage()
def currentEntry = getProjectEntry()
if (currentEntry != null) {
    currentEntry.saveImageData(getCurrentImageData())
}

project.getImageList().each { entry ->
    if (currentEntry != null && entry == currentEntry) return

    println "Processing ${entry.getImageName()}"
    setBatchProjectAndImage(project, entry.readImageData())
    processCurrentImage()
    entry.saveImageData(getCurrentImageData())
}


// =====================================================
// EXPORT MEASUREMENTS
// =====================================================

if (doExportMeasurements) {

    String separator = exportSeparatorChoice == "Comma" ? "," : "\t"

    String outputBaseName = exportFilename?.trim() ?: "measurements"
    // Strip a manually-typed extension, then re-add the correct one for the chosen separator
    outputBaseName = outputBaseName.replaceFirst(/(?i)\.(tsv|csv)$/, "")
    String outputFilename = outputBaseName + (exportSeparatorChoice == "Comma" ? ".csv" : ".tsv")

    String outDir = exportDirectory?.trim() ?: project.getPath().getParent().toString()
    def outFile = new File(outDir, outputFilename)
    outFile.getParentFile()?.mkdirs()

    new MeasurementExporter()
            .imageList(project.getImageList())
            .separator(separator)
            .exportType(PathDetectionObject.class)
            .exportMeasurements(outFile)

    println "Measurements exported to: ${outFile.getAbsolutePath()}"
}


// =====================================================
// SUMMARY
// =====================================================

println "========================================"
println "Annotation -> Detection Utility completed"
println "========================================"
println "Images processed : ${imagesProcessed}"
println "Objects converted: ${totalConverted}"
println "Detections removed: ${totalRemoved}"
println "========================================"
