import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Public class to hold all menu items on employee's end
 */
public class EmployeeMenuItems {

  private final EmployeeOperations employeeOperations;

  /**
   * Constructor for EmployeeMenuItems class.
   * @param employeeOperations an object from EmployeeOperations class
   */
  public EmployeeMenuItems(EmployeeOperations employeeOperations) {
    this.employeeOperations = employeeOperations;
  }


  /**
   * Print employee-end login menu in console and then print more employee menu items
   * based on user input.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void employee_login_menu(Connection con, Scanner sc) throws Exception {

    GroceryStoreApp groceryStoreApp = new GroceryStoreApp();

    String employee_login_input = "";

    while (true) {
      System.out.println("\nEmployee Login Page:");
      System.out.println("Please select an option:\n1. Employee Login"
              + "\n2. Back to User Type Menu\n3. Quit");

      if (sc.hasNext()) {
        employee_login_input = sc.next();
      }

      switch (employee_login_input) {
        case "1":
          this.employee_menu_type(con, sc);

        case "2":
          groceryStoreApp.main_menu(con, sc);

        case "3":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Identify employee type based on employee id input, then print employee information
   * in console and direct to different menu options based on employee type.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void employee_menu_type(Connection con, Scanner sc) throws Exception {

    String employee_type = "";

    ArrayList<String> employee_id_list = new ArrayList<>();

    String employee_id = "";
    String employee_password = "";

    CallableStatement cs_employee_all = con.prepareCall(
            "{call queryEmployeeAll()}"
    );

    ResultSet rs_employee_all = cs_employee_all.executeQuery();

    while (rs_employee_all.next()) {
      employee_id_list.add(Integer.toString(rs_employee_all.getInt(1)));
    }

    System.out.print("Please enter employee id: ");
    if (sc.hasNext()) {
      employee_id = sc.next();
    }

    while (!employee_id_list.contains(employee_id)) {
      System.out.println("You entered invalid input. Please re-enter employee id or 0 to quit");
      System.out.print("Please enter employee id: ");
      if (sc.hasNext()) {
        employee_id = sc.next();

        if (employee_id.equals("0")) {
          System.exit(0);
        }
      }
    }

    System.out.print("Please enter employee password: ");
    if (sc.hasNext()) {
      employee_password = sc.next();
    }

    // Default password for employee login
    while (!employee_password.equals("123456")) {
      System.out.println("You entered invalid input. Please re-enter employee password "
                        + "or 0 to quit");
      System.out.print("Please enter employee password: ");
      if (sc.hasNext()) {
        employee_password = sc.next();

        if (employee_password.equals("0")) {
          System.exit(0);
        }
      }
    }

    System.out.println("\nEmployee Information:");

    CallableStatement cs_employee_id = con.prepareCall(
            "{call queryEmployeeById(?)}"
    );

    cs_employee_id.setInt(1, Integer.parseInt(employee_id));

    ResultSet rs_employee_id = cs_employee_id.executeQuery();

    ResultSetMetaData rsmd_employee_table = rs_employee_id.getMetaData();

    String employee_table_columns = String.format("%-20s %-35s %-30s %-20s",
            rsmd_employee_table.getColumnName(1),
            rsmd_employee_table.getColumnName(2),
            rsmd_employee_table.getColumnName(3),
            rsmd_employee_table.getColumnName(4));
    System.out.println(employee_table_columns);

    // Use if instead of while because only 1 record should be printed for each unique employee id
    if (rs_employee_id.next()) {
      String out_employee_id = String.format("%-20d %-35s %-30s %-20f",
              rs_employee_id.getInt(1),
              rs_employee_id.getString(2),
              rs_employee_id.getString(3),
              rs_employee_id.getDouble(4));
      System.out.println(out_employee_id);
      employee_type = rs_employee_id.getString(3);
    }

    System.out.println("\nLogin successfully. You are: " + employee_type);

    switch (employee_type) {
      case "warehouse_manager":

        this.warehouse_manager_menu(con, sc, employee_id, employee_type);

      case "cashier":
        this.cashier_menu(con, sc, employee_id, employee_type);

      case "cleaner":
        this.cleaner_menu(con, sc, employee_id, employee_type);

      default:
        System.out.println("Invalid employee type.");
    }

    rs_employee_all.close();
    cs_employee_all.close();

    rs_employee_id.close();
    cs_employee_id.close();
  }


  public void warehouse_manager_menu(Connection con, Scanner sc, String employee_id,
                                     String employee_type) throws Exception {

    String warehouse_manager_menu_input = "";

    while (true) {
      System.out.println("\nPlease select an option:\n1. Show all product data"
              + "\n2. Look up product info by product id\n3. Look up product info by product name"
              + "\n4. Add a new product\n5. Update product price by product id"
              + "\n6. Update product stock by product id\n7. Delete a product by product id"
              + "\n8. Add a new store area\n9. Go back to employee login menu\n10. Quit");

      if (sc.hasNext()) {
        warehouse_manager_menu_input = sc.next();
      }

      switch(warehouse_manager_menu_input) {
        case "1":
          employeeOperations.show_all_products(con);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "2":
          employeeOperations.employee_look_up_product_by_id(con, sc);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "3":
          employeeOperations.employee_look_up_product_by_name(con, sc);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "4":
          employeeOperations.add_new_product(con, sc, employee_id);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "5":
          employeeOperations.update_product_price_by_id(con, sc);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "6":

        case "7":

        case "8":
          employeeOperations.add_new_store_area(con, sc);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "9":
          this.employee_login_menu(con, sc);

        case "10":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Print cashier menu and then perform an operation in console based on user input.
   * The allowed operations for a cashier are:
   *    (1) Check assigned check-out counter
   *    (2) Go back to employee login menu
   *    (3) Quit
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param employee_id an employee id
   * @param employee_type the employee's type based on the employee id
   * @throws Exception if any I/O operation in console failed
   */
  public void cashier_menu(Connection con, Scanner sc, String employee_id, String employee_type)
                                                                              throws Exception {

    String cashier_menu_input = "";

    while (true) {
      System.out.println("\nPlease select an option:\n1. Check assigned check-out counter"
              + "\n2. Go back to employee login menu\n3. Quit");

      if (sc.hasNext()) {
        cashier_menu_input = sc.next();
      }

      switch (cashier_menu_input) {
        case "1":
          employeeOperations.cashier_assigned_counter(con, employee_id);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "2":
          this.employee_login_menu(con, sc);

        case "3":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Print cleaner menu and then perform an operation in console based on user input.
   * The allowed operations for a cleaner are:
   *    (1) Check assigned cleaning area
   *    (2) Go back to employee login menu
   *    (3) Quit
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param employee_id an employee id
   * @param employee_type the employee's type based on the employee id
   * @throws Exception if any I/O operation in console failed
   */
  public void cleaner_menu(Connection con, Scanner sc, String employee_id, String employee_type)
                                                                              throws Exception {

    String cleaner_menu_input = "";

    while (true) {
      System.out.println("\nPlease select an option:\n1. Check assigned cleaning area"
              + "\n2. Go back to employee login menu\n3. Quit");

      if (sc.hasNext()) {
        cleaner_menu_input = sc.next();
      }

      switch (cleaner_menu_input) {
        case "1":
          employeeOperations.cleaner_assigned_area(con, employee_id);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "2":
          this.employee_login_menu(con, sc);

        case "3":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * If an employee's menu selection involves calling a SQL procedure, then once it's done,
   * a prompt will show up in console to ask the employee to either go back to the previous menu
   * (which is each employee type's main menu) or quit.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param employee_id an employee id
   * @param employee_type the employee's type based on the employee id
   * @throws Exception if any I/O operation in console failed
   */
  public void employee_after_result_menu(Connection con, Scanner sc, String employee_id,
                                        String employee_type) throws Exception {

    String employee_after_result_input = "";

    while (true) {
      System.out.print("\nPlease select 1 to go back to " + employee_type + " main menu, "
                      + "or 0 to quit: ");

      if (sc.hasNext()) {
        employee_after_result_input = sc.next();
      }

      switch (employee_after_result_input) {
        case "1":
          if (employee_type.equals("warehouse_manager")) {
            this.warehouse_manager_menu(con, sc, employee_id, employee_type);
          }
          else if (employee_type.equals("cashier")) {
            this.cashier_menu(con, sc, employee_id, employee_type);
          }
          else if (employee_type.equals("cleaner")) {
            this.cleaner_menu(con, sc, employee_id, employee_type);
          }

        case "0":
          System.exit(0);

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }
  }
}
