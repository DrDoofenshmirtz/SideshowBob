(ns
  ^{:doc 
  
  "Template for a service namespace to be loaded by an SSB app."
  
    :author "Frank Mosebach"}
  ssb.welcome
  (:require 
    [fm.ssb.boot :as boot]))

(boot/def-boot-hook (fn [config]
                      (assoc config ::message 
                                    "Sideshow Bob says: 'Welcome!'")))

