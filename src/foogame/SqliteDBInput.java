package foogame;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class SqliteDBInput {
	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException {
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:/Users/jackie/Downloads/games_anon (1).db");
		System.out.println("Opened database successfully");
		
		String sql = "SELECT * FROM 'games' WHERE (player_black IS 'TakticianBot' OR player_white IS 'TakticianBot') AND size IS 5 /*LIMIT 0,10*/;";

		Statement stmt = c.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		List<String> lines = new ArrayList<>();
		
		int countIllegalGames = 0;
		
		while (rs.next()) {
			/*System.out.println(rs.getString("player_white"));
			System.out.println(rs.getString("player_black"));
			System.out.println(rs.getString("result"));
			System.out.println(rs.getInt("date"));
			System.out.println(rs.getInt("Id"));*/
			//System.out.println(rs.getString("notation"));
			String result = rs.getString("result");
			String notation = rs.getString("notation");
			List<String> temp = TPNInput.translateDBtoPTN(notation, result);
			//System.out.println(temp);
			try {
				List<String> temp2 = TPNInput.processPTN(temp);
				//System.out.println(temp2);
				lines.addAll(temp2);
			} catch (NoSuchElementException e) {
				//e.printStackTrace();
				countIllegalGames++;
			}
			
            /*System.out.println(rs.getInt("date") +  "\t" +
                               rs.getInt("size") + "\t" +
                               rs.getString("player_white") + "\t" +
                               rs.getString("player_black") + "\t" + 
                               rs.getString("result") + "\t" +
                               rs.getString("notation"));*/
        }
		//System.out.println(lines);
		
		int numLines = TPNInput.writeOut(lines);

		System.out.printf("Ignored %d illegal games%n", countIllegalGames);
		System.out.printf("Writing out %d states/lines%n", numLines);
		//System.out.println();
		
		stmt.close();
		c.close();

		System.out.println("Opened database successfully");
	}
}
