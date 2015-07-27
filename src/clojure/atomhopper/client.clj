(ns atomhopper.client
  (import [org.apache.abdera.protocol.client AbderaClient]
          [org.apache.abdera Abdera]
          [org.apache.abdera.model Entry])
  (require [clojure.test.check.generators :as gen]
           [clojure.test.check :as tc]
           [clojure.test.check.properties :as prop]
           [clojure.test :refer [deftest is run-tests]]))

(defn make-request-option [client]
  (let [option (.getDefaultRequestOptions client)]
    (-> option
        (.setUseLocalCache false)
        (.setNoCache true)
        (.setMaxAge 0))
    option))

(defn post [abdera url ^String item]
  (let [client          (AbderaClient. abdera)
        nocache-request (make-request-option client)
        entry           (doto ^Entry (.newEntry abdera)
                          (.setTitle item))
        response        (.post client url entry nocache-request)]
    #_(println (.getStatusText response) " | " (System/currentTimeMillis) " | " item " | " url)))

(defn get-feed [abdera url total f]
  (let [client   (AbderaClient. abdera)
        response (.get client url)]
    (->> (seq (-> response
                  (.getDocument)
                  (.getRoot)
                  (.getEntries)))
         (take total)
         (map f))))

(defn read-feed-title [abdera url total]
  (get-feed abdera url total #(.getTitle %)))

(defn read-feed-id [abdera url total]
  (get-feed abdera url total (fn [entry]
                               (let [id (.getId entry)]
                                 #_(println id)
                                 id))))

(def server1 "http://ah1.crosswired.local:8080/namespace/feed")
(def server2 "http://ah2.crosswired.local:8080/namespace/feed")

(defn dopost [abdera server-url items]
  (let [servers (gen/elements server-url)]
    (doseq [arg (map #(vector %1 %2 %3) (repeat abdera) (gen/sample servers (count items)) items)]
      (apply post arg))))

(def not-blank (comp not clojure.string/blank?))

(deftest should-store-in-reverse-order-if-post-happen-within-a-limit-of-time
  (let [abdera        (new Abdera)
        items         (gen/sample (gen/not-empty (gen/list (gen/such-that not-blank gen/string-ascii))) 2)
        expectedItems (reverse (flatten items))]
    (doseq [item items]
      (Thread/sleep 1000)
      (dopost abdera [server1] item))
    (is (= (read-feed-title abdera server1 (count expectedItems))
           expectedItems))))

(deftest shoul-store-all-item-if-post-happen-immediately
  (let [abdera        (new Abdera)
        items         (gen/sample (gen/not-empty (gen/list (gen/such-that not-blank gen/string-ascii))) 2)
        expectedItems (flatten items)]
    (doseq [item items]
      (dopost abdera [server1] item))
    (is (and (clojure.set/subset? (set (read-feed-title abdera server1 (count expectedItems)))
                                  (set expectedItems))
             (clojure.set/superset? (set (read-feed-title abdera server1 (count expectedItems)))
                                    (set expectedItems))))))

(deftest should-not-gurantee-order-if-post-happen-immediately
  (let [abdera        (new Abdera)
        items         (gen/sample (gen/not-empty (gen/list (gen/such-that not-blank gen/string-ascii))) 2)
        expectedItems (reverse (flatten items))]
    (doseq [item items]
      (dopost abdera [server1] item))
    (is (not= (read-feed-title abdera server1 (count expectedItems))
              expectedItems))))

(def should-get-the-same-value
  (prop/for-all
   [items (gen/not-empty (gen/list (gen/such-that not-blank gen/string-ascii)))]
   (let [abdera        (new Abdera)
         expectedItems (flatten items)]
     (dopost abdera [server1 server2] items)
     (= (read-feed-id abdera server1 (count expectedItems))
        (read-feed-id abdera server2 (count expectedItems))))))

#_(tc/quick-check 100 should-get-the-same-value)
#_(run-tests)
