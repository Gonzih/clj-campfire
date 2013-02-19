(ns clj-campfire.core-test
  (:require [midje.sweet :refer :all]
            [clj-campfire.core :refer :all]
            [http.async.client :as http]))

(fact (stream-url "13") =>
      "https://streaming.campfirenow.com/room/13/live.json")

(fact (api-url "mydomain" "/data.json") =>
      "https://mydomain.campfirenow.com/data.json")

(fact "auth map generation"
  (let [stub-token "my-api-token"]
    (auth stub-token) => (auth stub-token true)

    (:type       (auth stub-token))       => :basic
    (:user       (auth stub-token))       => stub-token
    (:password   (auth stub-token))       => "x"
    (:preemptive (auth stub-token false)) => false))

(fact "callback passes data to process-data function"
  (let [data "some data"]
    (callback "some state" data) => [data :continue]
    (provided (process-data data) => anything :times 1)))

(let [raw-data "{\"room_id\":3,\"body\":\"pur\"}\r
                {\"room_id\":5,\"body\":\"yes\"}\r"
      parsed-data [{"room_id" 3 "body" "pur"}
                   {"room_id" 5 "body" "yes"}]]
  (fact "process-data maps process-message messages in data"
    (process-data raw-data) => anything
    (provided (parse-data raw-data) => parsed-data :times 1
              (process-message
                (as-checker #(some #{%} parsed-data)))
              => anything :times 2))

  (fact "parse-data returns collection of parsed messages"
    (parse-data raw-data) => parsed-data))

(fact "start function creates request stream"
      (start 10) => anything
      (provided
        (http/request-stream anything
                             :get
                             (stream-url 10)
                             anything
                             :auth (auth (token))
                             :timeout -1) => anything :times 1
        (read-line) => " " :times 1))

(fact "get-username uses get-user function"
  (get-username 1) => "purr"
  (provided
    (get-user 1) => {"name" "purr"}))

(fact "get-user uses send-request function"
  (get-user 1) => {"name" "myname"}
  (provided
    (send-request "/users/1.json") => {"user" {"name" "myname"}}))

(fact "room function uses room-ids function"
      (room) => 2
      (provided
        (room-ids) => [2 3]))

(fact "send-request returns parsed json data"
      (send-request "/rooms.json") => {"rooms" [{"name" "dev"}]}
      (provided
        (fetch-data "/rooms.json")
        => {:body "{\"rooms\":[{\"name\":\"dev\"}]}"} :times 1))
