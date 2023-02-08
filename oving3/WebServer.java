import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class WebServer { 
    public static void main(String[] args) throws IOException {
        final int PORTNR = 1234;
        ServerSocket server = new ServerSocket(PORTNR);
        Socket socket = server.accept(); //waiting on a connection

        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        writer.println("HTTP/1.0 200 OK");
        writer.println("Content-Type: text/html; charset=utf-8 \n");
        writer.println("<HTML><BODY>");
        writer.println("<H1> Welcome to this simple web server </h1>");
        writer.println("<UL>");

        String line = reader.readLine();

        while(!line.equals("")) {
            writer.println("<li>" + line + "</li>");
            line = reader.readLine();
        }

        writer.println("</UL> </BODY> </HTML>");

        writer.close();
        reader.close();
        socket.close();
        server.close();
    }
}
