import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Public class to hold some help methods that can benefit all other classes.
 */
public class SharedHelperMethods {

  /**
   * Helper method to validate a product id input.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @return a valid product id input
   * @throws SQLException if any SQL operation failed
   */
  public String validate_product_id_input(Connection con, Scanner sc) throws SQLException {

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

    return product_id;
  }


  /**
   * Helper method to print a formatted customer table in console.
   * @param rs_customer a ResultSet object of all information in the customer table
   * @throws SQLException if any SQL operation failed
   */
  public void print_formatted_customer_table(ResultSet rs_customer) throws SQLException {

    ResultSetMetaData rsmd_customer_table = rs_customer.getMetaData();

    String customer_table_columns = String.format("%-20s %-40s %-30s %-30s %-30s %-30s",
            rsmd_customer_table.getColumnName(1),
            rsmd_customer_table.getColumnName(2),
            rsmd_customer_table.getColumnName(3),
            rsmd_customer_table.getColumnName(4),
            rsmd_customer_table.getColumnName(5),
            rsmd_customer_table.getColumnName(6));
    System.out.println(customer_table_columns);

    while (rs_customer.next()) {
      String out_customer_id = String.format("%-20d %-40s %-30s %-30s %-30.2f %-30d",
              rs_customer.getInt(1),
              rs_customer.getString(2),
              rs_customer.getString(3),
              rs_customer.getString(4),
              rs_customer.getDouble(5),
              rs_customer.getInt(6));
      System.out.println(out_customer_id);
    }
  }
}
