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
import ball.upnp.ssdp.SSDPResponse;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Objects;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * {@link InetAddress} to last response time {@link java.util.Map}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Service
@NoArgsConstructor
public class Hosts extends InetAddressMap<Long> {
    private static final long serialVersionUID = -8398595760398572996L;

    private static final long MAX_AGE = 15L * 60 * 1000;

    @Autowired private NetworkInterfaces interfaces = null;
    @Autowired private ARPCache arp = null;
    @Autowired private SSDP ssdp = null;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 60 * 1000)
    public void update() {
        interfaces
            .stream()
            .map(NetworkInterface::getInterfaceAddresses)
            .flatMap(List::stream)
            .forEach(t -> add(t.getAddress()));

        arp.keySet()
            .stream()
            .forEach(t -> add(t));

        ssdp.values()
            .stream()
            .map(SSDP.Value::getSSDPMessage)
            .filter(t -> t instanceof SSDPResponse)
            .map(t -> ((SSDPResponse) t).getInetAddress())
            .forEach(t -> add(t));
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 60 * 1000)
    public void cull() {
        long now = System.currentTimeMillis();

        values().removeIf(t -> (now - t) > MAX_AGE);
    }

    /**
     * See {@link #put(Object,Object)}.
     *
     * @param   key             The {@link InetAddress} to add with a last
     *                          response time of "now."
     *
     * @return  {@code true} if {@link.this} map changes; {@code false}
     *          otherwise.
     */
    public boolean add(InetAddress key) {
        Long value = System.currentTimeMillis();

        return Objects.equals(put(key, value), value);
    }
}
