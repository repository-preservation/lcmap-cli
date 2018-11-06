# lcmap-cli
Command line interface for the LCMAP system

## Commands

| Command                      | Required Parameters  | Optional Parameters | Description               |
| ---------------------------- | ---------------------|---------------------| ------------------------- |
| lcmap grid                   |                      |                     | list configured grids     |
| lcmap grid show              | -g                   |                     | show grid configuration   |
| lcmap grid snap              | -g -x -y             |                     | snap point to tile/chip   |
| lcmap grid near              | -g -x -y             |                     | tile/chip xys near point  |
| lcmap tile lookup            | -g -t                |                     | look up tile x&y from tile|
| lcmap tile lookup            | -g -x -y             |                     | look up tile id from xy   |
| lcmap tile chips             | -g -t                |                     | list chip xys for tile    |
| lcmap ingest                 | -g -f                |                     | ingest a layer            |
| lcmap ingest list available  | -g -t                | -s -e -v            | list ingestable layers    |
| lcmap ingest list completed  | -g -t                | -s -e -v            | list ingested layers      |
| lcmap detect                 | -g -t                |                     | detect changes in tile    |
| lcmap detect list available  | -g                   | -v                  | list detectable tiles     |
| lcmap detect list completed  | -g                   | -v                  | listed detected tiles     |
| lcmap train                  | -g -t                |                     | train a model for a tile  |
| lcmap train list available   | -g                   | -v                  | list trainable tiles      | 
| lcmap train list completed   | -g                   | -v                  | list trained tiles        |
| lcmap predict                | -g -t                |                     | predict a tile            |
| lcmap predict list available | -g                   | -v                  | list predictable tiles    |
| lcmap predict list completed | -g                   | -v                  | list predicted tiles      |
| lcmap product maps           | -g --other-opts      | -v                  | create map products       |


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
