# QuPath Scripts

This directory contains the Groovy scripts for StarDist-based segmentation and morphometric analysis of *Drosophila* larval salivary gland secretory granules in QuPath.

## Scripts

### Script_1_detections.groovy

Performs the initial StarDist-based segmentation of secretory granules within user-defined regions of interest (ROIs).

The script allows the user to select the StarDist model and configure the analysis before applying object detection to the defined ROIs.

### Script_2_measurements.groovy

Performs morphometric analysis of the detected objects and data export.

The script includes additional filtering and measurement steps.

## Workflow

### Setting up QuPath

1. Install the StarDist extension. For that, open QuPath, go to `Extensions` > `Manage extensions` and install the StarDist extension from the QuPath Catalog.
2. Copy the provided Groovy scripts to the QuPath user script directory. To access the directory from QuPath, go to `Automate` > `User scripts...` > `Open script directory`. 
Once loaded, scripts can be accessed from `Automate` > `User scripts...` and executed from the script editor with `Run`.
3. Save the StarDist `.pb` models to a known directory (you will need those in the following steps).

### Running the workflow

1. Open QuPath and create a QuPath project in an empty folder (`Create project...` > choose your directory).
2. Import the images to be analyzed (drag them into the Image list, or select them using `Add images...`).
3. Define the ROIs (e.g., individual cells) to be analyzed in each image using the annotation tools (rectangle, ellipse, or closed polygon). *Note: segmentation will be performed only on defined ROIs, so ROIs should be defined in all images to be analyzed before continuing with the workflow.*
4. Open and run `Script_1_detections.groovy` to perform StarDist segmentation. After selecting the desired StarDist `.pb` model (path will be remembered for future runs), users can choose whether to perform the segmentation on the whole project, on a subset of images, or on selected ROIs in the currently open image. Relevant segmentation parameters can also be adjusted in this step.
5. When necessary, manually correct the resulting segmentation by removing incorrect detections (select + `Delete`) or adding missed objects using the annotation tools.
6. Open and run `Script_2_measurements.groovy` to generate shape measurements and export the resulting data for downstream analysis. Optional filtering and additional measurements can also be performed at this step.

## Video Tutorial

*Coming soon...*
