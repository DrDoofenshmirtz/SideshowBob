(ns
  ^{:doc 
  
  "Template for a service namespace to be loaded by an SSB app."
  
    :author "Frank Mosebach"}
  ssb.welcome
  (:require 
    [fm.ssb.boot :as boot]
    [fm.ssb.config :as cfg]
    [fm.websockets.rpc.request :as req]
    [fm.websockets.rpc.targets :as tar]))

(boot/def-boot-hook (fn [config]
                      (assoc config ::message 
                                    "Well, whatever you want him to say...")))

(tar/defroute (tar/prefixed-request-name-route "welcome" "."))

(tar/defaction say-hello []
  (let [message (-> (req/connection) cfg/get-config ::message)]
    message))

