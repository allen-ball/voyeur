/*
 * $Id$
 *
 * Copyright 2019 Allen D. Ball.  All rights reserved.
 */
package voyeur;

import java.net.NetworkInterface;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Network {@link Configuration}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@NoArgsConstructor @ToString
public class NetworkConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    @Bean
    public Set<NetworkInterface> interfaces() {
        Comparator<NetworkInterface> comparator =
            Comparator.comparing(NetworkInterface::getName);

        return new ConcurrentSkipListSet<>(comparator);
    }
}
