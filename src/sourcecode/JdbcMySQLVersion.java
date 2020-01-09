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
    String url = "jdbc:mysql://localhost:3306/testdb";
    String user = "testuser";
    String password = "test623";
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

        String query = "SELECT * from AllRooms";
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
        String query = "INSERT INTO AllRooms (roomname) VALUES (\"" + roomname + "\");";
        statement.execute(query);
    }

    public void addMessage(String roomname, String[] msgInfo) throws SQLException {
        String query = "INSERT INTO " + roomname + " (username, message) VALUES (\"" + msgInfo[0] + "\", \"" + msgInfo[1] + "\")";
        statement.execute(query);
    }

    public void createNewRoomTable(String roomname) throws SQLException {
        String query = "CREATE TABLE " + roomname + " (id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(255), message VARCHAR(255));";
        statement.execute(query);
    }
}
