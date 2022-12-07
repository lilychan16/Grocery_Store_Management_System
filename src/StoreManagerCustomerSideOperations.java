import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Public class to store methods to perform operations in store manager - customer management menu.
 */
public class StoreManagerCustomerSideOperations {

  SharedHelperMethods sharedHelperMethods;

  /**
   * Constructor for StoreManagerCustomerSideOperations class.
   * @param sharedHelperMethods an object from SharedHelperMethods class
   */
  public StoreManagerCustomerSideOperations(SharedHelperMethods sharedHelperMethods) {
    this.sharedHelperMethods = sharedHelperMethods;
  }


  /**
   * Method to show all customers' information.
   * @param con a connection to the database
   * @throws SQLException if any SQL operation failed
   */
  public void show_all_customers(Connection con) throws SQLException {

    System.out.println("\nPrint all customers:");

    CallableStatement cs_all_customers = con.prepareCall(
            "{call queryCustomerAll()}"
    );

    ResultSet rs_all_customers = cs_all_customers.executeQuery();

    sharedHelperMethods.print_formatted_customer_table(rs_all_customers);

    rs_all_customers.close();
    cs_all_customers.close();
  }


  /**
   * Method to look up customer information by entering a customer id.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @return a customer id that can be used by other methods
   * @throws SQLException if any SQL operation failed
   */
  public String look_up_customer_by_id(Connection con, Scanner sc) throws SQLException {

    String customer_id = this.validate_customer_id_input(con, sc);

    System.out.println("\nPrint customer information: ");

    CallableStatement cs_customer_by_id = con.prepareCall(
            "{call queryCustomerById(?)}"
    );

    cs_customer_by_id.setInt(1, Integer.parseInt(customer_id));

    ResultSet rs_customer_by_id = cs_customer_by_id.executeQuery();

    sharedHelperMethods.print_formatted_customer_table(rs_customer_by_id);

    rs_customer_by_id.close();
    cs_customer_by_id.close();

    return customer_id;
  }


  /**
   * Helper method to validate a customer id input.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @return a valid customer id input
   * @throws SQLException if any SQL operation failed
   */
  public String validate_customer_id_input(Connection con, Scanner sc) throws SQLException {

    String customer_id = "";

    ArrayList<String> customer_id_list = new ArrayList<>();

    CallableStatement cs_customer_all = con.prepareCall(
              "{call queryCustomerAll()}"
    );

    ResultSet rs_customer_all = cs_customer_all.executeQuery();

    while (rs_customer_all.next()) {
      customer_id_list.add(Integer.toString(rs_customer_all.getInt(1)));
    }

    System.out.print("\nPlease enter customer id: ");

    if (sc.hasNext()) {
      customer_id = sc.next();
    }

    while (!customer_id_list.contains(customer_id)) {
      System.out.println("You entered invalid input. Please re-enter customer id or 0 to quit");
      System.out.print("Please enter customer id: ");
      if (sc.hasNext()) {
        customer_id = sc.next();

        if (customer_id.equals("0")) {
          System.exit(0);
        }
      }
    }

    return customer_id;
  }


  /**
   * Method to look up customer information by entering a customer name.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws SQLException if any SQL operation failed
   */
  public void look_up_customer_by_name(Connection con, Scanner sc) throws SQLException {

    String customer_name = "";

    ArrayList<String> customer_name_list = new ArrayList<>();

    CallableStatement cs_customer_all = con.prepareCall(
            "{call queryCustomerAll()}"
    );

    ResultSet rs_customer_all = cs_customer_all.executeQuery();

    while (rs_customer_all.next()) {
      customer_name_list.add(rs_customer_all.getString(3).toLowerCase());
    }

    System.out.print("\nPlease enter customer name: ");

    if (sc.hasNextLine()) {
      sc.nextLine();
      customer_name = sc.nextLine();
    }

    while (!customer_name_list.contains(customer_name.toLowerCase())) {
      System.out.println("You entered invalid input. Please re-enter customer name or 0 to quit");
      System.out.print("Please enter customer name: ");
      if (sc.hasNextLine()) {
        customer_name = sc.nextLine();

        if (customer_name.equals("0")) {
          System.exit(0);
        }
      }
    }

    System.out.println("\nPrint customer information:");

    CallableStatement cs_customer_by_name = con.prepareCall(
            "{call queryCustomerByName(?)}"
    );

    cs_customer_by_name.setString(1, customer_name);

    ResultSet rs_customer_by_name = cs_customer_by_name.executeQuery();

    sharedHelperMethods.print_formatted_customer_table(rs_customer_by_name);

    rs_customer_by_name.close();
    cs_customer_by_name.close();
  }


  /**
   * Method to add a new customer to the MySQL database.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws SQLException if any SQL operation failed
   */
  public void add_new_customer(Connection con, Scanner sc) throws SQLException {

    String customer_email = "";
    String customer_name = "";
    int customer_birth_year;
    int customer_birth_month;
    int customer_birth_day;
    int points;

    ArrayList<String> customer_email_list = new ArrayList<>();

    CallableStatement cs_customer_all= con.prepareCall(
            "{call queryCustomerAll()}"
    );

    ResultSet rs_customer_all = cs_customer_all.executeQuery();

    while (rs_customer_all.next()) {
      customer_email_list.add(rs_customer_all.getString(2).toLowerCase());
    }

    System.out.print("\nPlease enter customer's email address: ");

    if (sc.hasNextLine()) {
      sc.nextLine();
      customer_email = sc.nextLine().toLowerCase();
    }

    while (customer_email_list.contains(customer_email)) {
      System.out.println("This email address already exists.");
      System.out.print("Please enter customer's email address: ");

      if (sc.hasNextLine()) {
        customer_email = sc.nextLine().toLowerCase();
      }
    }

    System.out.print("\nPlease enter customer's name: ");

    if (sc.hasNextLine()) {
      customer_name = sc.nextLine();
    }

    System.out.print("Please enter customer's birth year: ");

    customer_birth_year = sharedHelperMethods.validate_birth_year_input(sc);

    System.out.print("Please enter customer's birth month: ");

    customer_birth_month = sharedHelperMethods.validate_birth_month_input(sc);

    System.out.print("Please enter customer's birth day: ");

    customer_birth_day = sharedHelperMethods.validate_birth_day_input(sc, customer_birth_month,
                                                                        customer_birth_year);

    String customer_dob = customer_birth_year + "-" + customer_birth_month + "-"
                                                            + customer_birth_day;

    System.out.println("Please enter any promotional reward points for this customer, ");
    System.out.print("or enter 0 if no promotional reward points: ");

    points = this.validate_reward_points_input(sc);

    CallableStatement cs_insert_customer = con.prepareCall(
            "{call insertCustomer(?,?,?,?)}"
    );

    cs_insert_customer.setString(1, customer_email);
    cs_insert_customer.setString(2, customer_name);
    cs_insert_customer.setDate(3, Date.valueOf(customer_dob));
    cs_insert_customer.setInt(4, points);

    cs_insert_customer.executeUpdate();

    System.out.println("\nSuccessfully added a new customer.");
    System.out.println("\nThe updated customer table is as follows:");

    this.show_all_customers(con);

    rs_customer_all.close();
    cs_customer_all.close();

    cs_insert_customer.close();
  }


  /**
   * Method to delete a customer in the MySQL database by entering a customer id.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws SQLException if any SQL operation failed
   */
  public void delete_customer_by_id(Connection con, Scanner sc) throws SQLException {

    String customer_id = this.look_up_customer_by_id(con, sc);
    String delete_or_not = "";

    System.out.print("\nPlease enter 1 to confirm deletion, or 0 to re-enter customer id: ");

    if (sc.hasNext()) {
      delete_or_not = sc.next();
    }

    while(!delete_or_not.equals("1") && !delete_or_not.equals("0")) {
      System.out.print("Invalid input, please enter 1 to confirm deletion, "
                        + "or 0 to re-enter customer id: ");

      if (sc.hasNext()) {
        delete_or_not = sc.next();
      }
    }

    if (delete_or_not.equals("1")) {
      CallableStatement cs_delete_customer = con.prepareCall(
              "{call deleteCustomerById(?)}"
      );

      cs_delete_customer.setInt(1, Integer.parseInt(customer_id));

      cs_delete_customer.executeUpdate();

      System.out.println("\nSuccessfully delete the customer.");
      System.out.println("The updated customer table is as follows:");

      this.show_all_customers(con);

      cs_delete_customer.close();
    }
    else {
      this.delete_customer_by_id(con, sc);
    }
  }


  /**
   * Helper method to validate a reward points input.
   * @param sc the scanner to receive user input
   * @return a valid reward points input
   */
  public int validate_reward_points_input(Scanner sc) {

    String points_input;
    int points = 0;

    if (sc.hasNextLine()) {

      boolean flag = false;

      while (!flag) {
        try {
          points_input = sc.nextLine();
          points = Integer.parseInt(points_input);

          if (points < 0) {
            System.out.print("Points have been set to 0.\n");
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.println("\nYou did not enter a number.");
          System.out.println("Please enter any promotional reward points for this customer, ");
          System.out.print("or enter 0 if no promotional reward points: ");
        }
      }
    }

    return points;
  }
}
