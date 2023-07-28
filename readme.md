# Camel K Knative Example

[![kubernetes](https://github.com/openshift-integration/camel-k-example-knative/actions/workflows/kubernetes.yml/badge.svg)](https://github.com/openshift-integration/camel-k-example-knative/actions/workflows/kubernetes.yml)

This example demonstrates the idiomatic way of using Camel K in Knative for building event-driven applications. It leverages the Knative eventing broker as 
the central point that lets various services communicate via event pub/sub. It also shows how Camel K can be used for connecting the Knative event mesh with external systems, with integrations that can play the roles of "event source" or "event sink".

## Scenario

The example shows a simplified **trading system** that analyzes price variations of **Bitcoins** (BTC / USDT),
using different prediction algorithms, and informs downstream services when it's time to **buy** or **sell** bitcoins (via CloudEvents).
It uses **real data** from the bitcoin exchange market, obtained in real time via the `https://www.binance.com` Http service.

![Diagram](docs/Diagram.png)

The architecture is composed of the following Camel K integrations:

- **market-source**: creates a live feed of **BTC/USDT** events, from the Bitcoin exchange market, containing the current value of a bitcoin and related information.
- **simple-predictor**: it is triggered by variations in the **BTC/USDT** value and produces "suggested actions", as events for downstream services, telling if it's time to **buy** or **sell** at a specific value.
- **better-predictor**: it's an alternative prediction algorithm (**prediction algorithms are pluggable** in the architecture) that is wiser and generates less buy/sell events respect to the `simple-predictor`.
- **silly-investor**: this service believes blindly to the `simple-predictor` and buys/sells Bitcoins whenever the predictor suggests it.
- **cautious-investor-service**: this is a service built by another team that needs suggestions from the `better-predictor` but it needs to receive them via a pre-existing public REST API that it shared also with external entities.
- **cautious-investor-adapter-sink**: this Camel K integration listens to buy/sell events from the `better-predictor` and transforms them into REST calls to the `cautious-investor-service` using its public API.

All Camel K integrations described above (except the `market-source` which needs to poll the market for new data), are **"serverless"**, meaning that 
they scale down to zero when they don't receive new events or requests.

This means that the **whole infrastructure will not consume resources during the closing hours of the stock market** (unfortunately you won't be able to see this in the demo, because the Bitcoin market never closes, but we will simulate it).

## Preparing the cluster

This example can be run on any OpenShift 4.3+ cluster or a local development instance (such as [CRC](https://github.com/code-ready/crc)). Ensure that you have a cluster available and login to it using the OpenShift `oc` command line tool.

You need to create a new project named `camel-knative` for running this example. This can be done directly from the OpenShift web console or by executing the command `oc new-project camel-knative` on a terminal window.

You need to install the Camel K operator in the `camel-knative` project. To do so, go to the OpenShift 4.x web console, login with a cluster admin account and use the OperatorHub menu item on the left to find and install **"Red Hat Integration - Camel K"**. You will be given the option to install it globally on the cluster or on a specific namespace.
If using a specific namespace, make sure you select the `camel-knative` project from the dropdown list.
This completes the installation of the Camel K operator (it may take a couple of minutes).

When the operator is installed, from the OpenShift Help menu ("?") at the top of the WebConsole, you can access the "Command Line Tools" page, where you can download the **"kamel"** CLI, that is required for running this example. The CLI must be installed in your system path.

Refer to the **"Red Hat Integration - Camel K"** documentation for a more detailed explanation of the installation steps for the operator and the CLI.

You can use the following section to check if your environment is configured properly.

### Installing OpenShift Serverless

This demo also needs OpenShift Serverless (Knative) installed and working on the cluster.

You need to install the **OpenShift Serverless** operator from Operator Hub in your OpenShift installation, 
then use it to install both **Knative Serving** and **Knative Eventing**.

When the operator is installed, from the OpenShift Help menu ("?") at the top of the WebConsole, you can access the "Command Line Tools" page, where you can download the **"kn"** CLI, that is required for running this example. The CLI must be installed in your system path.

Refer to the OpenShift Serverless operator documentation for instructions on how to completely install it on your cluster.

## Checking requirements

**OpenShift CLI ("oc")**

The OpenShift CLI tool ("oc") will be used to interact with the OpenShift cluster.

**Connection to an OpenShift cluster**

You need to connect to an OpenShift cluster in order to run the examples.

**Apache Camel K CLI ("kamel")**

Apart from the support provided by the VS Code extension, you also need the Apache Camel K CLI ("kamel") in order to 
access all Camel K features.

**Knative CLI ("kn")**

In order to have access to all Knative features, you need to install the Knative CLI ("kn").

**Knative installed on the OpenShift cluster**

The cluster also needs to have Knative installed and working.

### Optional Requirements

The following requirements are optional. They don't prevent the execution of the demo, but may make it easier to follow.

**VS Code Extension Pack for Apache Camel**

The VS Code Extension Pack for Apache Camel by Red Hat provides a collection of useful tools for Apache Camel K developers,
such as code completion and integrated lifecycle management. They are **recommended** for the tutorial, but they are **not**
required.

You can install it from the VS Code Extensions marketplace.

## 1. Preparing the project

We'll connect to the `camel-knative` project and check the installation status.

To change project, open a terminal tab and type the following command:

```
oc project camel-knative
```

We should now check that the operator is installed. To do so, execute the following command on a terminal:

Upon successful creation, you should ensure that the Camel K operator is installed:

```
oc get csv
```

When Camel K is installed, you should find an entry related to `red-hat-camel-k-operator` in phase `Succeeded`.

You can now proceed to the next section.

## 2. Enabling the Knative Eventing Broker

The central piece of the event mesh that we're going to create is the Knative Eventing broker. It is a publish/subscribe entity 
that Camel K integrations will use to publish events or subscribe to it in order to being triggered when events of specific
types are available. Subscribers of the eventing broker are Knative serving services, that can scale down to zero when no
events are available for them.

To enable the eventing broker, we create a `default` broker in the current namespace using the Knative CLI:

```
kn broker create default
```

## 3. Push Bitcoin market data to the mesh

We'll create a `market-source.yaml` integration, using Camel **YAML DSL**,
with the role of taking live data from the Bitcoin market and pushing it to the event mesh, using the `market.btc.usdt` event type:


```
kamel run market-source.yaml --logs
```

The command above will run the integration and wait for it to run, then it will show the logs in the console.

**To exit the log view**, hit `ctrl+c` on the terminal window. The integration will **keep running** on the cluster.

## 4. Run some prediction algorithms

The market data feed available in the mesh can be now used to create different prediction algorithms that can publish events
when they believe it's the right time to sell or buy bitcoins, depending on the trend of the exchange.

In this example, we're going to run the same (basic) algorithm with different parameters*, obtaining two predictors.
The algorithm is basic, and it's just computing if the BTC variation respect to the last observed value is higher than a threshold (expressed in percentage).
The algorithm is bound to the event mesh via the `Predictor.java` integration file.

In real life, algorithms can be also much more complicated. For example, Camel K can be used to bridge an external machine learning as-a-service system that 
will compute much more accurate predictions. Algorithms can also be developed with other ad hoc tools and plugged directly inside the Knative mesh using the Knative APIs.

The first predictor that we're going to run is called `simple-predictor`:

```
kamel run --name simple-predictor -p predictor.name=simple Predictor.java -t knative-service.max-scale=1 --logs
```

NOTE: we're setting the maximum number of instances of the autoscaling service to 1 because it runs a basic algorithm that does not support scaling (stores data in memory)

The command above will deploy the integration and wait for it to run, then it will show the logs in the console.

**To exit the log view**, hit `ctrl+c` on the terminal window. The integration will **keep running** on the cluster.

The second one (`better-predictor`) will be just a variation of the first, with a different threshold:

```
kamel run --name better-predictor -p predictor.name=better -p algorithm.sensitivity=0.0005 Predictor.java -t knative-service.max-scale=1
```

You can play with the sensitivity of the `better-predictor` to make it do prediction faster or slower and see the effects on the downstream services.

Ensure that both predictors are running:

```
kamel get
```

You should wait also for the `better-predictor` integration to be running before proceeding.

## 5. Run a subscriber investor service

We are going to deploy a service that will listen to the events of type `predictor.simple` (i.e. generated by the simple predictor) and blindly executing the suggested actions (in this example, printing the action to the logs). 

It's thus called `silly-investor`. To run it:

```
kamel run SillyInvestor.java --logs
```

The command above will run the integration and wait for it to run, then it will show the logs in the console.
You should be able to see that the investor service is doing actions suggested by the simple predictions.

**To exit the log view**, hit `ctrl+c` on the terminal window. The integration will **keep running** on the cluster.

## 6. Connecting an external investor service

We'll simulate the presence of an existing investor service that is not directly connected to the mesh. It exposes a well defined API
that is available in the `CautiousInvestorService.java` file.

The service could have been developed with any language or framework, but since in this example it's developed with Camel K, it is automatically turned into 
an autoscaling serverless service.

To run it:

```
kamel run CautiousInvestorService.java -w
```

The `-w` flag (stands for "wait") in command above will make sure the command terminates on the terminal only when the integration is fully deployed.

Now we can deploy the `CautiousInvestorAdapterSink.java` integration, that will bring events from the "better" predictor right into the service APIs, after a simple transformation:

```
kamel run CautiousInvestorAdapterSink.java -w
```

Once the adapter sink is running, you can look at the external service logs to see if it's receiving recommendations.
The command for printing the logs is:

```
kamel logs cautious-investor-service
```

**To exit the log view**, hit `ctrl+c` on the terminal window.

You can alternatively follow the logs using the IDE plugin, by right clicking on a running integration on the integrations view.

**HINT**: if the pod does not run or the logs are not showing up, then probably there's nothing to show. Since the "better" predictor is not sensitive to small variations of the Bitcoin value, it's possible that the service will go down after some time to save resources. To force the service to come up again, you can edit the `CautiousInvestorAdapterSink.java` to change the starting URI from `knative:event/predictor.better` to `knative:event/predictor.simple`, then run again the integration. It's likely that the events generated by the simple predictor will trigger the downstream services more often.

## 7. When the market closes...

Bitcoin market never closes, but closing hours are expected to be present for standard markets.
We're going to simulate a closing on the market by stopping the source integration.

When the **market closes** and updates are no longer pushed into the event mesh, **all downstream services will scale down to zero**.
This includes the two prediction algorithms, the two services that receive events from the mesh and also the external investor service.

To simulate a market close, we will delete the `market-source`:

```
kamel delete market-source
```

To see the other services going down (it will take **about 2 minutes** for all services to go down), you can repeatedly run the following command:

```
oc get pod
```

At the end of the process, **no user pods will be running**.

To simulate now a reactivation of the market in the morning, you can create again the `market-source`:

```
kamel run market-source.yaml
```

Pods now will start again to run, one after the other, as soon as they are needed:

```
oc get pod
```

This terminates the example.

## 8. Uninstall

To clean up everything, execute the following command:

```oc delete project camel-knative```
