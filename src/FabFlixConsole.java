import java.util.Scanner;
import java.sql.*;

public class FabFlixConsole
{
	private FabFlixDBManager mManager;
	private Scanner mReader;
	
	private static final String mErrorHeader = "**ERROR**: ";
	private static final String mInfoHeader = "**INFO**: ";
	private static final String mMenuList = 
			"--------------------------------- MAIN MENU ----------------------------------\n" +
			"Please select an integer option from the console menu, or input '0' to quit:\n" + 
					"[1]\t Print out movies featuring a given star by ID\n" +
					"[2]\t Print out movies featuring a given star by first and/or last name";
	
	public FabFlixConsole() {
		try {
			mManager = new FabFlixDBManager();
	        mReader = new Scanner(System.in);
	        
		} catch (Exception e) {
			System.out.println(getErrorMessage("Unable to connect to JDBC driver!"));
		}
	}
	
	/**
	 * Runs the console program using JDBC.
	 */
	public void run() {
		
		while (attemptLogin()) {
			String input = "", output = "";
			
			System.out.println(getInfoMessage("Login successful into database '" + FabFlixDBManager.DATABASE_NAME + "'\n"));
			System.out.println(mMenuList);
			
			// Get the first user input from the user;
			if (input.isEmpty()) {
				System.out.print("Enter your integer command: ");
				
				// Keep prompting if user enters nothing
				while ((input = mReader.nextLine().trim()).isEmpty()) {
					System.out.print("Enter your integer command: ");
				}
			}
			
			while (!(input).equals("0")) {	// continue until user quits the main menu
				output = handleMenuInput(input);
				System.out.println(output);
				System.out.println();
				
				System.out.println(mMenuList);
				System.out.print("Enter your integer command: ");
				
				// Keep prompting if user enters nothing
				while ((input = mReader.nextLine().trim()).isEmpty()) {
					System.out.print("Enter your integer command: ");
				}
			}
			
			System.out.println(getInfoMessage("Logging out of database '" + FabFlixDBManager.DATABASE_NAME + "'...\n"));
		}
		
		// Close the Scanner
		if (mReader != null)
			mReader.close();
		
		System.out.print("Closing program...");
	}
	
	/**
	 * Handles user input and returns an output string from the resulting input(s).
	 * 
	 * @param input	String input from the integer command from the user
	 * @return	string representing the output from the input command
	 */
	private String handleMenuInput(String input) {
		int inputCommand;
		String output = "";
		
		try { inputCommand = Integer.parseInt(input); }
		catch (NumberFormatException e) { 
			System.out.println(getErrorMessage("Invalid command inputted. Please try again."));
			return output;
		}
		
		switch (inputCommand) {
		case 1:	// Get movies featuring star by star ID
			Integer id = promptInt("\tEnter the movie star's ID: ", "Invalid ID inputted. Unable to execute query.", true);
			if (id != null)
				output = mManager.getMoviesForStar(id);
			break;
		
		case 2: // Get movies featuring star by first and/or last name (of star)
			String firstName = promptString("\tEnter the movie star's first name (optional): ");
			String lastName = promptString("\tEnter the movie star's last name (optional): ");
			output = mManager.getMoviesForStar(firstName, lastName);
			break;
			
		default:	// Unknown command
			output = getErrorMessage("Unknown command inputted. Please try again");
			break;
		}
		return output;
	}
	
	/**
	 * Attempts to login into MySQL database with user input for user name and password.
	 * Automatically checks for erroneous inputs for user name/password.
	 * Prints out proper error messages while attempting login.
	 * Creates a connection to the specified database server if successfully logged in.
	 * 
	 * @return	true if login was successful, false if user quits the program
	 */
	private boolean attemptLogin() {
		String username = "", password = "";
		SQLException connectionError;
		
		System.out.print("Enter a username, or enter 'quit' to exit program: ");
		while (!(username = mReader.nextLine()).equals("quit")) {
			
			// Re-inquire for user name if user name was left empty
			if (username.isEmpty()) {
				System.out.print("Enter a username, or enter 'quit' to exit program: ");
				continue;
			}
			
			System.out.print("Enter a password: ");
			password = mReader.nextLine();
			if (password.isEmpty()) {
				// Re-inquire for user name again if password was left empty
				System.out.print(getErrorMessage("Invalid password provided (password was empty)!"));
				continue;
			}
			
			// Attempt connection with database, print our error if connection was unsuccessful
			if ((connectionError = mManager.attemptConnection(username, password)) != null) {
				switch (connectionError.getErrorCode()) {
				case 1045:
					System.out.println(getErrorMessage("Invalid login credentials provided. Please try again."));
					break;
				case 1049:
					System.out.println(getErrorMessage("Database '" + FabFlixDBManager.DATABASE_NAME + "' is not present."));
					return false;
				default:
					System.out.println(getErrorMessage(connectionError.getMessage()));
				}
				
				// Prompt for user login credentials again
				System.out.print("Enter a username, or enter 'quit' to exit program: ");
				continue;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Returns an error message with a properly inserted header for the console.
	 * 
	 * @param error	detailed error message following the header
	 * @return	full error message with headers
	 */
	public static String getErrorMessage(String error) {
		return mErrorHeader + error + "\n";
	}
	
	/**
	 * Returns an info message with a properly inserted header for the console.
	 * 
	 * @param info detailed message following the header
	 * @return	full info message with headers
	 */
	private String getInfoMessage(String info) {
		return mInfoHeader + info;
	}
	
	/**
	 * Prompts the user for an integer, and prints out proper prompt headers before input
	 * and error messages after input for non-integer inputs. Can repeatedly prompt user
	 * continuously until appropriate integer is inputted or prompt user only once using
	 * the <i>promptOnce</i> argument.
	 * 
	 * @param prompt	Prompt header that is displayed prior to user input
	 * @param errorMessage	Error message that is displayed after input for non-integer inputs
	 * @param promptOnce	if set to <b>true</b>, may return <b>null</b> for erroneous first input, otherwise keeps prompting user until correct input
	 * @return	integer from user input
	 */
	private Integer promptInt(String prompt, String errorMessage, boolean promptOnce) {
		Integer result = null;
		
		while (true) {
			try { 
				System.out.print(prompt);
				result = Integer.parseInt(mReader.nextLine().trim());
				break;
			}
			catch (NumberFormatException e) { 
				System.out.println(getErrorMessage(errorMessage));
				
				if (promptOnce)	break;
				else continue;
			}
		}
		return result;
	}
	
	/**
	 * Prompts the user for a string, printing out proper prompt headers before the input.
	 * 
	 * @param prompt	Prompt header displayed before input
	 * @return	string representing the user input
	 */
	private String promptString(String prompt) {
		System.out.print(prompt);
		return mReader.nextLine().trim();
	}
	
	/**
	 * Runs the main program for the JDBC client.
	 * 
	 * @param arg	unused
	 */
    public static void main(String[] arg)
    {
    	try {
        	FabFlixConsole console = new FabFlixConsole();
        	console.run();
    	}
    	catch (Exception e) {
    		// Catch any uncaught exceptions here (should try to catch them further up, however)
    		System.out.println(getErrorMessage(e.getMessage()));
    	}
    }
}
