import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Year;
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


  /**
   * Helper method to validate a birth year input.
   * @param sc the scanner to receive user input
   * @return a valid birth year input
   */
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


  /**
   * Helper method to validate a birth month input.
   * @param sc the scanner to receive user input
   * @return a valid birth month input
   */
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


  /**
   * Helper method to validate a birth day input.
   * @param sc the scanner to receive user input
   * @param birth_month a birth month input
   * @param birth_year a birth year input
   * @return a valid birth day input
   */
  public int validate_birth_day_input(Scanner sc, int birth_month, int birth_year) {

    String birth_day_input;
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
}
