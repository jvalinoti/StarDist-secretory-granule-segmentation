# Secretory Granule Segmentation Workflow

This directory contains the resources required to implement the StarDist-based workflow for automated segmentation and morphometric analysis of *Drosophila melanogaster* larval salivary gland secretory granules in QuPath.

## Contents

* QuPath scripts

1. `Script_1_detections.groovy` - Performs the initial StarDist-based segmentation of secretory granules within user-defined regions of interest (ROIs).

2. `Script_2_measurements.groovy` - Performs morphometric analysis of the detected objects and data export. The script includes additional filtering and measurement steps.

* StarDist Models

1. `WT-trained.pb` - StarDist model trained with 12 images of wild-type (WT) salivary glands with mature secretory granules.

2. `Phenotype-trained.pb` - StarDist model trained with 12 images of wild-type (WT) salivary glands plus 7 additional images of non-WT salivary glands displaying heterogeneous secretory granule sizes.

* `example_image.tif` - A representative microscopy image of a WT salivary gland with mature secretory granules provided to allow users to test the workflow before analyzing their own data. Secretory granules are marked with the fluorescent cargo Sgs3-GFP.


## Workflow

### Setting up QuPath

1. Install the StarDist extension. For that, open QuPath, go to `Extensions` > `Manage extensions` and install the StarDist extension from the QuPath Catalog.
2. Copy the provided scripts to the QuPath user script directory. To access the directory from QuPath, go to `Automate` > `User scripts...` > `Open script directory`. 
Once loaded, scripts can be accessed from `Automate` > `User scripts...` and executed from the script editor with `Run`.
3. Save the StarDist `.pb` models to a known directory. Besides the ones provided in this repository, users can find other compatible pre-trained models through the [StarDist QuPath Extension models repository](https://github.com/qupath/models/tree/main/stardist) (e.g. `dsb2018_heavy_augment.pb`).

### Running the workflow

1. Open QuPath and create a QuPath project in an empty folder (`Create project...` > choose your directory).
2. Import the images to be analyzed (drag them into the Image list, or select them using `Add images...`).
3. Define the ROIs (e.g., individual cells) to be analyzed in each image using the annotation tools (rectangle, ellipse, or closed polygon). *Note: segmentation will be performed only on defined ROIs, so ROIs should be defined in all images to be analyzed before continuing with the workflow.*
4. Open and run `Script_1_detections.groovy` to perform StarDist segmentation. After selecting the desired StarDist `.pb` model (path will be remembered for future runs), users can choose whether to perform the segmentation on the whole project, on a subset of images, or on selected ROIs in the currently open image. Relevant segmentation parameters can also be adjusted in this step.
5. When necessary, manually correct the resulting segmentation by removing incorrect detections (select + `Delete`) or adding missed objects using the annotation tools.
6. Open and run `Script_2_measurements.groovy` to generate shape measurements and export the resulting data for downstream analysis. Optional filtering and additional measurements can also be performed at this step.

### Video Tutorial

*Coming soon...*

## Software attribution and acknowledgements

This workflow builds upon [QuPath](https://qupath.github.io/), [StarDist](https://github.com/stardist/stardist), and the [QuPath StarDist extension](https://github.com/qupath/qupath-extension-stardist). We gratefully acknowledge the developers and contributors of these open-source projects for making these tools available to the scientific community.

`Script_1_detections.groovy` was developed from and substantially extends the templates provided with the QuPath StarDist extension and in the [QuPath StarDist documentation](https://qupath.readthedocs.io/en/stable/docs/deep/stardist.html). The QuPath StarDist extension is distributed under the Apache License 2.0.

