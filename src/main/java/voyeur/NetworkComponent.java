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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.Collections.list;

/**
 * Network {@link Component}
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Component
@NoArgsConstructor @ToString
public class NetworkComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private Set<NetworkInterface> interfaces;

    @PostConstruct
    public void init() { }

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
