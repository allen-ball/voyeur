/*
 * $Id$
 *
 * Copyright 2019 Allen D. Ball.  All rights reserved.
 */
package voyeur;

import ball.spring.HTML5Template;
import ball.ssdp.SSDPDiscoveryCache;
import ball.ssdp.SSDPMessage;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * UI {@link Controller} implementation
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Controller
@NoArgsConstructor @ToString
public class UIController extends HTML5Template {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private SSDPDiscoveryCache cache;

    @ModelAttribute("interfaces")
    public Enumeration<NetworkInterface> interfaces() throws Exception {
        return NetworkInterface.getNetworkInterfaces();
    }

    @ModelAttribute("ssdp")
    public SSDPDiscoveryCache ssdp() { return cache; }

    @ModelAttribute("upnp")
    public Map<URI,List<URI>> upnp() {
        Map<URI,List<URI>> map =
            ssdp().values().stream()
            .map(SSDPDiscoveryCache.Value::getSSDPMessage)
            .collect(groupingBy(SSDPMessage::getLocation,
                                mapping(SSDPMessage::getUSN, toList())));

        return map;
    }

    @RequestMapping(value = {
                        "/",
                        "/upnp/devices", "/upnp/ssdp",
                        "/network/interfaces"
                    })
    public String root(Model model) { return VIEW; }

    @RequestMapping(value = { "/index", "/index.htm", "/index.html" })
    public String index() { return "redirect:/"; }
}
