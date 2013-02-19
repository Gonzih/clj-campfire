(ns clj-campfire.core
  (:require [clojure.pprint :refer [pprint]]
            [http.async.client :as http]
            [cheshire.core :as json]
            [clojure.java.shell :as shell]
            [clojure.string]))

(declare callback process-data parse-data process-message get-username get-user get-username-memo room-ids fetch-data update-vals)

(def use-notify (atom false))
(def recent-amount 10)

(defn icon-path []
  (.getPath (clojure.java.io/resource "campfire.png")))

(defn room   [] (first (room-ids)))
(defn token  [] (get (System/getenv) "TOKEN"))
(defn domain [] (get (System/getenv) "DOMAIN"))

(def protocol "https")

(defn api-url [domain api-path]
  (str protocol "://" domain ".campfirenow.com" api-path))

(defn stream-url [room]
  (api-url "streaming" (str "/room/" room "/live.json")))

(defn auth
  ([token] (auth token true))
  ([token preemptive] {:type :basic :user token :password "x" :preemptive preemptive}))


(defn start [room]
  (with-open [client (http/create-client)]
    (let [response (http/request-stream client
                                        :get
                                        (stream-url room)
                                        callback
                                        :auth (auth (token))
                                        :timeout -1)]
      (read-line))))

(defn callback [state body]
  (let [data (str body)]
    (if-not (= data " ")
      (process-data data)))
  [body :continue])

(defn process-data [data]
    (doall (map process-message (parse-data data))))

(defn parse-data [data]
  (let [messages (clojure.string/split data #"\r")
        messages (map json/parse-string messages)]
    messages))

(defn process-message [data]
  (if (#{"TextMessage" "PasteMessage"} (data "type"))
    (let [body (data "body")
          username (get-username-memo (data "user_id"))]
      (println (str username ": " body))
      (if @use-notify
        (shell/sh "notify-send" username body "-i" (icon-path))))))

(defn send-request [path]
  (-> (fetch-data path)
      :body
      json/parse-string))

(defn fetch-data [path]
  (with-open [client (http/create-client)]
    (-> (http/GET client
                  (api-url (domain) path)
                  :auth (auth (token) false))
        http/await
        (update-vals [:body] deref)
        (update-vals [:body] str))))

(defn update-vals [map vals f]
  (reduce #(update-in %1 [%2] f) map vals))

(defn get-username [id]
  (-> (get-user id) (get "name")))

(def get-username-memo (memoize get-username))

(defn get-user [id]
  (-> (send-request (str "/users/" id ".json")) (get "user")))

(defn room-ids []
  (-> (send-request "/rooms.json")
      (get "rooms")
      (->> (map #(get % "id")))))

(defn -main []
  (-> (send-request (str "/room/" (room) "/recent.json?limit=" recent-amount))
      (get "messages")
      (->> (map process-message))
      doall)
  (reset! use-notify true)
  (start (room)))
