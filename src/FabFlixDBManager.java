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
			mConnection = null;
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
		} catch (NullPointerException e) {
			if (mConnection == null)
				return FabFlixConsole.getErrorMessage("No connection established with database server.");
			return FabFlixConsole.getErrorMessage("Error executing insertion of star.");
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
			if (mConnection == null)
				return FabFlixConsole.getErrorMessage("No connection established with database server.");
			return FabFlixConsole.getErrorMessage("Error executing insertion of star.");
		}
	}

	/**
	 * Inserts a star into the movie database. If an error occurs while inserting a star,
	 * the operation is canceled and a proper error message is returned by the method.
	 * We do not need to include the ID for a star because it is auto-incremented.
	 * 
	 * @param firstName	first name of the star
	 * @param lastName	last name of the star
	 * @param dob	date of birth for the star
	 * @param photoURL	URL linking to a photo of the star
	 * @return	output message if the insertion was a success or not
	 */
	public String insertStar(String firstName, String lastName, Date dob, String photoURL) {
		if (firstName == null)
			return FabFlixConsole.getErrorMessage("Invalid first name inputted. Cannot execute insertion."); 
		if (lastName == null || lastName.isEmpty())
			return FabFlixConsole.getErrorMessage("Invalid last name inputted. Cannot execute insertion.");
		
		StringBuffer buffer = new StringBuffer();
		String insertStatement = "insert into stars values(DEFAULT" + 
				",\"" + firstName + "\"" +
				",\"" + lastName + "\"" +
				"," + (dob == null ? "NULL" : "\"" + dob.toString() + "\"") +
				"," + (photoURL == null || photoURL.isEmpty() ? "\"\"" : "\"" + photoURL + "\"") + ")";
		
		try {
			Statement insert = mConnection.createStatement();
			insert.executeUpdate(insertStatement,Statement.RETURN_GENERATED_KEYS);
			ResultSet result = insert.getGeneratedKeys();
			buffer.append("\n");
			
			if (result != null && result.next())
				buffer.append(FabFlixConsole.getInfoMessage("Successfully added star into database! Newly added star's ID is " + result.getInt(1) + "\n"));
			else
				buffer.append(FabFlixConsole.getErrorMessage("Unable to add star into database.\n"));
			return buffer.toString();
			
		} catch (SQLException e) {
			// Return the proper error message 
			switch (e.getErrorCode()) {
			case 1146:	// Table not found
				return FabFlixConsole.getErrorMessage(e.getMessage() + ". Unable to run insertion.");
			default:
				return FabFlixConsole.getErrorMessage("Error code " + e.getErrorCode() + ": " + e.getMessage());
			}
		} catch (NullPointerException e) {
			if (mConnection == null)
				return FabFlixConsole.getErrorMessage("No connection established with database server.");
			return FabFlixConsole.getErrorMessage("Error executing insertion of star.");
		}
	}

	/**
	 * Inserts a customer into the movie database. If an error occurs while inserting a customer,
	 * the operation is canceled and a proper error message is returned by the method.
	 * If the credit card ID provided does not exist, an error message is returned. No
	 * ID is needed for the insertion since it is auto-incremented.
	 * 
	 * @param firstName	first name of customer
	 * @param lastName	last name of customer
	 * @param creditCardID	ID of the customers's credit card (must exist in database)
	 * @param address	address of customer
	 * @param email	e-mail address of customer login
	 * @param password	password for customer login
	 * @return	message that indicates whether operation succeeded or failed
	 */
	public String insertCustomer(String firstName, String lastName, String creditCardID, 
		String address, String email, String password) {
		
		if (lastName == null)
			return FabFlixConsole.getErrorMessage("Invalid last name inputted. Cannot insert customer.");
		if (creditCardID == null)
			return FabFlixConsole.getErrorMessage("Invalid credit card ID inputted. Cannot insert customer.");
		if (address == null)
			return FabFlixConsole.getErrorMessage("Invalid address inputted. Cannot insert customer.");
		if (email == null)
			return FabFlixConsole.getErrorMessage("Invalid e-mail address inputted. Cannot insert customer.");
		if (password == null)
			return FabFlixConsole.getErrorMessage("Invalid password inputted. Cannot insert customer.");
		
		if (!creditCardExistsInDB(creditCardID))
			return FabFlixConsole.getErrorMessage("Credit card does not exist inside database. Cannot insert customer.");
		
		String insertStatement = "insert into customers values(DEFAULT" + 
				",\"" + firstName + "\"" +
				",\"" + lastName + "\"" +
				",\"" + creditCardID + "\"" +
				",\"" + address + "\"" +
				",\"" + email + "\"" +
				",\"" + password + "\"" + ")";
		
		try {
			Statement insert = mConnection.createStatement();
			insert.executeUpdate(insertStatement,Statement.RETURN_GENERATED_KEYS);
			ResultSet result = insert.getGeneratedKeys();
			
			if (result != null && result.next())
				return FabFlixConsole.getInfoMessage("Successfully added customer into database! Newly added customer's ID is " + result.getInt(1) + ".\n"); 
			return FabFlixConsole.getInfoMessage("Unable to add customer into database.\n");
			
		} catch (SQLException e) {
			// Return the proper error message 
			switch (e.getErrorCode()) {
			case 1146:	// Table not found
				return FabFlixConsole.getErrorMessage(e.getMessage() + ". Unable to run insertion.");
			default:
				return FabFlixConsole.getErrorMessage("Error code " + e.getErrorCode() + ": " + e.getMessage());
			}
		} catch (NullPointerException e) {
			if (mConnection == null)
				return FabFlixConsole.getErrorMessage("No connection established with database server.");
			return FabFlixConsole.getErrorMessage("Error executing insertion of star.");
		}
	}
	
	/**
	 * Checks if a specified credit card (by ID) exists in the database.
	 * 
	 * @param creditCardID	id of the credit card
	 * @return	true if the credit card exists, false otherwise
	 */
	private boolean creditCardExistsInDB(String creditCardID) {
		String query = "select * from creditcards where id = \"" + creditCardID + "\"";
		
		try {
			Statement statement = mConnection.createStatement();
			ResultSet result = statement.executeQuery(query);
			return result != null && result.next();
			
		} catch (SQLException e) {
			// Return the proper error message 
			switch (e.getErrorCode()) {
			case 1146:	// Table not found
				System.out.println(FabFlixConsole.getErrorMessage("Credit card check: " + e.getMessage() + ". Unable to run insertion."));
				break;
			default:
				System.out.println(FabFlixConsole.getErrorMessage("Credit card check: " + "Error code " + e.getErrorCode() + ": " + e.getMessage()));
				break;
			}
			return false;
		} catch (NullPointerException e) {
			if (mConnection == null)
				System.out.println(FabFlixConsole.getErrorMessage("Credit card check: " + "No connection established with database server."));
			else
				System.out.println(FabFlixConsole.getErrorMessage("Credit card check: " + "Error executing insertion of star."));
			return false;
		}
	}
}
