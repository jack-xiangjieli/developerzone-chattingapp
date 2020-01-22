package sourcecode;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcMySQLVersion {
    String url ="jdbc:mysql://developerzoneserver.mysql.database.azure.com:3306/developerzone?useSSL=true&requireSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    String user = "xiangjie@developerzoneserver";
    String password = "*Xdf973535892";
    Connection connection;
    Statement statement;

    public JdbcMySQLVersion() throws SQLException {
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            System.out.println("connecting database...");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }

    }

    public boolean hasRoom(String roomname) throws SQLException {

        String query = "SELECT * from allrooms";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            String s = resultSet.getString(2);
            if (s.equals(roomname)) return true;
        }
        return false;
    }

    public ArrayList<String[]> getAllMessages(String roomname) throws SQLException {
        String query = "SELECT username, message from " + roomname;
        ResultSet resultSet = statement.executeQuery(query);
        ArrayList<String[]> allMessages = new ArrayList<>();
        while (resultSet.next()) {
            String username = resultSet.getString(1);
            String msg = resultSet.getString(2);
            String[] strArr = new String[]{username, msg};
            allMessages.add(strArr);
        }
        return allMessages;
    }

    public void addRoom(String roomname) throws SQLException {
        String query = "INSERT INTO allrooms (roomname, history_messages, current_online) VALUES (\"" + roomname + "\", 0, 1);";
        statement.execute(query);
    }

    public void addMessage(String roomname, String[] msgInfo) throws SQLException {
        String query = "INSERT INTO " + roomname + " (username, message) VALUES (\"" + msgInfo[0] + "\", \"" + msgInfo[1] + "\")";
        statement.execute(query);
        String query1 = "UPDATE allrooms SET history_messages = history_messages + 1 WHERE roomname = \"" + roomname + "\"";
        statement.execute(query1);
    }

    public void updateCurrentOnline(String roomname, int number) throws SQLException {
        String query = "UPDATE allrooms SET current_online = " + number + " WHERE roomname = \"" + roomname + "\"";
        statement.execute(query);
    }

    public void createNewRoomTable(String roomname) throws SQLException {
        String query = "CREATE TABLE " + roomname + " (id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(255), message VARCHAR(255));";
        statement.execute(query);
    }

    public ArrayList<String[]> getAllRooms() throws SQLException {
        String query = "SELECT * FROM allrooms";
        ResultSet resultSet = statement.executeQuery(query);
        ArrayList<String[]> allrooms = new ArrayList<>();
        while (resultSet.next()) {
            // get the second field of that entry in resultSet
            String roomname = resultSet.getString(2);
            String his = resultSet.getString(3);
            String cur = resultSet.getString(4);
            String[] roomInfo = {roomname, his, cur};
            allrooms.add(roomInfo);
        }
        return allrooms;
    }
}
