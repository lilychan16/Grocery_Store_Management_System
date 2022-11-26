import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Scanner;

public class EmployeeMenu {

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
      if (sc.hasNext()) {
        employee_id = sc.next();

        if (employee_id.equals("0")) {
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

    if (rs_employee_id.next()) {
      String out_employee_id = String.format("%-20d %-35s %-30s %-20d",
              rs_employee_id.getInt(1),
              rs_employee_id.getString(2),
              rs_employee_id.getString(3),
              rs_employee_id.getInt(4));
      System.out.println(out_employee_id);
      employee_type = rs_employee_id.getString(3);
    }

    if (employee_type.equals("cashier")) {
      System.out.println("\nLogin successfully. You are: cashier\n");
      this.cashier_menu(con, sc, employee_id);
    }
    else if (employee_type.equals("cleaner")) {
      System.out.println("\nLogin successfully. You are: cleaner\n");
      this.cleaner_menu(con, sc, employee_id);
    }

    rs_employee_all.close();
    cs_employee_all.close();

    rs_employee_id.close();
    cs_employee_id.close();
  }


  /**
   * Print cashier menu and then perform an operation in console based on user input.
   * The allowed operations for a cashier are:
   *    (1) Check assigned check-out counter
   *    (2) Quit
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param employee_id an employee id to identify employee type
   * @throws Exception if any I/O operation in console failed
   */
  public void cashier_menu(Connection con, Scanner sc, String employee_id) throws Exception {

    String cashier_menu_input = "";

    while (true) {
      System.out.println("Please select an option:\n1. Check assigned check-out counter"
              + "\n2. Quit");

      if (sc.hasNext()) {
        cashier_menu_input = sc.next();
      }

      switch (cashier_menu_input) {
        case "1":
          System.out.println("\nCheck-out Counter Information:");

          CallableStatement cs_cashier_counter = con.prepareCall(
                  "{call queryEmployeeSpecialById(?)}"
          );

          cs_cashier_counter.setInt(1, Integer.parseInt(employee_id));

          ResultSet rs_cashier_counter = cs_cashier_counter.executeQuery();

          if (rs_cashier_counter.next()) {
            String out_cashier_counter = "Your assigned check-out counter is: counter number " +
                    rs_cashier_counter.getInt(3);
            System.out.println(out_cashier_counter);
          }

          cashier_cleaner_after_result_menu(con, sc);

          rs_cashier_counter.close();
          cs_cashier_counter.close();

        case "2":
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
   *    (2) Quit
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param employee_id an employee id to identify employee type
   * @throws Exception if any I/O operation in console failed
   */
  public void cleaner_menu(Connection con, Scanner sc, String employee_id) throws Exception {

    String cleaner_menu_input = "";

    while (true) {
      System.out.println("Please select an option:\n1. Check assigned cleaning area"
              + "\n2. Quit");

      if (sc.hasNext()) {
        cleaner_menu_input = sc.next();
      }

      switch (cleaner_menu_input) {
        case "1":
          System.out.println("\nCleaning Area Information:");

          CallableStatement cs_cleaning_area = con.prepareCall(
                  "{call queryEmployeeSpecialById(?)}"
          );

          cs_cleaning_area.setInt(1, Integer.parseInt(employee_id));

          ResultSet rs_cleaning_area = cs_cleaning_area.executeQuery();

          if (rs_cleaning_area.next()) {
            String out_cleaning_area = "Your assigned cleaning_area is: Area " +
                    rs_cleaning_area.getInt(4) +
                    "  "+ rs_cleaning_area.getString(5);
            System.out.println(out_cleaning_area);
          }

          cashier_cleaner_after_result_menu(con, sc);

          rs_cleaning_area.close();
          cs_cleaning_area.close();

        case "2":
          System.exit(0);

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * If a cashier or cleaner's menu selection involves calling a SQL procedure,
   * then once it's done, a prompt will show up in console to ask the employee to either
   * go back to employee login menu or quit.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void cashier_cleaner_after_result_menu(Connection con, Scanner sc) throws Exception {

    String after_result_input = "";

    while (true) {
      System.out.print("\nPlease select 1 to go back to employee login menu, or 0 to quit: ");

      if (sc.hasNext()) {
        after_result_input = sc.next();
      }

      switch (after_result_input) {
        case "1":
          this.employee_login_menu(con, sc);

        case "0":
          System.exit(0);

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }
  }
}
