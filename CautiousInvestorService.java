// camel-k: language=java

import org.apache.camel.builder.RouteBuilder;

public class CautiousInvestorService extends RouteBuilder {
  @Override
  public void configure() throws Exception {

      rest().post("/{currency}")
        .route()
        .log("Asking the internal department to BUY ${header.currency}...");

      rest().delete("/{currency}")
        .route()
        .log("Asking the department to SELL ${header.currency}...");

  }
}
