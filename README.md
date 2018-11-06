# lcmap-cli
Command line interface for the LCMAP system

## Commands

| Command                                                                                               | Description               |
| ----------------------------------------------------------------------------------------------------- | ------------------------- |
| lcmap grid                                                                                            | list configured grids     |
| lcmap grid show --grid [conus,alaska,hawaii]                                                          | show grid configuration   |
| lcmap grid snap --grid conus --x 123.0 --y 456.0                                                      | snap point to tile/chip   |
| lcmap grid near --grid conus --x 123.0 --y 456.0                                                      | tile/chip xys near point  |
| lcmap tile lookup --grid conus --tile 001001                                                          | look up tile x&y from tile|
| lcmap tile lookup --grid conus --x 67.2 --y 63.3                                                      | look up tile id from xy   |
| lcmap tile chips  --grid conus --tile 001001                                                          | list chip xys for tile    |
| lcmap ingest --grid conus --source layerN.tiff                                                        | ingest a layer            |
| lcmap ingest list available --grid conus --tile 001001 [--start 1999/01/01 --end 2018/01/01 --verbose]| list ingestable layers    |
| lcmap ingest list completed --grid conus --tile 001001 [--start 1999/01/01 --end 2018/01/01 --verbose]| list ingested layers      |
| lcmap detect --grid conus --tile 001001                                                               | detect changes in tile    |
| lcmap detect list available --grid conus --verbose                                                    | list detectable tiles     |
| lcmap detect list completed --grid conus --verbose                                                    | listed detected tiles     |
| lcmap train --grid conus --tile 001001                                                                | train a model for a tile  |
| lcmap train list available --grid conus --verbose                                                     | list trainable tiles      | 
| lcmap train list completed --grid conus --verbose                                                     | list trained tiles        |
| lcmap predict --grid conus --tile 001001                                                              | predict a tile            |
| lcmap predict list available --grid conus --verbose                                                   | list predictable tiles    |
| lcmap predict list completed --grid conus --verbose                                                   | list predicted tiles      |
| lcmap product maps --grid conus --other-opts --verbose                                                | create map products       |


### Parameters

Not all commands accept all parameters.  Use lcmap <command> <subcommand> -h for usage

| Parameter   | Description                              |
| ----------- | ---------------------------------------- |
| --grid -g   | grid identifier (conus, alaska, hawaii)  |
| --tile -t   | tile idenifier (001001, 012019)          |
| --x -x      | horizontal projection coordinate         |
| --y -y      | vertical projection coordinate           |
| --source -s | source layer filename (no path           |
| --verbose -v| display additional information           |
| --help -h   | display help                             |
