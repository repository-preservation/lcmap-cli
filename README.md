# lcmap-cli
Command line interface for the LCMAP system

## Commands

| Command                      | Required Parameters  | Optional Parameters | Description                     |
| ---------------------------- | ---------------------|---------------------| ------------------------------- |
| lcmap grids                  |                      |                     | list configured grids           |
| lcmap grid                   | -g                   |                     | show grid configuration         |
| lcmap snap                   | -g -x -y             |                     | snap point to tile/chip         |
| lcmap near                   | -g -x -y             |                     | tile/chip xys near point        |
| lcmap tile                   | -g -t | -g -x -y     |                     | look up a tile id or xy         |
| lcmap chips                  | -g -t                |                     | list chip xys for tile          |
| lcmap ingest                 | -g -f                |                     | ingest a layer                  |
| lcmap ingest-list-available  | -g -t                | -s -e -v            | list ingestable layers          |
| lcmap ingest-list-completed  | -g -t                | -s -e -v            | list ingested layers            |
| lcmap detect                 | -g -t | -g -x -y     |                     | detect changes for tile or chip |
| lcmap train                  | -g -t                |                     | train a model for a tile        |
| lcmap predict                | -g -t | -g -x -y     |                     | predict a tile or chip          |
| lcmap products               | -g --other-opts      | -v                  | create map products             |


### Parameters

Not all commands accept all parameters.  Use lcmap <command> <subcommand> -h for usage

| Parameter     | Description                                  |
| ------------- | -------------------------------------------- |
| -g --grid     | grid identifier (conus, alaska, hawaii)      |
| -t --tile     | tile identifier (001001, 012019)             |
| -x --x        | horizontal projection coordinate             |
| -y --y        | vertical projection coordinate               |
| -f --source   | source layer filename (layer1.tiff, no path) |
| -s --start    | start date (YYYY/MM/DD)                      |
| -e --end      | end date (YYYY/MM/DD)                        |
| -v --verbose  | display additional information               |
| -h --help     | display help                                 |
