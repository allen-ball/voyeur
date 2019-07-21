/*
 * $Id$
 *
 * Copyright 2019 Allen D. Ball.  All rights reserved.
 */
package voyeur;

import ball.spring.HTML5Controller;
import ball.upnp.ssdp.SSDPDiscoveryCache;
import ball.upnp.ssdp.SSDPMessage;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
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
public class UIController extends HTML5Controller {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private SSDPDiscoveryCache ssdp;
    @Autowired private Set<NetworkInterface> interfaces;

    @ModelAttribute("upnp")
    public Map<URI,List<URI>> upnp() {
        Map<URI,List<URI>> map =
            ssdp().values().stream()
            .map(SSDPDiscoveryCache.Value::getSSDPMessage)
            .collect(groupingBy(SSDPMessage::getLocation,
                                ConcurrentSkipListMap::new,
                                mapping(SSDPMessage::getUSN, toList())));

        return map;
    }

    @ModelAttribute("ssdp")
    public SSDPDiscoveryCache ssdp() { return ssdp; }

    @ModelAttribute("interfaces")
    public Set<NetworkInterface> interfaces() { return interfaces; }

    @RequestMapping(value = {
                        "/",
                        "/upnp/devices", "/upnp/ssdp",
                        "/network/interfaces"
                    })
    public String root(Model model) { return template(); }

    @RequestMapping(value = { "/index", "/index.htm", "/index.html" })
    public String index() { return "redirect:/"; }
}
