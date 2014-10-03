(defproject helpshift/sensu-client "0.1.0-SNAPSHOT"
  :description "Library to send alerts to sensu-client"
  :url "http://helpshift.mobi"
  :license {:name "EPL"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"
                  :exclusions [org.clojure/clojure]]]
  :global-vars {*warn-on-reflection* true}
  :min-lein-version "2.2.0")
