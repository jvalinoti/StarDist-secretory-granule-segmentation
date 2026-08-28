# QuPath Scripts

This directory contains the Groovy scripts for StarDist-based segmentation and morphometric analysis of Drosophila larval salivary gland secretory granules in QuPath.

## Scripts

### Script_1_detections.groovy

Performs the initial StarDist-based segmentation of secretory granules within user-defined regions of interest (ROIs).

The script allows the user to select the StarDist model and configure the analysis before applying object detection to the defined ROIs.

### Script_2_measurements.groovy

Performs morphometric analysis of the detected objects and data export.

The script includes additional filtering and measurement steps.

## Workflow

Before running, scripts must be loaded to QuPath script directory. To access to the script directory from QuPath, go to `Automate` > `User scripts...` > `Open script directory`. 
Once scripts are loaded:

1. Create a QuPath project in an empty folder (`Create project...` > choose your directory).
2. Import the images to be analyzed (drag them into the Image list, or select them using `Add images...`).
3. Define the ROIs (e.g., individual cells) to be analyzed in each image using the annotation tools (rectangle, ellipse, or closed polygon).
4. Open and run `Script_1_detections.groovy` to perform StarDist segmentation. Users can choose whether to perform the segmentation on the whole project, on a subset of images, or on selected ROIs in the currently open image. Scripts can be accessed from `Automate` > `User scripts...` and executed from the script editor with `Run`. 
5. When necessary, manually correct the resulting segmentation by removing incorrect detections (select + `Delete`) or adding missed objects using the annotation tools.
6. Open and run `Script_2_measurements.groovy` to generate shape measurements and export the resulting data for downstream analysis. Optional filtering and additional measurements can also be performed at this step.
