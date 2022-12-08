import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Public class to store methods to perform operations in customer menu.
 */
public class CustomerOperations {

  SharedHelperMethods sharedHelperMethods;
  LinkedHashMap<Integer, LinkedList<String>> cart = new LinkedHashMap<>();


  /**
   * Constructor for CustomerOperations class.
   * @param sharedHelperMethods an object from SharedHelperMethods class
   */
  public CustomerOperations(SharedHelperMethods sharedHelperMethods) {
    this.sharedHelperMethods = sharedHelperMethods;
  }


  /**
   * Method to look up product information on customer's end by entering a product id.
   * The customer can then decide if the product should be added to the shopping cart.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void customer_look_up_product_by_id(Connection con, Scanner sc) throws Exception {

    String add_to_cart_input = "";

    String product_id = sharedHelperMethods.validate_product_id_input(con, sc);

    System.out.println("\nPrint product information:");

    CallableStatement cs_product_by_id = con.prepareCall(
            "{call queryProductByIdCustomerVersion(?)}"
    );

    cs_product_by_id.setInt(1, Integer.parseInt(product_id));

    ResultSet rs_product_by_id = cs_product_by_id.executeQuery();

    this.print_formatted_product_table_customer(rs_product_by_id);

    boolean flag = false;

    while (!flag) {

      System.out.print("\nAdd this product to cart? 1 for yes, 0 for no: ");

      if (sc.hasNext()) {
        add_to_cart_input = sc.next();
      }

      switch(add_to_cart_input) {
        case "1":
          this.add_product_to_cart(con, sc, product_id);
          flag = true;
          break;

        case "0":
          this.after_add_to_cart_menu(con, sc);
          flag = true;
          break;

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }

    rs_product_by_id.close();
    cs_product_by_id.close();
  }


  /**
   * Add a product to shopping cart. The cart is maintained by a LinkedHashMap
   * with product_id as key, and a LinkedList as value to store the product's
   * desired quantity (index 0) and price (index 1).
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param product_id a valid product id input
   * @throws Exception if any I/O operation in console failed
   */
  public void add_product_to_cart(Connection con, Scanner sc, String product_id) throws Exception {

    int stock = 0;
    double price = 0;
    int quantity = 0;

    LinkedList<String> quantity_price_list = new LinkedList<>();

    String quantity_input;

    if (cart.containsKey(Integer.parseInt(product_id))) {
      System.out.println("\nThis product is already in the cart. "
                          + "If you'd like to update the quantity, "
                          + "please go to Check Cart option.");
    }
    else {
      CallableStatement cs_product_by_id = con.prepareCall(
              "{call queryProductByIdCustomerVersion(?)}"
      );

      cs_product_by_id.setInt(1, Integer.parseInt(product_id));

      ResultSet rs_product_by_id = cs_product_by_id.executeQuery();

      while (rs_product_by_id.next()) {
        stock = rs_product_by_id.getInt(3);
        price = rs_product_by_id.getDouble(2);
      }

      System.out.print("Please enter quantity: ");

      if (sc.hasNext()) {

        boolean flag = false;

        while (!flag) {
          try {
            quantity_input = sc.next();
            quantity = Integer.parseInt(quantity_input);

            while (quantity < 0) {
              System.out.println("\nQuantity must be >= 0.");
              System.out.print("Please enter quantity: ");

              if (sc.hasNext()) {
                quantity_input = sc.next();
                quantity = Integer.parseInt(quantity_input);
              }
            }

            while (quantity > stock) {
              System.out.println("\nQuantity input is greater than current product stock.");
              System.out.print("Please enter quantity: ");

              if (sc.hasNext()) {
                quantity_input = sc.next();
                quantity = Integer.parseInt(quantity_input);
              }
            }

            flag = true;

          } catch (NumberFormatException e) {
            System.out.print("\nYou did not enter a number.");
            System.out.print("\nPlease enter quantity: ");
          }
        }
      }

      quantity_price_list.add(String.valueOf(quantity));
      quantity_price_list.add(String.valueOf(price));

      cart.put(Integer.parseInt(product_id), quantity_price_list);

      CallableStatement cs_decrease_stock = con.prepareCall(
              "{call updateProductStockByIdAfterAddToCart(?,?)}"
      );

      cs_decrease_stock.setInt(1, Integer.parseInt(product_id));
      cs_decrease_stock.setInt(2, quantity);

      cs_decrease_stock.executeUpdate();
    }

    this.after_add_to_cart_menu(con, sc);
  }


  /**
   * Print a menu to ask if the customer want to add more products to the
   * shopping cart or not. If yes, print out more menu options for the customer
   * to select a method to search for more products.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void after_add_to_cart_menu(Connection con, Scanner sc) throws Exception {

    boolean flag = false;

    String after_add_to_cart_menu_input = "";

    while (!flag) {

      System.out.print("\nDo you want to add more products to cart? 1 for yes, 0 for no: ");

      if (sc.hasNext()) {
        after_add_to_cart_menu_input = sc.next();
      }

      switch (after_add_to_cart_menu_input) {
        case "1":
          customer_product_look_up_type(con, sc);
          flag = true;
          break;

        case "0":
          flag = true;
          break;

        default:
          System.out.print("\nInvalid input, please re-enter");
      }

    }
  }


  /**
   * Helper function to direct the customer to different methods (by product id or
   * by product name) to search for more products.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void customer_product_look_up_type(Connection con, Scanner sc) throws Exception {

    boolean flag = false;

    String product_look_up_type = "";

    while (!flag) {
      System.out.print("Do you want to search product by id or by name? "
                          + "1 for id, 2 for name: ");

      if (sc.hasNext()) {
        product_look_up_type = sc.next();
      }

      switch(product_look_up_type) {
        case "1":
          this.customer_look_up_product_by_id(con, sc);
          flag = true;
          break;

        case "2":
          this.customer_look_up_product_by_name(con, sc);
          flag = true;
          break;

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Method to look up product information on customer's end by entering a product name.
   * The customer can then decide if the product should be added to the shopping cart.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void customer_look_up_product_by_name(Connection con, Scanner sc) throws Exception {

    String product_name = "";
    String add_to_cart_input = "";

    String product_id_input = "";

    ArrayList<String> product_name_list = new ArrayList<>();

    ArrayList<String> product_id_list = new ArrayList<>();

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
            "{call queryProductByNameCustomerVersion(?)}"
    );

    cs_product_by_name.setString(1, product_name);

    ResultSet rs_product_by_name = cs_product_by_name.executeQuery();

    this.print_formatted_product_table_customer(rs_product_by_name);

    boolean flag = false;

    while (!flag) {

      System.out.print("\nAdd this product to cart? 1 for yes, 0 for no: ");

      if (sc.hasNext()) {
        add_to_cart_input = sc.next();
      }

      switch(add_to_cart_input) {
        case "1":

          CallableStatement cs_product_by_name_another = con.prepareCall(
                  "{call queryProductByNameCustomerVersion(?)}"
          );

          cs_product_by_name_another.setString(1, product_name);

          ResultSet rs_product_by_name_another = cs_product_by_name_another.executeQuery();

          while (rs_product_by_name_another.next()) {
            product_id_list.add(String.valueOf(rs_product_by_name_another.getInt(1)));
          }

          // In the future, when a customer search a product by name, there might be
          // multiple products with the same name but have different prices, brands, etc.
          // So we need to ask the customer for the unique product id to add to the cart.
          System.out.print("Please enter the product id of the product you'd like to add: ");

          if (sc.hasNext()) {
            product_id_input = sc.next();
          }

          while (!product_id_list.contains(product_id_input)) {
            System.out.println("You entered invalid input. Please re-enter product id or 0 to quit");
            System.out.print("Please enter product id: ");

            if (sc.hasNext()) {
              product_id_input = sc.next();

              if (product_name.equals("0")) {
                System.exit(0);
              }
            }
          }

          this.add_product_to_cart(con, sc, product_id_input);
          flag = true;
          break;

        case "0":
          this.after_add_to_cart_menu(con, sc);
          flag = true;
          break;

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }

    rs_product_by_name.close();
    cs_product_by_name.close();
  }


  /**
   * Method to redeem all available reward points in a customer profile.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param customer_id a valid customer id input
   * @throws SQLException if any SQL operation failed
   */
  public void redeem_points(Connection con, Scanner sc, String customer_id) throws SQLException {

    String redeem_points_input = "";

    CallableStatement cs_customer_info = con.prepareCall(
            "{call queryCustomerById(?)}"
    );

    cs_customer_info.setInt(1, Integer.parseInt(customer_id));

    ResultSet rs_customer_info = cs_customer_info.executeQuery();

    sharedHelperMethods.print_formatted_customer_table(rs_customer_info);

    System.out.println("\nEvery 100 reward points = 1 grocery dollar.");

    boolean flag = false;

    while (!flag) {
      System.out.print("\nPlease enter 1 to confirm points redemption, "
                        + "0 to go back to customer main menu or quit: ");

      if (sc.hasNext()) {
        redeem_points_input = sc.next();
      }

      switch (redeem_points_input) {
        case "1":
          CallableStatement cs_redeem_points = con.prepareCall(
                  "{call updateCustomerDollarById(?)}"
          );

          cs_redeem_points.setInt(1, Integer.parseInt(customer_id));

          cs_redeem_points.executeUpdate();

          System.out.println("\nYour updated reward points balance "
                              + "and grocery dollars balance are as follows.");
          System.out.println("Note that reward points < 100 were unable to redeem.\n");

          ResultSet rs_updated_customer_info = cs_customer_info.executeQuery();

          sharedHelperMethods.print_formatted_customer_table(rs_updated_customer_info);

          rs_updated_customer_info.close();

          cs_redeem_points.close();

          flag = true;
          break;

        case "0":
          flag = true;
          break;

        default:
          System.out.print("\nInvalid input, please re-enter");

      }
    }

    rs_customer_info.close();
    cs_customer_info.close();
  }


  /**
   * Method to print out products in shopping cart, update the shopping cart, and check out.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void cart_operations(Connection con, Scanner sc, String customer_id) throws Exception {

    String check_cart_input = "";

    boolean flag = false;

    while (!flag) {
      System.out.println("\nPlease select an option:\n1. Check cart\n2. Check out\n3. Update cart"
              + "\n4. Go back to customer main menu or Quit");

      if (sc.hasNext()) {
        check_cart_input = sc.next();
      }

      switch (check_cart_input) {
        case "1":
          this.print_cart();
          flag = true;
          break;

        case "2":
          this.check_out(con, sc, customer_id);
          flag = true;
          break;

        case "3":
          this.update_cart(con, sc);
          flag = true;
          break;

        case "4":
          flag = true;
          break;

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Helper method to format the shopping cart.
   */
  public double print_cart() {

    int product_id;
    String product_quantity;
    String product_price;

    double cart_total = 0;

    System.out.println("\nCart Summary:");

    for (Map.Entry<Integer, LinkedList<String>> c : this.cart.entrySet()) {

      product_id = c.getKey();

      LinkedList<String> value_list = c.getValue();

      product_quantity = value_list.get(0);
      product_price = value_list.get(1);

      System.out.println("Product ID: " + product_id
              + ", Quantity: " + product_quantity
              + ", Unit Price: " + product_price);

      cart_total = cart_total
              + Integer.parseInt(product_quantity) * Double.parseDouble(product_price);
    }

    System.out.println("Your cart total is: " + String.format("%.2f", cart_total));

    return cart_total;
  }


  /**
   * Check out a shopping cart, create a new order, and store product info in this new order.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param customer_id a valid customer id input
   * @throws SQLException if any SQL operation failed
   */
  public void check_out(Connection con, Scanner sc, String customer_id) throws SQLException {

    String redeem_or_not = "";
    String apply_grocery_dollar_input;
    double apply_grocery_dollar = 0;
    double cart_total;
    double grocery_dollar = 0;

    cart_total = this.print_cart();

    System.out.println("\nDo you want to use grocery dollars?");
    System.out.print("Please enter 1 for yes, 0 for no: ");

    if (sc.hasNext()) {
      redeem_or_not = sc.next();
    }

    while (!redeem_or_not.equals("1") && !redeem_or_not.equals("0")) {
      System.out.println("\nInvalid input, please re-enter.");
      System.out.println("Do you want to use grocery dollars?");
      System.out.print("Please enter 1 for yes, 0 for no: ");

      if (sc.hasNext()) {
        redeem_or_not = sc.next();
      }
    }

    if (redeem_or_not.equals("1")) {

      CallableStatement cs_get_grocery_dollar = con.prepareCall(
              "{call queryCustomerById(?)}"
      );

      cs_get_grocery_dollar.setInt(1, Integer.parseInt(customer_id));

      ResultSet rs_get_grocery_dollar = cs_get_grocery_dollar.executeQuery();

      if (rs_get_grocery_dollar.next()) {
        grocery_dollar = rs_get_grocery_dollar.getDouble(5);
        System.out.println("\nYour grocery dollar balance is: "
                            + String.format("%.2f", grocery_dollar));
      }

      System.out.print("\nPlease enter the grocery dollar amount you'd like to use: ");

      if (sc.hasNext()) {

        boolean flag = false;

        while (!flag) {
          try {
              apply_grocery_dollar_input = sc.next();
              apply_grocery_dollar = Double.parseDouble(apply_grocery_dollar_input);

              while (apply_grocery_dollar < 0 || !(apply_grocery_dollar < cart_total
                                                    && apply_grocery_dollar < grocery_dollar)) {
                System.out.println("Grocery dollar amount cannot be less than 0, "
                                    + "greater than cart total, "
                                    + "or greater than your current grocery dollar balance.");
                System.out.print("Please enter the grocery dollar amount you'd like to use: ");

                if (sc.hasNext()) {
                  apply_grocery_dollar_input = sc.next();
                  apply_grocery_dollar = Double.parseDouble(apply_grocery_dollar_input);
                }
              }

              flag = true;

          } catch (NumberFormatException e) {
            System.out.println("\nYou did not enter a number.");
            System.out.print("Please enter the grocery dollar amount you'd like to use: ");
          }
        }
      }

      CallableStatement cs_update_dollars = con.prepareCall(
              "{call updateCustomerDollarsById(?,?)}"
      );

      cs_update_dollars.setDouble(1, apply_grocery_dollar);
      cs_update_dollars.setInt(2, Integer.parseInt(customer_id));

      cs_update_dollars.executeUpdate();

      cart_total = cart_total - apply_grocery_dollar;

      this.generate_order(con, customer_id, cart_total);
    }
    else {
      this.generate_order(con, customer_id, cart_total);
    }
  }


  /**
   * Helper method to create a new order and clear the shopping cart.
   * @param con a connection to the database
   * @param customer_id a valid customer id input
   * @param cart_total total amount of a shopping cart
   * @throws SQLException if any SQL operation failed
   */
  public void generate_order(Connection con, String customer_id, double cart_total)
                                                                            throws SQLException {

    int product_id;
    String product_quantity;
    int order_id = 0;
    double formatted_cart_total;

    formatted_cart_total = Double.parseDouble(String.format("%.2f", cart_total));

    // add cart total amount and customer id to customer_order table,
    // an order id will be automatically generated in the table
    CallableStatement cs_insert_order = con.prepareCall(
            "{call insertCustomerOrder(?,?)}"
    );

    cs_insert_order.setDouble(1, formatted_cart_total);
    cs_insert_order.setInt(2, Integer.parseInt(customer_id));

    cs_insert_order.executeUpdate();

    DateTimeFormatter order_dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate now_date = LocalDate.now();
    String order_date = order_dtf.format(now_date);

    // use order date, order total amount, and customer id to get the order id
    CallableStatement cs_get_order_id = con.prepareCall(
            "{call getOrderIdByDateAmountCustomerId(?,?,?)}"
    );

    cs_get_order_id.setDate(1, Date.valueOf(order_date));
    cs_get_order_id.setDouble(2, formatted_cart_total);
    cs_get_order_id.setInt(3, Integer.parseInt(customer_id));

    ResultSet rs_get_order_id = cs_get_order_id.executeQuery();

    if (rs_get_order_id.next()) {
      order_id = rs_get_order_id.getInt(1);
    }

    // add product info (id, purchased quantity) associated with the new order id
    CallableStatement cs_insert_order_products = con.prepareCall(
            "{call insertOrderContainsProduct(?,?,?)}"
    );

    for (Map.Entry<Integer, LinkedList<String>> c : this.cart.entrySet()) {

      cs_insert_order_products.setInt(1, order_id);

      product_id = c.getKey();
      cs_insert_order_products.setInt(2, product_id);

      LinkedList<String> value_list = c.getValue();

      product_quantity = value_list.get(0);
      cs_insert_order_products.setInt(3, Integer.parseInt(product_quantity));

      cs_insert_order_products.executeUpdate();
    }

    // update reward points in customer table
    // 1 dollar = 1 point
    CallableStatement cs_update_points = con.prepareCall(
            "{call updateCustomerPointsById(?,?)}"
    );

    cs_update_points.setInt(1, (int) formatted_cart_total);
    cs_update_points.setInt(2, Integer.parseInt(customer_id));

    cs_update_points.executeUpdate();

    System.out.println("\nSuccessfully checked out.");
    System.out.println("Your order summary is as follows:\n");

    CallableStatement cs_order_summary_by_id = con.prepareCall(
            "{call getOrderByOrderId(?)}"
    );

    cs_order_summary_by_id.setInt(1, order_id);

    ResultSet rs_order_summary_by_id = cs_order_summary_by_id.executeQuery();

    this.print_formatted_order_summary_table(rs_order_summary_by_id);

    System.out.println("\nYour order detail is as follows:\n");

    this.print_formatted_order_detail_table(con, order_id);

    System.out.println("\nYour new reward point and grocery dollar balances are as follows:\n");

    CallableStatement cs_get_updated_customer = con.prepareCall(
            "{call queryCustomerById(?)}"
    );

    cs_get_updated_customer.setInt(1, Integer.parseInt(customer_id));

    ResultSet rs_get_updated_customer = cs_get_updated_customer.executeQuery();

    sharedHelperMethods.print_formatted_customer_table(rs_get_updated_customer);

    // clear all products in the shopping cart after check out
    cart.clear();
  }


  /**
   * Method to check a customer's order and the product info in that order.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @param customer_id a valid customer id input
   * @throws SQLException if any SQL operation failed
   */
  public void check_orders(Connection con, Scanner sc, String customer_id) throws SQLException {

    String check_orders_input = "";
    String after_check_orders_input = "";
    ArrayList<String> order_id_list_by_customer = new ArrayList<>();

    System.out.println("\nYour orders are as follows:");

    CallableStatement cs_get_all_orders = con.prepareCall(
            "{call getOrderByCustomerId(?)}"
    );

    cs_get_all_orders.setInt(1, Integer.parseInt(customer_id));

    ResultSet rs_get_all_orders = cs_get_all_orders.executeQuery();

    this.print_formatted_order_summary_table(rs_get_all_orders);

    ResultSet rs_get_order_ids_customer = cs_get_all_orders.executeQuery();

    while (rs_get_order_ids_customer.next()) {
      order_id_list_by_customer.add(String.valueOf(rs_get_order_ids_customer.getInt(1)));
    }

    System.out.print("\nPlease enter the order id to check order details: ");

    if (sc.hasNext()) {
      check_orders_input = sc.next();
    }

    while (!order_id_list_by_customer.contains(check_orders_input)) {
      System.out.println("Your order id input is invalid, please re-enter.");
      System.out.print("Please enter the order id to check order details: ");

      if (sc.hasNext()) {
        check_orders_input = sc.next();
      }
    }

    System.out.println("\nThe order detail for order " + check_orders_input + " is as follows:");

    this.print_formatted_order_detail_table(con, Integer.parseInt(check_orders_input));

    boolean flag = false;

    while (!flag ) {

      System.out.print("\nPlease enter 1 to check more orders, "
              + "0 to go back to customer main menu or quit: ");

      if (sc.hasNext()) {
        after_check_orders_input = sc.next();
      }

      switch (after_check_orders_input) {
        case "1":
          this.check_orders(con, sc, customer_id);
          flag = true;
          break;

        case "0":
          flag = true;
          break;

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Helper method to print a formatted customer_order table in console.
   * @param rs_order_summary a ResultSet object of all information in the customer_order table
   * @throws SQLException if any SQL operation failed
   */
  public void print_formatted_order_summary_table(ResultSet rs_order_summary) throws SQLException {

    ResultSetMetaData rsmd_order_table = rs_order_summary.getMetaData();

    String order_table_columns = String.format("%-20s %-30s %-20s %-20s",
            rsmd_order_table.getColumnName(1),
            rsmd_order_table.getColumnName(2),
            rsmd_order_table.getColumnName(3),
            rsmd_order_table.getColumnName(4));
    System.out.println(order_table_columns);

    while (rs_order_summary.next()) {
      String out_order_summary = String.format("%-20d %-30s %-20.2f %-20d",
              rs_order_summary.getInt(1),
              rs_order_summary.getString(2),
              rs_order_summary.getDouble(3),
              rs_order_summary.getInt(4));
      System.out.println(out_order_summary);
    }
  }


  /**
   * Helper method to print a formatted order_contains_product table in console.
   * @param con a connection to the database
   * @param order_id a valid order id input
   * @throws SQLException if any SQL operation failed
   */
  public void print_formatted_order_detail_table(Connection con, int order_id) throws SQLException {

    CallableStatement cs_order_detail_by_id = con.prepareCall(
            "{call getOrderProductsByOrderId(?)}"
    );

    cs_order_detail_by_id.setInt(1, order_id);

    ResultSet rs_order_detail_by_id = cs_order_detail_by_id.executeQuery();

    ResultSetMetaData rsmd_order_products_table = rs_order_detail_by_id.getMetaData();

    String order_products_table_columns = String.format("%-20s %-20s %-20s",
            rsmd_order_products_table.getColumnName(1),
            rsmd_order_products_table.getColumnName(2),
            rsmd_order_products_table.getColumnName(3));
    System.out.println(order_products_table_columns);

    while (rs_order_detail_by_id.next()) {
      String out_order_detail = String.format("%-20d %-20d %-20d",
              rs_order_detail_by_id.getInt(1),
              rs_order_detail_by_id.getInt(2),
              rs_order_detail_by_id.getInt(3));
      System.out.println(out_order_detail);
    }
  }


  /**
   * Method to update the shopping cart. The customer can either delete a product
   * from the cart, or update a product's desired quantity.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void update_cart(Connection con, Scanner sc) throws Exception {

    this.print_cart();

    String update_cart_input = "";

    boolean flag = false;

    while (!flag) {
      System.out.println("\nPlease select an option: \n1. Delete a product from cart"
              + "\n2. Update product quantity\n3. Go back to customer menu or Quit");

      if (sc.hasNext()) {
        update_cart_input = sc.next();
      }

      switch (update_cart_input) {
        case "1":
          this.delete_product_from_cart(con, sc);
          flag = true;
          break;

        case "2":
          this.update_product_quantity(con, sc);
          flag = true;
          break;

        case "3":
          flag = true;
          break;

        default:
          System.out.print("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Helper method to delete a product from the shopping cart.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void delete_product_from_cart(Connection con, Scanner sc) throws Exception {

    String delete_product_id_input;
    int delete_product_id = 0;
    int unwanted_quantity;

    Set<Integer> product_id_set = cart.keySet();

    this.print_cart();

    System.out.print("\nEnter the id of the product you'd like to delete from cart: ");

    if (sc.hasNext()) {

      boolean flag = false;

      while (!flag) {
        try {
          delete_product_id_input = sc.next();
          delete_product_id = Integer.parseInt(delete_product_id_input);

          while (!product_id_set.contains(delete_product_id)) {
            System.out.println("\nYou entered invalid input. Please refer to your cart.");
            System.out.print("Enter the id of the product you'd like to delete from cart: ");

            if (sc.hasNext()) {
              delete_product_id_input = sc.next();
              delete_product_id = Integer.parseInt(delete_product_id_input);
            }
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.println("\nYou did not enter a number.");
          System.out.print("Enter the id of the product you'd like to delete from cart: ");
        }
      }
    }

    unwanted_quantity = Integer.parseInt(cart.get(delete_product_id).get(0));

    CallableStatement cs_add_back_stock = con.prepareCall(
            "{call putUnwantedProductQuantityBackInStock(?,?)}"
    );

    cs_add_back_stock.setInt(1, delete_product_id);
    cs_add_back_stock.setInt(2, unwanted_quantity);

    cs_add_back_stock.executeUpdate();

    cart.remove(delete_product_id);

    System.out.println("\nSuccessfully delete product from cart.");

    this.print_cart();

    this.after_update_cart_menu(con, sc);
  }


  /**
   * Helper method to update a product's quantity in the shopping cart.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void update_product_quantity(Connection con, Scanner sc) throws Exception {

    String update_product_id_input;
    int update_product_id = 0;

    String updated_product_quantity_input;
    int update_product_quantity = 0;

    int stock = 0;

    Set<Integer> product_id_set = cart.keySet();

    this.print_cart();

    System.out.print("\nEnter the id of the product you'd like to update quantity: ");

    if (sc.hasNext()) {

      boolean flag = false;

      while (!flag) {
        try {
          update_product_id_input = sc.next();
          update_product_id = Integer.parseInt(update_product_id_input);

          while (!product_id_set.contains(update_product_id)) {
            System.out.println("\nYou entered invalid input. Please refer to your cart.");
            System.out.print("Enter the id of the product you'd like to update quantity: ");

            if (sc.hasNext()) {
              update_product_id_input = sc.next();
              update_product_id = Integer.parseInt(update_product_id_input);
            }
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.println("\nYou did not enter a number.");
          System.out.print("Enter the id of the product you'd like to update quantity: ");
        }
      }
    }

    System.out.println("\nProduct information:");

    CallableStatement cs_product_by_id = con.prepareCall(
            "{call queryProductByIdCustomerVersion(?)}"
    );

    cs_product_by_id.setInt(1, update_product_id);

    ResultSet rs_product_by_id = cs_product_by_id.executeQuery();

    ResultSetMetaData rsmd_product_table = rs_product_by_id.getMetaData();

    String product_table_columns = String.format("%-20s %-20s %-20s %-40s %-30s %-20s",
            rsmd_product_table.getColumnName(1),
            rsmd_product_table.getColumnName(2),
            rsmd_product_table.getColumnName(3),
            rsmd_product_table.getColumnName(4),
            rsmd_product_table.getColumnName(5),
            rsmd_product_table.getColumnName(6));
    System.out.println(product_table_columns);

    while (rs_product_by_id.next()) {
      String out_product = String.format("%-20d %-20.2f %-20d %-40s %-30s %-20d",
              rs_product_by_id.getInt(1),
              rs_product_by_id.getDouble(2),
              rs_product_by_id.getInt(3),
              rs_product_by_id.getString(4),
              rs_product_by_id.getString(5),
              rs_product_by_id.getInt(6));
      System.out.println(out_product);
      stock = rs_product_by_id.getInt(3);
    }

    int old_product_quantity = Integer.parseInt(cart.get(update_product_id).get(0));

    System.out.print("\nEnter the new quantity: ");

    if (sc.hasNext()) {

      boolean flag = false;

      while (!flag) {
        try {
          updated_product_quantity_input = sc.next();
          update_product_quantity = Integer.parseInt(updated_product_quantity_input);

          while (update_product_quantity < 0) {
            System.out.println("\nQuantity must be >= 0.");
            System.out.print("Please enter quantity: ");

            if (sc.hasNext()) {
              updated_product_quantity_input = sc.next();
              update_product_quantity = Integer.parseInt(updated_product_quantity_input);
            }
          }

          while (update_product_quantity > stock) {
            System.out.println("\nQuantity input is greater than current product stock.");
            System.out.print("Please enter quantity: ");

            if (sc.hasNext()) {
              updated_product_quantity_input = sc.next();
              update_product_quantity = Integer.parseInt(updated_product_quantity_input);
            }
          }

          flag = true;

        } catch (NumberFormatException e) {
          System.out.println("\nYou did not enter a number.");
          System.out.print("Please enter quantity: ");
        }
      }
    }

    double product_price_in_cart = Double.parseDouble(cart.get(update_product_id).get(1));

    if (old_product_quantity > update_product_quantity) {

      LinkedList<String> decrease_quantity_price_list = new LinkedList<>();

      int unwanted_quantity = old_product_quantity - update_product_quantity;

      CallableStatement cs_add_some_stock_back = con.prepareCall(
              "{call putUnwantedProductQuantityBackInStock(?,?)}"
      );

      cs_add_some_stock_back.setInt(1, update_product_id);
      cs_add_some_stock_back.setInt(2, unwanted_quantity);

      cs_add_some_stock_back.executeUpdate();

      decrease_quantity_price_list.add(String.valueOf(update_product_quantity));
      decrease_quantity_price_list.add(String.valueOf(product_price_in_cart));

      cart.put(update_product_id, decrease_quantity_price_list);

      System.out.println("\nSuccessfully decrease product quantity in cart.");

      this.print_cart();
    }
    else if (old_product_quantity < update_product_quantity) {

      LinkedList<String> increase_quantity_price_list = new LinkedList<>();

      int newly_added_quantity = update_product_quantity - old_product_quantity;

      CallableStatement cs_decrease_stock_again = con.prepareCall(
              "{call updateProductStockByIdAfterAddToCart(?,?)}"
      );

      cs_decrease_stock_again.setInt(1, update_product_id);
      cs_decrease_stock_again.setInt(2, newly_added_quantity);

      cs_decrease_stock_again.executeUpdate();

      increase_quantity_price_list.add(String.valueOf(update_product_quantity));
      increase_quantity_price_list.add(String.valueOf(product_price_in_cart));

      cart.put(update_product_id, increase_quantity_price_list);

      System.out.println("\nSuccessfully increase product quantity in cart.");

      this.print_cart();
    }
    else {
      System.out.println("The product quantity remains the same.");
    }

    this.after_update_cart_menu(con, sc);
  }


  /**
   * Print a menu to ask if the customer want to update more products in the
   * shopping cart or not. If yes, print out more menu options for the customer
   * to select a method to update products in cart.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void after_update_cart_menu(Connection con, Scanner sc) throws Exception {

    boolean flag = false;

    String after_update_cart_menu_input = "";

    while (!flag) {

      System.out.print("\nDo you want to update more products in cart? 1 for yes, 0 for no: ");

      if (sc.hasNext()) {
        after_update_cart_menu_input = sc.next();
      }

      switch (after_update_cart_menu_input) {
        case "1":
          update_product_operation_type(con, sc);
          flag = true;
          break;

        case "0":
          flag = true;
          break;

        default:
          System.out.print("\nInvalid input, please re-enter");
      }

    }
  }


  /**
   * Helper method to direct the customer to different methods (delete a product, or
   * update a product's quantity) to update products in the shopping cart.
   * @param con a connection to the database
   * @param sc the scanner to receive user input
   * @throws Exception if any I/O operation in console failed
   */
  public void update_product_operation_type(Connection con, Scanner sc) throws Exception {

    boolean flag = false;

    String update_operation_type = "";

    while (!flag) {
      System.out.print("Do you want to delete a product from cart or update product quantity? "
                        + "1 for delete, 2 for update: ");

      if (sc.hasNext()) {
        update_operation_type = sc.next();
      }

      switch(update_operation_type) {
        case "1":
          this.delete_product_from_cart(con, sc);
          flag = true;
          break;

        case "2":
          this.update_product_quantity(con, sc);
          flag = true;
          break;

        default:
          System.out.println("\nInvalid input, please re-enter");
      }
    }
  }


  /**
   * Helper method to print a formatted product table (customer version) in console.
   * @param rs_product a ResultSet object of all information except warehouse_manager_id
   *                   in the product table
   * @throws SQLException if any SQL operation failed
   */
  public void print_formatted_product_table_customer(ResultSet rs_product) throws SQLException {

    ResultSetMetaData rsmd_product_table = rs_product.getMetaData();

    String product_table_columns = String.format("%-20s %-20s %-20s %-40s %-30s %-20s",
            rsmd_product_table.getColumnName(1),
            rsmd_product_table.getColumnName(2),
            rsmd_product_table.getColumnName(3),
            rsmd_product_table.getColumnName(4),
            rsmd_product_table.getColumnName(5),
            rsmd_product_table.getColumnName(6));
    System.out.println(product_table_columns);

    while (rs_product.next()) {
      String out_product = String.format("%-20d %-20.2f %-20d %-40s %-30s %-20d",
              rs_product.getInt(1),
              rs_product.getDouble(2),
              rs_product.getInt(3),
              rs_product.getString(4),
              rs_product.getString(5),
              rs_product.getInt(6));
      System.out.println(out_product);
    }
  }
}
