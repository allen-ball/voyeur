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
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Network interface {@link Configuration}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@NoArgsConstructor @ToString @Log4j2
public class NetworkInterfaceConfiguration {
    @Bean
    public Set<NetworkInterface> interfaces() {
        Comparator<NetworkInterface> comparator =
            Comparator
            .<NetworkInterface>comparingInt(t -> isLoopback(t) ? -1 : 1)
            .thenComparing(t -> t.getName().replaceAll("[0-9]", ""))
            .thenComparingInt(t -> t.getIndex());

        return new ConcurrentSkipListSet<>(comparator);
    }

    private static boolean isLoopback(NetworkInterface ni) {
        boolean isLoopback = false;

        try {
            isLoopback = ni.isLoopback();
        } catch (Exception exception) {
        }

        return isLoopback;
    }
}
