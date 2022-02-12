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
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * {@link InetAddress} {@link java.util.Map} abstract base class.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
public abstract class InetAddressMap<V> extends ConcurrentSkipListMap<InetAddress,V> {
    private static final long serialVersionUID = 4096581096396052201L;

    private static final Comparator<InetAddress> COMPARATOR =
        Comparator
        .<InetAddress>comparingInt(t -> t.isLoopbackAddress() ? -1 : 1)
        .thenComparingInt(t -> t.getAddress().length)
        .thenComparing(t -> new BigInteger(1, t.getAddress()));

    /**
     * Sole constructor.
     */
    protected InetAddressMap() { super(COMPARATOR); }
}
