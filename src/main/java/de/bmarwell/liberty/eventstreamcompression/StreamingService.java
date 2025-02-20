package de.bmarwell.liberty.eventstreamcompression;

import de.bmarwell.liberty.eventstreamcompression.models.SystemLoad;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

@ApplicationScoped
@Path("/sse")
public class StreamingService {

    private Logger logger = Logger.getLogger(StreamingService.class.getName());

    private static final RandomGenerator LoadGenerator = RandomGeneratorFactory.getDefault().create();

    private Sse sse;
    private SseBroadcaster broadcaster;

    @Resource
    ManagedScheduledExecutorService executor;

    @GET
    @Path("/")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void subscribeToSystem(
        @Context SseEventSink sink,
        @Context Sse sse
    ) {
        executor.scheduleAtFixedRate(this::createSystemLoad, 2L,  5L, TimeUnit.SECONDS);

        if (this.sse == null || this.broadcaster == null) {
            this.sse = sse;
            this.broadcaster = sse.newBroadcaster();
        }

        this.broadcaster.register(sink);
        logger.info("New sink registered to broadcaster.");
    }

    public void broadcastData(String name, Object data) {
        if (broadcaster != null) {
            OutboundSseEvent event = sse.newEventBuilder()
                .name(name)
                .data(data.getClass(), data)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build();
            broadcaster.broadcast(event);
        } else {
            logger.info("Unable to send SSE. Broadcaster context is not set up.");
        }
    }

    public void createSystemLoad() {
        SystemLoad systemLoad = new SystemLoad(
            getCanonicalHostName(),
            BigDecimal.valueOf(LoadGenerator.nextDouble()).setScale(2, RoundingMode.HALF_UP)
        );

        logger.info("Message received from system.load topic. " + systemLoad);
        broadcastData("systemLoad", systemLoad.toString());
    }

    private static String getCanonicalHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
