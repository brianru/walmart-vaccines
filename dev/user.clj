(ns user
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [cheshire.core :as json])
  (:import [java.util.zip GZIPInputStream]))

(def cookie
  "hasACID=1; com.wm.reflector=\"reflectorid:0000000000000000000000@lastupd:1613618093762@firstcreate:1613618093762\"; DL=63105%2C%2C%2Cip%2C63105%2C%2C; TB_Latency_Tracker_100=1; TB_Navigation_Preload_01=1; TB_SFOU-100=; TB_DC_Flap_Test=0; vtc=eexAdnsVK_sDuQ2ZI0VsG0; bstc=eexAdnsVK_sDuQ2ZI0VsG0; mobileweb=0; xpa=-nqgU|5Z16k|7Evb-|9Kguz|hn7Gx; xpm=1%2B1613618137%2BeexAdnsVK_sDuQ2ZI0VsG0~bedc33b6-dba3-454b-821f-64ea96ae94ef%2B0; exp-ck=7Evb-29Kguz1; TS01b0be75=01538efd7cab89c1fa731ee432f9720d219a98ff6721231fb16b98f9e854e8839e74cf72d0670a9c039c1cc705b3d8074121fc1057; TS013ed49a=01538efd7cab89c1fa731ee432f9720d219a98ff6721231fb16b98f9e854e8839e74cf72d0670a9c039c1cc705b3d8074121fc1057; akavpau_p8=1613618748~id=e4e54312869532a3f6cf091b2ca35e35; TBV=7; adblocked=false; TS011baee6=01c5a4e2f9812774539b1739d9bc6df95318dd990c65c844a606c79107dbed0f9a5dc7047816e88b5281c610240291ce2868397f3c; TS018dc926=01c5a4e2f97a5b98a9e8dd271f05cbe89d2da146cafd65c16528794f35ff97e559e9b0ce07b271e33c4315f69bb2a85296c049440b; TS01e3f36f=01c5a4e2f97a5b98a9e8dd271f05cbe89d2da146cafd65c16528794f35ff97e559e9b0ce07b271e33c4315f69bb2a85296c049440b; __gads=ID=3092c3f32c049138-22b4dbe1a7c6009f:T=1613618097:S=ALNI_Mad40LODgBUvi5xkwCAdVmlOQdf1g; _abck=ecoqndlojff78dicngl0_1947; s_pers=%20s_v%3DY%7C1613620722161%3B%20gpv_p11%3Dno%2520value%7C1613620722196%3B%20gpv_p44%3Dno%2520value%7C1613620722209%3B%20s_vs%3D1%7C1613620722214%3B%20s_fid%3D701969FA85BB8647-3874E78E02937464%7C1676690922481%3B; tb_sw_supported=false; _pxvid=7b523597-7197-11eb-851a-0242ac120017; s_pers_2=+s_fid%3D701969FA85BB8647-3874E78E02937464%7C1676690099145%3BuseVTC%3DY%7C1676733299; s_sess_2=%20s_cc%3Dtrue%3B; _gcl_au=1.1.200160294.1613618100; _uetsid=7bdf61d0719711eb9a2cdb4d68c3d64b; _uetvid=7bdfc220719711eb89cc7109a658fe2f; s_sess=%20ent%3DAccount%253ASignIn%3B%20cp%3DY%3B%20s_sq%3D%3B%20s_cc%3Dtrue%3B%20cps%3D0%3B%20chan%3Dorg%3B%20v59%3DAccount%3B%20v54%3DAccount%253A%2520SignIn%3B; _fbp=fb.1.1613618100169.824182776; s_vi=[CS]v1|3016EDDA2D13BA4D-4000096618AE984C[CE]; wm_ul_plus=INACTIVE|1613704537019; auth=MTAyOTYyMDE4thI0LN3YApXyECrR%2FrUyVVKmr1Z9lUPVRpmsMppnhS18YHCDFnRAIbMK%2BsEnHyadHTS66b0wlyszzFQI6pYsHk9YXptqDS44VDez3iGKD%2FXxl773I%2B54vTjXFs8Snxk2RMvR4vpAGLQJj3IgMhxaynKJnzQpQ307IT2jqPV0haG4qDg0VFcYAH9cFy%2BWboGgSViSsQ9h8go%2FEWcJJIDkd1L6CZSzVZykBKTRtORKnJVxU44fd5CBJWII9nXGtMY3DL8AYcF%2B38xlvT2qB%2F%2BUo%2FsOJ%2BZXWBTvPWdBH%2Bws2IHJ7bu6s6nDdjEVZva4DZJV4XyeFk9XemoBc8j0YJzVOcchuHkBeSEalmNtIVf%2FJufFivioazjpgPe5%2F9%2FI6ihnlpzDKHiOaSv1NwIhcPhFmQ%3D%3D; rtoken=MDgyNTUyMDE4ZnnVEJXVGYXGokplntUwrDz1wyYyhrYhB5RiuSe27Ueexpc45QHt1qqa%2Bhf5oS50rtKhfq66Z3iEpafwTwQTBqOQol97XA8bVyt4jeGixy%2FypljPa79ytFzvZ%2F%2Fv4btbhGoIKh%2BLAsSlCeoQ6IOtnRsHLVXrCpmYcqCdnDJOoPcJjRy8nA9WU2Ds2%2BnBssE6rXM7%2BYOWNra45oJl3lgXdlxEIV5q6DbD2UYXTqJNlZECzwg%2BtTCdktVbfAyNDw9e0xRGEY9aysHgDXAQOlgvXmiaiip3Ra6s0VWtSSmLqvqn7VDHHxITG1xeMXOdeGeBP3QpI6venEwCcqBYdkMHUdn938zYUuNLpXL9Zp7KoiP9VaiA35vftdqYj9BnYtoWPuVr3nDUPJzdEcexNxeFRg%3D%3D; SPID=f2ff7ce4c266edb61282abdfb928d863fb006e48fd051b06126fff9c40d439dded80b3f7cdaec7add2923ceaab30f004wmcxo; CID=bedc33b6-dba3-454b-821f-64ea96ae94ef; hasCID=1; customer=%7B%22firstName%22%3A%22Brian%22%2C%22lastNameInitial%22%3A%22R%22%2C%22rememberme%22%3Atrue%7D; type=REGISTERED; WMP=4; ACID=bedc33b6-dba3-454b-821f-64ea96ae94ef; location-data=63105%3ASaint%20Louis%3AMO%3A%3A8%3A1|3z2%3B%3B1.6%2C2d1%3B%3B4.45%2C22u%3B%3B6.87%2Cx0%3B%3B8.41%2Cz5%3B%3B8.75%2Cwp%3B%3B10.11%2C1ph%3B%3B10.53%2Ckr%3B%3B10.76%2Cmd%3B%3B10.99%2C4kn%3B%3B11.68||7|1|1xnl%3B16%3B0%3B1.21%2C1xo8%3B16%3B2%3B2.83%2C1xp0%3B16%3B4%3B5.11%2C1xmu%3B16%3B5%3B5.57%2C1xo7%3B16%3B6%3B6.11; TBWL-94-pharmacyTimeslots=c9cc25v8eed2:0:3hn2qvj1bvned:1sxlemt5f5w6v")

(def cookie2 "hasACID=1; com.wm.reflector=\"reflectorid:0000000000000000000000@lastupd:1613618093762@firstcreate:1613618093762\"; DL=63105%2C%2C%2Cip%2C63105%2C%2C; TB_Latency_Tracker_100=1; TB_Navigation_Preload_01=1; TB_SFOU-100=; TB_DC_Flap_Test=0; vtc=eexAdnsVK_sDuQ2ZI0VsG0; bstc=eexAdnsVK_sDuQ2ZI0VsG0; mobileweb=0; xpa=-nqgU|5Z16k|7Evb-|9Kguz|hn7Gx; xpm=1%2B1613618137%2BeexAdnsVK_sDuQ2ZI0VsG0~bedc33b6-dba3-454b-821f-64ea96ae94ef%2B0; exp-ck=7Evb-29Kguz1; TS01b0be75=01538efd7c670277b4f28909c43974ef45b5273e8cdff080a6510f558f44f25869c8a945b68b84d331fbb86ba057921f4b18e4302a; TS013ed49a=01538efd7cab89c1fa731ee432f9720d219a98ff6721231fb16b98f9e854e8839e74cf72d0670a9c039c1cc705b3d8074121fc1057; akavpau_p8=1613620748~id=eb056c95b68208091834e55c52c6c4f7; TBV=7; adblocked=false; TS011baee6=01c5a4e2f9b6a1d6beaf3f6c50fc055006ec36cfb9fc6d0fcee83280340c6f3b30e97935e4df5e6fbffac3e804ec2103695864acdc; TS018dc926=01c5a4e2f97a5b98a9e8dd271f05cbe89d2da146cafd65c16528794f35ff97e559e9b0ce07b271e33c4315f69bb2a85296c049440b; TS01e3f36f=01c5a4e2f97a5b98a9e8dd271f05cbe89d2da146cafd65c16528794f35ff97e559e9b0ce07b271e33c4315f69bb2a85296c049440b; __gads=ID=3092c3f32c049138-22b4dbe1a7c6009f:T=1613618097:S=ALNI_Mad40LODgBUvi5xkwCAdVmlOQdf1g; _abck=ecoqndlojff78dicngl0_1947; s_pers=%20s_v%3DY%7C1613622187591%3B%20gpv_p11%3Dno%2520value%7C1613622187634%3B%20gpv_p44%3Dno%2520value%7C1613622187645%3B%20s_vs%3D1%7C1613622187651%3B%20s_fid%3D701969FA85BB8647-3874E78E02937464%7C1676692388080%3B; tb_sw_supported=false; _pxvid=7b523597-7197-11eb-851a-0242ac120017; s_pers_2=+s_fid%3D701969FA85BB8647-3874E78E02937464%7C1676690099145%3BuseVTC%3DY%7C1676733299; s_sess_2=%20s_cc%3Dtrue%3B; _gcl_au=1.1.200160294.1613618100; _uetsid=7bdf61d0719711eb9a2cdb4d68c3d64b; _uetvid=7bdfc220719711eb89cc7109a658fe2f; s_sess=%20ent%3DAccount%253ASignIn%3B%20cp%3DY%3B%20s_sq%3D%3B%20s_cc%3Dtrue%3B%20cps%3D0%3B%20chan%3Dorg%3B%20v59%3DAccount%3B%20v54%3DAccount%253A%2520SignIn%3B; _fbp=fb.1.1613618100169.824182776; s_vi=[CS]v1|3016EDDA2D13BA4D-4000096618AE984C[CE]; wm_ul_plus=INACTIVE|1613704537019; auth=MTAyOTYyMDE4thI0LN3YApXyECrR%2FrUyVVKmr1Z9lUPVRpmsMppnhS18YHCDFnRAIbMK%2BsEnHyadHTS66b0wlyszzFQI6pYsHk9YXptqDS44VDez3iGKD%2FXxl773I%2B54vTjXFs8Snxk2RMvR4vpAGLQJj3IgMhxaynKJnzQpQ307IT2jqPV0haG4qDg0VFcYAH9cFy%2BWboGgSViSsQ9h8go%2FEWcJJIDkd1L6CZSzVZykBKTRtORKnJWy7bku0%2FQnMoUenoW4LdSX5w7JNgRimkP%2FzL3x55LK8ezwYVlQy1XHT4BexWp1i%2BAybQs7AZ3Mo06YcEbfWojRla0Csi8wu3XoL2o9HrBVD1F744EbxZSHWwe7qRKKvY8MvwBhwX7fzGW9PaoH%2F5SjmoGyqRSvmjMBKmkCnN2fDA%3D%3D; rtoken=MDgyNTUyMDE4ZnnVEJXVGYXGokplntUwrDz1wyYyhrYhB5RiuSe27Ueexpc45QHt1qqa%2Bhf5oS50rtKhfq66Z3iEpafwTwQTBqOQol97XA8bVyt4jeGixy%2FypljPa79ytFzvZ%2F%2Fv4btbhGoIKh%2BLAsSlCeoQ6IOtnRsHLVXrCpmYcqCdnDJOoPcJjRy8nA9WU2Ds2%2BnBssE6rXM7%2BYOWNra45oJl3lgXdlxEIV5q6DbD2UYXTqJNlZECzwg%2BtTCdktVbfAyNDw9e0xRGEY9aysHgDXAQOlgvXmiaiip3Ra6s0VWtSSmLqvqn7VDHHxITG1xeMXOdeGeBP3QpI6venEwCcqBYdkMHUdn938zYUuNLpXL9Zp7KoiP9VaiA35vftdqYj9BnYtoWPuVr3nDUPJzdEcexNxeFRg%3D%3D; SPID=f2ff7ce4c266edb61282abdfb928d863fb006e48fd051b06126fff9c40d439dded80b3f7cdaec7add2923ceaab30f004wmcxo; CID=bedc33b6-dba3-454b-821f-64ea96ae94ef; hasCID=1; customer=%7B%22firstName%22%3A%22Brian%22%2C%22lastNameInitial%22%3A%22R%22%2C%22rememberme%22%3Atrue%7D; type=REGISTERED; WMP=4; ACID=bedc33b6-dba3-454b-821f-64ea96ae94ef; location-data=63105%3ASaint%20Louis%3AMO%3A%3A8%3A1|3z2%3B%3B1.6%2C2d1%3B%3B4.45%2C22u%3B%3B6.87%2Cx0%3B%3B8.41%2Cz5%3B%3B8.75%2Cwp%3B%3B10.11%2C1ph%3B%3B10.53%2Ckr%3B%3B10.76%2Cmd%3B%3B10.99%2C4kn%3B%3B11.68||7|1|1xnl%3B16%3B0%3B1.21%2C1xo8%3B16%3B2%3B2.83%2C1xp0%3B16%3B4%3B5.11%2C1xmu%3B16%3B5%3B5.57%2C1xo7%3B16%3B6%3B6.11; _pxde=daf628828f4a82207d7b73e26867ee45979f355986302dc3a55dd0701352cfc5:eyJ0aW1lc3RhbXAiOjE2MTM2MjAzMzYzMzcsImZfa2IiOjAsImlwY19pZCI6W119; _px3=e5b2a3c629269a5715d8b33f5cf6822580be1dc176f0590c18aee79186c35c24:ra4kHDuduEHRS1fwTR765f+6vgcT0ysOUWEHjBG+ezvLShhZNJInawa3aIa53ZTCBcfkNZO3M3GAk2r5GmtDzw==:1000:rtFzj2ZqZ1O64cmBDOnAgwB3wFh8iDpCBVV5e4qB9wjCorDR1zPfTeiJT69PJdAePBnZRWNg33hQGRGRLTNk2uTjJTsN5dcRrwv3TlC8dkvgquS7UzwHXbjC1BNr5aJKfGPYRNkjACb1Gr1WXzQpXRSp2ljEFQI42pnJFWARWQc=; next-day=null|true|true|null|1613620148 ")

(defn check-availability [store-id start-date end-date]
  (let [response (try
                   @(http/post "https://www.walmart.com/pharmacy/v2/clinical-services/time-slots/bedc33b6-dba3-454b-821f-64ea96ae94ef"
                               {:headers {"User-Agent" "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0"
                                          "Accept-Language" "en-us,en;q=0.5"
                                          "Cookie" cookie2
                                          "Referer" "https://www.walmart.com/pharmacy/clinical-services/immunization/scheduled?imzType=covid&action=SignIn&rm=true"
                                          "rx-electrode" true
                                          "WPharmacy-TrackingID" "abb31e81-6ad6-4faa-bb27-9232f679421d"
                                          "WPharmacy-Source" "web/firefox78.0.0/Linux x86_64/bedc33b6-dba3-454b-821f-64ea96ae94ef"
                                          "Origin" "https://www.walmart.com"
                                          "Pragma" "no-cache"
                                          "Cache-Control" "no-cache"
                                          "Content-Type" "application/json"}
                                :body (json/generate-string {"startDate" start-date
                                                             "endDate" end-date
                                                             "imzStoreNumber" {"USStoreId" store-id}})})
                   (catch Exception e
                     (-> (ex-data e)
                         (update :body (fn [body] (bs/to-string (GZIPInputStream. body)))))))
        ]
    (-> response
        (update :body (fn [body] (json/parse-string (bs/to-string body)))))
    ))


(defn success? [response]
  (= "1" (get response "status")))

(defn has-appointments? [response]
  (some->> (get-in response ["data" "slotDays"])
           (map #(get % "slots"))
           (some not-empty)))

(comment

  (def x (check-availability 337 "02172021" "02232021"))

  (def store-ids (range 1 5000))


  (def x
    (-> (check-availability 337 "02172021" "02232021")
        (:body)))
  (clojure.pprint/pprint x)

  x
  (def result
    (->> (range 1 100)
         (pmap (fn [id]
                 {:response (check-availability id "02182021" "02242021")
                  :id id}))
         (filter (every-pred (comp success? :body :response)
                             (comp has-appointments? :body :response)))))

  (clojure.pprint/pprint )
  (first result)

  body

  (clojure.pprint/pprint x)

  )

;; TODO pull the type name and address out of there
;; class=store-type-name
;; class=store-address
;; store-service-microdata itemtype=schema:pharmacy itemprop:department
;; store-hours-list
(defn get-store [id]
  (let [response @(http/get (format "https://www.walmart.com/store/%s" id)
                            {:headers {"Content-Type" "application/json"}})]
    (-> response
        (update :body bs/to-string))))

(comment

  (println
   (:body (get-store 2)))

  )



