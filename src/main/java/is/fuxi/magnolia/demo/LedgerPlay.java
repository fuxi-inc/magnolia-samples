package is.fuxi.magnolia.demo;

import com.github.javafaker.Faker;
import is.fuxi.magnolia.TrustedLedgerClient;
import is.fuxi.magnolia.v1.Event;
import is.fuxi.magnolia.v1.GeneralResponse;
import is.fuxi.magnolia.v1.TracingRequest;
import is.fuxi.magnolia.v1.TracingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class LedgerPlay {
    private static Logger logger = LoggerFactory.getLogger(IdentifierPlay.class);
    private TrustedLedgerClient client;
    private Faker faker = new Faker();

    public LedgerPlay(TrustedLedgerClient client) {
        this.client = client;
    }

    public void show() {
        // 存证
        Event event = Event.newBuilder()
                .setId(faker.internet().domainName())
                .setName(faker.book().author())
                .setContent(faker.book().title())
                .setOperator(faker.artist().name())
                .setType(faker.music().key())
                .build();

        GeneralResponse generalResponse = client.getStub().track(event);
        assertThat(generalResponse.getResult().getStatusCode()).isEqualTo(200);
        logger.info("track event:{} successfully, result: {}", event, generalResponse);

        event = Event.newBuilder(event)
                .setContent(faker.music().instrument())
                .build();
        generalResponse = client.getStub().track(event);
        assertThat(generalResponse.getResult().getStatusCode()).isEqualTo(200);
        logger.info("track event:{} successfully, result: {}", event, generalResponse);

        // 溯源
        TracingResponse response = client.getStub().trace(TracingRequest.newBuilder()
                .setEventId(event.getId())
                .build());
        assertThat(response.getResult().getStatusCode()).isEqualTo(200);
        logger.info("trace event:{} successfully, result: {}", event.getId(), response);
    }

}
