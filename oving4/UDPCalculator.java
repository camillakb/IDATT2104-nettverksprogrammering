import java.io.*;
import java.net.*;
import java.util.function.Function;

class Calculation {
    static double result;

    /**
     * Method to calculate the user input
     * 
     * @param clientInput input string from the client
     * @return the result of the calculation
     */
    static Function<String, String> calculate = (clientInput) -> {
        if (clientInput.contains("+")) {
            String[] elements = clientInput.split("\\+");
            result = Double.parseDouble(elements[0]) + Double.parseDouble(elements[1]);

        } else if (clientInput.contains("-")) {
            String[] elements = clientInput.split("-");
            result = Double.parseDouble(elements[0]) - Double.parseDouble(elements[1]);

        } else {
            return "The input has to be either addition or subtraction.\n";
        }
        return String.valueOf(result) + "\n";
    };
}


class UDPCalculator {
    public static void main(String[] args) throws IOException {
        final int PORTNR = 1250;
        byte[] inputBuffer = new byte[1024];
        byte[] outputBuffer;
        DatagramPacket receivedPacket = new DatagramPacket(inputBuffer, inputBuffer.length);

        // initialize socket
        try (DatagramSocket server = new DatagramSocket(PORTNR);) {
            System.out.println("Log for the server page. Listening on " + PORTNR + "...");

            while (true) {
                server.receive(receivedPacket);
                String receivedMessage = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                System.out.println("Packet received: " + receivedMessage);
                String response = Calculation.calculate.apply(receivedMessage);
                outputBuffer = response.getBytes();
                DatagramPacket outboundPacket = new DatagramPacket(outputBuffer, outputBuffer.length);
                outboundPacket.setAddress(receivedPacket.getAddress());
                outboundPacket.setPort(receivedPacket.getPort());
                server.send(outboundPacket);
                System.out.println("Response sent: " + response);
            }
        }
    }
}