(ns lcmap-cli.numbers)

(defmulti numberize type)
    
(defmethod numberize :default [n]
  nil)

(defmethod numberize Number [number]
  number)

(defmethod numberize String [string]
  (let [number-format (java.text.NumberFormat/getInstance)]
    (try
      (.parse number-format string)
      (catch java.text.ParseException ex nil))))
