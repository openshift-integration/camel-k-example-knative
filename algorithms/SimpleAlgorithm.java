package algorithms;

import org.apache.camel.BindToRegistry;
import org.apache.camel.PropertyInject;

@BindToRegistry("algorithm")
public class SimpleAlgorithm {

  @PropertyInject(value="algorithm.sensitivity", defaultValue = "0.0001")
  private double sensitivity;

  private Double previous;
  
  public Action predict(double value) {
    Double reference = previous;
    this.previous = value;

    if (reference != null && value < reference * (1 - sensitivity)) {
      return new Action("buy", value);
    } else if (reference != null && value > reference * (1 + sensitivity)) {
      return new Action("sell", value);
    }
    return null;
  }

  static class Action {
    private String operation;
    private double value;

    public Action(String operation, double value) {
      this.operation = operation;
      this.value = value;
    }

    public String getOperation() {
      return operation;
    }

    public double getValue() {
      return value;
    }

    @Override
    public String toString() {
      return this.operation + " at " + this.value;
    }

  }

}
