import java.net.*;
import java.sql.*;


import static spark.Spark.*;
import spark.*;

public class Application {
    final static int PORT = 7000;
    private static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            return Integer.parseInt(herokuPort);
        }
        return PORT;
    }

    public static void main(String[] args) {
        port(getHerokuAssignedPort());
        get("/", (req, res) -> "Hi Heroku 00!");
        workWithDatabase();
    }

    

    private static void workWithDatabase(){
        try (Connection conn = getConnection()) {
            String sql;

            if ("SQLite".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName())) {
                sql = "CREATE TABLE IF NOT EXISTS Authors (id INTEGER PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE," +
                        " numOfBooks INTEGER, nationality VARCHAR(30));";
            }
            else {
                sql = "CREATE TABLE IF NOT EXISTS Authors (id serial PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE," +
                        " numOfBooks INTEGER, nationality VARCHAR(30));";
            }

            Statement st = conn.createStatement();
            st.execute(sql);

            sql = "INSERT INTO Authors(name, numOfBooks, nationality) VALUES ('Leo Tolstoy', 12, 'Russian');";
            st.execute(sql);

        } catch (URISyntaxException | SQLException e) {
            e.printStackTrace();
        }
    }
    private static Connection getConnection() throws URISyntaxException, SQLException {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null) {
            // Not on Heroku, so use SQLite
            return DriverManager.getConnection("jdbc:sqlite:./MyBooksApp.db");
        }

        URI dbUri = new URI(databaseUrl);

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':'
                + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

        return DriverManager.getConnection(dbUrl, username, password);
    }


}
