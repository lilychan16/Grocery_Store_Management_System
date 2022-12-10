import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Public class to store methods to perform operations in warehouse manager menu.
 */
public class WarehouseManagerOperations {

  SharedHelperMethods sharedHelperMethods;

  /**
   * Constructor for WarehouseManagerOperations class.
   * @param sharedHelperMethods an object from SharedHelperMethods class
   */
  public WarehouseManagerOperations(SharedHelperMethods sharedHelperMethods) {
    this.sharedHelperMethods = sharedHelperMethods;
  }


  /**
   * Method to show all products' information.
   * @param con a connection to the database
   * @throws SQLException if any SQL operation failed
   */
  public void show_all_products(Connection con) throws SQLException {

    System.out.println("\nPrint all products:");

    CallableStatement cs_all_products = con.prepareCall(
            "{call queryProductAll()}"
    );

    ResultSet rs_all_products = cs_all_products.executeQuery();

    this.print_formatted_product_table_employee(rs_all_products);

    rs_all_products.close();
    cs_all_products.close();
  }


  /**
   * Method to look up product information on employee's end by entering a product id.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @return a product id that can be used by other methods
   * @throws SQLException if any SQL operation failed
   */
  public String employee_look_up_product_by_id(Connection con, Scanner sc) throws SQLException {

    String product_id = sharedHelperMethods.validate_product_id_input(con, sc);

    System.out.println("\nPrint product information:");

    CallableStatement cs_product_by_id = con.prepareCall(
            "{call queryProductByIdEmployeeVersion(?)}"
    );

    cs_product_by_id.setInt(1, Integer.parseInt(product_id));

    ResultSet rs_product_by_id = cs_product_by_id.executeQuery();

    this.print_formatted_product_table_employee(rs_product_by_id);

    rs_product_by_id.close();
    cs_product_by_id.close();

    return product_id;
  }


  /**
   * Method to look up product information on employee's end by entering a product name.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws SQLException if any SQL operation failed
   */
  public void employee_look_up_product_by_name(Connection con, Scanner sc) throws SQLException {

    String product_name = "";

    ArrayList<String> product_name_list = new ArrayList<>();

    CallableStatement cs_product_all = con.prepareCall(
            "{call queryProductAll()}"
    );

    ResultSet rs_product_all = cs_product_all.executeQuery();

    while (rs_product_all.next()) {
      product_name_list.add(rs_product_all.getString(4).toLowerCase());
    }

    System.out.print("\nPlease enter product name: ");

    if (sc.hasNextLine()) {
      sc.nextLine();
      // Since sc.next() was called right before this method,
      // there was a new line ('\n') which next() did not consume.
      // We need to first use an extra call to nextLine() to consume the '\n',
      // before calling nextLine() to get the product name.
      // See: https://stackoverflow.com/questions/23450524/java-scanner-doesnt-wait-for-user-input
      product_name = sc.nextLine();
      // since product name input may be a string with spaces,
      // we use nextLine() method instead of next() to read the whole line
    }

    while (!product_name_list.contains(product_name.toLowerCase())) {
      System.out.println("You entered invalid input. Please re-enter product name or 0 to quit");
      System.out.print("Please enter product name: ");
      if (sc.hasNextLine()) {
        product_name = sc.nextLine();

        if (product_name.equals("0")) {
          System.exit(0);
        }
      }
    }

    System.out.println("\nPrint product information:");

    CallableStatement cs_product_by_name = con.prepareCall(
            "{call queryProductByNameEmployeeVersion(?)}"
    );

    cs_product_by_name.setString(1, product_name);

    ResultSet rs_product_by_name = cs_product_by_name.executeQuery();

    this.print_formatted_product_table_employee(rs_product_by_name);

    rs_product_by_name.close();
    cs_product_by_name.close();
  }


  /**
   * Method to add a new product to the MySQL database.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param employee_id a warehouse manager's id
   * @throws SQLException if any SQL operation failed
   */
  public void add_new_product(Connection con, Scanner sc, String employee_id)
                                                                  throws SQLException {

    String product_name = "";
    String category_name = "";
    String area_id_input;
    String warehouse_manager_id_input = "";
    double product_price;
    int product_stock;
    int area_id = 0;
    int warehouse_manager_id;

    ArrayList<String> category_list;
    Map<Integer, String> area_map;

    System.out.print("\nPlease enter product name: ");

    if (sc.hasNextLine()) {
      sc.nextLine();
      product_name = sc.nextLine();
    }

    System.out.print("\nPlease enter product price (per lb or unit): ");

    product_price = this.validate_product_price_input(sc);

    System.out.print("\nPlease enter product stock: ");

    product_stock = this.validate_product_stock_input(sc);

    category_list = this.get_category(con);

    System.out.println("\nPlease enter a category name based on the category table above,");
    System.out.print("input name needs to exactly match the name presented in the category table: ");

    if (sc.hasNextLine()) {
      category_name = sc.nextLine();
    }

    while (!category_list.contains(category_name.toLowerCase())) {
      System.out.println("\nYou entered invalid input. Please refer to the category table.");
      System.out.println("Please enter a category name based on the category table above,");
      System.out.print("input name needs to exactly match the name presented in the category table: ");

      if (sc.hasNextLine()) {
        category_name = sc.nextLine();
      }
    }

    area_map = this.get_area(con);

    ArrayList<Integer> area_id_list = new ArrayList<>(area_map.keySet());

    System.out.print("\nPlease enter an area id based on the area table above: ");

    if (sc.hasNextLine()) {

      boolean flag = false;

      while (!flag) {
        try {
          area_id_input = sc.nextLine();
          area_id = Integer.parseInt(area_id_input);

          while (!area_id_list.contains(area_id)) {
            System.out.println("\nYou entered invalid input. Please refer to the area table.");
            System.out.print("Please enter an area id based on the area table above: ");

            if (sc.hasNextLine()) {
              area_id_input = sc.nextLine();
              area_id = Integer.parseInt(area_id_input);
            }
          }

          // the area name needs to match the category name when adding a new product
          while (!area_map.get(area_id).equalsIgnoreCase(category_name)) {
            System.out.println("\nThe area name does not match the category name. "
                    + "Please make sure you select the correct area id.");
            System.out.print("Please enter an area id based on the area table above: ");

            if (sc.hasNextLine()) {
              area_id_input = sc.nextLine();
              area_id = Integer.parseInt(area_id_input);
            }
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.print("\nYou did not enter a number.");
          System.out.print("\nPlease enter an area id based on the area table above: ");
        }
      }
    }

    System.out.print("\nPlease enter the warehouse manager id (should be your id): ");

    if (sc.hasNextLine()) {
      warehouse_manager_id_input = sc.nextLine();
    }

    while (!warehouse_manager_id_input.equals(employee_id)) {
      System.out.print("\nYou entered invalid input. Please enter the warehouse manager id "
              + "(should be your id): ");

      if (sc.hasNextLine()) {
        warehouse_manager_id_input = sc.nextLine();
      }
    }

    warehouse_manager_id = Integer.parseInt(warehouse_manager_id_input);

    CallableStatement cs_insert_product = con.prepareCall(
                "{call insertProduct(?,?,?,?,?,?)}"
    );

    cs_insert_product.setDouble(1, product_price);
    cs_insert_product.setInt(2, product_stock);
    cs_insert_product.setString(3, product_name);
    cs_insert_product.setString(4, category_name);
    cs_insert_product.setInt(5, area_id);
    cs_insert_product.setInt(6, warehouse_manager_id);

    cs_insert_product.executeUpdate();

    System.out.println("\nSuccessfully added a new product.");
    System.out.println("\nThe updated product table is as follows:");

    this.show_all_products(con);

    cs_insert_product.close();
  }


  /**
   * Helper method to validate a product price input.
   * @param sc the scanner to receive user input
   * @return a valid product price input
   */
  public double validate_product_price_input(Scanner sc) {

    String product_price_input;
    double product_price = 0;

    if (sc.hasNextLine()) {

      boolean flag = false;

      while (!flag) {
        try {
          product_price_input = sc.nextLine();
          product_price = Double.parseDouble(product_price_input);

          if (product_price < 0) {
            System.out.print("\nProduct price has been set to 0.\n");
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.print("\nYou did not enter a number.");
          System.out.print("\nPlease enter product price (per lb or unit): ");
        }
      }
    }

    return product_price;
  }


  /**
   * Helper method to validate a product stock input.
   * @param sc the scanner to receive user input
   * @return a valid product stock input
   */
  public int validate_product_stock_input(Scanner sc) {

    String product_stock_input;
    int product_stock = 0;

    if (sc.hasNextLine()) {

      boolean flag = false;

      while (!flag) {
        try {
          product_stock_input = sc.nextLine();
          product_stock = Integer.parseInt(product_stock_input);

          if (product_stock < 0) {
            System.out.print("\nProduct stock has been set to 0.\n");
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.print("\nYou did not enter a number.");
          System.out.print("\nPlease enter product stock: ");
        }
      }
    }

    return product_stock;
  }


  /**
   * Helper method to print a formatted product table (employee version) in console.
   * @param rs_product a ResultSet object of all information in the product table
   * @throws SQLException if any SQL operation failed
   */
  public void print_formatted_product_table_employee(ResultSet rs_product) throws SQLException {

    ResultSetMetaData rsmd_product_table = rs_product.getMetaData();

    String product_table_columns = String.format("%-20s %-20s %-20s %-40s %-30s %-20s %-20s",
            rsmd_product_table.getColumnName(1),
            rsmd_product_table.getColumnName(2),
            rsmd_product_table.getColumnName(3),
            rsmd_product_table.getColumnName(4),
            rsmd_product_table.getColumnName(5),
            rsmd_product_table.getColumnName(6),
            rsmd_product_table.getColumnName(7));
    System.out.println(product_table_columns);

    while (rs_product.next()) {
      String out_product = String.format("%-20d %-20.2f %-20d %-40s %-30s %-20d %-20d",
              rs_product.getInt(1),
              rs_product.getDouble(2),
              rs_product.getInt(3),
              rs_product.getString(4),
              rs_product.getString(5),
              rs_product.getInt(6),
              rs_product.getInt(7));
      System.out.println(out_product);
    }
  }


  /**
   * Method to update a product's price by entering a product id.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws SQLException if any SQL operation failed
   */
  public void update_product_price_by_id(Connection con, Scanner sc) throws SQLException {

    String product_id = this.employee_look_up_product_by_id(con, sc);

    System.out.print("\nPlease enter the new price (per lb or unit): ");

    sc.nextLine();
    double new_price = this.validate_product_price_input(sc);

    CallableStatement cs_update_product_price = con.prepareCall(
              "{call updateProductPriceById(?,?)}"
    );

    cs_update_product_price.setInt(1, Integer.parseInt(product_id));
    cs_update_product_price.setDouble(2, new_price);

    cs_update_product_price.executeUpdate();

    System.out.println("\nSuccessfully updated the price.");
    System.out.println("The updated product information is as follows:");

    CallableStatement cs_updated_product_info = con.prepareCall(
            "{call queryProductByIdEmployeeVersion(?)}"
    );

    cs_updated_product_info.setInt(1, Integer.parseInt(product_id));

    ResultSet rs_updated_product_info = cs_updated_product_info.executeQuery();

    this.print_formatted_product_table_employee(rs_updated_product_info);

    cs_update_product_price.close();
    cs_updated_product_info.close();
    rs_updated_product_info.close();
  }


  /**
   * Method to update a product's stock by entering a product id.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws SQLException if any SQL operation failed
   */
  public void update_product_stock_by_id(Connection con, Scanner sc) throws SQLException {

    String product_id = this.employee_look_up_product_by_id(con, sc);

    System.out.print("\nPlease enter the new stock: ");

    sc.nextLine();
    int new_stock = this.validate_product_stock_input(sc);

    CallableStatement cs_update_product_stock = con.prepareCall(
            "{call updateProductStockById(?,?)}"
    );

    cs_update_product_stock.setInt(1, Integer.parseInt(product_id));
    cs_update_product_stock.setInt(2, new_stock);

    cs_update_product_stock.executeUpdate();

    System.out.println("\nSuccessfully updated the stock.");
    System.out.println("The updated product information is as follows:");

    CallableStatement cs_updated_product_info = con.prepareCall(
            "{call queryProductByIdEmployeeVersion(?)}"
    );

    cs_updated_product_info.setInt(1, Integer.parseInt(product_id));

    ResultSet rs_updated_product_info = cs_updated_product_info.executeQuery();

    this.print_formatted_product_table_employee(rs_updated_product_info);

    cs_update_product_stock.close();
    cs_updated_product_info.close();
    rs_updated_product_info.close();
  }


  /**
   * Method to delete a product in the MySQL database by entering a product id.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws SQLException if any SQL operation failed
   */
  public void delete_product_by_id(Connection con, Scanner sc) throws SQLException {

    String product_id = this.employee_look_up_product_by_id(con, sc);
    String delete_or_not = "";

    System.out.print("\nPlease enter 1 to confirm deletion, or 0 to re-enter product id: ");

    if (sc.hasNext()) {
      delete_or_not = sc.next();
    }

    while (!delete_or_not.equals("1") && !delete_or_not.equals("0")) {
      System.out.print("Invalid input, please enter 1 to confirm deletion, "
                        + "or 0 to re-enter product id: ");

      if (sc.hasNext()) {
        delete_or_not = sc.next();
      }
    }

    if (delete_or_not.equals("1")) {
      CallableStatement cs_delete_product = con.prepareCall(
              "{call deleteProductById(?)}"
      );

      cs_delete_product.setInt(1, Integer.parseInt(product_id));

      cs_delete_product.executeUpdate();

      System.out.println("\nSuccessfully delete the product.");
      System.out.println("The updated product table is as follows:");

      this.show_all_products(con);

      cs_delete_product.close();
    }
    else {
      this.delete_product_by_id(con, sc);
    }
  }


  /**
   * Helper method to get the names of all product categories.
   * @param con a connection to the database
   * @return a list of product categories
   * @throws SQLException if any SQL operation failed
   */
  public ArrayList<String> get_category(Connection con) throws SQLException {

    ArrayList<String> category_list = new ArrayList<>();

    CallableStatement cs_category = con.prepareCall(
            "{call queryCategoryAll()}"
    );

    ResultSet rs_category = cs_category.executeQuery();

    ResultSetMetaData rsmd_category_table = rs_category.getMetaData();

    String category_table_columns = rsmd_category_table.getColumnName(1);
    System.out.println();
    System.out.println(category_table_columns);

    while (rs_category.next()) {
      category_list.add(rs_category.getString(1).toLowerCase());
      String out_category = rs_category.getString(1);
      System.out.println(out_category);
    }

    rs_category.close();
    cs_category.close();

    return category_list;
  }


  /**
   * Helper method to get the information of all store areas.
   * @param con a connection to the database
   * @return a map to store area information, with area_id as key and area_name as value
   * @throws SQLException if any SQL operation failed
   */
  public Map<Integer, String> get_area(Connection con) throws SQLException {

    Map<Integer, String> area_map = new HashMap<>();

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
      area_map.put(rs_area.getInt(1), rs_area.getString(2));
      String out_area = String.format("%-20d %-20s",
              rs_area.getInt(1),
              rs_area.getString(2));
      System.out.println(out_area);
    }

    rs_area.close();
    cs_area.close();

    return area_map;
  }


  /**
   * Method to add a new store area to the MySQL database.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws SQLException if any SQL operation failed
   */
  public void add_new_store_area(Connection con, Scanner sc) throws SQLException {

    String area_name_input = "";
    Map<Integer, String> area_map;
    ArrayList<String> area_name_list = new ArrayList<>();

    System.out.println("\nReview current store area: ");

    area_map = this.get_area(con);

    ArrayList<String> temporary_area_name_list = new ArrayList<>(area_map.values());

    for (String s : temporary_area_name_list) {
      area_name_list.add(s.toLowerCase());
    }

    System.out.print("\nPlease enter new area name: ");

    if (sc.hasNextLine()) {
      sc.nextLine();
      area_name_input = sc.nextLine();
    }

    while (area_name_list.contains(area_name_input.toLowerCase())) {
      System.out.println("\nThe area name already exists.");
      System.out.print("Please enter a new area name or 0 to quit: ");

      if (sc.hasNextLine()) {
        area_name_input = sc.nextLine();

        if (area_name_input.equals("0")) {
          System.exit(0);
        }
      }
    }

    CallableStatement cs_add_area = con.prepareCall(
                "{call insertStoreAreaByName(?)}"
    );

    cs_add_area.setString(1, area_name_input);

    cs_add_area.executeUpdate();

    System.out.println("\nSuccessfully added a new store area.");
    System.out.println("\nThe updated store area table is as follows:");

    this.get_area(con);

    cs_add_area.close();
  }
}
