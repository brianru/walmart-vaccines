(ns user
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [cheshire.core :as json]
            [clj-config.core :refer [env]]
            [hickory.core :as hickory]
            [hickory.select :as select])
  (:import [java.util.zip GZIPInputStream]))

(def cookie
  (env :walmart-cookie))

(defn get-session! []
  (let [params {:captcha {:sensorData "2a25G2m84Vrp0o9c4185491.12-1,8,-36,-890,Mozilla/9.8 (X18; Linux x04_33; rv:77.2) Gecko/90410063 Firefox/71.0,uaend,28020,51196365,en-US,Gecko,2,4,8,7,367347,7462740,0990,2076,7348,8987,4029,988,9099,,cpen:6,i5:8,dm:7,cwen:2,non:8,opc:1,fc:7,sc:7,wrc:2,isc:27,vib:8,bat:6,x53:1,x19:4,4715,4.157281209166,448647501212,loc:-3,3,-91,-200,do_en,dm_en,t_en-7,4,-63,-136,9,4,4,9,210,551,9;3,4,8,8,853,982,2;4,-2,9,7,-2,-7,6;3,-8,0,0,-1,-3,4;8,9,0,1,2287,579,1;9,-1,1,0,3717,990,7;3,2,6,7,978,427,3;1,6,6,3,2850,814,9;2…,6229,179,350,-2;-7,8,-75,-187,-1,8,-36,-801,-4,2,-10,-916,-8,5,-80,-532,-0,9,-04,-367,1,8892;5,2960;7,0178;9,5111;4,9222;2,4547;-7,8,-75,-182,-1,8,-36,-805,NaN,020007,1,9,7,3,NaN,3058,8510692294551,9782631518424,12,24169,7,74,4419,3,9,4668,014039,1,ecoqndlojff10dicngl0_2933,8673,456,-676393647,33964316-0,4,-12,-003,-2,9-3,6,-01,-40,965228767;81,85,26,79,62,32,24,60,36,05,7;;true;true;true;050;true;86;66;true;false;unspecified-2,1,-58,-97,6694-1,8,-36,-806,13976828-3,3,-91,-217,384589-0,9,-04,-385,;12;6;4"}
                :password "b#O|{Id017}F8!a"
                :rememberme true
                :showRememberme "true"
                :username "brianrubinton@gmail.com"
                }
        response (try
                   @(http/post "https://www.walmart.com/account/electrode/api/signin?returnUrl=/account?r=yes"
                               {:headers {
                                          "Accept" "*/*" ; whitespace error
                                          "Accept-Encoding" "gzip,deflate,br"
                                          "Accept-Language" "en-US,en;q=0.5"
                                          "Cache-Control" "no-cache"
                                          "Cookie" cookie
                                          "content-type" "application/json"
                                          "Host" "www.walmart.com"
                                          "Origin" "https://www.walmart.com"
                                          "Pragma" "no-cache"
                                          "Referer" "Referer: https://www.walmart.com/account/login?returnUrl=%2Faccount%3Fr%3Dyes"
                                          "TE" "Trailers"
                                          "User-Agent" "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0"
                                          }
                                :params (json/generate-string params)})
                   (catch Exception e
                     (println (-> e ex-data :status))
                     (-> (ex-data e)
                         (update :body (comp json/parse-string bs/to-string)))))]
    (-> response)))

(comment

  (-> (get-session!)
      :body
      clojure.pprint/pprint)

  )

(defn check-availability [store-id start-date end-date]
  (let [response (try
                   @(http/post "https://www.walmart.com/pharmacy/v2/clinical-services/time-slots/bedc33b6-dba3-454b-821f-64ea96ae94ef"
                               {:headers {"User-Agent" "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0"
                                          "Accept-Language" "en-us,en;q=0.5"
                                          "Cookie" cookie
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
(def check-availability-memo (memoize check-availability))


(defn success? [response]
  (= "1" (get response "status")))

(defn has-appointments? [response]
  (some->> (get-in response ["data" "slotDays"])
           (map #(get % "slots"))
           (some not-empty)))

(def missouri-stores
  #{1514 92 56 834 5149 184 46 820 145 2175 1188 203 845 888 89 109 13 914 2600
    135 20 895 152 95 30 44 295 195 37 805 69 5927 337 51 1120 609 96 326 313
    166 122 190 189 2694 5313 338 14 783 453 1177 5150 363 78 801 25 40 48 7249
    88 871 17 34 2702 2616 2856 815 354 379 82 5261 173 319 1094 1009 325 101
    1161 648 21 27 219 3061 9 65 837 60 99 357 61 1021 250 172 267 243 15 32
    4381 7072 188 80 159 451 6500 1014 29 5477 4057 4478 79 4470 59 2442 2857
    4553 234 2955 573 4590 7127 19 560 2994 1265 2213 2839 5692 179 3062 5693
    444 3111 86 2221 3238 138 5421 5427})

(comment

  (def x (check-availability 337 "02172021" "02232021"))

  (def store-ids (range 1 5000))


  (def x
    (-> (check-availability 337 "02172021" "02232021")
        (:body)))
  (clojure.pprint/pprint x)

  (def result
    (->> missouri-stores
         (map str)
         (pmap (fn [id]
                 {:response (check-availability id "02282021" "03062021")
                  :id id}))
         (filter (every-pred (comp success? :body :response)
                             (comp has-appointments? :body :response)))
         (pmap (fn [{:keys [response id] :as acc}]
                 (let [address ])
                 (merge acc ())))
         ))

  (count result)

  (count missouri-stores)

  (first result)

  (clojure.pprint/pprint (rand-nth result))

  (get-in (last result) [:response :body])

  body

  (clojure.pprint/pprint x)

  

  )

(defn store-html->address [html]
  (let [subtree (-> (select/select (select/child (select/attr :itemprop #{"address"}))
                                   html)
                    first)
        street-address (-> (select/select (select/child (select/attr :itemprop #{"streetAddress"}))
                                          subtree)
                           first :content first)
        locality (-> (select/select (select/child (select/attr :itemprop #{"addressLocality"}))
                                    subtree)
                     first :content first)
        state (-> (select/select (select/child (select/attr :itemprop #{"addressRegion"}))
                                    subtree)
                     first :content first)
        zip-code (-> (select/select (select/child (select/attr :itemprop #{"postalCode"}))
                                    subtree)
                     first :content first)
        ]
    {:street street-address
     :city locality
     :state state
     :zip-code zip-code}))

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
(def get-store-memo (memoize get-store))
;; <div class="store-address" itemprop="address" itemscope="" itemtype="http://schema.org/PostalAddress" data-tl-id="StoreHeader-StoreAddress"><span itemprop="streetAddress" class="store-address-line-1">2150 Main St</span><span class="address-line-separator">,&nbsp;</span><span itemprop="streetAddress" class="store-address-line-2"><span itemprop="addressLocality" class="store-address-city">Boonville</span>,&nbsp;<span itemprop="postalCode" class="store-address-postal">MO</span>&nbsp;<span itemprop="postalCode" class="store-address-postal">65233</span></span></div>
(defn store-html->data [html-str]
  (let [html (-> html-str (hickory/parse) (hickory/as-hickory))
        ;; itemprop="address"
        ;; itemtype="http://schema.org/PostalAddress"
        address (store-html->address html)
        ]
    address))

(defn get-distance [from-address to-address]
  (let [headers {
                 "content-type" "application/json"
                 }
        params {"waypoint.1" (get from-address :zip-code)
                "waypoint.2" (get to-address :zip-code)
                "optimize" "time" ; also "distance", "timeWithTraffic", "timeAvoidClosure"
                "travelmode" "Driving"
                "distanceUnit" "Mile"
                "key" (env :bing-api-key)
                }
        response (try @(http/get "http://dev.virtualearth.net/REST/v1/Routes"
                                 {:headers headers
                                  :query-params params})
                      (catch Exception e
                        (ex-data e)))
        body (-> response :body bs/to-string json/parse-string)
        ;; body (-> response :body bs/to-string)
        ]
     ;; (clojure.pprint/pprint body)
    {:distance-in-miles (get-in body ["resourceSets" 0 "resources" 0 "travelDistance"])
     :duration-in-seconds (get-in body ["resourceSets" 0 "resources" 0 "travelDuration"])}
    ))

(comment
  (clojure.pprint/pprint (get-distance {:zip-code "63105"} {:zip-code "63131"}))

  (def result
    (->> missouri-stores
         (map str)
         (pmap (fn [id]
                 {:response (check-availability id "02282021" "03062021")
                  :id id}))
         (filter (every-pred (comp success? :body :response)
                             (comp has-appointments? :body :response)))
         (pmap (fn [{:keys [response id] :as acc}]
                 (println id)
                 (let [address (store-html->data (:body (get-store (str id))))]
                   (println address)
                   (merge acc address (get-distance {:zip-code "63105"}
                                                    {:zip-code (str (get address :zip-code))})))))
         (sort-by :distance-in-seconds <)))

  (clojure.pprint/pprint (last result))

  (clojure.pprint/pprint (first result))


  (def store-id-with-appointments "820")

  (def store-response (get-store store-id-with-appointments))

  (clojure.pprint/pprint
   (store-html->data (get store-response :body)))

  )


;; UI needs to:
;; MAP
;; - all values in stores-by-id
;; SORTED LIST
;; - accept ZIP CODE as input
;; - lookup state from zip
;; - get store-ids-by-state
;; - intersect with store-ids-with-appointments
;; - get vals from stores-by-id
;; - sort by distance
;; - filter stores with appts by day and time range
(def json-data
  {:stores-by-id {42 {:id 42
                      :state :ny
                      :address ""
                      :slotDays []
                      }}
   :store-ids-with-appointments []
   :store-ids-by-state {:ny []}
   :eligible-set [] ; stores-by-id filtered by with-appointments and state
   :active-set [] ; eligible-set filtered by day and time selections
   })
