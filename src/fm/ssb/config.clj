(ns
  ^{:doc 
  
  "Load an SSB app configuration from a config file."
  
    :author "Frank Mosebach"}
  fm.ssb.config
  (:require
    [clojure.contrib.logging :as log]
    [fm.ssb.boot :as boot]))

(def ^{:private true :const true} default-config
                                  {:app-name     "app"
                                   :ws-service   {:port     17500
                                                  :services []}   
                                   :http-service {:port      20500
                                                  :root-path "."
                                                  :app-path  "index.html"}})

(defn- quote-values [config & keys]
  (reduce (fn [config key]
            (if-let [value (key config)]
              (assoc config key `'~value)
              config)) 
          config 
          keys))

(defn- complete-ws-config [template config]
  (update-in template [:ws-service] #(-> %
                                         (merge (:ws-service config))
                                         (quote-values :boot :services))))

(defn- complete-http-config [template config]
  (if-let [http-service (:http-service config)]
    (update-in template [:http-service] merge http-service)
    (dissoc template :http-service)))

(defn- complete-config [config app-name]
  (-> default-config
      (complete-ws-config config)
      (complete-http-config config)
      (assoc :app-name (str app-name))))

(defmacro defapp [app-name & {:as config}]
  (let [config (complete-config config app-name)]
    `(def ~'config- ~config)))

(defn- call-boot-hook [config]
  (if-let [boot-ns (get-in config [:ws-service :boot])]
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
  (let [config-ns (create-ns 'fm.ssb.config._)]
    (if-let [config-var (try
                          (binding [*ns* config-ns]
                            (refer-clojure)
                            (use '[fm.ssb.config :only (defapp)])
                            (load-file config-path))
                          (ns-resolve config-ns 'config-)
                          (catch Exception invalid-config
                            (log/error "Invalid app config!" invalid-config)
                            nil)
                          (finally
                            (remove-ns 'fm.ssb.config._)))]
      (call-boot-hook @config-var)
      default-config)))

(def ^{:private true :const true} config-key ::config)

(defn- with-config [connection config]
  (assoc connection config-key config))

(defn connection-handler [config]
  (if config
    (fn [connection]
      (log/debug (format "Attach config to connection (id: %s)." 
                         (:id connection)))
      (with-config connection config))
    identity))

(defn get-config [connection]
  (get connection config-key))

