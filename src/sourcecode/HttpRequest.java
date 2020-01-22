package sourcecode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    Map<String, String> requestMap = new HashMap<>();
    String firstLine;
    String fileName;


    public HttpRequest(Socket clientSocket) throws Exception {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            firstLine = in.readLine();
            System.out.println(firstLine);
            String[] strArr = firstLine.split(" ");

            // route to the requested html file
            if (strArr[1].equals("/")) {
                fileName = "index.html";
            }
            else if (strArr[1].equals("/chatroom")) {
                fileName = "chatroom.html";
            }
            else if (strArr[1].equals("/list")) {
                fileName = "list.html";
            }
            else fileName = strArr[1].substring(1);

            String curLine;
            while((curLine = in.readLine()) != null) {
                if (curLine.equals("")) break;
//                System.out.println(curLine);
                String[] stringArr = curLine.split(": ");
                if (stringArr.length == 2) requestMap.put(stringArr[0], stringArr[1]);
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
            throw new Exception("Bad request!");
        }

    }

    public String getFileName() {
        return fileName;
    }


    public String getFirstLine() throws IOException {
        return firstLine;
    }

    public boolean isWebSocket() {
        return requestMap.containsKey("Sec-WebSocket-Key");
    }


}
