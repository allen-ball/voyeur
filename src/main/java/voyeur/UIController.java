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
import ball.spring.AbstractController;
import ball.upnp.ssdp.SSDPDiscoveryCache;
import ball.upnp.ssdp.SSDPMessage;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * UI {@link Controller} implementation.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Controller
@NoArgsConstructor @ToString @Log4j2
public class UIController extends AbstractController {
    @Autowired private SSDPDiscoveryCache ssdp = null;
    @Autowired private Set<NetworkInterface> interfaces = null;
    @Autowired private NetworkMap map = null;

    @ModelAttribute("upnp")
    public Map<URI,List<URI>> upnp() {
        Map<URI,List<URI>> map =
            ssdp().values()
            .stream()
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

    @ModelAttribute("hosts")
    public Set<InetAddress> hosts() { return map.keySet(); }

    @RequestMapping(value = {
                        "/",
                        "/upnp/devices", "/upnp/ssdp",
                        "/network/interfaces", "/network/hosts"
                    })
    public String root(Model model) { return getViewName(); }

    @RequestMapping(value = { "/index", "/index.htm", "/index.html" })
    public String index() { return "redirect:/"; }
}
