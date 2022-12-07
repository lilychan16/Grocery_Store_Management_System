import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Public class to hold all menu items on employee's end.
 */
public class EmployeeMenuItems {

  StoreManagerCustomerSideOperations storeManagerCustomerSideOperations;
  StoreManagerEmployeeSideOperations storeManagerEmployeeSideOperations;
  WarehouseManagerOperations warehouseManagerOperations;
  CashierCleanerOperations cashierCleanerOperations;

  /**
   * Constructor for EmployeeMenuItems class.
   * @param warehouseManagerOperations an object from EmployeeOperations class
   * @param cashierCleanerOperations an object from CashierCleanerOperations class
   * @param storeManagerCustomerSideOperations an object from StoreManagerCustomerSideOperations class
   * @param storeManagerEmployeeSideOperations an object from StoreManagerEmployeeSideOperations class
   */
  public EmployeeMenuItems(StoreManagerCustomerSideOperations storeManagerCustomerSideOperations,
                           StoreManagerEmployeeSideOperations storeManagerEmployeeSideOperations,
                           WarehouseManagerOperations warehouseManagerOperations,
                           CashierCleanerOperations cashierCleanerOperations) {

    this.storeManagerCustomerSideOperations = storeManagerCustomerSideOperations;
    this.storeManagerEmployeeSideOperations = storeManagerEmployeeSideOperations;
    this.warehouseManagerOperations = warehouseManagerOperations;
    this.cashierCleanerOperations = cashierCleanerOperations;
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

    String employee_table_columns = String.format("%-20s %-35s %-30s %-30s %-20s",
            rsmd_employee_table.getColumnName(1),
            rsmd_employee_table.getColumnName(2),
            rsmd_employee_table.getColumnName(3),
            rsmd_employee_table.getColumnName(4),
            rsmd_employee_table.getColumnName(5));
    System.out.println(employee_table_columns);

    // Use if instead of while because only 1 record should be printed for each unique employee id
    if (rs_employee_id.next()) {
      String out_employee_id = String.format("%-20d %-35s %-30s %-30s %-20.2f",
              rs_employee_id.getInt(1),
              rs_employee_id.getString(2),
              rs_employee_id.getString(3),
              rs_employee_id.getString(4),
              rs_employee_id.getDouble(5));
      System.out.println(out_employee_id);
      employee_type = rs_employee_id.getString(3);
    }

    System.out.println("\nLogin successfully. You are: " + employee_type);

    switch (employee_type) {
      case "store_manager":
        this.store_manager_main_menu(con, sc);

      case "warehouse_manager":
        this.warehouse_manager_menu(con, sc, employee_id, employee_type);

      case "cashier":
        this.cashier_menu(con, sc, employee_id, employee_type);

      case "cleaner":
        this.cleaner_menu(con, sc, employee_id, employee_type);

      default:
        System.out.println("Not a valid employee type based on current record. "
                            + "Please update the employee_menu_type method.");
    }

    rs_employee_all.close();
    cs_employee_all.close();

    rs_employee_id.close();
    cs_employee_id.close();
  }


  /**
   * Print store manager main menu in console and then print more store manager menu items
   * based on user input.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void store_manager_main_menu(Connection con, Scanner sc) throws Exception {

    String store_manager_main_menu_input = "";

    while (true) {
      System.out.println("\nPlease select an option:\n1. Manage employee data"
              + "\n2. Manage customer data\n3. Go back to employee login menu\n4. Quit");

      if (sc.hasNext()) {
        store_manager_main_menu_input = sc.next();
      }

      switch (store_manager_main_menu_input) {
        case "1":
          this.store_manager_employee_side_menu(con, sc);

        case "2":
          this.store_manager_customer_side_menu(con, sc);

        case "3":
          this.employee_login_menu(con, sc);

        case "4":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Print store manager - employee management menu and then perform an operation
   * in console based on user input.
   * The allowed employee management operations for a store manager are:
   *    (1) Show all employee data
   *    (2) Look up employee info by employee id
   *    (3) Look up employee info by employee name
   *    (4) Add a new employee
   *    (5) Delete an employee by employee id
   *    (6) Go back to store manager main menu
   *    (7) Quit
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void store_manager_employee_side_menu(Connection con, Scanner sc) throws Exception {

    String store_manager_menu_type = "employee";
    String store_manager_employee_side_input = "";

    while (true) {
      System.out.println("\nPlease select an option:\n1. Show all employee data"
              + "\n2. Look up employee info by employee id"
              + "\n3. Look up employee info by employee name\n4. Add a new employee"
              + "\n5. Delete an employee by employee id\n6. Go back to store manager main menu"
              + "\n7. Quit");

      if (sc.hasNext()) {
        store_manager_employee_side_input = sc.next();
      }

      switch (store_manager_employee_side_input) {
        case "1":
          storeManagerEmployeeSideOperations.show_all_employees(con);
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "2":
          storeManagerEmployeeSideOperations.look_up_employee_by_id(con, sc);
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "3":
          storeManagerEmployeeSideOperations.look_up_employee_by_name(con, sc);
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "4":
          this.store_manager_add_new_employee_menu(con, sc);

        case "5":
          storeManagerEmployeeSideOperations.delete_employee_by_id(con, sc);
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "6":
          this.store_manager_main_menu(con, sc);

        case "7":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Print store manager - customer management menu and then perform an operation
   * in console based on user input.
   * The allowed customer management operations for a store manager are:
   *    (1) Show all customer data
   *    (2) Look up customer info by customer id
   *    (3) Look up customer info by customer name
   *    (4) Add a new customer
   *    (5) Delete a customer by customer id
   *    (6) Go back to store manager main menu
   *    (7) Quit
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void store_manager_customer_side_menu(Connection con, Scanner sc) throws Exception {

    String store_manager_menu_type = "customer";
    String store_manager_customer_side_input = "";

    while (true) {
      System.out.println("\nPlease select an option:\n1. Show all customer data"
              + "\n2. Look up customer info by customer id"
              + "\n3. Look up customer info by customer name\n4. Add a new customer"
              + "\n5. Delete a customer by customer id\n6. Go back to store manager main menu"
              + "\n7. Quit");

      if (sc.hasNext()) {
        store_manager_customer_side_input = sc.next();
      }

      switch (store_manager_customer_side_input) {
        case "1":
          storeManagerCustomerSideOperations.show_all_customers(con);
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "2":
          storeManagerCustomerSideOperations.look_up_customer_by_id(con, sc);
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "3":
          storeManagerCustomerSideOperations.look_up_customer_by_name(con, sc);
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "4":
          storeManagerCustomerSideOperations.add_new_customer(con, sc);
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "5":
          storeManagerCustomerSideOperations.delete_customer_by_id(con, sc);
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "6":
          this.store_manager_main_menu(con, sc);

        case "7":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Print a menu for store manager to add a new employee to the database.
   * The allowed employee types to be added are:
   *    (1) Store Manager
   *    (2) Warehouse Manager
   *    (3) Cashier
   *    (4) Cleaner
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void store_manager_add_new_employee_menu(Connection con, Scanner sc) throws Exception {

    String store_manager_menu_type = "employee";
    String add_new_employee_menu_input = "";

    while (true) {
      System.out.println("\nPlease select an employee type to be added:\n1. Store Manager"
              + "\n2. Warehouse Manager\n3. Cashier"
              + "\n4. Cleaner\n5. Go back to store manager - employee management menu"
              + "\n6. Quit");

      if (sc.hasNext()) {
        add_new_employee_menu_input = sc.next();
      }

      switch(add_new_employee_menu_input) {
        case "1":
          storeManagerEmployeeSideOperations.add_new_employee(con, sc, "store_manager");
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "2":
          storeManagerEmployeeSideOperations.add_new_employee(con, sc, "warehouse_manager");
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "3":
          storeManagerEmployeeSideOperations.add_new_employee(con, sc, "cashier");
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "4":
          storeManagerEmployeeSideOperations.add_new_employee(con, sc, "cleaner");
          this.store_manager_after_result_menu(con, sc, store_manager_menu_type);

        case "5":
          this.store_manager_employee_side_menu(con, sc);

        case "6":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Print warehouse manager menu and then perform an operation in console based on user input.
   * The allowed operations for a warehouse manager are:
   *    (1) Show all product data
   *    (2) Look up product info by product id
   *    (3) Look up product info by product name
   *    (4) Add a new product
   *    (5) Update product price by product id
   *    (6) Update product stock by product id
   *    (7) Delete a product by product id
   *    (8) Add a new store area
   *    (9) Go back to employee login menu
   *    (10) Quit
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param employee_id an employee id
   * @param employee_type the employee's type based on the employee id
   * @throws Exception if any I/O operation in console failed
   */
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
          warehouseManagerOperations.show_all_products(con);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "2":
          warehouseManagerOperations.employee_look_up_product_by_id(con, sc);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "3":
          warehouseManagerOperations.employee_look_up_product_by_name(con, sc);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "4":
          warehouseManagerOperations.add_new_product(con, sc, employee_id);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "5":
          warehouseManagerOperations.update_product_price_by_id(con, sc);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "6":
          warehouseManagerOperations.update_product_stock_by_id(con, sc);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "7":
          warehouseManagerOperations.delete_product_by_id(con, sc);
          this.employee_after_result_menu(con, sc, employee_id, employee_type);

        case "8":
          warehouseManagerOperations.add_new_store_area(con, sc);
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
          cashierCleanerOperations.cashier_assigned_counter(con, employee_id);
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
          cashierCleanerOperations.cleaner_assigned_area(con, employee_id);
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
   * If a store manager's menu selection involves calling a SQL procedure, then once it's done,
   * a prompt will show up in console to ask the employee to either go back to the previous menu
   * (which is either the employee management menu or the customer management menu) or quit.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param store_manager_menu_type the type of store manager menus, either employee or customer
   * @throws Exception if any I/O operation in console failed
   */
  public void store_manager_after_result_menu(Connection con, Scanner sc,
                                              String store_manager_menu_type) throws Exception {

    String store_manager_after_result_input = "";

    while (true) {
      System.out.print("\nPlease select 1 to go back to store manager - " + store_manager_menu_type
              + " management menu, or 0 to quit: ");

      if (sc.hasNext()) {
        store_manager_after_result_input = sc.next();
      }

      switch (store_manager_after_result_input) {
        case "1":
          if (store_manager_menu_type.equals("employee")) {
            this.store_manager_employee_side_menu(con, sc);
          }
          else if (store_manager_menu_type.equals("customer")) {
            this.store_manager_customer_side_menu(con, sc);
          }
          else {
            System.out.println("Not a valid store manager menu type based on current record. "
                    + "Please update the store_manager_after_result_menu method.");
          }

        case "0":
          System.exit(0);

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * This method is used by every employee type except store manager.
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
          else {
            System.out.println("Not a valid employee type based on current record. "
                                + "Please update the employee_after_result_menu method.");
          }

        case "0":
          System.exit(0);

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }
  }
}
