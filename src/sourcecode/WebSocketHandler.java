package sourcecode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

public class WebSocketHandler {
    Socket clientSocket;
    HttpRequest request;

    public WebSocketHandler(Socket clientSocket, HttpRequest request) throws NoSuchAlgorithmException, IOException {
        this.clientSocket = clientSocket;
        this.request = request;
    }

    public void makeWebSocketResponse(Socket clientSocket) throws IOException, NoSuchAlgorithmException {
        OutputStream os = clientSocket.getOutputStream();
        String encode = getWebSocketAccept(request.requestMap.get("Sec-WebSocket-Key"));
        System.out.println(encode);
        os.write(("HTTP/1.1 101 Switching Protocols\r\n" + "Upgrade: websocket\r\n" + "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + encode + "\r\n\r\n").getBytes());
    }

    public String getWebSocketAccept(String WebSocketKey) throws NoSuchAlgorithmException{
        String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String concatenatedString = WebSocketKey + magicString;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return Base64.getEncoder().encodeToString(md.digest(concatenatedString.getBytes()));
    }



    public void WSMessageHandler(SocketChannel socketChannel, Map<String, Room> roomMap, JdbcMySQLVersion jdbcMySQLVersion) throws IOException, SQLException {
        String joinMsgPayload = decodeMessage(socketChannel.socket());

        String[] joinMsgArr = new String[2];
        int firstSpacePos = joinMsgPayload.indexOf(' ');
        joinMsgArr[0] = joinMsgPayload.substring(0, firstSpacePos);
        joinMsgArr[1] = joinMsgPayload.substring(firstSpacePos + 1);

        // determine whether is "get list" message or "join room" message
        if (joinMsgArr[0].equals("get")) {     // get list message
            ArrayList<String[]> allrooms = jdbcMySQLVersion.getAllRooms();
            Collections.sort(allrooms, (a, b) -> Integer.parseInt(b[1]) - Integer.parseInt(a[1]));
            for (String[] info : allrooms) {
                sendMessage(socketChannel.socket(), info[0], info[1] + " " + info[2]);
            }
        }
        else {   // join room message
            String roomname = joinMsgArr[1];

            if (roomMap.containsKey(roomname)) {
                // add client socket to the room object
                roomMap.get(roomname).addClient(socketChannel);
            }
            else {
                Room room = new Room(socketChannel, roomname, jdbcMySQLVersion);
                room.addClient(socketChannel);
                roomMap.put(roomname, room);
            }
        }

    }

    public static String decodeMessage(Socket socket) throws IOException {
        String msg = "";
        DataInputStream is = new DataInputStream(socket.getInputStream());
        byte firstByte = is.readByte();
        int finCode = (firstByte & 0xff) >> 7;
        int opCode = firstByte & 0xf;

        byte secondByte = is.readByte();
        int mask = (secondByte & 0xff) >> 7;
        int payloadLength = secondByte & 0x7f;

        if (payloadLength <= 125) {
            byte[] maskKey = new byte[4];
            is.read(maskKey);

            byte[] clientMessageBytes = new byte[payloadLength];
            is.read(clientMessageBytes);

            for (int i = 0; i < payloadLength; i++) {
                int realNumber = clientMessageBytes[i] ^ maskKey[i % 4];
                char c = (char) (realNumber & 0xFF);
                msg += c;
            }
        }
        return msg;
    }

    public static void sendMessage(Socket socket, String username, String message) throws IOException {
        byte[] messageBytes = encodeMessage(toJSON(username, message));
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        os.write(messageBytes);
    }


    public static byte[] encodeMessage(String message) {
        byte[] messageBytes = new byte[2 + message.length()];
        messageBytes[0] = (byte)(-127);
        messageBytes[1] = (byte) message.length();
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            messageBytes[i + 2] = (byte) c;
        }
        return messageBytes;
    }


    public static String toJSON(String username, String message) {
        return "{ \"user\" : \"" + username + "\", \"message\" : \"" + message + "\" }";
    }
}
