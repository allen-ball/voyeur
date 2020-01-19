/*
 * $Id$
 *
 * Copyright 2019, 2020 Allen D. Ball.  All rights reserved.
 */
package voyeur;

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
