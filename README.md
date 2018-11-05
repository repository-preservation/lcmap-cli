# lcmap-cli
Command line interface for the LCMAP system

## Commands

| Command                                                  | Description               |
| -------------------------------------------------------- | ------------------------- |
| `lcmap tile lookup conus|alaska|hawaii hv`               | resolve h/v to x/y        |


lcmap grid
lcmap grid --id conus
lcmap grid --id alaska
lcmap grid --id hawaii
lcmap grid snap --id conus --x 123.0 --y 456.0
lcmap grid near --id conus --x 123.0 --y 456.0

lcmap tile xy    --grid conus --id 001001
lcmap tile id    --grid conus --x 67.2 --y 63.3
lcmap tile chips --grid conus --id 001001
lcmap tile near  --grid conus --id 001001


lcmap ard ingest --grid conus --source layerN.tiff
<what do we return here maybe nothing>

lcmap ard ingested --grid conus --tile 001001 [--start 1999/01/01 --end 2018/01/01 --verbose]
layer1.tiff
layer2.tiff

lcmap ard not-ingested --grid conus --tile 001001 [--start 1999/01/01 --end 2018/01/01 --verbose]
layer3.tiff
layer4.tiff

lcmap ard ingestable --grid conus --tile 001001 [--start 1999/01/01 --end 2018/01/01 --verbose]
layer1.tiff
layer2.tiff
layer3.tiff
layer4.tiff


| `lcmap tile reverse-lookup --grid=CONUS|Alaska|Hawaii hv`| resolve x/y to h/v        |
| `lcmap tile status`          | display tile status       |
| `lcmap tile chips`           | display chip x/y for tile |
| `lcmap ard  ingestable`      | list ingestable tiles     |
| `lcmap ard  ingested`        | list ingested tiles       |
| `lcmap ard  not-ingested`    | list uningested tiles     |
| `lcmap ard  ingest`          | ingest ard tile           |

| `lcmap ard  download`        | download ard data         |


| `lcmap ccdc detectable`      | list detectable tiles     |
| `lcmap ccdc detected`        | list detected tiles       |
| `lcmap ccdc not-detected`    | list undetected tiles     |
| `lcmap ccdc detect`          | detect changes in tiles   |
| `lcmap ccdc trainable`       | list trainable tiles      |
| `lcmap ccdc trained`         | list trained tiles        |
| `lcmap ccdc not-trained`     | list untrained tiles      |
| `lcmap ccdc train`           | train a tile model        |
| `lcmap ccdc classifiable`    | list classifiable tiles   |
| `lcmap ccdc classified`      | list classified tiles     |
| `lcmap ccdc not-classified`  | list unclassified tiles   |
| `lcmap ccdc classify`        | classify a tile           |
| `lcmap ccdc download`        | download ccdc results     |
| `lcmap products maps`        | create map products       |


| `lcmap conus tile lookup h01v01`          | resolve h/v to x/y        |
| `lcmap conus tile reverse-lookup`  | resolve x/y to h/v        |
| `lcmap conus tile status`          | display tile status       |
| `lcmap conus tile chips`           | display chip x/y for tile |
| `lcmap conus ard  ingestable`      | list ingestable tiles     |
| `lcmap conus ard  ingested`        | list ingested tiles       |
| `lcmap conus ard  not-ingested`    | list uningested tiles     |
| `lcmap conus ard  ingest`          | ingest ard tile           |
| `lcmap conus ard  download`        | download ard data         |
| `lcmap conus ccdc detectable`      | list detectable tiles     |
| `lcmap conus ccdc detected`        | list detected tiles       |
| `lcmap conus ccdc not-detected`    | list undetected tiles     |
| `lcmap conus ccdc detect`          | detect changes in tiles   |
| `lcmap conus ccdc trainable`       | list trainable tiles      |
| `lcmap conus ccdc trained`         | list trained tiles        |
| `lcmap conus ccdc not-trained`     | list untrained tiles      |
| `lcmap conus ccdc train`           | train a tile model        |
| `lcmap conus ccdc classifiable`    | list classifiable tiles   |
| `lcmap conus ccdc classified`      | list classified tiles     |
| `lcmap conus ccdc not-classified`  | list unclassified tiles   |
| `lcmap conus ccdc classify`        | classify a tile           |
| `lcmap conus ccdc download`        | download ccdc results     |
| `lcmap conus products maps`        | create map products       |

