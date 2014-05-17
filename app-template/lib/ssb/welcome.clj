(ns
  ^{:doc 
  
  "Template for a service namespace to be loaded by an SSB app."
  
    :author "Frank Mosebach"}
  ssb.welcome
  (:require 
    [fm.ssb.boot :as boot]
    [fm.websockets.rpc.targets :as tar]))

(boot/def-boot-hook (fn [config]
                      (assoc config ::message 
                                    "Sideshow Bob says: 'Welcome!'")))

(tar/defroute (tar/prefixed-request-name-route "welcome" "."))

(tar/defaction say-hello []
  "Well, whatever you want him to say...")

