# lcmap-cli
Command line interface for the LCMAP system

## Commands

| Command                      | Required Parameters       | Optional Parameters  | Description                     |
| ---------------------------- | ------------------------- |--------------------- | ------------------------------- |
| lcmap grids                  |                           |                      | list configured grids           |
| lcmap grid                   | --grid --dataset          |                      | show grid configuration         |
| lcmap snap                   | --grid --dataset --x --y  |                      | snap point to tile/chip         |
| lcmap near                   | --grid --dataset --x --y  |                      | tile/chip xys near point        |
| lcmap tile-to-xy             | --grid --dataset --tile   |                      | look up a tile xy from id       |
| lcmap xy-to-tile             | --grid --dataset --x --y  |                      | look up a tile id from xy       | 
| lcmap chips                  | --grid --dataset --tile   |                      | list chip xys for tile          |
| lcmap ingest                 | --grid --dataset --source |                      | ingest a layer                  |
| lcmap ingest-list-available  | --grid --dataset --tile   | --start --end        | list ingestable layers          |
| lcmap ingest-list-completed  | --grid --dataset --tile   | --start --end        | list ingested layers            |
| lcmap detect-tile            | --grid --tile             |                      | detect changes for a tile       |
| lcmap detect-chip            | --grid --cx --cy          |                      | detect changes for a chip       |
| lcmap train                  | --grid --tile             |                      | train a model for a tile        |
| lcmap predict-tile           | --grid --tile             |                      | predict a tile                  |
| lcmap predict-chip           | --grid --cx --cy          |                      | predict a chip                  |
| lcmap products               | --grid --other-opts       |                      | create map products             |


### Parameters

Not all commands accept all parameters.  Use lcmap <command> <subcommand> -h for usage

| Parameter   | Description                                  |
| ----------- | -------------------------------------------- |
|  --grid     | grid identifier (conus, alaska, hawaii)      |
|  --tile     | tile identifier (001001, 012019, ...)        |
|  --dataset  | dataset identifier (ard, aux, ccdc)          |
|  --x        | horizontal projection coordinate             |
|  --y        | vertical projection coordinate               |
|  --cx       | chip x coordinate                            |
|  --cy       | chip y coordinate                            |
|  --source   | source layer filename (layer1.tiff, no path) |
|  --start    | start date (YYYY/MM/DD)                      |
|  --end      | end date (YYYY/MM/DD)                        |
|  --verbose  | display additional information               |
| -h --help   | display help                                 |


## Example Change Detection Workflow

```bash

    # Detect changes in a tile
    # Successful chips go to standard out
    # Errors go to standard error
	
    $ lcmap detect-tile --grid CONUS --tile 025007 >> 025007-success.txt 2>> 025007-error.txt
	
    # Fill in any chips that experienced errors without re-running the whole tile
	
    # Get chips x and y coordinates as bash arrays
    $ xs=(`cat 025007-error.txt | jq .[.x]`)
    $ ys=(`cat 025007-error.txt | jq .[.y]`)

    # Iterate and run individual chips
    for index in $(seq 0 $((${#xs[@]} - 1)));
    do
        cx=${xs[$index]}
        cy=${xy]$index]}
        lcmap detect-chip --grid CONUS --cx $cx --cy $cy >> $cx_$cy-success.txt 2>> $cx_$cy-error.txt;
    done	
```
