(defproject clj-campfire "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[midje "1.4.0"]]
                   :plugins [[lein-midje "2.0.1"]]}}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [http.async.client "0.5.2"]
                 [cheshire "5.0.1"]
                 [org.slf4j/slf4j-nop "1.7.2"]
                 [org.clojure/tools.logging "0.2.6"]]
  :main clj-campfire.core)
