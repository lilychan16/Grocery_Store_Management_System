import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Public class to store methods to perform operations in customer menu.
 */
public class CustomerOperations {

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
