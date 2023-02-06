import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
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
            throw new IllegalArgumentException("Addition or subtraction ONLY!!!");
        }
        return clientInput + " = " + String.valueOf(result);
    };
}

class SimpleWebServer {
    static Function<String, String> respond = (request) -> {

        String response = "HTTP/1.0 200 OK\n";
        response += "Content-Type: text/html; charset=utf-8 \n";
        response += "\n";
        response += "<HTML><BODY>";
        response += "<H1> Hilsen. Du har koblet deg opp til min enkle web-tjener </h1>";
        response += "<UL>";

        response += request.lines().reduce((list, line) -> list += "<LI>" + line + "</LI>");
        response += "</UL> </BODY> </HTML>";

        return response;
    };
}

class TCPWorkerThread extends Thread {
    private Socket connection;
    private BufferedReader reader;
    private PrintWriter writer;
    private Function<String, String> responseFunction;

    public TCPWorkerThread(Socket connection, Function<String, String> responseFunction) throws IOException {
        this.connection = connection;
        this.responseFunction = responseFunction;
        InputStreamReader readinput = new InputStreamReader(connection.getInputStream());
        this.reader = new BufferedReader(readinput);
        this.writer = new PrintWriter(connection.getOutputStream(), true);
    }

    public void run() {
        /* Sends a greeting to the client */
        writer.println("Please enter something for me to calculate, then press enter:\n");

        /* Gets data from client */
        CharBuffer requestBuff = CharBuffer.allocate(1024);

        try {
            reader.read(requestBuff);

            while (requestBuff.toString() != null) {
                System.out.println("Received " + requestBuff.toString() + " in " + this.getId());
                try { 
                    String res = this.responseFunction.apply(requestBuff.toString());
                    writer.write(res);

                } catch(NumberFormatException | IndexOutOfBoundsException e) {
                    writer.println("Invalid input. Please try a new expression, ex. '3+3'");
                } catch(IllegalArgumentException e) {
                    writer.println(e.getMessage());
                }

                requestBuff.clear();
                reader.read(requestBuff);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } finally {
            /* Close the connection */
            try {
                reader.close();
                connection.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            writer.close();
        }
    }
}


class SocketServer {
    public static void main(String[] args) throws IOException {
        final int PORTNR = 1250;

        try (ServerSocket server = new ServerSocket(PORTNR);) {
            System.out.println("Log for the server page. Listening on " + PORTNR + "...");

            while (true) {
                Socket connection = server.accept(); // waiting until someone connects
                System.out.println("New client connected" + connection.getInetAddress());
                TCPWorkerThread worker = new TCPWorkerThread(connection, Calculation.calculate);
                worker.start();
            }
        }
    }
}
