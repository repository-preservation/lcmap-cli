# lcmap-cli
[![Build Status](https://travis-ci.org/USGS-EROS/lcmap-cli.svg?branch=develop)](https://travis-ci.org/USGS-EROS/lcmap-cli)

Command line interface for the LCMAP system

## Get the Code
[Releases](https://github.com/USGS-EROS/lcmap-cli/releases)/[Release tags](https://github.com/USGS-EROS/lcmap-cli/tags)

```bash
    $ git clone https://github.com/usgs-eros/lcmap-cli
    $ git checkout tags/<tag_name>
```

## Building
```bash
    $ lein bin
```

## Commands

| Command                     | Parameters                                          | Description                    |
| --------------------------- | --------------------------------------------------- | ------------------------------ |
| lcmap grids                 |                                                     | list configured grids          |
| lcmap grid                  | --grid --dataset                                    | show grid configuration        |
| lcmap snap                  | --grid --dataset --x --y                            | snap point to tile/chip        |
| lcmap near                  | --grid --dataset --x --y                            | tile/chip xys near point       |
| lcmap tile-to-xy            | --grid --dataset --tile                             | look up a tile xy from id      |
| lcmap xy-to-tile            | --grid --dataset --x --y                            | look up a tile id from xy      | 
| lcmap chips                 | --grid --dataset --tile                             | list chip xys for tile         |
| lcmap ingest                | --grid --dataset --source                           | ingest a layer                 |
| lcmap ingest-list-available | --grid --dataset --tile                             | list ingestable layers         |
| lcmap ingest-list-completed | --grid --dataset --tile                             | list ingested layers           |
| lcmap detect                | --grid --tile --acquired                            | detect changes for a tile      |
| lcmap detect-chip           | --grid --cx --cy --acquired                         | detect changes for a chip      |
| lcmap train                 | --grid --tile --acquired --date                     | train a model for a tile       |
| lcmap predict               | --grid --tile --month --day --acquired              | predict a tile                 |
| lcmap predict-chip          | --grid --tx --ty --cx --cy --month --day --acquired | predict a chip                 |
| lcmap product               | --grid --tile --names --years                       | generate product data for tile |
| lcmap product-chip          | --grid --cx --cy --names --years                    | generate product data for chip |
| lcmap raster                | --grid --tile --names --years                       | generate tile size map tiff    |

### Parameters

Not all commands accept all parameters.  Use lcmap <command> -h for usage

| Parameter   | Description                                       |
| ----------- | ------------------------------------------------- |
|  --grid     | grid identifier (conus, alaska, hawaii)           |
|  --tile     | tile identifier (001001, 012019, ...)             |
|  --dataset  | dataset identifier (ard, aux, ccdc)               |
|  --x        | horizontal projection coordinate                  |
|  --y        | vertical projection coordinate                    |
|  --tx       | tile x coordinate                                 |
|  --ty       | tile y coordinate                                 |
|  --cx       | chip x coordinate                                 |
|  --cy       | chip y coordinate                                 |
|  --source   | source layer filename (layer1.tiff, no path)      |
|  --acquired | iso8601 date range string (YYYY-MM-DD/YYYY-MM-DD) |
|  --names    | names of products to create, comma separated      |
|  --date     | iso8601 date string (YYYY-MM-DD)                  |
|  --day      | day of week (numerical)                           |
|  --month    | month (numerical)                                 |
|  --years    | years for which product values are calculated     |
|  --verbose  | display additional information                    |
| -h --help   | display help                                      |

## Configuration
lcmap-cli requires n config file at ~/.usgs/lcmap-cli.edn.

```edn

{:http-options  {:timeout 2400000}
 :grids {:conus  {:ard "http://host:port/path"
                  :aux "http://host:port/path"
                  :ccdc "http://host:port/path"
                  :grid "/grid"
                  :snap "/grid/snap"
                  :near "/grid/near"
                  :inventory "/inventory"
                  :sources "/sources"
                  :tile "/tile"
                  :chip "/chip"
                  :pixel "/pixel"
                  :segment "/segment"
                  :prediction "/prediction"
                  :product "/product"
                  :raster "/raster"
                  :segment-instance-count 25
                  :prediction-instance-count 1
                  :prediction-sleep-for 1000
                  :product-instance-count 1
                  :raster-instance-count 1
                  :segment-sleep-for 1000
                  :product-month "07"
                  :product-day "01"}
         :alaska {:ard "http://host:port/path"
                  :aux "http://host:port/path"
                  :ccdc "http://host:port/path"
                  :grid "/grid"
                  :snap "/grid/snap"
                  :near "/grid/near"
                  :inventory "/inventory"
                  :sources "/sources"
                  :tile "/tile"
                  :chip "/chip"
                  :pixel "/pixel"
                  :segment "/segment"
                  :prediction "/prediction"
                  :product "/product"
                  :raster "/raster"
                  :segment-instance-count 1
                  :prediction-instance-count 1
                  :prediction-sleep-for 1000
                  :product-instance-count 1
                  :raster-instance-count 1
                  :segment-sleep-for 1000
                  :product-month "07"
                  :product-day "01"}
         :hawaii {:ard "http://host:port/path"
                  :aux "http://host:port/path"
                  :ccdc "http://host:port/path"
                  :grid "/grid"
                  :snap "/grid/snap"
                  :near "/grid/near"
                  :inventory "/inventory"
                  :sources "/sources"
                  :tile "/tile"
                  :chip "/chip"
                  :pixel "/pixel"
                  :segment "/segment"
                  :prediction "/prediction"
                  :product "/product"
                  :raster "/raster"
                  :segment-instance-count 1
                  :prediction-instance-count 1
                  :prediction-sleep-for 1000
                  :product-instance-count 1
                  :raster-instance-count 1
                  :segment-sleep-for 1000
                  :product-month "07"
                  :product-day "01"}}}
```

## Examples

```bash

   # Detect changes in a tile
   # Successful chips go to standard out
   # Errors go to standard error
	
   $ lcmap detect --grid conus \
                  --tile 025007 \
                  --acquired 1982-01-01/2017-12-31 \
                  >> 025007-success.txt 2>> 025007-error.txt
	
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
                         --acquired 1982-01-01/2017-12-31 \
                         >> $cx_$cy-success.txt 2>> $cx_$cy-error.txt;
       echo "{cx:$cx, cy:$cy}";
   done
	
	
   # Running a single chip.
   # Result evaluation/filling in errors & timeouts only...
   # ... starting up the JVM with Clojure is slow & inefficient
   # Use lcmap detect for tile sized runs.
    
   $ lcmap detect-chip --cx 1484415 \
                       --cy 2414805 \
                       --grid conus \
                       --acquired 1982-01-01/2017-12-31;
   {"acquired":"1982-01-01/2017-12-31","cx":1484415,"cy":2414805}
	
   # Schedule change detection in bulk.

   time lcmap detect --grid conus \
                     --tile 028006 \
                     --acquired 1982-01-01/2017-12-31 \
                     >> ~/028006-success.txt 2>> ~/028006-error.txt;
					  
   time lcmap detect --grid conus \
                     --tile 028008 \
                     --acquired 1982-01-01/2017-12-31 \
                     >> ~/028008-success.txt 2>> ~/028008-error.txt;
					  
   time lcmap detect --grid conus \
                     --tile 029006 \
                     --acquired 1982-01-01/2017-12-31 \
                     >> ~/029006-success.txt 2>> ~/029006-error.txt;
					  
   time lcmap detect --grid conus \
                     --tile 029007 \
                     --acquired 1982-01-01/2017-12-31 \
                     >> ~/029007-success.txt 2>> ~/029007-error.txt;
					  
   time lcmap detect --grid conus \
                     --tile 029008 \
                     --acquired 1982-01-01/2017-12-31 \
                     >> ~/029008-success.txt 2>> ~/029008-error.txt;

   
   # Training tiles
   $ lcmap train --tile 032003 \
                 --grid conus \
                 --acquired 1982/2018 \
                 --date 2001-07-01

    {"acquired":"1982/2018","chips":22500,"date":"2001-07-01","tx":2234415,"ty":2864805}

   
   # Generating predicted probabilities
   $ lcmap predict --tile 028008 \
                   --grid conus \
                   --acquired 1982/2018 \
                   --month 7 \
                   --day 1

    # successful predictions 
	
    {"acquired":"1982/2018","cx":2339415,"cy":2720805,"day":"1","month":"7","tx":2234415,"ty":2864805}
    {"acquired":"1982/2018","cx":2336415,"cy":2729805,"day":"1","month":"7","tx":2234415,"ty":2864805}
    {"acquired":"1982/2018","cx":2336415,"cy":2723805,"day":"1","month":"7","tx":2234415,"ty":2864805}

    # unsuccessful predictions

    {"tx":2234415.0,"ty":2864805.0,"cx":2372415.0,"cy":2789805.0,"month":"7","day":"1","acquired":"1982/2018","error":"{:response {:opts {:timeout 43200000, :body \"{\\\"tx\\\":2234415.0,\\\"ty\\\":2864805.0,\\\"month\\\":\\\"7\\\",\\\"day\\\":\\\"1\\\",\\\"acquired\\\":\\\"1982/2018\\\",\\\"cx\\\":2372415.0,\\\"cy\\\":2789805.0}\", :headers {\"Content-Type\" \"application/json\"}, :method :post, :url \"http://host/path/prediction\"}, :body \"{\\\"acquired\\\":\\\"1982/2018\\\",\\\"cx\\\":2372415,\\\"cy\\\":2789805,\\\"day\\\":\\\"1\\\",\\\"exception\\\":\\\"predictions exception: Input numpy.ndarray must be 2 dimensional\\\",\\\"month\\\":\\\"7\\\",\\\"tx\\\":2234415,\\\"ty\\\":2864805}\\n\", :headers {:connection \"keep-alive\", :content-length \"178\", :content-type \"application/json\", :date \"Thu, 13 Jun 2019 17:23:42 GMT\", :server \"nginx/1.12.2\"}, :status 500}}"}

   $ lcmap predict-chip --tx 123456 \
                        --ty 654321 \
                        --cx 123456 \
                        --cy 654321 \
                        --grid conus \
                        --acquired 1982/2018 \
                        --month 7 \
                        --day 1
   
   # success
   
    {"acquired":"1982/2018","cx":123456,"cy":654321,"day":"1","month":"7","tx":123456,"ty":654321}
   
   # failure
   
    {"tx":123456.0,"ty":654321.0,"cx":123456.0,"cy":654321.0,"month":"7","day":"1","acquired":"1982/2018","error":"{:response {:opts {:timeout 43200000, :body \"{\\\"tx\\\":123456.0,\\\"ty\\\":654321.0,\\\"month\\\":\\\"7\\\",\\\"day\\\":\\\"1\\\",\\\"acquired\\\":\\\"1982/2018\\\",\\\"cx\\\":123456.0,\\\"cy\\\":654321.0}\", :headers {\"Content-Type\" \"application/json\"}, :method :post, :url \"http://host/path/prediction\"}, :body \"{\\\"acquired\\\":\\\"1982/2018\\\",\\\"cx\\\":123456,\\\"cy\\\":654321,\\\"day\\\":\\\"1\\\",\\\"exception\\\":\\\"predictions exception: Input numpy.ndarray must be 2 dimensional\\\",\\\"month\\\":\\\"7\\\",\\\"tx\\\":123456,\\\"ty\\\":654321}\\n\", :headers {:connection \"keep-alive\", :content-length \"178\", :content-type \"application/json\", :date \"Thu, 13 Jun 2019 17:23:42 GMT\", :server \"nginx/1.12.2\"}, :status 500}}"}

   

   # Creating Products
   # Before we can produce a map, we need to have the product values calculated for 
   # every chip in the requested tile.
   $ lcmap product --grid conus \
                   --tile 027008 \
                   --names length-of-segment,time-since-change \
                   --years 2002/2006 \
                   >> 2002_2006_product_success.txt 2>> 2002_2006_product_errors.txt;

   # example stdout output:
   # {"product":["length-of-segment", "time-since-change"],"cx":1484415.0,"cy":2111805.0,"dates":["2002-07-01"]}
   # {"product":["length-of-segment", "time-since-change"],"cx":1484415.0,"cy":2114805.0,"dates":["2002-07-01"]}
   # ...

   # example stderr output if there was a problem calculating product values for a chip
   # {"failed_dates":[{"2002-07-01": "validation error"}],"product":"length-of-segment","cx":1484415.0,"cy":2114805.0,"dates":["2002-07-01"]}
   # example stderr output if there was a problem parsing the maps request
   # {"error":"problem processing /products request: 'helpful_error_message'", "dates":"2002/2006", "product":"length-of-segment", "tile":"027008", "grid":"conus"}
   

   # Creating Maps
   # Producing tile sized product maps
   $ lcmap raster --grid conus \
                  --tile 027008 \
                  --names length-of-segment \
                  --years 2002/2006 \
                  >> 2002_2006_maps_success.txt 2>> 2002_2006_raster_errors.txt;

   # example stdout output:
   # {"tile":"027008","date":"2002-07-01","grid":"conus","tiley":2114805.0,"tilex":1484415.0,"product":["length-of-segment"],"resource":"maps","map_name":"LCMAP-CU-027008-2002-20190320-V01-SCSTAB.tif"}

   # example stderr output:
   # {"error":"problem processing /maps request: 'helpful_error_message'", "date":"2002-07-01", "tile":"027008", "tilex":"111111", "tiley":"222222", "product":"length-of-segment"}
      

```
