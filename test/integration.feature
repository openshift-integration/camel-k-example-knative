Feature: all integrations run and print the correct messages

  Scenario:
    Given integration simple-predictor is running
    Then integration simple-predictor should print Latest value for BTC/USDT is: 10.0

  Scenario:
    Given integration better-predictor is running
    Then integration better-predictor should print Latest value for BTC/USDT is: 10.0

  Scenario:
    Given integration silly-investor is running
    Then integration silly-investor should print Let's buy at price
  
  Scenario:
    Given integration cautious-investor-adapter-sink is running
    Then integration cautious-investor-adapter-sink should print Informing the external service

  Scenario:
    Given integration cautious-investor-service is running
    Then integration cautious-investor-service should print Asking the department
