(defproject helpshift/sensu-client "0.12.0"
  :description "Library to send alerts to sensu-client."
  :url "https://www.helpshift.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.10.1"]
                 [clj-http "3.12.3"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.3"]]}}
  :global-vars {*warn-on-reflection* true}
  :plugins [[codox "0.8.10"]]
  :codox {:defaults {:doc/format :markdown}
          :src-dir-uri "https://github.com/helpshift/sensu-client/blob/master/"
          :src-linenum-anchor-prefix "L"})
