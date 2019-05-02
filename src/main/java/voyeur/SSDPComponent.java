/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package voyeur;

import ball.upnp.ssdp.SSDPDiscoveryCache;
import ball.upnp.ssdp.SSDPDiscoveryThread;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link ball.upnp.ssdp} {@link Component}
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Component
@NoArgsConstructor @ToString
public class SSDPComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private SSDPDiscoveryCache cache;
    @Autowired private SSDPDiscoveryThread thread;

    @PostConstruct
    public void init() {
        thread.addListener(cache);
        thread.start();
    }

    @PreDestroy
    public void destroy() { }
}
