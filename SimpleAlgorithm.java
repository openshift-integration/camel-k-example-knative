import org.apache.camel.BindToRegistry;

@BindToRegistry("algorithm")
public class SimpleAlgorithm {

  private static final double VARIATION = 0.0001;

  private Double previous;
  
  public Action predict(double value) {
    Double reference = previous;
    this.previous = value;

    if (reference != null && value < reference * (1-VARIATION)) {
      return new Action("buy", value);
    } else if (reference != null && value > reference * (1+VARIATION)) {
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
