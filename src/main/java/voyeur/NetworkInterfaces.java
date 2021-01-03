package voyeur;
/*-
 * ##########################################################################
 * Local Area Network Voyeur
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2019 - 2021 Allen D. Ball
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
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Network interface {@link java.util.Set} and {@link Service}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Service
@Log4j2
public class NetworkInterfaces extends ConcurrentSkipListSet<NetworkInterface> {
    private static final long serialVersionUID = -7886800390686536953L;

    private static final Comparator<NetworkInterface> COMPARATOR =
        Comparator
        .<NetworkInterface>comparingInt(t -> isLoopback(t) ? -1 : 1)
        .thenComparing(t -> t.getName().replaceAll("[\\p{Digit}]", ""))
        .thenComparingInt(t -> t.getIndex());

    private static boolean isLoopback(NetworkInterface ni) {
        var isLoopback = false;

        try {
            isLoopback = ni.isLoopback();
        } catch (Exception exception) {
        }

        return isLoopback;
    }

    /**
     * Sole constructor.
     */
    public NetworkInterfaces() { super(COMPARATOR); }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 60 * 1000)
    public void update() throws Exception {
        var list = Collections.list(NetworkInterface.getNetworkInterfaces());

        addAll(list);
        retainAll(list);
    }
}
