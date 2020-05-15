// camel-k: language=java

import org.apache.camel.builder.RouteBuilder;

public class SillyInvestor extends RouteBuilder {
  @Override
  public void configure() throws Exception {

    from("knative:event/predictor.simple")
      .unmarshal().json()
      .log("Let's ${body[operation]} at price ${body[value]} immediately!!")
      .setBody().constant("");

  }
}
