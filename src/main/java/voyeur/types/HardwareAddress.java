package voyeur.types;
/*-
 * ##########################################################################
 * Local Area Network Voyeur
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2019 - 2021 Allen D. Ball
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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

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
     * {@link String} constructor.
     *
     * @param   string          The {@link String} representation.
     */
    public HardwareAddress(String string) {
        Stream.of(string.split("[:]"))
            .map(t -> Integer.valueOf(t, 16))
            .map(t -> t & 0xFF)
            .forEach(t -> list.add(t.byteValue()));
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
            .collect(joining(":"));

        return string;
    }
}
