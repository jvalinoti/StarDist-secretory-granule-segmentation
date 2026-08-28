# StarDist Models

This directory contains the trained StarDist models used for secretory granule segmentation in the associated manuscript.

## Models

### WT-trained.pb

StarDist model trained using 13 images of wild-type (WT) salivary glands with mature secretory granules.

### Phenotype-trained.pb

StarDist model trained with the same 13 images used for WT-trained model, plus 7 additional images of non-WT salivary glands displaying heterogeneous granule sizes.

## Model training and conversion

StarDist models were trained following the workflow described by Pécot et al. (2022). The trained models were subsequently converted to frozen TensorFlow .pb format for use with the StarDist extension in QuPath, following the model-conversion procedure described in the QuPath models repository.

The .pb files provided in this directory can be selected directly when running Script_1_detections.groovy.

## References

Pécot, T., Cuitiño, M. C., Johnson, R. H., Timmers, C. & Leone, G. Deep learning tools and modeling to estimate the temporal expression of cell cycle proteins from 2D still images. *PLoS Computational Biology* **18**, e1009949 (2022).

QuPath models repository: https://github.com/qupath/models/tree/main/stardist
