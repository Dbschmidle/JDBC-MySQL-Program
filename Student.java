/* 
 * Program that allows you to connect to a MySQL Database via the command line. 
 * Uses JDBC drivers to establish connection. The program can execute sql commands
 * line-by-line or accept an entire sql file to run. 
 * 
 * Originally part of a project for CS450 Database Concepts 
 */


import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.ibatis.jdbc.SQL;

import oracle.jdbc.driver.*;




public class Student{
    static Connection con;
    static Statement stmt;
    static Scanner scan = new Scanner(System.in);

    public static void main(String argv[])
    {
	    connectToDatabase();
    } 

    

    public static void connectToDatabase()
    {
	String driverPrefixURL="jdbc:mysql://localhost:3306/?user=root";
	String jdbc_url="";
	
        // IMPORTANT: DO NOT PUT YOUR LOGIN INFORMATION HERE. INSTEAD, PROMPT USER FOR HIS/HER LOGIN/PASSWD
        String username="xxxxxxxx";
        String password="xxxxxxxx";

        
        System.out.println("Enter Username: ");
        username = scan.nextLine();

        System.out.println("Enter Password: ");
        password = scan.nextLine();

	
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");

	        //Register Oracle driver
            //DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        
        } catch (Exception e) {
            System.out.println("Failed to load JDBC/ODBC driver."); 
            return;
        }

       try{
            System.out.println(driverPrefixURL+jdbc_url);
            con=DriverManager.getConnection(driverPrefixURL+jdbc_url, username, password);
            DatabaseMetaData dbmd=con.getMetaData();
            stmt=con.createStatement();

            System.out.println("Connected.");

            if(dbmd==null){
                System.out.println("No database meta data");
            }
            else {
                System.out.println("Database Product Name: "+dbmd.getDatabaseProductName());
                System.out.println("Database Product Version: "+dbmd.getDatabaseProductVersion());
                System.out.println("Database Driver Name: "+dbmd.getDriverName());
                System.out.println("Database Driver Version: "+dbmd.getDriverVersion());

                // Our code starts here
                String sqlFileLocation;
                System.out.println("\nSQL Script Path: ");
                sqlFileLocation = scan.nextLine();
                
                String sqlFile = sqlFileLocation;
                // execute the sql script 
                executeSQLFile(sqlFile);

                menu();

            }
        }catch( Exception e) {e.printStackTrace();}

    }// End of connectToDatabase()

    /*
    *  1. User can view contents of each table, show name of each table 
    *   and display tuples of the table that the user selects.
    *  2. User can search by PUBLICATIONID and return all attributes from
    *     PUBLICATIONS table and include a number of authors field in the result
    *  3. User can search database by specifying one or more input attributes from 
    *     AUTHOR, TITLE, YEAR, TYPE. Which will return the result PUBLICATIONID, 
    *     AUTHOR, TITLE, YEAR, and TYPE.
    */
    private static void menu(){
        String table = "\na. View table Contents\nb. Search by PUBLICATIONID\nc. Search by one or more attributes\nd. Exit\n";
        String userInput = "x";

        do {
            
            System.out.println(table);
            userInput = scan.nextLine();
            

            switch (userInput){

                case "a":
                    loadTable();
                    break;

                case "b":
                    searchByPubID();
                    break;

                case "c":
                    searchBy();
                    break;

                case "d":
                    break;
                
                default:
                    // invalid input
                    System.out.println("\"" + userInput +"\" was not a valid option...");
                    break;

            }


        } while ( userInput.equals("d") == false);

        System.out.println("Goodbye!");
        
    }

    /*
     * Executes an SQL script provided at inputFile.
     * 
     */
    private static void executeSQLFile(String sqlFileName){
        
        try {
            FileReader freader = new FileReader(sqlFileName);
            
            BufferedReader bufferedReader = new BufferedReader(freader);

            StringBuilder builder = new StringBuilder();
            String line;

            

            while ( (line = bufferedReader.readLine()) != null ){
                if (line.isEmpty()){
                    // skip empty lines
                    continue;
                }
                line = line.trim();
                builder = builder.append(line).append(" ");
                

                // execute the builded SQL string if it ends with a semicolon.
                if ( line.endsWith(";")){
                    if (line.startsWith("--")){
                        continue;
                    }
                    
                    // prepare the statment and execute
                    PreparedStatement pst = con.prepareStatement(builder.toString().substring(0, builder.length() - 2)); 

                    try {
                        
                       
                        try {
                            pst.executeUpdate();   

                        } catch (SQLException e){
                            System.out.println("FAILED: "+builder.toString().substring(0, builder.length() - 1));
                            System.out.println(e);
                            pst.executeQuery();
                        }
    
                        pst.close();
                        System.out.println("EXECUTED: "+builder.toString().substring(0, builder.length() - 2));

                    } catch (SQLException e) {
                        
                        System.out.println("FAILED: "+builder.toString().substring(0, builder.length() - 1));
                        System.out.println(e);
                        return;
                    } 

                    builder.setLength(0);
                
                }
            }
            bufferedReader.close();
            freader.close();

        } catch (Exception e){
            System.out.println("Unable to execute SQL File");
            System.out.println(e);
            return;
        }
        
        System.out.println(sqlFileName+" successfully executed.");
    }


    /*
     *  Execute an sql statement given the connection and return the result as a string.
     */
    private static String[] executeSQLstatement(String sqlQuery){
        String retString[] = {};
        ResultSet rset= null;
        try {
            PreparedStatement pst = con.prepareStatement(sqlQuery);
            rset = pst.executeQuery();


            while (rset.next()){
                int i = 1;
                while (true){
                    try{
                        System.out.print(rset.getString(i++) + ", ");
                    } catch (SQLException e){
                        System.out.println();
                        break;
                    }

                }
            
            }
            
        } catch (SQLException e){
            System.out.println(e);
            return retString;
        }

        return retString;

    }
    /*
     * Show the contents of each table.
     * Prompt the user for the tables they would like to view.
     * 
     */
    private static void loadTable(){
        String prompt[] = {"PUBLICATIONS (Yes/No):", "AUTHORS (Yes/No)"};
        String userInput;

        int responses[] = {-1, -1};


        for (int i = 0; i < 2; i++){
            while(true){
                System.out.println(prompt[i]);
                userInput = scan.nextLine();
                if (userInput.equals("Yes")){
                    responses[i] = 1;

                    break;
                }
                if (userInput.equals("No")){
                    responses[i] = 0;
                    break;
                }
                System.out.println("\""+userInput+"\" is not a valid response.");
            }
        }
    

        if (responses[0] == 1){
            // prompt for attributes
            System.out.println("Specify the attributes [PUBLICATIONID, YEAR, TYPE, TITLE, SUMMARY]: ");
            System.out.println("Seperate with commas or type \"ALL\" to view all.");
            userInput = scan.nextLine();
            String user_cols[] = userInput.split(",");


            if (user_cols[0].equals("ALL")){
                System.out.println("\nPUBLICATIONS: ");
                executeSQLstatement("SELECT * FROM PUBLICATIONS");
            } else {
                String sqlQuery = "SELECT " + userInput + " FROM PUBLICATIONS";
                System.out.println("\nPUBLICATIONS: ");
                executeSQLstatement(sqlQuery);
            }            
        }

        if (responses[1] == 1){
           // prompt for attributes
           System.out.println("Specify the attributes [PUBLICATIONID, AUTHOR]: ");
           System.out.println("Seperate with commas or type \"ALL\" to view all.");
           userInput = scan.nextLine();
           String user_cols[] = userInput.split(",");


           if (user_cols[0].equals("ALL")){
                System.out.println("\nAUTHORS: ");
               executeSQLstatement("SELECT * FROM AUTHORS");
           } else {
                String sqlQuery = "SELECT " + userInput + " FROM AUTHORS";
                System.out.println("\nAUTHORS: ");
                executeSQLstatement(sqlQuery); 
           }           
        }

    }


    /*
     * Search by an publicationid
     */
    private static void searchByPubID(){
        System.out.print("PUBLICATIONID: ");
        Integer pubID;
        Scanner scan1 = new Scanner(System.in);
        while(true){
            try {
                pubID = scan1.nextInt();
                break;
            }
            catch(Exception e) {
                System.out.println("Invalid PUBLICATIONID...");
                return;
            }
        }
        String pubIDString = pubID.toString();

        String sqlQuery = "SELECT PUBLICATIONID, YEAR, TYPE, TITLE, COUNT(*) FROM AUTHORS NATURAL JOIN PUBLICATIONS WHERE PUBLICATIONID = "+pubIDString+" GROUP BY PUBLICATIONID, YEAR, TYPE, TITLE";
        executeSQLstatement(sqlQuery);

    }


    /*
     * Search database by specifying one or more input attributes from
     *  [AUTHOR, TITLE, YEAR, TYPE].
     * 
     *  The result will be a table with 
     *  [PUBLICATIONID, AUTHOR, TITLE, YEAR, TYPE]
     */
    private static void searchBy(){
        ArrayList<String> inputFields = new ArrayList<String>();
        Scanner inputs = new Scanner(System.in);
        
        String[] inputFieldString = {"AUTHOR", "TITLE", "YEAR","TYPE"};

        System.out.println("Input Fields: ");
        
        for (int i = 0; i < 4; i++){
            System.out.print(inputFieldString[i]+": ");
            inputFields.add(inputs.nextLine());
            System.out.println();
        }
        
        // Pattern match for author and/or title
        for(int i = 0; i < 4; i++){
            if (inputFields.get(i).equals("") == false){
                String sqlQuery1 = "SELECT PUBLICATIONID, AUTHOR, TITLE, YEAR, TYPE FROM PUBLICATIONS NATURAL JOIN AUTHORS";
                String sqlQueryCondition = " WHERE "+inputFieldString[i]+" LIKE \'"+inputFields.get(i)+"\'";
                //System.out.println(sqlQuery1+sqlQueryCondition);
                executeSQLstatement(sqlQuery1+sqlQueryCondition);
                break;
            }
        }
    }


}// End of class
