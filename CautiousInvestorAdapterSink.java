// camel-k: language=java

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class CautiousInvestorAdapterSink extends RouteBuilder {
  @Override
  public void configure() throws Exception {

    from("knative:event/predictor.better")
      .removeHeaders("*")
      .unmarshal().json()
      .log("Received prediction to ${body[operation]} BTC")
      .choice()
        .when().simple("${body[operation]} == 'buy'")
          .to("seda:buy?waitForTaskToComplete=Never")
        .when().simple("${body[operation]} == 'sell'")
          .to("seda:sell?waitForTaskToComplete=Never")
        .otherwise()
          .log("Unsupported operation: ${body[operation]}")
      .end()
      .setBody().constant("");


      from("seda:buy")
        .log("Informing the external service to BUY...")
        .setHeader(Exchange.HTTP_METHOD, constant("POST"))
        .marshal().json()
        .to("http://cautious-investor-service.{{env:NAMESPACE}}.svc.cluster.local/BTC");


      from("seda:sell")
        .log("Informing the external service to SELL...")
        .setHeader(Exchange.HTTP_METHOD, constant("DELETE"))
        .marshal().json()
        .to("http://cautious-investor-service.{{env:NAMESPACE}}.svc.cluster.local/BTC");

  }
}
