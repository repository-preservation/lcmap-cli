(ns lcmap-cli.config)

(def environment
  nil)


(def grids
  {:conus {:ard "http://lcmap-test.cr.usgs.gov/ard_cu_c01_v01"
           :aux "http://lcmap-test.cr.usgs.gov/aux_cu_c01_v01"
           :ccdc "http://lcmap-test.cr.usgs.gov/ard_cu_c01_v01_aux_cu_v01_ccdc_1_0"
           :grid "/grid"
           :snap "/grid/snap"
           :near "/grid/near"
           :inventory "/inventory"
           :sources "/sources"
           :tile "/tile"
           :chip "/chip"
           :pixel "/pixel"
           :segment "/segment"
           :annual-prediction "/annual_prediction"}
   :alaska {:ard "http://host:port/ard_ak_c01_v01"
            :aux "http://host:port/aux_ak_v01"
            :ccdc "http://host:port/ard_ak_c01_v01_aux_ak_v01_ccdc_1_0"
            :grid "/grid"
            :snap "/grid/snap"
            :near "/grid/near"
            :inventory "/inventory"
            :sources "/sources"
            :tile "/tile"
            :chip "/chip"
            :pixel "/pixel"
            :segment "/segment"
            :annual-prediction "/annual_prediction"}
   :hawaii {:ard "http://host:port/ard_hi_c01_v01"
            :aux "http://host:port/aux_hi_v01"
            :ccdc "http://host:port/ard_hi_c01_v01_aux_hi_v01_ccdc_1_0"
            :grid "/grid"
            :snap "/grid/snap"
            :near "/grid/near"
            :inventory "/inventory"
            :sources "/sources"
            :tile "/tile"
            :chip "/chip"
            :pixel "/pixel"
            :segment "/segment"
            :annual-prediction "/annual_prediction"}})
