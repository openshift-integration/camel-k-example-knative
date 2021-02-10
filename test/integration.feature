Feature: all integrations run and print the correct messages

  Scenario: Integration simple-predictor consumes events
    Given Camel-K integration simple-predictor is running
    Then Camel-K integration simple-predictor should print Latest value for BTC/USDT is: 10.0

  Scenario: Integration better-predictor consumes events
    Given Camel-K integration better-predictor is running
    Then Camel-K integration better-predictor should print Latest value for BTC/USDT is: 10.0

  Scenario: Integration silly-investor consumes events
    Given Camel-K integration silly-investor is running
    Then Camel-K integration silly-investor should print Let's buy at price
  
  Scenario: Integration cautious-investor-adapter-sink consumes events
    Given Camel-K integration cautious-investor-adapter-sink is running
    Then Camel-K integration cautious-investor-adapter-sink should print Informing the external service

  Scenario: Integration cautious-investor-service consumes events
    Given Camel-K integration cautious-investor-service is running
    Then Camel-K integration cautious-investor-service should print Asking the department
