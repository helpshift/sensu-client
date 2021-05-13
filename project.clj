(defproject helpshift/sensu-client "0.11.0"
  :description "Library to send alerts to sensu-client."
  :url "https://www.helpshift.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [cheshire "5.10.0"]
                 [clj-http "3.12.0"]]
  :global-vars {*warn-on-reflection* true}
  :plugins [[codox "0.8.10"]]
  :codox {:defaults {:doc/format :markdown}
          :src-dir-uri "https://github.com/helpshift/sensu-client/blob/master/"
          :src-linenum-anchor-prefix "L"})
