package sourcecode;

import java.io.*;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

public class HttpResponse {

    HttpRequest request;

    public HttpResponse(HttpRequest request) throws IOException {
        this.request = request;
    }

    public void makeHTTPResponse(Socket clientSocket) throws IOException, InterruptedException {

        String fileName = request.getFileName();
        String filePath = "resources/" + fileName;
        String contentType;
        if (fileName.endsWith(".js")) contentType = "text/javascript";
        else if (fileName.endsWith(".css")) contentType = "text/css";
        else contentType = "text/html";

        File file = new File(filePath);
        System.out.println(filePath);
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
        if (file.exists()) {
            System.out.println("file exists");
            FileInputStream fs = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];

            // send out the header
            System.out.println(clientSocket.isClosed());
            System.out.println("start to send the header");
            System.out.println(clientSocket.isOutputShutdown());
            dos.writeBytes("HTTP/1.1 200 OK\r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + data.length);
            dos.writeBytes("\r\n\r\n");
            System.out.println("finish sending the header");

            fs.transferTo(dos);
            System.out.println("finish sending the file");
            dos.close();


        } else {
            dos.write(("HTTP/1.1 404 NOT FOUND\r\n" + "Cache-Control: max-age=604800\r\n" + "Content-Type: text/html\r\n"
            + "\r\n\r\n" + "<h1> 404 : file not found </h1>\r\n").getBytes());

            dos.flush();
            dos.close();
        }

    }

}
