# sensu-client

A library to send alerts to sensu-client.

## Installing

![Clojars Project](http://clojars.org/helpshift/sensu-client/latest-version.svg)

## Usage

### For sensu-core

    '(require [sensu-client.core :as sensu])

    (sensu/send-alert :critical "host2 is down"
                      :host "sensu.company.com"
                      :port 3030
                      :name "host2-uptime-check"
                      :refresh 60
                      :occurrences 1
                      :proto :udp)

### For sensu-go

```clojure
    '(require [sensu-client.core :as sensu])

    (sensu/send-alert-sensu-go :critical "host2 is down"
                               :host "sensu.company.com"
                               :port 3031
                               :name "host2-uptime-check"
                               :labels {:contacts "team"})
```

Please refer [this](https://docs.sensu.io/sensu-go/6.0/observability-pipeline/observe-schedule/agent/) for more details.

## License

Copyright © 2014 Helpshift Inc.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
