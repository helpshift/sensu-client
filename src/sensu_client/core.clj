(ns sensu-client.core
  (:require [cheshire.core :as cc]
            [clj-http.client :as http])
  (:import [java.net InetSocketAddress]
           [java.nio.channels SocketChannel DatagramChannel]
           [java.nio ByteBuffer]))

(def ^{:doc "Sensu status map."} sensu-status {:ok 0, :warning 1, :critical 2})

(defn ^:private sensu-payload
  "JSONify payload and optionally add length header."
  [prefix-length? {:keys [status name message refresh] :as payload}]
  (let [json-payload (cc/generate-string (assoc payload
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


(defn ^:private sensu-go-event
  "Create the sensu-go agent metadata"
  [{:keys [name handlers status message labels annotations]}]
  {:check {:handlers handlers
           :metadata {:name name
                      :labels labels
                      :annotations annotations}
           :status (sensu-status status)
           :output message}})


(defn ^:private send-post-event
  "Send the data to the sensu agent over HTTP POST endpoint."
  [host port event]
  (let [url (format "http://%s:%s/events"
                    host
                    port)]
    (http/post url
               {:content-type :json
                :body (cc/generate-string event)})))


(defn send-alert-sensu-go
  "Send a `status` and `message` to Sensu via HTTP API.

  Takes the following optional keys:
    `:host` - hostname or IP to connect to, default localhost
    `:port` - api-port number to connect to, default 3031
    `:name` - check name to set in the alert, default mycheck
    `:labels` - custom attributes to include with observation data in events
    `:handlers` - array of handlers that will handle the event on sensu
    `:annotations` - on-identifying metadata to include with observation data in events

  For supported fields and how to use them in event, please refer:
  https://docs.sensu.io/sensu-go/latest/observability-pipeline/observe-schedule/checks/#top-level-attributes

  Note: To be used for sensu-go agent."
  [status message
   & {:keys [host port name labels annotations handlers] :as event-data
      :or {host "localhost"
           port 3031
           name "mycheck"
           labels {}
           annotations {}
           handlers []}}]
  (let [event (sensu-go-event (assoc event-data
                                     :handlers handlers
                                     :status status
                                     :annotations annotations
                                     :message message))]
    (send-post-event host port event)))


(defn send-alert
  "Send a status, name, message to Sensu via TCP on host, port.

   Make sure you catch for java.net.Connection exceptions when using
   TCP.

   Takes the following optional keys:

       `:host` - hostname or IP to connect to, default localhost
       `:port` - port number to connect to, default 3030
       `:name` - check name to set in the alert being sent, default mycheck
       `:refresh` - how frequently to send alerts in seconds, default 60 seconds
       `:occurrences` - number of times this exception should happen before we alert
       `:proto` - intended for TCP/UDP, default UDP
       `:meta` - metadata map (merged at top-level)

   Examples:

       (send-alert :critical \"things have failed badly\"
                   :name \"myservice\"
                   :proto :udp)

       (send-alert :critical \"things have failed badly\"
                   :name \"myservice\"
                   :proto :tcp
                   :meta {:slack_ext
                           {:slack_channel \"#dev-alerts\"}})

  Note: To be used for sensu-core agents"
  [status message
   & {:keys [host port name refresh proto prefix-length? occurrences meta]
      :or {host "localhost"
           port 3030
           name "mycheck"
           refresh 60
           proto :tcp
           prefix-length? true
           occurrences 1
           meta {}}}]
  {:pre [(contains? sensu-status status)]}
  (let [payload (sensu-payload prefix-length?
                               (into meta
                                     {:status status
                                      :name name
                                      :output message
                                      :refresh refresh
                                      :occurrences occurrences}))]
    (with-open [chan ^java.nio.channels.WritableByteChannel
                (client-channel host port proto)]
      (.write chan (ByteBuffer/wrap (.getBytes ^String payload))))))
