# A StarDist-based workflow for automated morphometric analysis of *Drosophila* salivary gland secretory granules.

This repository provides the StarDist models and QuPath scripts developed for automated segmentation and morphometric analysis of *Drosophila melanogaster* larval salivary gland secretory granules.  The workflow combines custom-trained StarDist models with QuPath to enable automated object detection, manual segmentation curation when required, and subsequent extraction of morphometric measurements.

This repository contains the models, scripts, example data, and supporting resources developed for the manuscript **"[A deep learning framework for quantitative analysis of secretory granule morphology using StarDist]"** (submitted).

## Repository contents

* [`Scripts`](./scripts/README.md) - Groovy scripts implementing the StarDist-based segmentation and measurement workflow in QuPath.
* [`Models`](./models/README.md) - Custom-trained StarDist models for secretory granule segmentation.
* [`Example images`](./example_images/README.md) - Representative microscopy images for testing the segmentation workflow with the supplied StarDist models and QuPath scripts.
* [`Reviewer materials`](./reviewer_materials/README.md) - Supporting resources underlying the development and validation of the workflow described in the associated manuscript.


## Requirements

The segmentation and morphometric-analysis workflow requires:

* [QuPath](https://qupath.github.io/) (tested on version 0.7.0).
* The [QuPath StarDist extension](https://github.com/qupath/qupath-extension-stardist) (tested on version 0.6.0).
* One of the StarDist `.pb` models available in the [QuPath StarDist extension repository](https://github.com/qupath/models/tree/main/stardist) or in the [`models`](./models/) section of this repository.

Detailed instructions for implementation are described in the [`scripts`](./scripts/) directory.



## References

* Schmidt, U., Weigert, M., Broaddus, C. & Myers, G. [Cell Detection with Star-convex Polygons](https://doi.org/10.1007/978-3-030-00934-2_30). Medical Image Computing and Computer Assisted Intervention – MICCAI 2018, 265–273 (2018). 
* Bankhead, P. et al. [QuPath: Open source software for digital pathology image analysis](https://doi.org/10.1038/s41598-017-17204-5). Scientific Reports 7, 16878 (2017). 

