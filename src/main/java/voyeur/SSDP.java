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
import ball.upnp.ssdp.SSDPDiscoveryCache;
import ball.upnp.ssdp.SSDPDiscoveryService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@link SSDPDiscoveryCache} {@link Service}.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@Service
@NoArgsConstructor @Log4j2
public class SSDP extends SSDPDiscoveryCache {
    private static final long serialVersionUID = 881598533396699066L;

    /** @serial */ @Value("${ssdp.product}") private String product = null;

    @PostConstruct
    public void init() throws Exception {
        new SSDPDiscoveryService(product).addListener(this);
    }

    @PreDestroy
    public void destroy() { }
}
