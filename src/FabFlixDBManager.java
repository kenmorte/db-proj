import java.sql.*;

public class FabFlixDBManager
{
	private Connection mConnection;
	
	public static final String DATABASE_NAME = "moviedb";
	
	public FabFlixDBManager()  throws Exception {
		// Incorporate mySQL driver
		Class.forName("com.mysql.jdbc.Driver").newInstance();
	}
	
	/**
	 * Attempts to make a connection with MySQL database with a specified user name and password.
	 * Uses the database login credentials (not the ones specified from the schema).
	 * 
	 * @param username	user name field for login
	 * @param password	password field for login
	 * @return	<i>null</i> if connection was successful, a <i>SQLException</i> object if the connection failed
	 */
	public SQLException attemptConnection(String username, String password) {
		try {
			// Attempt to create a connection to the database
			mConnection = DriverManager.getConnection(
				"jdbc:mysql:///" + FabFlixDBManager.DATABASE_NAME + "?useSSL=false", 
				username, 
				password);
			
		} catch (SQLException e) {
			return e;
		}
		return null;
	}
	
	/**
	 * Returns the resulting string from querying movies that feature a given star by the star's ID number.
	 * A proper error message is returned if an error occurred attempting to query the database.
	 * 
	 * @param id	integer representing the star's ID number
	 * @return	output string for the resulting query of movies
	 */
	public String getMoviesForStar(Integer id) {
		if (id == null)
			return FabFlixConsole.getErrorMessage("Invalid ID inputted. Cannot execute query.");

		Statement select;
		ResultSet result;
		StringBuffer buffer = new StringBuffer();
		
		try {
			// Create and run the query onto the database
			select = mConnection.createStatement();
			result = select.executeQuery(
				"select distinct m.* from stars_in_movies sm, movies m where sm.star_id = " + 
					id + " and sm.movie_id = m.id;");
			
			// Create the output string from the query
			buffer.append("\n");
			while (result.next()) {
				buffer.append("ID = " +  result.getInt(1) + "\n");
				buffer.append("Title = " +  result.getString(2) + "\n");
				buffer.append("Year = " +  result.getInt(3) + "\n");
				buffer.append("Director = " +  result.getString(4) + "\n");
				buffer.append("Banner URL = " +  (result.getString(5).isEmpty() ? "N/A" : result.getString(5)) + "\n");
				buffer.append("Trailer URL = " +  (result.getString(6).isEmpty() ? "N/A" : result.getString(6)) + "\n");
				buffer.append("\n");
			}
			
			return buffer.toString();
		}
		catch (SQLException e) {
			// Return the proper error message 
			switch (e.getErrorCode()) {
			case 1146:
				return FabFlixConsole.getErrorMessage(e.getMessage() + ". Unable to run query.");
			default:
				return FabFlixConsole.getErrorMessage(e.getMessage());
			}
		}
	}

	/**
	 * Returns the resulting string from querying movies that feature a given star by the star's first/last name.
	 * A proper error message is returned if an error occurred attempting to query the database.
	 * 
	 * @param firstName	the star's first name
	 * @param lastName	the star's last name
	 * @return	output string for the resulting query of movies
	 */
	public String getMoviesForStar(String firstName, String lastName) {
		if (firstName == null || lastName == null)
			return FabFlixConsole.getErrorMessage("Invalid names inputted. Cannot execute query.");
		
		String query = "select m.* from stars s, stars_in_movies sm, movies m where s.id = sm.star_id and m.id = sm.movie_id ";
		Statement select;
		ResultSet result;
		StringBuffer buffer = new StringBuffer();
		
		// Obtain the correct query based on the inputs for the first name and the last name
		if (!firstName.isEmpty() && !lastName.isEmpty())
			query += "and s.first_name = \"" + firstName + "\" and s.last_name = \"" + lastName + "\"";
		else if (!lastName.isEmpty())
			query += "and s.last_name = \"" + lastName + "\"";
		else if (!firstName.isEmpty())
			query += "and s.first_name = \"" + firstName + "\"";
		
		try {
			// Create and run the query onto the database
			select = mConnection.createStatement();
			result = select.executeQuery(query);
			
			// Create the output string from the query
			buffer.append("\n");
			while (result.next()) {
				buffer.append("ID = " +  result.getInt(1) + "\n");
				buffer.append("Title = " +  result.getString(2) + "\n");
				buffer.append("Year = " +  result.getInt(3) + "\n");
				buffer.append("Director = " +  result.getString(4) + "\n");
				buffer.append("Banner URL = " +  (result.getString(5).isEmpty() ? "N/A" : result.getString(5)) + "\n");
				buffer.append("Trailer URL = " +  (result.getString(6).isEmpty() ? "N/A" : result.getString(6)) + "\n");
				buffer.append("\n");
			}
			
			return buffer.toString();
		}
		catch (SQLException e) {
			// Return the proper error message 
			switch (e.getErrorCode()) {
			case 1146:	// Table not found
				return FabFlixConsole.getErrorMessage(e.getMessage() + ". Unable to run query.");
			default:
				return FabFlixConsole.getErrorMessage(e.getMessage());
			}
		}
	}
}
