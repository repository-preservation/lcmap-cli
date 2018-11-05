# lcmap-cli
Command line interface for the LCMAP system

## Commands

| Command                                                                                                | Description               |
| -------------------------------------------------------------------------------------------------------| ------------------------- |
| `lcmap grid`                                                                                           | list configured grids     |
| `lcmap grid --id [conus|alaska|hawaii]`                                                                  | display grid configuration|
| `lcmap grid snap --id conus --x 123.0 --y 456.0`                                                       | snap point to tile/chip   |
| `lcmap grid near --id conus --x 123.0 --y 456.0`                                                       | tile/chip xys near point  |
| `lcmap tile xy --grid conus --id 001001`                                                               | look up tile xy from id   |
| `lcmap tile id --grid conus --x 67.2 --y 63.3`                                                         | look up tile id from xy   |
| `lcmap tile chips --grid conus --id 001001`                                                            | list chip xys for tile id |
| `lcmap ard ingest execute --grid conus --source layerN.tiff`                                           | ingest a layer            |
| `lcmap ard ingest available --grid conus --tile 001001 [--start 1999/01/01 --end 2018/01/01 --verbose]`| list ingestable layers    |
| `lcmap ard ingest complete --grid conus --tile 001001 [--start 1999/01/01 --end 2018/01/01 --verbose]` | list ingested layers   |
| `lcmap ccdc detect execute --grid conus --tile 001001`                                                 | detect changed in tile    |
| `lcmap ccdc detect available --grid conus --verbose`                                                   | list detectable tiles     |
| `lcmap ccdc detect complete --grid conus --verbose`                                                    | listed detected tiles     |
| `lcmap ccdc train execute --grid conus --tile 001001`                                                  | train a model for a tile  |
| `lcmap ccdc train available --grid conus --verbose`                                                    | list trainable tiles      | 
| `lcmap ccdc train complete --grid conus --verbose`                                                     | list trained tiles        |
| `lcmap ccdc predict execute --grid conus --tile 001001`                                                | classify a tile           |
| `lcmap ccdc predict available --grid conus --verbose`                                                  | classify a tile           |
| `lcmap ccdc predict complete --grid conus --verbose`                                                   | classify a tile           |
| `lcmap products maps`                                                                                  | create map products       |
