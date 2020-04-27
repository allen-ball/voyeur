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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.util.stream.Collectors.toList;

/**
 * {@link InetAddress} to {@code nmap} {@link Document} output
 * {@link java.util.Map}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Service
@NoArgsConstructor @Log4j2
public class Hosts extends InetAddressMap<Document> {
    private static final long serialVersionUID = -4713999339531142905L;

    private static final long MAX_AGE = 15L * 60 * 1000;

    private static final List<String> NMAP_ARGV =
        Arrays.asList("nmap", "-n", "-oX", "-", "-PS");

    private static final DocumentBuilder BUILDER;

    static {
        try {
            BUILDER =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    /** @serial */ @Autowired private NetworkInterfaces interfaces = null;
    /** @serial */ @Autowired private ARPCache arp = null;
    /** @serial */ @Autowired private SSDP ssdp = null;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 60 * 1000)
    public void update() {
        try {
            Document empty = BUILDER.newDocument();

            interfaces
                .stream()
                .map(NetworkInterface::getInterfaceAddresses)
                .flatMap(List::stream)
                .forEach(t -> putIfAbsent(t.getAddress(), empty));

            arp.keySet()
                .stream()
                .forEach(t -> putIfAbsent(t, empty));

            ssdp.values()
                .stream()
                .map(SSDP.Value::getSSDPMessage)
                .filter(t -> t instanceof SSDPResponse)
                .map(t -> ((SSDPResponse) t).getInetAddress())
                .forEach(t -> putIfAbsent(t, empty));

            for (InetAddress key : keySet()) {
                List<String> argv = NMAP_ARGV.stream().collect(toList());

                argv.add(key.getHostAddress());

                ProcessBuilder builder =
                    new ProcessBuilder(argv)
                    .inheritIO()
                    .redirectOutput(PIPE);
                Process process = builder.start();

                try (InputStream in = process.getInputStream()) {
                    Document value = BUILDER.parse(in);

                    put(key, value);
                }
            }
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }
}
