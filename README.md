# A StarDist-based workflow for automated morphometric analysis of *Drosophila* salivary gland secretory granules.

This repository provides a ready-to-use workflow for automated segmentation and morphometric analysis of *Drosophila melanogaster* larval salivary gland secretory granules with StarDist on QuPath. It includes two custom-trained StarDist models and QuPath scripts required to implement the workflow.

The repository was developed for the manuscript **“A deep learning framework for quantitative analysis of secretory granule morphology using StarDist”** (submitted). Supporting materials for manuscript review, including training data, benchmarking resources, and figure source data, are also provided in the [`reviewer materials`](./reviewer_materials) section.

## Repository contents

* [`Workflow`](./workflow) - Ready-to-use QuPath workflow, including the Groovy scripts, trained StarDist models, and detailed instructions for implementation.
* [`Reviewer materials`](./reviewer_materials) - Supporting resources underlying the development and validation of the workflow described in the associated manuscript.


## Requirements

The segmentation and morphometric analysis workflow requires:

* [QuPath](https://qupath.github.io/) (tested on version 0.7.0).
* The [StarDist extension](https://github.com/qupath/qupath-extension-stardist) for QuPath (tested on version 0.6.0).
* One of the StarDist `.pb` models available in the [QuPath StarDist extension repository](https://github.com/qupath/models/tree/main/stardist) or in the [`workflow`](./workflow/) section of this repository.

Detailed instructions for implementation are described in the [`workflow`](./workflow/) section.



## References

* Schmidt, U., Weigert, M., Broaddus, C. & Myers, G. [Cell Detection with Star-convex Polygons](https://doi.org/10.1007/978-3-030-00934-2_30). Medical Image Computing and Computer Assisted Intervention – MICCAI 2018, 265–273 (2018). 
* Bankhead, P. et al. [QuPath: Open source software for digital pathology image analysis](https://doi.org/10.1038/s41598-017-17204-5). Scientific Reports 7, 16878 (2017). 

