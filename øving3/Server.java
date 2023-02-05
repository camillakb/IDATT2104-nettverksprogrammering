import java.io.*;
import java.net.*;

class Calculation {
    static double result;

    /**
     * Method to calculate the user input
     * @param clientInput input string from the client
     * @return the result of the calculation
     */
    static double calculate(String clientInput) {
        if (clientInput.contains("+")) {
            String[] elements = clientInput.split("\\+");
            result = Double.parseDouble(elements[0]) + Double.parseDouble(elements[1]);
        
        } if (clientInput.contains("-")) {
            String[] elements = clientInput.split("-");
            result = Double.parseDouble(elements[0]) - Double.parseDouble(elements[1]);
        }
        return result;
    }

    /**
     * Method to see if the client's input is valid
     * @param clientInput input string from the client
     * @return true if the input is valid, false if not.
     */
    static boolean validInput(String clientInput) {
        String regex = ".*[a-zA-Z].*";

        if (!clientInput.contains("+") && !clientInput.contains("-")) {
            return false;
        }

        if (clientInput.contains(regex)) {
            return false;
        }

        return true;
    }
}

class SocketServer {
    public static void main(String[] args) throws IOException {
        final int PORTNR = 1250;

        ServerSocket server = new ServerSocket(PORTNR);
        System.out.println("Log for the server page. Waiting...");
        Socket connection = server.accept(); // waiting until someone connects

        /* Opens stream for communication with the client */
        InputStreamReader readinput = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(readinput);
        PrintWriter writer = new PrintWriter(connection.getOutputStream(), true);

        /* Sends a greeting to the client */
        writer.println("Please enter something for me to calculate, then press enter:\n"); // only addition or subtraction possible

        /* Gets data from client */
        String inputLine = reader.readLine(); // gets line of input
        while (inputLine != null) {
            if (Calculation.validInput(inputLine)) { //input is an equation
                double res = Calculation.calculate(inputLine);
                writer.println(inputLine + "=" + res); // sends a response to the client
            
            } else {
                writer.println("Invalid input. Please try a new expression, ex. '3+3'");
            }
            
            inputLine = reader.readLine();
        }

        /* Close the connection */
        reader.close();
        writer.close();
        connection.close();
        server.close();
    }
}
