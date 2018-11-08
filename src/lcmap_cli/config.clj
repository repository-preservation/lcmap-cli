(ns lcmap-cli.config)

(def environment
  nil)


(def grids
  {:conus {:ard "http://host:port/ard_cu_c01_v01"
           :aux "http://host:port/aux_cu_c01_v01"
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
            :grid "/grid"
            :snap "/grid/snap"
            :near "/grid/near"
            :inventory "/inventory"
            :sources "/sources"
            :tile "/tile"
            :chip "/chip"
            :pixel "/pixel"
            :segment "/segment"}
   :hawaii {:ard "http://host:port/ard_hi_c01_v01"
            :aux "http://host:port/aux_hi_v01"
            :grid "/grid"
            :snap "/grid/snap"
            :near "/grid/near"
            :inventory "/inventory"
            :sources "/sources"
            :tile "/tile"
            :chip "/chip"
            :pixel "/pixel"
            :segment "/segment"}})
