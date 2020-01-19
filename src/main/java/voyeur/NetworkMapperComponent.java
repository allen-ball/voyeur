/*
 * $Id$
 *
 * Copyright 2019, 2020 Allen D. Ball.  All rights reserved.
 */
package voyeur;

import ball.upnp.ssdp.SSDPDiscoveryCache;
import ball.upnp.ssdp.SSDPResponse;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.Collections.list;

/**
 * Network mapper {@link Component}.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Component
@NoArgsConstructor @ToString @Log4j2
public class NetworkMapperComponent {
    private static final long MAX_AGE = 15L * 60 * 1000;

    @Autowired private Set<NetworkInterface> interfaces = null;
    @Autowired private SSDPDiscoveryCache cache = null;
    @Autowired private NetworkMap map = null;

    @PostConstruct
    public void init() { }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 60 * 1000)
    public void interfaces() {
        interfaces.stream()
            .map(NetworkInterface::getInterfaceAddresses)
            .flatMap(List::stream)
            .forEach(t -> map.add(t.getAddress()));
    }
/*
    @EventListener(ApplicationReadyEvent.class)
    public void nmapPing() throws Exception {
        new ProcessBuilder("nmap", "-n", "-oX", "-", "-PS", "10.0.1.0/24")
            .inheritIO()
            .start()
            .waitFor();
    }
*/
    @Scheduled(fixedDelay = 60 * 1000)
    public void ssdp() {
        cache.values()
            .stream()
            .map(SSDPDiscoveryCache.Value::getSSDPMessage)
            .filter(t -> t instanceof SSDPResponse)
            .map(t -> ((SSDPResponse) t).getInetAddress())
            .forEach(t -> map.add(t));
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void cull() {
        long now = System.currentTimeMillis();

        map.values().removeIf(t -> (now - t) > MAX_AGE);
    }

    @PreDestroy
    public void destroy() { }
}
