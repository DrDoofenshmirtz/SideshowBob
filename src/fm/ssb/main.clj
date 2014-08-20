(ns
  ^{:doc 
  
  "Launches an SSB application."
  
    :author "Frank Mosebach"}
  fm.ssb.main
  (:gen-class
    :name fm.ssb.Main
    :main true)
  (:require
    [clojure.contrib.logging :as log]
    [fm.resources.store :as rstore]
    [fm.websockets.resources :as rscs]
    [fm.websockets.server :as wss]
    [fm.ssb.config :as cfg]
    [fm.ssb.handler :as hdlr]
    [fm.ssb.file-resource-finder :as frf]
    [fm.ssb.resource-server :as rss])
  (:import
    (java.io File)))

(def ^{:private true} resource-store (ref nil))

(defn- check-config-path [config-path]
  (let [config-path (-> config-path str .trim)]
    (when (.isEmpty config-path)
      (throw (IllegalArgumentException. 
               "A non-empty config path is required!")))
    (when-not (-> config-path File. .isFile)
      (throw (IllegalArgumentException. (format "Cannot load config file '%s'!" 
                                                config-path))))
    config-path))

(defn- load-config [config-path]
  (-> (check-config-path config-path)
      cfg/load-config
      (assoc :resource-store 
             (rstore/partition-store resource-store ::application))))

(defn- store-provider [connection]
  {:application (rstore/partition-store resource-store 
                                        ::application)
   :connection  (rstore/partition-store resource-store 
                                        [::connection (:id connection)])})

(defn- start-resource-server [{:keys [app-name http-service] :as config}]
  (if-let [{:keys [port root-path app-path]} http-service]
    (do
      (log/debug (format "Starting resource server (config: %s)..." 
                         http-service))
      (let [finder      (frf/finder root-path app-path) 
            stop-server (rss/start-up port app-name finder)]
        (fn [log]
          (try
            (log "Stopping resource server...")
            (stop-server)
            (log "...done.")
            (catch Exception error
              (log "Failed to stop resource server!" error))))))
    (do
      (log/debug "No resource server (http-service) configured.")
      (log/debug "Starting in 'embedded' mode...")
      (fn [log]))))

(defn- start-app-server [{ws-service :ws-service :as config}]
  (log/debug (format "Starting app server (config: %s)..." ws-service))
  (let [{port :port} ws-service
        handler      (hdlr/app-handler config store-provider)
        stop-server  (wss/start-up port handler)]
    (fn [log]
      (try
        (log "Stopping app server...")
        (stop-server)
        (log "...done.")
        (catch Exception error
          (log "Failed to stop app server!" error))))))

(defn- start-servers [config]
  (log/debug (format "Starting servers for Sideshow Bob app: %s..." config))
  (let [stop-resource-server (start-resource-server config)
        stop-app-server      (start-app-server config)]
    (log/debug "...done. Waiting for clients.")    
    (fn stop-servers [log]
      (log (format "Stopping servers of Sideshow Bob app: %s..." config))
      (stop-resource-server log)
      (stop-app-server log)
      (log "Bye!"))))

(defn- close-resources [log]
  (doseq [key (keys @resource-store)]
    (log (format "Closing resources for %s..." key))
    (rscs/application-expired! (rstore/partition-store resource-store key))
    (log "...closed.")))

(defn- log->log 
  ([message]
    (log/debug message))
  ([message error]
    (log/error message error)))

(defn- log->console 
  ([message]
    (println message))
  ([message error]
    (binding [*out* *err*]
      (println message)
      (.printStackTrace error))))

(defn run [config-path]
  (let [config       (load-config config-path)
        stop-servers (start-servers config)]
    (fn shut-down
      ([]
        (shut-down log->log))
      ([log]
        (close-resources log)
        (stop-servers log)))))

(defn -main [& args]
  (let [config-path (-> args first str .trim)]
    (if (.isEmpty config-path)
      (println "Usage: fm.ssb.Main config-path")
      (let [shut-down (run config-path)]
        (.addShutdownHook (Runtime/getRuntime)
                          (Thread. (partial shut-down log->console) 
                                   "fm.ssb.main/shut-down"))))))

