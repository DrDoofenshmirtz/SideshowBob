(ns
  ^{:doc 
  
  "Define a hook supposed to be called when an SSB app is booted."
  
    :author "Frank Mosebach"}
  fm.ssb.boot
  (:require 
    [clojure.contrib.logging :as log]))

(defmacro def-boot-hook [hook]
  (let [hook-name (gensym "__boot-hook__")
        hook-meta {::hook {::name `'~hook-name ::type ::boot}}]    
   `(def ~(vary-meta hook-name merge hook-meta)
          (vary-meta ~hook merge ~hook-meta))))

(defn- hook-attributes [hook]
  (::hook (meta hook)))

(defn- boot-hook [hook]
  (when (= ::boot (-> hook hook-attributes ::type))
    hook))

(defn find-boot-hook [ns-name]
  (try
    (require ns-name)
    (->> (ns-interns ns-name)
         vals
         (some boot-hook))
    (catch Exception find-failed
      (log/error (format "Search for boot hook in '%s' failed!" ns-name) 
                 find-failed)
      nil)))

