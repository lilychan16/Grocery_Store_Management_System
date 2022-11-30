import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class EmployeeSQLOperations {

  public void show_all_products(Connection con) throws SQLException {

    System.out.println("\nPrint all products:");

    CallableStatement cs_all_products = con.prepareCall(
            "{call queryProductAll()}"
    );

    ResultSet rs_all_products = cs_all_products.executeQuery();

    this.print_formatted_product_table(rs_all_products);

    rs_all_products.close();
    cs_all_products.close();
  }


  public void employee_look_up_product_by_id(Connection con, Scanner sc) throws SQLException {

    String product_id = "";

    ArrayList<String> product_id_list = new ArrayList<>();

    CallableStatement cs_product_all = con.prepareCall(
            "{call queryProductAll()}"
    );

    ResultSet rs_product_all = cs_product_all.executeQuery();

    while (rs_product_all.next()) {
      product_id_list.add(Integer.toString(rs_product_all.getInt(1)));
    }

    System.out.print("\nPlease enter product id: ");

    if (sc.hasNext()) {
      product_id = sc.next();
    }

    while (!product_id_list.contains(product_id)) {
      System.out.println("You entered invalid input. Please re-enter product id or 0 to quit");
      System.out.print("Please enter product id: ");
      if (sc.hasNext()) {
        product_id = sc.next();

        if (product_id.equals("0")) {
          System.exit(0);
        }
      }
    }

    System.out.println("\nPrint product information:");

    CallableStatement cs_product_by_id = con.prepareCall(
            "{call queryProductByIdEmployeeVersion(?)}"
    );

    cs_product_by_id.setInt(1, Integer.parseInt(product_id));

    ResultSet rs_product_by_id = cs_product_by_id.executeQuery();

    this.print_formatted_product_table(rs_product_by_id);

    rs_product_by_id.close();
    cs_product_by_id.close();
  }




  public void print_formatted_product_table(ResultSet rs_product) throws SQLException {

    ResultSetMetaData rsmd_product_table = rs_product.getMetaData();

    String product_table_columns = String.format("%-20s %-20s %-20s %-45s %-30s %-20s %-20s",
            rsmd_product_table.getColumnName(1),
            rsmd_product_table.getColumnName(2),
            rsmd_product_table.getColumnName(3),
            rsmd_product_table.getColumnName(4),
            rsmd_product_table.getColumnName(5),
            rsmd_product_table.getColumnName(6),
            rsmd_product_table.getColumnName(7));
    System.out.println(product_table_columns);

    while (rs_product.next()) {
      String out_product = String.format("%-20d %-20d %-20d %-45s %-30s %-20d %-20d",
              rs_product.getInt(1),
              rs_product.getInt(2),
              rs_product.getInt(3),
              rs_product.getString(4),
              rs_product.getString(5),
              rs_product.getInt(6),
              rs_product.getInt(7));
      System.out.println(out_product);
    }
  }


  /**
   * Call MySQL database to get the assigned check-out counter information for the cashier.
   * @param con a connection to the database
   * @param employee_id an employee id
   * @throws Exception if any I/O operation in console failed
   */
  public void cashier_assigned_counter(Connection con, String employee_id) throws Exception {

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

    rs_cashier_counter.close();
    cs_cashier_counter.close();
  }


  /**
   * Call MySQL database to get the assigned cleaning area information for the cleaner.
   * @param con a connection to the database
   * @param employee_id an employee id
   * @throws Exception if any I/O operation in console failed
   */
  public void cleaner_assigned_area(Connection con, String employee_id) throws Exception {

    System.out.println("\nCleaning Area Information:");

    CallableStatement cs_cleaning_area = con.prepareCall(
            "{call queryEmployeeSpecialById(?)}"
    );

    cs_cleaning_area.setInt(1, Integer.parseInt(employee_id));

    ResultSet rs_cleaning_area = cs_cleaning_area.executeQuery();

    if (rs_cleaning_area.next()) {
      String out_cleaning_area = "Your assigned cleaning area is: Area " +
              rs_cleaning_area.getInt(4) +
              "  "+ rs_cleaning_area.getString(5);
      System.out.println(out_cleaning_area);
    }

    rs_cleaning_area.close();
    cs_cleaning_area.close();
  }
}
