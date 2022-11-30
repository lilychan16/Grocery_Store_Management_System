import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

public class CashierCleanerOperations {

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
