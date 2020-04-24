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
import java.net.InetAddress;
import java.util.Objects;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link InetAddress} to last response time {@link java.util.Map}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Service
@NoArgsConstructor
public class NetworkMap extends InetAddressMap<Long> {
    private static final long serialVersionUID = -7995278362750060947L;

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
