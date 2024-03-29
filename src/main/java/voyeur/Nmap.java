package voyeur;
/*-
 * ##########################################################################
 * Local Area Network Voyeur
 * %%
 * Copyright (C) 2019 - 2022 Allen D. Ball
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
import ball.xml.XalanConstants;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.NUMBER;

/**
 * {@link InetAddress} to {@code nmap} output {@link Document}
 * {@link java.util.Map}.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@RestController
@RequestMapping(value = { "/network/nmap/" },
                produces = MediaType.APPLICATION_XML_VALUE)
@Service
@NoArgsConstructor @Log4j2
public class Nmap extends InetAddressMap<Document> implements XalanConstants {
    private static final long serialVersionUID = -1039375546367339774L;

    private static final Duration INTERVAL = Duration.ofMinutes(60);

    private static final String NMAP = "nmap";
    private static final List<String> NMAP_ARGV =
        Stream.of(NMAP, "--no-stylesheet", "-oX", "-", "-n", "-PS", "-A")
        .collect(toList());

    /** @serial */ @Autowired private NetworkInterfaces interfaces = null;
    /** @serial */ @Autowired private ARPCache arp = null;
    /** @serial */ @Autowired private SSDP ssdp = null;
    /** @serial */ @Autowired private ThreadPoolTaskExecutor executor = null;
    /** @serial */ private DocumentBuilderFactory factory = null;
    /** @serial */ private XPath xpath = null;
    /** @serial */ private Transformer transformer = null;
    /** @serial */ private boolean disabled = true;

    @PostConstruct
    public void init() throws Exception {
        factory = DocumentBuilderFactory.newInstance();

        xpath = XPathFactory.newInstance().newXPath();

        transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OMIT_XML_DECLARATION, NO);
        transformer.setOutputProperty(INDENT, YES);
        transformer.setOutputProperty(XALAN_INDENT_AMOUNT.toString(), String.valueOf(2));

        try {
            var argv = Stream.of(NMAP, "-version").collect(toList());

            log.info("{}", argv);

            var process =
                new ProcessBuilder(argv)
                .inheritIO()
                .redirectOutput(PIPE)
                .start();

            try (var in = process.getInputStream()) {
                new BufferedReader(new InputStreamReader(in, UTF_8)).lines()
                    .forEach(t -> log.info("{}", t));
            }

            disabled = (process.waitFor() != 0);
        } catch (Exception exception) {
            disabled = true;
        }

        if (disabled) {
            log.warn("nmap command is not available");
        }
    }

    @PreDestroy
    public void destroy() { }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 30 * 1000)
    public void update() {
        if (! isDisabled()) {
            try {
                var empty = factory.newDocumentBuilder().newDocument();

                empty.appendChild(empty.createElement("nmaprun"));

                interfaces.stream()
                    .map(NetworkInterface::getInterfaceAddresses)
                    .flatMap(List::stream)
                    .map(InterfaceAddress::getAddress)
                    .filter(t -> (! t.isMulticastAddress()))
                    .forEach(t -> putIfAbsent(t, empty));

                arp.keySet().stream()
                    .filter(t -> (! t.isMulticastAddress()))
                    .forEach(t -> putIfAbsent(t, empty));

                ssdp.values().stream()
                    .filter(t -> t instanceof SSDPResponse)
                    .map(t -> ((SSDPResponse) t).getSocketAddress())
                    .map(t -> ((InetSocketAddress) t).getAddress())
                    .forEach(t -> putIfAbsent(t, empty));

                keySet().stream()
                    .filter(t -> INTERVAL.compareTo(getOutputAge(t)) < 0)
                    .map(Worker::new)
                    .forEach(t -> executor.execute(t));
            } catch (Exception exception) {
                log.error("{}", exception.getMessage(), exception);
            }
        }
    }

    @RequestMapping(value = { "{ip}.xml" })
    public String nmap(@PathVariable String ip) throws Exception {
        var out = new ByteArrayOutputStream();

        transformer.transform(new DOMSource(get(InetAddress.getByName(ip))), new StreamResult(out));

        return out.toString(UTF_8.name());
    }

    public boolean isDisabled() { return disabled; }

    public Set<Integer> getPorts(InetAddress key) {
        var ports = new TreeSet<Integer>();
        var list = (NodeList) get(key, "/nmaprun/host/ports/port/@portid", NODESET);

        if (list != null) {
            for (int i = 0; i < list.getLength(); i += 1) {
                ports.add(Integer.parseInt(list.item(i).getNodeValue()));
            }
        }

        return ports;
    }

    public Set<String> getProducts(InetAddress key) {
        var products = new LinkedHashSet<String>();
        var list = (NodeList) get(key, "/nmaprun/host/ports/port/service/@product", NODESET);

        if (list != null) {
            for (int i = 0; i < list.getLength(); i += 1) {
                products.add(list.item(i).getNodeValue());
            }
        }

        return products;
    }

    private Duration getOutputAge(InetAddress key) {
        long start = 0;
        var number = (Number) get(key, "/nmaprun/runstats/finished/@time", NUMBER);

        if (number != null) {
            start = number.longValue();
        }

        return Duration.between(Instant.ofEpochSecond(start), Instant.now());
    }

    private Object get(InetAddress key, String expression, QName qname) {
        Object object = null;
        var document = get(key);

        if (document != null) {
            try {
                object = xpath.compile(expression).evaluate(document, qname);
            } catch (Exception exception) {
                log.error("{}", exception.getMessage(), exception);
            }
        }

        return object;
    }

    @RequiredArgsConstructor @EqualsAndHashCode @ToString
    private class Worker implements Runnable {
        private final InetAddress key;

        @Override
        public void run() {
            try {
                var argv = NMAP_ARGV.stream().collect(toList());

                if (key instanceof Inet4Address) {
                    argv.add("-4");
                } else if (key instanceof Inet6Address) {
                    argv.add("-6");
                }

                argv.add(key.getHostAddress());

                var builder = factory.newDocumentBuilder();
                var process =
                    new ProcessBuilder(argv)
                    .inheritIO()
                    .redirectOutput(PIPE)
                    .start();

                try (var in = process.getInputStream()) {
                    put(key, builder.parse(in));

                    int status = process.waitFor();

                    if (status != 0) {
                        throw new IOException(argv + " returned exit status " + status);
                    }
                }
            } catch (Exception exception) {
                remove(key);
                log.error("{}", exception.getMessage(), exception);
            }
        }
    }
}
