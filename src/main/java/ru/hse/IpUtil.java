package ru.hse;

import java.net.*;
import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IpUtil {
    public static String resolveIPAddresses() {
        try {
            String ip = NetworkInterface.networkInterfaces()
                    .flatMap(networkInterface -> toStream(networkInterface.getInetAddresses()))
                    .filter(inetAddress -> !inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                    .map(InetAddress::getHostAddress)
                    .collect(Collectors.joining(", "));
            return ip;
        } catch(SocketException e) {
            return "127.0.0.1";
        }
    }

    private static Stream<InetAddress> toStream(Enumeration<InetAddress> inetAddresses) {
        return  java.util.stream.StreamSupport.stream(Spliterators.spliteratorUnknownSize(inetAddresses.asIterator(), Spliterator.ORDERED), false);
    }
}
