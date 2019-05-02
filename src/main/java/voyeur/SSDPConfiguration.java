/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package voyeur;

import ball.upnp.ssdp.SSDPDiscoveryCache;
import ball.upnp.ssdp.SSDPDiscoveryThread;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link ball.upnp.ssdp} {@link Configuration}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@NoArgsConstructor @ToString
public class SSDPConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    @Bean
    public SSDPDiscoveryCache cache() { return new SSDPDiscoveryCache(); }

    @Bean
    public SSDPDiscoveryThread thread() throws Exception {
        return new SSDPDiscoveryThread(60);
    }
}
