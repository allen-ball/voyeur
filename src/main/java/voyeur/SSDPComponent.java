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
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
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
@NoArgsConstructor @ToString @Log4j2
public class SSDPComponent {
    @Autowired private SSDPDiscoveryCache cache = null;
    @Autowired private SSDPDiscoveryThread thread = null;

    @PostConstruct
    public void init() {
        thread.addListener(cache);
        thread.start();
    }

    @PreDestroy
    public void destroy() { }
}
