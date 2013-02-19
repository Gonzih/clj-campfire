(ns clj-campfire.core-test
  (:require [midje.sweet :refer :all]
            [clj-campfire.core :refer :all]))

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

(fact "callback should pass data to process-data function"
  (let [data "some data"]
    (callback "some state" data) => [data :continue]
    (provided (process-data data) => anything :times 1)))

(let [raw-data "{\"room_id\":3,\"body\":\"pur\"}\r
                {\"room_id\":5,\"body\":\"yes\"}\r"
      parsed-data [{"room_id" 3 "body" "pur"}
                   {"room_id" 5 "body" "yes"}]]
  (fact "process-data should call process-message for each message in data"
    (process-data raw-data) => anything
    (provided (parse-data raw-data) => parsed-data :times 1
              (process-message
                (as-checker #(some #{%} parsed-data)))
              => anything :times 2))

  (fact "parse-data should return collection of parsed messages"
    (parse-data raw-data) => parsed-data))
