import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Public class to store methods to perform operations in store manager menu.
 */
public class StoreManagerEmployeeSideOperations {

  public void show_all_employees(Connection con) throws SQLException {

    System.out.println("\nPrint all employees:");

    CallableStatement cs_all_employees = con.prepareCall(
            "{call queryEmployeeAll()}"
    );

    ResultSet rs_all_employees = cs_all_employees.executeQuery();

    this.print_formatted_employee_table(rs_all_employees);

    rs_all_employees.close();
    cs_all_employees.close();
  }


  public String look_up_employee_by_id(Connection con, Scanner sc) throws SQLException {

    String employee_id = this.validate_employee_id_input(con, sc);

    System.out.println("\nPrint employee information: ");

    CallableStatement cs_employee_by_id = con.prepareCall(
            "{call queryEmployeeById(?)}"
    );

    cs_employee_by_id.setInt(1, Integer.parseInt(employee_id));

    ResultSet rs_employee_by_id = cs_employee_by_id.executeQuery();

    this.print_formatted_employee_table(rs_employee_by_id);

    rs_employee_by_id.close();
    cs_employee_by_id.close();

    return employee_id;
  }


  public String validate_employee_id_input(Connection con, Scanner sc) throws SQLException {

    String employee_id = "";

    ArrayList<String> employee_id_list = new ArrayList<>();

    CallableStatement cs_employee_all = con.prepareCall(
            "{call queryEmployeeAll()}"
    );

    ResultSet rs_employee_all = cs_employee_all.executeQuery();

    while (rs_employee_all.next()) {
      employee_id_list.add(Integer.toString(rs_employee_all.getInt(1)));
    }

    System.out.print("\nPlease enter employee id: ");

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

    return employee_id;
  }


  public void look_up_employee_by_name(Connection con, Scanner sc) throws SQLException {

    String employee_name = "";

    ArrayList<String> employee_name_list = new ArrayList<>();

    CallableStatement cs_employee_all = con.prepareCall(
            "{call queryEmployeeAll()}"
    );

    ResultSet rs_employee_all = cs_employee_all.executeQuery();

    while (rs_employee_all.next()) {
      employee_name_list.add(rs_employee_all.getString(2).toLowerCase());
    }

    System.out.print("\nPlease enter employee name: ");

    if (sc.hasNextLine()) {
      sc.nextLine();
      employee_name = sc.nextLine();
    }

    while (!employee_name_list.contains(employee_name.toLowerCase())) {
      System.out.println("You entered invalid input. Please re-enter employee name or 0 to quit");
      System.out.print("Please enter employee name: ");
      if (sc.hasNextLine()) {
        employee_name = sc.nextLine();

        if (employee_name.equals("0")) {
          System.exit(0);
        }
      }
    }

    System.out.println("\nPrint employee information:");

    CallableStatement cs_employee_by_name = con.prepareCall(
            "{call queryEmployeeByName(?)}"
    );

    cs_employee_by_name.setString(1, employee_name);

    ResultSet rs_employee_by_name = cs_employee_by_name.executeQuery();

    this.print_formatted_employee_table(rs_employee_by_name);

    rs_employee_by_name.close();
    cs_employee_by_name.close();
  }


  public void add_new_employee(Connection con, Scanner sc, String new_employee_type)
                                                                              throws SQLException {

    String employee_name = "";
    int employee_birth_year;
    int employee_birth_month;
    int employee_birth_day;
    double employee_hourly_wage;
    int new_employee_id = 0;

    System.out.print("\nPlease enter employee name: ");

    if (sc.hasNextLine()) {
      sc.nextLine();
      employee_name = sc.nextLine();
    }

    System.out.print("Please enter employee's birth year: ");

    employee_birth_year = this.validate_birth_year_input(sc);

    System.out.print("Please enter employee's birth month: ");

    employee_birth_month = this.validate_birth_month_input(sc);

    System.out.print("Please enter employee's birth day: ");

    employee_birth_day = this.validate_birth_day_input(sc, employee_birth_month,
                                                        employee_birth_year);

    String employee_dob = employee_birth_year + "-" + employee_birth_month + "-"
                                                            + employee_birth_day;

    System.out.print("Please enter employee's hourly wage: ");

    employee_hourly_wage = this.validate_hourly_wage_input(sc);

    // add a new employee to employee table
    CallableStatement cs_insert_employee = con.prepareCall(
            "{call insertEmployee(?,?,?,?)}"
    );

    cs_insert_employee.setString(1, employee_name);
    cs_insert_employee.setString(2, new_employee_type);
    cs_insert_employee.setDate(3, Date.valueOf(employee_dob));
    cs_insert_employee.setDouble(4, employee_hourly_wage);

    cs_insert_employee.executeUpdate();

    // get new employee id
    CallableStatement cs_get_employee_id = con.prepareCall(
            "{call getEmployeeIdByNameAndBirthDate(?,?)}"
    );

    cs_get_employee_id.setString(1, employee_name);
    cs_get_employee_id.setDate(2, Date.valueOf(employee_dob));

    ResultSet rs_get_employee_id = cs_get_employee_id.executeQuery();

    if (rs_get_employee_id.next()) {
      new_employee_id = rs_get_employee_id.getInt(1);
    }

    if (new_employee_type.equals("store_manager")) {
      // add a new employee to store manager table
      CallableStatement cs_insert_store_manager = con.prepareCall(
              "{call insertStoreManager(?)}"
      );

      cs_insert_store_manager.setInt(1, new_employee_id);

      cs_insert_store_manager.executeUpdate();

      System.out.println("\nSuccessfully added a new store manager.");
      System.out.println("\nThe updated store manager table is as follows:");

      this.print_store_manager_table(con);
    }
    else if (new_employee_type.equals("warehouse_manager")) {
      // add a new employee to warehouse manager table
      CallableStatement cs_insert_warehouse_manager = con.prepareCall(
              "{call insertWarehouseManager(?)}"
      );

      cs_insert_warehouse_manager.setInt(1, new_employee_id);

      cs_insert_warehouse_manager.executeUpdate();

      System.out.println("\nSuccessfully added a new warehouse manager.");
      System.out.println("\nThe updated warehouse manager table is as follows:");

      this.print_warehouse_manager_table(con);
    }
    else if (new_employee_type.equals("cashier")) {

      String new_cashier_counter_input;
      int new_cashier_counter = 0;

      ArrayList<Integer> counter_id_list = this.get_counter_number(con);

      System.out.print("\nPlease enter a counter number based on the counter table above: ");

      if (sc.hasNextLine()) {

        boolean flag = false;

        while (!flag) {
          try {
            new_cashier_counter_input = sc.nextLine();
            new_cashier_counter = Integer.parseInt(new_cashier_counter_input);

            while (!counter_id_list.contains(new_cashier_counter)) {
              System.out.println("\nYou entered invalid input. Please refer to the counter table.");
              System.out.print("Please enter a counter number based on the counter table above: ");

              if (sc.hasNextLine()) {
                new_cashier_counter_input = sc.nextLine();
                new_cashier_counter = Integer.parseInt(new_cashier_counter_input);
              }
            }

            flag = true;

          } catch (NumberFormatException e) {
            System.out.print("\nYou did not enter a number.");
            System.out.print("\nPlease enter a counter number based on the counter table above: ");
          }
        }
      }

      // add a new employee to cashier table
      CallableStatement cs_insert_cashier = con.prepareCall(
              "{call insertCashier(?,?)}"
      );

      cs_insert_cashier.setInt(1, new_employee_id);
      cs_insert_cashier.setInt(2, new_cashier_counter);

      cs_insert_cashier.executeUpdate();

      System.out.println("\nSuccessfully added a new cashier.");
      System.out.println("\nThe updated cashier table is as follows:");

      this.print_cashier_table(con);
    }
    else if (new_employee_type.equals("cleaner")) {

      String new_cleaning_area_input = "";
      int new_cleaning_area = 0;

      ArrayList<Integer> area_id_list = this.get_area_id(con);

      System.out.print("\nPlease enter a cleaning area id based on the area table above: ");

      if (sc.hasNextLine()) {

        boolean flag = false;

        while (!flag) {
          try {
            new_cleaning_area_input = sc.nextLine();
            new_cleaning_area = Integer.parseInt(new_cleaning_area_input);

            while (!area_id_list.contains(new_cleaning_area)) {
              System.out.println("\nYou entered invalid input. Please refer to the area table.");
              System.out.print("Please enter a cleaning area id based on the area table above: ");

              if (sc.hasNextLine()) {
                new_cleaning_area_input = sc.nextLine();
                new_cleaning_area = Integer.parseInt(new_cleaning_area_input);
              }
            }

            flag = true;
          } catch (NumberFormatException e) {
            System.out.print("\nYou did not enter a number.");
            System.out.print("\nPlease enter a cleaning area id based on the area table above: ");
          }
        }
      }

      // add a new employee to cleaner table
      CallableStatement cs_insert_cleaner = con.prepareCall(
              "{call insertCleaner(?,?)}"
      );

      cs_insert_cleaner.setInt(1, new_employee_id);
      cs_insert_cleaner.setInt(2, new_cleaning_area);

      cs_insert_cleaner.executeUpdate();

      System.out.println("\nSuccessfully added a new cleaner.");
      System.out.println("\nThe updated cleaner table is as follows:");

      this.print_cleaner_table(con);
    }
    else {
      System.out.println("Not a valid employee type based on current record. "
                          + "Please update the add_new_employee method.");
    }

    System.out.println("\nThe updated employee table is as follows:");

    this.show_all_employees(con);

    rs_get_employee_id.close();
    cs_get_employee_id.close();
    cs_insert_employee.close();
  }


  public void delete_employee_by_id(Connection con, Scanner sc) throws SQLException {

    String employee_id;
    String employee_type;
    String delete_or_not = "";

    employee_id = this.look_up_employee_by_id(con, sc);
    employee_type = this.get_employee_type_by_id(con, employee_id);

    while (employee_type.equals("store_manager")) {
      System.out.println("\nYou are not allowed to delete a store manager. "
                          + "Please re-enter employee id.");

      employee_id = this.look_up_employee_by_id(con, sc);
      employee_type = this.get_employee_type_by_id(con, employee_id);
    }

    System.out.print("\nPlease enter 1 to confirm deletion, or 0 to re-enter employee id: ");

    if (sc.hasNext()) {
      delete_or_not = sc.next();
    }

    while (!delete_or_not.equals("1") && !delete_or_not.equals("0")) {
      System.out.print("\nInvalid input, please enter 1 to confirm deletion, "
                        + "or 0 to re-enter employee id: ");

      if (sc.hasNext()) {
        delete_or_not = sc.next();
      }
    }

    if (delete_or_not.equals("1")) {
      
      if (employee_type.equals("warehouse_manager")) {
        CallableStatement cs_delete_warehouse_manager = con.prepareCall(
                "{call deleteWarehouseManagerById(?)}"
        );

        cs_delete_warehouse_manager.setInt(1, Integer.parseInt(employee_id));

        cs_delete_warehouse_manager.executeUpdate();

        System.out.println("\nSuccessfully delete the warehouse manager.");
        System.out.println("The updated warehouse manager table is as follows:");

        this.print_warehouse_manager_table(con);

        cs_delete_warehouse_manager.close();
      }
      else if (employee_type.equals("cashier")) {
        CallableStatement cs_delete_cashier = con.prepareCall(
                "{call deleteCashierById(?)}"
        );

        cs_delete_cashier.setInt(1, Integer.parseInt(employee_id));

        cs_delete_cashier.executeUpdate();

        System.out.println("\nSuccessfully delete the cashier.");
        System.out.println("The updated cashier table is as follows:");

        this.print_cashier_table(con);

        cs_delete_cashier.close();
      }
      else if (employee_type.equals("cleaner")) {
        CallableStatement cs_delete_cleaner = con.prepareCall(
                "{call deleteCleanerById(?)}"
        );

        cs_delete_cleaner.setInt(1, Integer.parseInt(employee_id));

        cs_delete_cleaner.executeUpdate();

        System.out.println("\nSuccessfully delete the cleaner.");
        System.out.println("The updated cleaner table is as follows:");

        this.print_cleaner_table(con);

        cs_delete_cleaner.close();
      }
      else {
        System.out.println("Not a valid employee type based on current record. "
                + "Please update the delete_employee_by_id method.");
      }

      CallableStatement cs_delete_employee = con.prepareCall(
              "{call deleteEmployeeById(?)}"
      );

      cs_delete_employee.setInt(1, Integer.parseInt(employee_id));

      cs_delete_employee.executeUpdate();

      System.out.println("\nThe updated employee table is as follows:");

      this.show_all_employees(con);

      cs_delete_employee.close();
    }
    else {
      this.delete_employee_by_id(con, sc);
    }

  }


  public String get_employee_type_by_id(Connection con, String employee_id) throws SQLException {

    String employee_type = "";

    CallableStatement cs_get_employee_type = con.prepareCall(
            "{call queryEmployeeTypeById(?)}"
    );

    cs_get_employee_type.setInt(1, Integer.parseInt(employee_id));

    ResultSet rs_get_employee_type = cs_get_employee_type.executeQuery();

    while (rs_get_employee_type.next()) {
      employee_type = rs_get_employee_type.getString(1);
    }

    rs_get_employee_type.close();
    cs_get_employee_type.close();

    return employee_type;
  }


  public void print_formatted_table_one_integer_column(ResultSet rs_one_integer_column)
                                                                            throws SQLException {

    ResultSetMetaData rsmd_one_integer_column_table = rs_one_integer_column.getMetaData();

    String table_column = rsmd_one_integer_column_table.getColumnName(1);
    System.out.println();
    System.out.println(table_column);

    while (rs_one_integer_column.next()) {
      int out_column = rs_one_integer_column.getInt(1);
      System.out.println(out_column);
    }
  }


  public void print_formatted_table_two_integer_columns(ResultSet rs_two_integers_columns)
                                                                            throws SQLException {

    ResultSetMetaData rsmd_two_integer_columns_table = rs_two_integers_columns.getMetaData();

    String table_columns = String.format("%-20s %-20s",
            rsmd_two_integer_columns_table.getColumnName(1),
            rsmd_two_integer_columns_table.getColumnName(2));
    System.out.println(table_columns);

    while (rs_two_integers_columns.next()) {
      String out_columns = String.format("%-20d %-20d",
              rs_two_integers_columns.getInt(1),
              rs_two_integers_columns.getInt(2));
      System.out.println(out_columns);
    }
  }


  public ArrayList<Integer> get_counter_number(Connection con) throws SQLException {

    ArrayList<Integer> counter_id_list = new ArrayList<>();

    CallableStatement cs_counter_id = con.prepareCall(
            "{call getAllCounterId()}"
    );

    ResultSet rs_counter_id = cs_counter_id.executeQuery();

    ResultSetMetaData rsmd_counter_table = rs_counter_id.getMetaData();

    String counter_table_column = rsmd_counter_table.getColumnName(1);
    System.out.println();
    System.out.println(counter_table_column);

    while (rs_counter_id.next()) {
      counter_id_list.add(rs_counter_id.getInt(1));
      int out_counter_id = rs_counter_id.getInt(1);
      System.out.println(out_counter_id);
    }

    rs_counter_id.close();
    cs_counter_id.close();

    return counter_id_list;
  }


  public ArrayList<Integer> get_area_id(Connection con) throws SQLException {

    ArrayList<Integer> area_id_list = new ArrayList<>();

    CallableStatement cs_area = con.prepareCall(
            "{call queryAreaAll()}"
    );

    ResultSet rs_area = cs_area.executeQuery();

    ResultSetMetaData rsmd_area_table = rs_area.getMetaData();

    String area_table_columns = String.format("%-20s %-20s",
            rsmd_area_table.getColumnName(1),
            rsmd_area_table.getColumnName(2));
    System.out.println();
    System.out.println(area_table_columns);

    while (rs_area.next()) {
      area_id_list.add(rs_area.getInt(1));
      String out_area = String.format("%-20d %-20s",
              rs_area.getInt(1),
              rs_area.getString(2));
      System.out.println(out_area);
    }

    rs_area.close();
    cs_area.close();

    return area_id_list;
  }


  public int validate_birth_year_input(Scanner sc) {

    String birth_year_input;
    int birth_year = 0;

    if (sc.hasNextLine()) {

      boolean flag = false;

      while (!flag) {
        try {
          birth_year_input = sc.nextLine();
          birth_year = Integer.parseInt(birth_year_input);

          while (birth_year < 1930 || birth_year > (Year.now().getValue() - 18)) {
            System.out.println("Employee birth year must be between 1930 and "
                                + (Year.now().getValue() - 18) + ".");
            System.out.print("Please enter employee's birth year: ");

            if (sc.hasNextLine()) {
              birth_year_input = sc.nextLine();
              birth_year = Integer.parseInt(birth_year_input);
            }
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.print("\nYou did not enter a number.");
          System.out.print("\nPlease enter employee's birth year: ");
        }
      }
    }

    return birth_year;
  }


  public int validate_birth_month_input(Scanner sc) {

    String birth_month_input;
    int birth_month = 0;

    if (sc.hasNextLine()) {

      boolean flag = false;

      while (!flag) {
        try {
          birth_month_input = sc.nextLine();
          birth_month = Integer.parseInt(birth_month_input);

          while (birth_month < 1 || birth_month > 12) {
            System.out.println("Employee's birth month must be between 1 and 12.");
            System.out.print("Please enter employee's birth month: ");

            if (sc.hasNextLine()) {
              birth_month_input = sc.nextLine();
              birth_month = Integer.parseInt(birth_month_input);
            }
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.print("\nYou did not enter a number.");
          System.out.print("\nPlease enter employee's birth month: ");
        }
      }
    }

    return birth_month;
  }


  public int validate_birth_day_input(Scanner sc, int birth_month, int birth_year) {

    String birth_day_input = "";
    int birth_day = 0;

    if (sc.hasNextLine()) {

      boolean flag = false;

      while (!flag) {
        try {
          birth_day_input = sc.nextLine();
          birth_day = Integer.parseInt(birth_day_input);

          if (birth_month == 2) {
            if (Year.of(birth_year).isLeap()) {
              while (birth_day < 1 || birth_day > 29) {
                System.out.println("Employee's birth year is a leap year. "
                                    + "Birth day must be between 1 and 29.");
                System.out.print("Please enter employee's birth day: ");

                if (sc.hasNextLine()) {
                  birth_day_input = sc.nextLine();
                  birth_day = Integer.parseInt(birth_day_input);
                }
              }
            }
            else {
              while (birth_day < 1 || birth_day > 28) {
                System.out.println("Employee's birth day must be between 1 and 28.");
                System.out.print("Please enter employee's birth day: ");

                if (sc.hasNextLine()) {
                  birth_day_input = sc.nextLine();
                  birth_day = Integer.parseInt(birth_day_input);
                }
              }
            }
          }
          else if (birth_month == 4 || birth_month == 6 || birth_month == 9 || birth_month == 11) {
            while (birth_day < 1 || birth_day > 30) {
              System.out.println("Employee's birth day must be between 1 and 30.");
              System.out.print("Please enter employee's birth day: ");

              if (sc.hasNextLine()) {
                birth_day_input = sc.nextLine();
                birth_day = Integer.parseInt(birth_day_input);
              }
            }
          }
          else {
            while (birth_day < 1 || birth_day > 31) {
              System.out.println("Employee's birth day must be between 1 and 31.");
              System.out.print("Please enter employee's birth day: ");

              if (sc.hasNextLine()) {
                birth_day_input = sc.nextLine();
                birth_day = Integer.parseInt(birth_day_input);
              }
            }
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.print("\nYou did not enter a number.");
          System.out.print("\nPlease enter employee's birth day: ");
        }
      }
    }

    return birth_day;
  }


  public double validate_hourly_wage_input(Scanner sc) {

    String hourly_wage_input;
    double hourly_wage = 0;

    if (sc.hasNextLine()) {

      boolean flag = false;

      while (!flag) {
        try {
          hourly_wage_input = sc.nextLine();
          hourly_wage = Double.parseDouble(hourly_wage_input);

          if (hourly_wage < 0) {
            System.out.print("\nHourly wage has been set to 0.\n");
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.print("\nYou did not enter a number.");
          System.out.print("\nPlease enter employee's hourly wage: ");
        }
      }
    }

    return hourly_wage;
  }


  public void print_formatted_employee_table(ResultSet rs_employee) throws SQLException {

    ResultSetMetaData rsmd_employee_table = rs_employee.getMetaData();

    String employee_table_columns = String.format("%-20s %-35s %-30s %-30s %-20s",
            rsmd_employee_table.getColumnName(1),
            rsmd_employee_table.getColumnName(2),
            rsmd_employee_table.getColumnName(3),
            rsmd_employee_table.getColumnName(4),
            rsmd_employee_table.getColumnName(5));
    System.out.println(employee_table_columns);

    while (rs_employee.next()) {
      String out_employee = String.format("%-20d %-35s %-30s %-30s %-20.2f",
              rs_employee.getInt(1),
              rs_employee.getString(2),
              rs_employee.getString(3),
              rs_employee.getString(4),
              rs_employee.getDouble(5));
      System.out.println(out_employee);
    }
  }


  public void print_store_manager_table(Connection con) throws SQLException {

    CallableStatement cs_all_store_managers = con.prepareCall(
            "{call queryStoreManagerAll()}"
    );

    ResultSet rs_all_store_managers = cs_all_store_managers.executeQuery();

    this.print_formatted_table_one_integer_column(rs_all_store_managers);

    rs_all_store_managers.close();
    cs_all_store_managers.close();
  }


  public void print_warehouse_manager_table(Connection con) throws SQLException {

    CallableStatement cs_all_warehouse_managers = con.prepareCall(
            "{call queryWarehouseManagerAll()}"
    );

    ResultSet rs_all_warehouse_managers = cs_all_warehouse_managers.executeQuery();

    this.print_formatted_table_one_integer_column(rs_all_warehouse_managers);

    rs_all_warehouse_managers.close();
    cs_all_warehouse_managers.close();
  }


  public void print_cashier_table(Connection con) throws SQLException {

    CallableStatement cs_all_cashiers = con.prepareCall(
            "{call queryCashierAll()}"
    );

    ResultSet rs_all_cashiers = cs_all_cashiers.executeQuery();

    this.print_formatted_table_two_integer_columns(rs_all_cashiers);

    rs_all_cashiers.close();
    cs_all_cashiers.close();
  }


  public void print_cleaner_table(Connection con) throws SQLException {

    CallableStatement cs_all_cleaners = con.prepareCall(
            "{call queryCleanerAll()}"
    );

    ResultSet rs_all_cleaners = cs_all_cleaners.executeQuery();

    this.print_formatted_table_two_integer_columns(rs_all_cleaners);

    rs_all_cleaners.close();
    cs_all_cleaners.close();
  }
}
