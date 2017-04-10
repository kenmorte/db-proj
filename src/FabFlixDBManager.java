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
				"select m.* from stars_in_movies sm, movies m where sm.star_id = " + 
					id + " and sm.movie_id = m.id;");
			buffer.append("\n");
			
			// If there are no initial results, add a message for no results found
			boolean resultsFound = false;
			
			// Create the output string from the query
			while (result.next()) {
				resultsFound = true;
				buffer.append("ID = " +  result.getInt(1) + "\n");
				buffer.append("Title = " +  result.getString(2) + "\n");
				buffer.append("Year = " +  result.getInt(3) + "\n");
				buffer.append("Director = " +  result.getString(4) + "\n");
				buffer.append("Banner URL = " +  (result.getString(5).isEmpty() ? "N/A" : result.getString(5)) + "\n");
				buffer.append("Trailer URL = " +  (result.getString(6).isEmpty() ? "N/A" : result.getString(6)) + "\n");
				buffer.append("\n");
			}
			
			if (!resultsFound)
				buffer.append("No results found!\n");
			
			return buffer.toString();
		}
		catch (SQLException e) {
			// Return the proper error message 
			switch (e.getErrorCode()) {
			case 1146:
				return FabFlixConsole.getErrorMessage(e.getMessage() + ". Unable to run query.");
			default:
				return FabFlixConsole.getErrorMessage("Error code " + e.getErrorCode() + ": " + e.getMessage());
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
		
		// Uncomment if we want to have no results appear when empty first and last name are inputted
		/*
		else
			query += "and s.first_name = \"\" and s.last_name = \"\"";
		*/
		
		try {
			// Create and run the query onto the database
			select = mConnection.createStatement();
			result = select.executeQuery(query);
			buffer.append("\n");
			
			// If there are no initial results, add a message for no results found
			boolean resultsFound = false;
			
			// Create the output string from the query
			while (result.next()) {
				resultsFound = true;
				buffer.append("ID = " +  result.getInt(1) + "\n");
				buffer.append("Title = " +  result.getString(2) + "\n");
				buffer.append("Year = " +  result.getInt(3) + "\n");
				buffer.append("Director = " +  result.getString(4) + "\n");
				buffer.append("Banner URL = " +  (result.getString(5).isEmpty() ? "N/A" : result.getString(5)) + "\n");
				buffer.append("Trailer URL = " +  (result.getString(6).isEmpty() ? "N/A" : result.getString(6)) + "\n");
				buffer.append("\n");
			}
			
			if (!resultsFound)
				buffer.append("No results found!\n");
			
			return buffer.toString();
			
		} catch (SQLException e) {
			// Return the proper error message 
			switch (e.getErrorCode()) {
			case 1146:	// Table not found
				return FabFlixConsole.getErrorMessage(e.getMessage() + ". Unable to run query.");
			default:
				return FabFlixConsole.getErrorMessage("Error code " + e.getErrorCode() + ": " + e.getMessage());
			}
		} catch (NullPointerException e) {
			return FabFlixConsole.getErrorMessage("No connection established with database server.");
		}
	}

	public String insertStar(Integer id, String name, Date dob, String photoURL) {
		if (id == null)
			return FabFlixConsole.getErrorMessage("Invalid ID inputted. Cannot execute insertion.");
		if (name == null || name.isEmpty())
			return FabFlixConsole.getErrorMessage("Invalid name inputted. Cannot execute insertion.");
		
		StringBuffer buffer = new StringBuffer();
		String[] fullName = getFullName(name);
		String firstName = fullName[0], lastName = fullName[1];
		String insertStatement = "insert into stars values(" + 
				id + 
				",\"" + firstName + "\"" +
				",\"" + lastName + "\"" +
				"," + (dob == null ? "NULL" : "\"" + dob.toString() + "\"") +
				"," + (photoURL == null || photoURL.isEmpty() ? "\"\"" : "\"" + photoURL + "\"") + ")";
		
		try {
			Statement insert = mConnection.createStatement();
			int rowCount = insert.executeUpdate(insertStatement);
			buffer.append("\n");
			
			if (rowCount == 0)
				buffer.append(FabFlixConsole.getErrorMessage("Unable to add star into database.\n"));
			else
				buffer.append(FabFlixConsole.getInfoMessage("Successfully added star into database!\n"));
			return buffer.toString();
			
		} catch (SQLException e) {
			// Return the proper error message 
			switch (e.getErrorCode()) {
			case 1146:	// Table not found
				return FabFlixConsole.getErrorMessage(e.getMessage() + ". Unable to run insertion.");
			case 1062: // Duplicate primary key entry
				return FabFlixConsole.getErrorMessage("Duplicate ID '" + id + "' already found for table \"star\". Unable to run insertion.");
			default:
				return FabFlixConsole.getErrorMessage("Error code " + e.getErrorCode() + ": " + e.getMessage());
			}
		} catch (NullPointerException e) {
			return FabFlixConsole.getErrorMessage("No connection established with database server.");
		}
	}
	
	/**
	 * Returns a String[] in the format of [<i>first name</i>, <i>last name</i>] from a given String of a full name.
	 * Accomplishes error checking if only one part of a name is given, which sets it to be the last name of the result.
	 * 
	 * @param name	full name of a star
	 * @return	String array of size 2 with the first index being the first name and the second index being the last name
	 */
	private String[] getFullName(String name) {
		String[] result = new String[2];	// result holds: [first_name, last_name]
		result[0] = ""; result[1] = "";
		
		String[] nameSplit = name.split(" ");
		switch (nameSplit.length) {
		case 1:		// Star has a single name, add it as last name and leave first name empty
			result[0] = "";
			result[1] = name;
			break;
		default:
			for (int i = 0; i < nameSplit.length-1; i++)
				result[0] += nameSplit[i] + " ";
			result[0] = result[0].substring(0, result[0].length()-1);	// to take out the extra space in the end
			result[1] = nameSplit[nameSplit.length-1];
			break;
		}
		
		return result;
	}
}
