package foogame;

import java.sql.*;

public class SqliteDBInput {
	public static void main(String args[]) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:/Users/jackie/Downloads/games_anon.db");
		System.out.println("Opened database successfully");
		
		String sql = "SELECT * FROM 'games' WHERE (player_black IS 'TakticianBot' OR player_white IS 'TakticianBot') AND size IS 5 LIMIT 0,30;";

		Statement stmt = c.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		while (rs.next()) {
            System.out.println(rs.getInt("date") +  "\t" + 
                               rs.getInt("size") + "\t" + 
                               rs.getString("player_white") + "\t" + 
                               rs.getString("player_black") + "\t" +  
                               rs.getString("result") + "\t" +
                               rs.getString("notation"));
        }
		
		stmt.close();
		c.close();

		System.out.println("Opened database successfully");
	}
}
