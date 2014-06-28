(defapp file-upload
  :ws-service   {:port     17500
                 :boot     ssb.file-upload
                 :services [ssb.file-upload]}   
  :http-service {:port      20500
                 :root-path "."
                 :app-path  "file-upload.html"})

