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
 * Network interface {@link Configuration}.
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
