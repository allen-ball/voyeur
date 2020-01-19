/*
 * $Id$
 *
 * Copyright 2019, 2020 Allen D. Ball.  All rights reserved.
 */
package voyeur.types;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Hardware Address.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
public class HardwareAddress {
    private final ArrayList<Byte> list = new ArrayList<>();

    /**
     * {@link NetworkInterface} constructor.
     *
     * @param   netif           The {@link NetworkInterface}.
     *
     * @throws  SocketException If the hardware address canoot be read.
     */
    public HardwareAddress(NetworkInterface netif) throws SocketException {
        this(netif.getHardwareAddress());
    }

    /**
     * {@code byte} array constructor.
     *
     * @param   bytes           The {@code byte} array representing the
     *                          hardware address.
     */
    public HardwareAddress(byte[] bytes) {
        if (bytes != null) {
            for (byte b : bytes) {
                list.add(b);
            }
        }
    }

    @Override
    public String toString() {
        String string =
            list.stream()
            .map(t -> String.format("%02x", t))
            .collect(Collectors.joining(":"));

        return string;
    }
}
