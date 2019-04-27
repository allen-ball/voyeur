/*
 * $Id$
 *
 * Copyright 2019 Allen D. Ball.  All rights reserved.
 */
package voyeur;

import ball.spring.HTML5Template;
import ball.ssdp.SSDPDiscoveryCache;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @ModelAttribute("ssdp")
    public SSDPDiscoveryCache ssdp() { return cache; }

    @RequestMapping(value = { "/", "/ssdp" })
    public String root(Model model) { return VIEW; }

    @RequestMapping(value = { "/index", "/index.htm", "/index.html" })
    public String index() { return "redirect:/"; }
}
