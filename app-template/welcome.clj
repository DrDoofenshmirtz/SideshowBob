(defapp welcome
  :ws-service   {:port     17500
                 :boot     ssb.welcome
                 :services [ssb.welcome]}   
  :http-service {:port      20500
                 :root-path "."
                 :app-path  "welcome.html"})

