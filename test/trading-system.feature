Feature: Trading system

  Scenario: Create Knative broker
    Given create Knative broker default
    Then Knative broker default is running

  Scenario: Create market source
    Given load Camel-K integration MarketSourceMock.java
    Then Camel-K integration market-source-mock should be running

  Scenario: Create simple-predictor
    Given load Camel-K integration Predictor.java with configuration
      | name         | simple-predictor |
      | properties   | predictor.name=simple |
      | traits       | knative-service.max-scale=1 |
    Then Camel-K integration simple-predictor should be running

  Scenario: Create better-predictor
    Given load Camel-K integration Predictor.java with configuration
      | name         | better-predictor |
      | properties   | predictor.name=better,algorithm.sensitivity=0.0005 |
      | traits       | knative-service.max-scale=1 |
    Then Camel-K integration better-predictor should be running

  Scenario: Create silly-investor
    Given load Camel-K integration SillyInvestor.java
    Then Camel-K integration silly-investor should be running

  Scenario: Create cautious-investor-service
    Given load Camel-K integration CautiousInvestorService.java
    Then Camel-K integration cautious-investor-service should be running

  Scenario: Create cautious-investor-adapter-sink
    Given load Camel-K integration CautiousInvestorAdapterSink.java
    Then Camel-K integration cautious-investor-adapter-sink should be running

  Scenario: Integrations consume events
    Then Camel-K integration simple-predictor should print Latest value for BTC/USDT is: 10.0
    Then Camel-K integration better-predictor should print Latest value for BTC/USDT is: 10.0
    Then Camel-K integration silly-investor should print Let's buy at price 10.0 immediately!!
    Then Camel-K integration cautious-investor-adapter-sink should print Informing the external service to BUY...
    Then Camel-K integration cautious-investor-service should print Asking the internal department to BUY BTC

  Scenario: Delete Camel K resources
    Given delete Camel-K integration market-source-mock
    Given delete Camel-K integration simple-predictor
    Given delete Camel-K integration better-predictor
    Given delete Camel-K integration silly-investor
    Given delete Camel-K integration cautious-investor-adapter-sink
    Given delete Camel-K integration cautious-investor-service
