# lcmap-cli
Command line interface for the LCMAP system

## Commands

| Command                                                                                            | Description               |
| ---------------------------------------------------------------------------------------------------| ------------------------- |
| `lcmap grid`                                                                                       | list configured grids     |
| `lcmap grid --id [conus,alaska,hawaii]`                                                            | display grid configuration|
| `lcmap snap --grid conus --x 123.0 --y 456.0`                                                      | snap point to tile/chip   |
| `lcmap near --grid conus --x 123.0 --y 456.0`                                                      | tile/chip xys near point  |
| `lcmap tile xy --grid conus --id 001001`                                                           | look up tile xy from id   |
| `lcmap tile id --grid conus --x 67.2 --y 63.3`                                                     | look up tile id from xy   |
| `lcmap chips --grid conus --tile 001001`                                                           | list chip xys for tile id |
| `lcmap ingest execute --grid conus --source layerN.tiff`                                           | ingest a layer            |
| `lcmap ingest available --grid conus --tile 001001 [--start 1999/01/01 --end 2018/01/01 --verbose]`| list ingestable layers    |
| `lcmap ingest complete --grid conus --tile 001001 [--start 1999/01/01 --end 2018/01/01 --verbose]` | list ingested layers   |
| `lcmap detect execute --grid conus --tile 001001`                                                  | detect changed in tile    |
| `lcmap detect available --grid conus --verbose`                                                    | list detectable tiles     |
| `lcmap detect complete --grid conus --verbose`                                                     | listed detected tiles     |
| `lcmap train execute --grid conus --tile 001001`                                                   | train a model for a tile  |
| `lcmap train available --grid conus --verbose`                                                     | list trainable tiles      | 
| `lcmap train complete --grid conus --verbose`                                                      | list trained tiles        |
| `lcmap predict execute --grid conus --tile 001001`                                                 | classify a tile           |
| `lcmap predict available --grid conus --verbose`                                                   | classify a tile           |
| `lcmap predict complete --grid conus --verbose`                                                    | classify a tile           |
| `lcmap products maps`                                                                              | create map products       |
