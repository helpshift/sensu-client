(ns sensu-client.core
  (:require [clojure.data.json :as json])
  (:import [java.net InetSocketAddress]
           [java.nio.channels SocketChannel DatagramChannel]
           [java.nio ByteBuffer]))

(def ^{:doc "Sensu status map."} sensu-status {:ok 0, :warning 1, :critical 2})

(defn ^:private sensu-payload
  "JSONify payload and optionally add length header."
  [prefix-length? {:keys [status name message refresh] :as payload}]
  (let [json-payload (json/write-str (assoc payload
                                       :status (sensu-status status)
                                       :standalone true))]
    (if prefix-length?
      (str (count (.getBytes ^String json-payload)) "\n" json-payload)
      json-payload)))


(defn ^:private client-channel
  "Create a new TCP/UDP connection."
  [host port proto]
  (let [sockaddr (InetSocketAddress. ^String host ^int port)]
    (if (= proto :tcp)
      (SocketChannel/open sockaddr)
      (.connect (DatagramChannel/open) sockaddr))))


(defn send-alert
  "Send a status, name, message to Sensu via TCP on host, port.

   Make sure you catch for java.net.Connection exceptions when using
   TCP.

   Takes the following optional keys:

       `:host` - hostname or ip to connect to, default localhost
       `:port` - port number to connect to, default 3030
       `:name` - check name to set in the alert being sent, default mycheck
       `:refresh` - how frequently to send alerts in seconds, default 60 seconds
       `:occurrences` - number of times this exception should happen before we alert
       `:proto` - intended for TCP/UDP, default udp

   Examples:

       (send-alert :critical \"things have failed badly\"
                   :name \"myservice\"
                   :proto :udp)

       (send-alert :critical \"things have failed badly\"
                   :name \"myservice\"
                   :proto :tcp)"
  [status message
   & {:keys [host port name refresh proto prefix-length? occurrences]
      :or {host "localhost"
           port 3030
           name "mycheck"
           refresh 60
           proto :udp
           prefix-length? false
           occurrences 1}}]
  {:pre [(contains? sensu-status status)]}
  (let [payload (sensu-payload prefix-length? {:status status
                                               :name name
                                               :output message
                                               :refresh refresh
                                               :occurrences occurrences})]
    (with-open [chan ^java.nio.channels.WritableByteChannel
                (client-channel host port proto)]
      (.write chan (ByteBuffer/wrap (.getBytes ^String payload))))))
