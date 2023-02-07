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
            throw new IllegalArgumentException("The input has to be either addition or subtraction.");
        }
        return clientInput + " = " + String.valueOf(result);
    };
}

class SimpleWebServer { // class for the web server
    static Function<String, String> respond = (request) -> {

        String response = "HTTP/1.0 200 OK\n";
        response += "Content-Type: text/html; charset=utf-8 \n";
        response += "\n";
        response += "<HTML><BODY>";
        response += "<H1> Welcome to this simple web server </h1>";
        response += "<UL>";

        response += request.lines().reduce("", (String list, String line) -> list + "<li>" + line + "</li>");
        response += "</UL> </BODY> </HTML>";

        return response;
    };
}

class TCPWorkerThread extends Thread {
    private Socket connection;
    private BufferedReader reader;
    private PrintWriter writer;
    private Function<String, String> responseFunction;

    /**
     * Constructor for the TCP worker thread
     * 
     * @param connection       socket connection
     * @param responseFunction function to respond with
     * @throws IOException
     */
    public TCPWorkerThread(Socket connection, Function<String, String> responseFunction) throws IOException {
        this.connection = connection;
        this.responseFunction = responseFunction;
        InputStreamReader readinput = new InputStreamReader(connection.getInputStream());
        this.reader = new BufferedReader(readinput);
        this.writer = new PrintWriter(connection.getOutputStream(), true);
    }

    /**
     * Method to run the program
     */
    public void run() {
        /* Sends a greeting to the client */
        writer.println("Please enter something for me to calculate, then press enter:\n");

        try {
            while (true) {
                String line = reader.readLine();
                
                System.out.println("Received " + line + " in thread " + this.getId());

                try {
                    String res = this.responseFunction.apply(line);
                    writer.println(res);

                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    writer.println("Invalid input. Please try a new expression, ex. '3+3'");
                } catch (IllegalArgumentException e) {
                    writer.println(e.getMessage());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            /* Close the connection */
            try {
                reader.close();
                connection.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            writer.close();
        }
    }
}

class SocketServer {
    public static void main(String[] args) throws IOException {
        final int PORTNR = 1250;

        // initialize socket
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
