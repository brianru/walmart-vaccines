(ns user
  (:require [walmart-vaccines.core :refer :all])
  (:import [java.util.zip GZIPInputStream]))

(comment

  (-> (get-session!)
      :body
      clojure.pprint/pprint)

  )



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
