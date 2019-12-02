/*
 * $Id$
 *
 * Copyright 2019 Allen D. Ball.  All rights reserved.
 */
package voyeur;

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
 * Network interface {@link Component}
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Component
@NoArgsConstructor @ToString @Log4j2
public class NetworkInterfaceComponent {
    @Autowired private Set<NetworkInterface> interfaces = null;

    @PostConstruct
    public void init() { }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 60 * 1000)
    public void interfaces() throws Exception {
        List<NetworkInterface> list =
            list(NetworkInterface.getNetworkInterfaces());

        interfaces.addAll(list);
        interfaces.retainAll(list);
    }

    @PreDestroy
    public void destroy() { }
}
