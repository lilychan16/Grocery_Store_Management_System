import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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


  public CustomerOperations(SharedHelperMethods sharedHelperMethods) {
    this.sharedHelperMethods = sharedHelperMethods;
  }


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
          System.out.print("\nPlease enter the product id of the product you'd like to add: ");

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


  public void check_cart_check_out(Connection con, Scanner sc) throws Exception {

    String check_cart_input = "";

    this.print_cart(con, sc);

    boolean flag = false;

    while (!flag) {
      System.out.println("\nPlease select an option:\n1. Check out\n2. Update cart"
              + "\n3. Go back to customer main menu or Quit");

      if (sc.hasNext()) {
        check_cart_input = sc.next();
      }

      switch (check_cart_input) {
        case "1":

        case "2":
          this.update_cart(con, sc);
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


  public void print_cart(Connection con, Scanner sc) {

    String product_quantity;
    String product_price;

    double cart_total = 0;

    System.out.println("\nCart Summary:");

    for (Map.Entry<Integer, LinkedList<String>> c : this.cart.entrySet()) {

      int product_id = c.getKey();

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
  }


  public void update_cart(Connection con, Scanner sc) throws Exception {

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


  public void delete_product_from_cart(Connection con, Scanner sc) throws Exception {

    String delete_product_id_input = "";
    int delete_product_id = 0;
    int unwanted_quantity;

    Set<Integer> product_id_set = cart.keySet();

    this.print_cart(con, sc);

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

    this.print_cart(con, sc);

    this.after_update_cart_menu(con, sc);
  }


  public void update_product_quantity(Connection con, Scanner sc) throws Exception {

    String update_product_id_input = "";
    int update_product_id = 0;

    String updated_product_quantity_input = "";
    int update_product_quantity = 0;

    int stock = 0;

    Set<Integer> product_id_set = cart.keySet();

    this.print_cart(con, sc);

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

      this.print_cart(con, sc);
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

      this.print_cart(con, sc);
    }
    else {
      System.out.println("The product quantity remains the same.");
    }

    this.after_update_cart_menu(con, sc);
  }


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