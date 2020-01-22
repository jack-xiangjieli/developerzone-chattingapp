package sourcecode;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.*;

public class Room {
    public String roomname;
    private Selector selector;
    private Set<SocketChannel> clientsInTheRoom = new HashSet<>();
    private Set<SocketChannel> clientsToBeAdded = new HashSet<>();

    private ArrayList<String[]> allMessages;
    JdbcMySQLVersion jdbcMySQLVersion;

    /**
     * The constructor take a socket channel as a parameter
     * Because we call the constructor when a client wants to join a room that doesn't exist
     * And we create a new room for the client, and add the client to the room
     */
    public Room(SocketChannel socketChannel, String roomname, JdbcMySQLVersion jdbcMySQLVersion) throws IOException, SQLException {
        this.roomname = roomname;

        this.jdbcMySQLVersion = jdbcMySQLVersion;

        // retrieve all the history messages in this room
        // if there is no such <roomname> table in the database, create one in the database
        // if there is , then query all messages into the allmessages arraylist;
        if (!jdbcMySQLVersion.hasRoom(roomname)) {
            allMessages = new ArrayList<>();
            jdbcMySQLVersion.addRoom(roomname);
            jdbcMySQLVersion.createNewRoomTable(roomname);
        } else {
            allMessages = jdbcMySQLVersion.getAllMessages(roomname);
        }


        // This is a constructor call
        this.selector = Selector.open();

        // A Channel has to be in non-blocking mode before it can be registered with the selector
        socketChannel.configureBlocking(false);
        this.selector.selectNow(); // Ben's slides say that this is to make the selector happy
        socketChannel.register(selector, SelectionKey.OP_READ);

        clientsInTheRoom.add(socketChannel);

        // Create a new thread for the new room
        // In this project, one thread handles all for one room
        Thread thread = new Thread(() -> {
            try {
                serverRoom();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }


    public void serverRoom() throws IOException, SQLException {
        while (true) {
            selector.select();

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            ArrayList<String[]> newMessages = new ArrayList<>();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    iterator.remove();
                    key.cancel();
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    socketChannel.configureBlocking(true);

                    Socket socket = socketChannel.socket();

                    String msg = WebSocketHandler.decodeMessage(socket);
                    System.out.println("decoded message: " + msg);
                    String[] tmpArr = new String[2];
                    int firstSpacePos = msg.indexOf(' ');

                    // firstSpacePos <= 0 means that current client has left the room
                    // and we received a message of his leaving, so we just remove the
                    // client socket from the clientsInTheRoom set and continue the loop
                    if (firstSpacePos <= 0) {
                        clientsInTheRoom.remove(socketChannel);
                        jdbcMySQLVersion.updateCurrentOnline(roomname, clientsInTheRoom.size());
                        continue;
                    }

                    tmpArr[0] = msg.substring(0, firstSpacePos);
                    tmpArr[1] = msg.substring(firstSpacePos + 1);
                    newMessages.add(new String[]{tmpArr[0], tmpArr[1]});
                    allMessages.add(new String[]{tmpArr[0], tmpArr[1]});
                    jdbcMySQLVersion.addMessage(roomname, new String[]{tmpArr[0], tmpArr[1]});

                    socketChannel.configureBlocking(false);
                    selector.selectNow();
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }
            addAllClients();
            sendMessagesToAllClients(newMessages);
        }
    }




    /**
     * The synchronized keyword makes sure that
     * only one client channel is being added to the room at one time
     */
    public synchronized void addClient(SocketChannel socketChannel) {
        clientsToBeAdded.add(socketChannel);
        // This will wake up the selector even it doesn't have a readable key
        selector.wakeup();
    }

    /**
     * We need the synchonized key word since the selector, clientsInTheRoom,
     * and the clientsToBeAdded variables are being shared
     */
    public synchronized void addAllClients() throws IOException, SQLException {
        for (SocketChannel socketChannel : clientsToBeAdded) {
            socketChannel.configureBlocking(false);
            selector.selectNow();
            socketChannel.register(selector, SelectionKey.OP_READ);

            sendMessages(socketChannel, allMessages);

            clientsInTheRoom.add(socketChannel);
        }
        clientsToBeAdded.clear();
        jdbcMySQLVersion.updateCurrentOnline(roomname, clientsInTheRoom.size());
    }


    public void sendMessagesToAllClients(ArrayList<String[]> newMessages) throws IOException {
        for (SocketChannel socketChannel : clientsInTheRoom) {
            sendMessages(socketChannel, newMessages);
        }
    }

    public void sendMessages(SocketChannel socketChannel, ArrayList<String[]> messages) throws IOException {
        for (var message : messages) {
            SelectionKey key = socketChannel.keyFor(selector);
            key.cancel();

            socketChannel.configureBlocking(true);
            WebSocketHandler.sendMessage(socketChannel.socket(), message[0], message[1]);

            socketChannel.configureBlocking(false);
            selector.selectNow();
            socketChannel.register(selector, SelectionKey.OP_READ);
        }
    }

}





/*
public class Room {
    private Selector selector;
    private List<SocketChannel> clients = new ArrayList<>();
    private List<String[]> messages = new ArrayList<>();


    public Room(SocketChannel socketChannel) throws IOException {
        selector = Selector.open();

        socketChannel.configureBlocking(false);
        selector.selectNow();
        socketChannel.register(selector, SelectionKey.OP_READ);

        clients.add(socketChannel);

        Thread thread = new Thread(() -> {
            try {
                serverRoom();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();

    }

    public synchronized void add(SocketChannel socketChannel) throws IOException {
        clients.add(socketChannel);
        selector.wakeup();
        socketChannel.configureBlocking(false);
        sendMessages(socketChannel, messages);
    }

    public void sendMessages(SocketChannel socketChannel, List<String[]> messages) throws IOException {
        for (String[] msg : messages) {
            SelectionKey k = socketChannel.keyFor(selector);
            k.cancel();

            socketChannel.configureBlocking(true);
            WebSocketHandler.sendMessage(socketChannel.socket(), msg[0], msg[1]);

            socketChannel.configureBlocking(false);
            selector.selectNow();
            socketChannel.register(selector, SelectionKey.OP_READ);
        }
    }

    public void serverRoom() throws IOException {
        while (true) {
            selector.select();

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            ArrayList<String[]> newMessages = new ArrayList<>();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    iterator.remove();
                    key.cancel();
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    socketChannel.configureBlocking(true);

                    Socket socket = socketChannel.socket();
                    String[] tmpArr = WebSocketHandler.decodeMessage(socket).split(" ");
                    newMessages.add(new String[]{tmpArr[0], tmpArr[1]});
                    messages.add(new String[]{tmpArr[0], tmpArr[1]});

                    socketChannel.configureBlocking(false);
                    selector.selectNow();
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }
            for (SocketChannel socketChannel : clients) {
                sendMessages(socketChannel, newMessages);
            }
        }
    }

}

 */
