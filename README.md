# lcmap-cli
Command line interface for the LCMAP system

## Get the Code
Released code is available on the master branch.  Release versions are tagged.

git clone https://github.com/usgs-eros/lcmap-cli

## Building
lein bin

## Commands

| Command                      | Required Parameters                | Description                     |
| ---------------------------- | ---------------------------------- | ------------------------------- |
| lcmap grids                  |                                    | list configured grids           |
| lcmap grid                   | --grid --dataset                   | show grid configuration         |
| lcmap snap                   | --grid --dataset --x --y           | snap point to tile/chip         |
| lcmap near                   | --grid --dataset --x --y           | tile/chip xys near point        |
| lcmap tile-to-xy             | --grid --dataset --tile            | look up a tile xy from id       |
| lcmap xy-to-tile             | --grid --dataset --x --y           | look up a tile id from xy       | 
| lcmap chips                  | --grid --dataset --tile            | list chip xys for tile          |
| lcmap ingest                 | --grid --dataset --source          | ingest a layer                  |
| lcmap ingestable             | --grid --dataset --tile --acquired | list ingestable layers          |
| lcmap ingested               | --grid --dataset --tile --acquired | list ingested layers            |
| lcmap detect                 | --grid --tile --acquired           | detect changes for a tile       |
| lcmap detect-chip            | --grid --cx --cy --acquired        | detect changes for a chip       |
| lcmap train                  | --grid --tile                      | train a model for a tile        |
| lcmap predict                | --grid --tile                      | predict a tile                  |
| lcmap predict-chip           | --grid --cx --cy                   | predict a chip                  |
| lcmap products               | --grid --other-args                | create map products             |


### Parameters

Not all commands accept all parameters.  Use lcmap <command> -h for usage

| Parameter   | Description                                       |
| ----------- | ------------------------------------------------- |
|  --grid     | grid identifier (conus, alaska, hawaii)           |
|  --tile     | tile identifier (001001, 012019, ...)             |
|  --dataset  | dataset identifier (ard, aux, ccdc)               |
|  --x        | horizontal projection coordinate                  |
|  --y        | vertical projection coordinate                    |
|  --cx       | chip x coordinate                                 |
|  --cy       | chip y coordinate                                 |
|  --source   | source layer filename (layer1.tiff, no path)      |
|  --acquired | iso8601 date range string (YYYY-MM-DD/YYYY-MM-DD) |
|  --verbose  | display additional information                    |
| -h --help   | display help                                      |


## Examples

```bash

   # Detect changes in a tile
   # Successful chips go to standard out
   # Errors go to standard error
	
   $ lcmap detect --grid conus --tile 025007 --acquired 1982-01-01/2017-12-31 >> 025007-success.txt 2>> 025007-error.txt
	
   # Fill in any chips that experienced errors without re-running the whole tile
	
   # Get chips x and y coordinates as bash arrays
   $ xs=(`cat 025007-error.txt | jq '.cx'`)
   $ ys=(`cat 025007-error.txt | jq '.cy'`)

   # Iterate and run individual chips
   for index in $(seq 0 $((${#xs[@]} - 1)));
   do
       cx=${xs[$index]};
       cy=${xy[$index]};
       lcmap detect-chip --cx $cx \
	                 --cy $cy \
			 --grid conus \
			 --acquired 1982-01-01/2017-12-31 >> $cx_$cy-success.txt 2>> $cx_$cy-error.txt;
       echo "{cx:$cx, cy:$cy}";
   done
	
	
   # Running a single chip.
   # Result evaluation/filling in errors & timeouts only...
   # ... starting up the JVM with Clojure is slow & inefficient
   # Use lcmap detect for tile sized runs.
    
   $ lcmap detect-chip --cx 1484415 --cy 2414805 --grid conus --acquired 1982-01-01/2017-12-31;
   {"acquired":"1982-01-01/2017-12-31","cx":1484415,"cy":2414805}
	
   # Schedule change detection in bulk.

   time lcmap detect --grid conus \
                     --tile 028006 \
		     --acquired 1982-01-01/2017-12-31 \
		     >> ~/devops/028006-success.txt 2>> ~/devops/028006-error.txt;
					  
   time lcmap detect --grid conus \
                     --tile 028008 \
	             --acquired 1982-01-01/2017-12-31 \
		     >> ~/devops/028008-success.txt 2>> ~/devops/028008-error.txt;
					  
   time lcmap detect --grid conus \
                     --tile 029006 \
	             --acquired 1982-01-01/2017-12-31 \
	             >> ~/devops/029006-success.txt 2>> ~/devops/029006-error.txt;
					  
   time lcmap detect --grid conus \
                     --tile 029007 \
	             --acquired 1982-01-01/2017-12-31 \
	             >> ~/devops/029007-success.txt 2>> ~/devops/029007-error.txt;
					  
   time lcmap detect --grid conus \
                     --tile 029008 \
	             --acquired 1982-01-01/2017-12-31 \
	             >> ~/devops/029008-success.txt 2>> ~/devops/029008-error.txt;
```
