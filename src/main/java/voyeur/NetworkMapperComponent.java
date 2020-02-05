package voyeur;
/*-
 * ##########################################################################
 * Local Area Network Voyeur
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2019 - 2020 Allen D. Ball
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ##########################################################################
 */
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
