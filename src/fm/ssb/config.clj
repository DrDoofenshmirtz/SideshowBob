(ns
  ^{:doc 
  
  "Load an SSB app configuration from a config file."
  
    :author "Frank Mosebach"}
  fm.ssb.config
  (:require
    [clojure.contrib.logging :as log]
    [fm.websockets.resources :as rsc]
    [fm.ssb.boot :as boot]))

(def ^{:private true :const true} default-config 
                                  {:ws-port   17500
                                   :http-port 20500
                                   :root-path "."
                                   :app-path  "index.html"
                                   :services  []})

(defn- quote-values [config & keys]
  (reduce (fn [config key]
            (if-let [value (key config)]
              (assoc config key `'~value)
              config)) 
          config 
          keys))

(defmacro defapp [app-name & {:as config}]
  (let [config (merge default-config config)
        config (quote-values config :boot :services)
        config (assoc config :app-name (str app-name))]
    `(def ~'config- ~config)))

(defn- call-boot-hook [config]
  (if-let [boot-ns (:boot config)]
    (if-let [boot-hook (boot/find-boot-hook boot-ns)]
      (try
        (boot-hook config)
        (catch Exception boot-error
          (log/error "Boot hook failed!" boot-error)
          nil))
      (throw (IllegalStateException. 
               (format "No boot hook found in namespace '%s'!" boot-ns))))
    config))

(defn load-config [config-path]
  (when (let [current-ns (-> *ns* str symbol)] 
          (try
            (in-ns 'fm.ssb.config._)
            (refer-clojure)
            (use '[fm.ssb.config :only (defapp)])
            (load-file config-path)
            true
            (catch Exception invalid-config
              (log/error "Invalid app config!" invalid-config)
              false)
            (finally 
              (in-ns current-ns))))
    (when-let [config (ns-resolve 'fm.ssb.config._ 'config-)]
      (call-boot-hook @config))))

(def ^{:private true :const true} config-key :fm.ssb/config)

(defn- store-config [connection config]
  (rsc/store! connection config-key config :connection))

(defn connection-handler [config]
  (fn [connection]
    (store-config connection config)
    connection))

(defn get-config [connection]
  (rsc/get-resource connection config-key))

