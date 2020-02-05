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
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * {@link java.util.Map} implementation mapping host ({@link InetAddress})
 * to last response.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
public class NetworkMap extends ConcurrentSkipListMap<InetAddress,Long> {
    private static final long serialVersionUID = -3303365313265425744L;

    private static final Comparator<InetAddress> COMPARATOR =
        Comparator
        .<InetAddress>comparingInt(t -> t.getAddress().length)
        .thenComparingInt(t -> t.isLoopbackAddress() ? -1 : 1)
        .thenComparing(t -> new BigInteger(1, t.getAddress()));

    /**
     * Sole constructor.
     */
    public NetworkMap() { super(COMPARATOR); }

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
