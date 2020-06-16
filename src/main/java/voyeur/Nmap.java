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
import ball.xml.XalanConstants;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.util.stream.Collectors.toList;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.NUMBER;

/**
 * {@link InetAddress} to {@code nmap} {@link Document} output
 * {@link java.util.Map}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@RestController @Service
@NoArgsConstructor @Log4j2
public class Nmap extends InetAddressMap<Document> implements XalanConstants {
    private static final long serialVersionUID = 1L;

    private static final Duration MAX_AGE = Duration.ofMinutes(15);

    private static final List<String> NMAP_ARGV =
        Arrays.asList("nmap", "-n", "-PS", "-A",
                      "--no-stylesheet", "-oX", "-");

    private static final DocumentBuilderFactory FACTORY;
    private static final XPath XPATH;
    private static final Transformer TRANSFORMER;

    static {
        try {
            FACTORY = DocumentBuilderFactory.newInstance();

            XPATH = XPathFactory.newInstance().newXPath();

            TRANSFORMER =
                TransformerFactory.newInstance().newTransformer();
            TRANSFORMER.setOutputProperty(OMIT_XML_DECLARATION, NO);
            TRANSFORMER.setOutputProperty(INDENT, YES);
            TRANSFORMER.setOutputProperty(XALAN_INDENT_AMOUNT.toString(),
                                          String.valueOf(2));
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    /** @serial */ @Autowired private NetworkInterfaces interfaces = null;
    /** @serial */ @Autowired private ARPCache arp = null;
    /** @serial */ @Autowired private SSDP ssdp = null;
    /** @serial */ private final ThreadPoolExecutor executor =
        (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 30 * 1000)
    public void update() {
        try {
            Document empty = FACTORY.newDocumentBuilder().newDocument();

            interfaces
                .stream()
                .map(NetworkInterface::getInterfaceAddresses)
                .flatMap(List::stream)
                 .map(InterfaceAddress::getAddress)
                .filter(t -> (! t.isMulticastAddress()))
                .forEach(t -> putIfAbsent(t, empty));

            arp.keySet()
                .stream()
                .filter(t -> (! t.isMulticastAddress()))
                .forEach(t -> putIfAbsent(t, empty));

            ssdp.values()
                .stream()
                .map(SSDP.Value::getSSDPMessage)
                .filter(t -> t instanceof SSDPResponse)
                .map(t -> ((SSDPResponse) t).getInetAddress())
                .forEach(t -> putIfAbsent(t, empty));

            if (executor.getActiveCount() == 0) {
                keySet()
                    .stream()
                    .map(Worker::new)
                    .forEach(t -> executor.execute(t));
            }
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    @RequestMapping(value = { "/network/nmap/{ip}.xml" },
                    produces = MediaType.APPLICATION_XML_VALUE)
    public String nmap(@PathVariable String ip) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TRANSFORMER.transform(new DOMSource(get(InetAddress.getByName(ip))),
                              new StreamResult(out));

        return out.toString("UTF-8");
    }

    public Set<Integer> getPorts(InetAddress key) {
        Set<Integer> ports = new TreeSet<>();
        Document document = get(key);

        if (document != null) {
            try {
                XPathExpression expression =
                    XPATH.compile("/nmaprun/host/ports/port/@portid");
                NodeList list =
                    (NodeList) expression.evaluate(document, NODESET);

                for (int i = 0; i < list.getLength(); i += 1) {
                    ports.add(Integer.parseInt(list.item(i).getNodeValue()));
                }
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
            }
        }

        return ports;
    }

    public Set<String> getProducts(InetAddress key) {
        Set<String> products = new LinkedHashSet<>();
        Document document = get(key);

        if (document != null) {
            try {
                XPathExpression expression =
                    XPATH.compile("/nmaprun/host/ports/port/service/@product");
                NodeList list =
                    (NodeList) expression.evaluate(document, NODESET);

                for (int i = 0; i < list.getLength(); i += 1) {
                    products.add(list.item(i).getNodeValue());
                }
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
            }
        }

        return products;
    }

    @RequiredArgsConstructor @ToString
    private class Worker implements Runnable {
        private final InetAddress key;

        @Override
        public void run() {
            try {
                XPathExpression expression =
                    XPATH.compile("/nmaprun/runstats/finished/@time");
                long start =
                    ((Number) expression.evaluate(get(key), NUMBER))
                    .longValue();
                Duration duration =
                    Duration.between(Instant.ofEpochSecond(start),
                                     Instant.now());

                if (MAX_AGE.compareTo(duration) < 0) {
                    List<String> argv = NMAP_ARGV.stream().collect(toList());

                    if (key instanceof Inet4Address) {
                        argv.add("-4");
                    } else if (key instanceof Inet6Address) {
                        argv.add("-6");
                    }

                    argv.add(key.getHostAddress());

                    DocumentBuilder builder = FACTORY.newDocumentBuilder();
                    Process process =
                        new ProcessBuilder(argv)
                        .inheritIO()
                        .redirectOutput(PIPE)
                        .start();

                    try (InputStream in = process.getInputStream()) {
                        put(key, builder.parse(in));
                        /*
                         * save(get(key),
                         *      "target/" + key.getHostAddress() + ".xml");
                         */
                    }
                }
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
            }
        }

        private void save(Document document, String path) {
            try {
                TRANSFORMER.transform(new DOMSource(document),
                                      new StreamResult(new File(path)));
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
            }
        }
    }
}