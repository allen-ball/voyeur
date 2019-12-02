/*
 * $Id$
 *
 * Copyright 2019 Allen D. Ball.  All rights reserved.
 */
package voyeur;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Network mapper {@link Configuration}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@NoArgsConstructor @ToString @Log4j2
public class NetworkMapperConfiguration {
    @Bean
    public NetworkMap networkMap() { return new NetworkMap(); }
}
