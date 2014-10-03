(ns sensu-client.core
  (:require [clojure.data.json :as json])
  (:import [java.net InetSocketAddress]
           [java.nio.channels SocketChannel DatagramChannel]
           [java.nio ByteBuffer]))

(def sensu-status {:ok 0
                   :warning 1
                   :critical 2})

(defn make-sensu-payload
  "JSONifies payload, adds length header"
  ^:private
  [json_prefix {:keys [status name message refresh] :as payload}]
  (let [s-status (sensu-status status)
        json-payload (json/write-str (assoc (assoc payload :status s-status) :standalone true))
        payload-length (count (.getBytes json-payload))]
    (if json_prefix
      (str payload-length "\n" json-payload)
      json-payload)))

;; XXX Handle java.net.ConnectException for tcp proto.
;;     May be we should just let the callee handle it
;;     as per his convenience.
(defn get-client-channel
  ^:private
  ([host port proto]
   (let [sockaddr (InetSocketAddress. host port)]
     (if (= proto "tcp")
       (.. SocketChannel (open sockaddr))
       (.. DatagramChannel (open) (connect sockaddr)))))
  ([port proto]
   (get-client-channel "localhost" port proto))
  ([proto]
   (get-client-channel 3030 proto))
  ([]
   (get-client-channel "tcp")))


(defn send-alert-to-sensu
  "Sends a status, name, message to Sensu via TCP on host, port.
   Make sure you catch for java.net.Connection exceptions when using
   TCP.
   Takes the following optional keys:
   :host hostname or ip to connect to, default localhost
   :port port number to connect to, default 3030
   :name check name to set in the alert being sent, default mycheck
   :refresh how frequently to send alerts in seconds, default 60 seconds
   :occurrences number of times this exception should happen before we raise alert, default 1
   :proto intended for TCP v/s UDP, default tcp
   Examples:
    (send-alert-to-sensu :critical \"things have failed badly\" :name \"myservice\" :proto \"udp\")
    (send-alert-to-sensu :critical \"things have failed badly\" :name \"myservice\" :proto \"tcp\")"
  [status message
   & {:keys [host port name refresh proto json-prefix occurrences]
      :or {host "localhost"
           port 3030
           name "mycheck"
           refresh 60
           proto "tcp"
           json-prefix false
           occurrences 1}}]
  {:pre [(sensu-status status)]}
  (let [payload (make-sensu-payload json-prefix {:status status
                                     :name name
                                     :output message
                                     :refresh refresh
                                     :occurrences occurrences})
        client-channel (get-client-channel host port proto)]
    (.write client-channel (ByteBuffer/wrap (.getBytes payload)))
    (.close client-channel)))
