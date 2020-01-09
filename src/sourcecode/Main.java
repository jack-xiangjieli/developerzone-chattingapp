package sourcecode;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, SQLException {
	    Map<String, Room> roomMap = new HashMap<>();
	    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        JdbcMySQLVersion jdbcMySQLVersion = new JdbcMySQLVersion();

	    while(true) {
	        try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                HttpRequest request = new HttpRequest(socketChannel.socket());
                if (!request.isWebSocket()) {
                    HttpResponse response = new HttpResponse(request);
                    response.makeHTTPResponse(socketChannel.socket());
                } else {
                    WebSocketHandler webSocketHandler = new WebSocketHandler(socketChannel.socket(), request);
                    webSocketHandler.makeWebSocketResponse(socketChannel.socket());
                    webSocketHandler.WSMessageHandler(socketChannel, roomMap, jdbcMySQLVersion);
                }
            } catch (Exception e) {
	            e.printStackTrace();
            }


            /*
            HttpRequest request = new HttpRequest(clientServer);
            if (request.getFirstLine().equals("GET / HTTP/1.1")) {
                System.out.println("get the right http request");
                HttpResponse response = new HttpResponse(request);
                response.makeHTTPResponse(clientServer);
            }
            */

            //clientServer.close();

        }


        // read and interpret the clients' request
        // check if the file exists
        // send out the header
        // if the file exists, we need to read the file and ...
        // using while (true) to handle multiple clients
    }
}
