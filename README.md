# StarDist-secretory-granule-segmentation
A StarDist-based workflow for automated morphometric analysis of *Drosophila* salivary gland secretory granules.

This repository provides the StarDist models and QuPath scripts developed for automated segmentation and morphometric analysis of *Drosophila melanogaster* larval salivary gland secretory granules.  The workflow combines custom-trained StarDist models with QuPath to enable automated object detection, manual segmentation curation when required, and subsequent extraction of morphometric measurements.

The repository accompanies the associated manuscript and is intended to provide a ready-to-use implementation of the image-analysis workflow as well as the resources required to evaluate its development and validation.

## Repository contents

### `scripts/`

Groovy scripts implementing the StarDist-based segmentation and measurement workflow in QuPath.

* `Script_1_detections.groovy` performs StarDist-based object detection within user-defined regions of interest (ROIs).
* `Script_2_measurements.groovy` performs subsequent object processing, measurements, and data export.

Detailed instructions are provided in the [`scripts/README.md`](./scripts/README.md).

### `models/`

Custom-trained StarDist models used for secretory granule segmentation.

* `WT-trained.pb`
* `Phenotype-trained.pb`

The models are provided in `.pb` format for direct use with the StarDist extension in QuPath.

Further information on model training and conversion is provided in the [`models/README.md`](./models/README.md).

### `example_data/`

Representative microscopy images for testing the segmentation workflow with the supplied StarDist models and QuPath scripts.

Instructions for using these images are provided in the [`example_data/README.md`](./example_data/README.md).

### `reviewer_materials/`

Supporting resources underlying the development and validation of the workflow described in the associated manuscript.

These materials include:

* a source-data spreadsheet underlying the quantitative analyses and figures presented in the manuscript.
* training images and corresponding ground-truth segmentation masks used for StarDist model development;
* the Jupyter notebook used for segmentation benchmarking;

Further information is provided in the [`reviewer_materials/README.md`](./reviewer_materials/README.md).


## Requirements

The segmentation and morphometric-analysis workflow requires:

* [QuPath](https://qupath.github.io/) (tested on version 0.7.0);
* the [QuPath StarDist extension](https://github.com/qupath/qupath-extension-stardist) (tested on version 0.6.0) ;
* one of the StarDist `.pb` models available in the [QuPath StarDist extension repository](https://github.com/qupath/models/tree/main/stardist) or in the [`models`](./models/) section of this repository.

Detailed instructions for implementation are described in the [`scripts`](./scripts/) directory.



## References

Schmidt, U., Weigert, M., Broaddus, C. & Myers, G. [Cell Detection with Star-convex Polygons](https://doi.org/10.1007/978-3-030-00934-2_30). Medical Image Computing and Computer Assisted Intervention – MICCAI 2018, 265–273 (2018). 
Bankhead, P. et al. [QuPath: Open source software for digital pathology image analysis](https://doi.org/10.1038/s41598-017-17204-5). Scientific Reports 7, 16878 (2017). 

