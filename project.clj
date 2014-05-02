;; Leiningen project file for the SideshowBob clojure project.

(defproject fm/fm-ssb "1.0.0-SNAPSHOT"
  :description "Sideshow Bob: A simple WebSockets RPC Application Framework."
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [commons-codec/commons-codec "1.6"]
                 [com.sun.net.httpserver/http "20070405"]
                 [fm/fm-core "1.0.0-SNAPSHOT"]
                 [fm/fm-resources "1.0.0-SNAPSHOT"]
                 [fm/fm-websockets "1.0.0-SNAPSHOT"]]                 
  :aot [fm.ssb.main]
  :jar-name "fm-ssb.jar"
  :omit-source false
  :jar-exclusions [#"(?:^|/).svn/" 
                   #"(?:^|/).git/" 
                   #"(?:^|/)project.clj"])

