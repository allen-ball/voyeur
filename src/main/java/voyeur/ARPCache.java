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
import ball.annotation.CompileTimeCheck;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import voyeur.types.HardwareAddress;

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

/**
 * {@link java.net.InetAddress} to {@link HardwareAddress}
 * {@link java.util.Map}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@Service
@NoArgsConstructor @Log4j2
public class ARPCache extends InetAddressMap<HardwareAddress> {
    private static final long serialVersionUID = 8171059827964411042L;

    private static final Path PATH = Paths.get("/proc/net/arp");

    private static final ProcessBuilder BUILDER =
        new ProcessBuilder("arp", "-an")
        .inheritIO()
        .redirectOutput(PIPE);

    @CompileTimeCheck
    private static final Pattern PATTERN =
        Pattern.compile("(?i)^(?<host>.+)"
                        + " [(](?<inet>[\\p{Digit}.]+)[)]"
                        + " at (?<mac>[\\p{XDigit}:]+) .*$");

    /** @serial */ private boolean disabled = false;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 60 * 1000)
    public void update() {
        if (! isDisabled()) {
            try {
                var map = Files.exists(PATH) ? parse(PATH) : parse(BUILDER);

                if (map != null) {
                    putAll(map);
                    keySet().retainAll(map.keySet());
                }
            } catch (Exception exception) {
                log.error("{}", exception.getMessage(), exception);
            }
        }
    }

    public boolean isDisabled() { return disabled; }

    private ARPCache parse(Path path) throws Exception {
        var map =
            Files.lines(path, UTF_8)
            .skip(1)
            .map(t -> t.split("[\\p{Space}]+"))
            .collect(toMap(k -> getInetAddress(k[0]), v -> new HardwareAddress(v[3]), (t, u) -> t, ARPCache::new));

        return map;
    }

    private ARPCache parse(ProcessBuilder builder) throws Exception {
        ARPCache map = null;

        try {
            var process = builder.start();

            try (var in = process.getInputStream()) {
                map =
                    new BufferedReader(new InputStreamReader(in, UTF_8)).lines()
                    .map(PATTERN::matcher)
                    .filter(Matcher::matches)
                    .collect(toMap(k -> getInetAddress(k.group("inet")), v -> new HardwareAddress(v.group("mac")),
                                   (t, u) -> t, ARPCache::new));
            }

            disabled = (process.waitFor() != 0);
        } catch (Exception exception) {
            disabled = true;
        }

        if (disabled) {
            log.warn("arp command is not available");
        }

        return map;
    }

    private InetAddress getInetAddress(String string) {
        InetAddress address = null;

        try {
            address = InetAddress.getByName(string);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }

        return address;
    }
}
