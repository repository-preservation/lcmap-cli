(ns lcmap-cli.util)

;; add-usr-path and amend-usr-path blatantly ripped off from the
;; USGS-EROS/lcmap-chipmunk project on GitHub, created by
;; Jon Morton https://github.com/jmorton
;; 
(defn add-usr-path
  ""
  [& paths]
  (let [field (.getDeclaredField ClassLoader "usr_paths")]
    (try (.setAccessible field true)
         (let [original (vec (.get field nil))
               updated  (distinct (concat original paths))]
           (.set field nil (into-array updated)))
         (finally
           (.setAccessible field false)))))


(defn amend-usr-path
  ""
  [more-paths]
  (apply add-usr-path more-paths))
