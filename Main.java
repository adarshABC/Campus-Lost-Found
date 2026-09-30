import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class Main {

    static void saveItem(Item i) {
        String sql = "INSERT INTO items(item_name, location, report_date, status, student_name, contact) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, i.itemName);
            ps.setString(2, i.location);
            ps.setString(3, i.date);
            ps.setString(4, i.status);
            ps.setString(5, i.studentName);
            ps.setString(6, i.contact);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
        }
    }

    static List<Item> loadItems(String search) {
        List<Item> list = new ArrayList<>();
        String sql = (search == null)
                ? "SELECT * FROM items"
                : "SELECT * FROM items WHERE item_name LIKE ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (search != null) ps.setString(1, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Item(
                    rs.getString("item_name"),
                    rs.getString("location"),
                    rs.getString("report_date"),
                    rs.getString("status"),
                    rs.getString("student_name"),
                    rs.getString("contact")));
            }
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
        }
        return list;
    }

    // Found item ka id dhoondta hai (nahi mila toh -1)
    static int findFoundItemId(String name) {
        String sql = "SELECT id FROM items WHERE item_name LIKE ? AND status = 'Found' LIMIT 1";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
        }
        return -1;
    }

    static void saveClaim(Claim cl) {
        String sql = "INSERT INTO claims(item_id, student_name, claim_date, status) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cl.itemId);
            ps.setString(2, cl.studentName);
            ps.setString(3, cl.claimDate);
            ps.setString(4, cl.status);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
        }
    }

    static void showClaims() {
        String sql = "SELECT c.student_name, c.claim_date, c.status, i.item_name "
                   + "FROM claims c JOIN items i ON c.item_id = i.id";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.println("+---------------------------+");
                System.out.println("| Item     : " + rs.getString("item_name"));
                System.out.println("| Claimed by: " + rs.getString("student_name"));
                System.out.println("| Date     : " + rs.getString("claim_date"));
                System.out.println("| Status   : " + rs.getString("status"));
                System.out.println("+---------------------------+");
            }
            if (!any) System.out.println("No claims yet.");
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
        }
    }

      static void showMatches(String itemName, String status) {
      String opposite = status.equals("Lost") ? "Found" : "Lost";
      String sql = "SELECT * FROM items WHERE item_name LIKE ? AND status = ?";
      try (Connection c = DBConnection.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, "%" + itemName + "%");
        ps.setString(2, opposite);
        ResultSet rs = ps.executeQuery();
        boolean any = false;
        while (rs.next()) {
            if (!any) {
                System.out.println("\n*** MATCH FOUND! Ye " + opposite + " items milte-julte hain: ***");
                any = true;
            }
            new Item(
                rs.getString("item_name"),
                rs.getString("location"),
                rs.getString("report_date"),
                rs.getString("status"),
                rs.getString("student_name"),
                rs.getString("contact")).display();
        }
    }   catch (SQLException e) {
        System.out.println("DB error: " + e.getMessage());
    }
}

    static void manageClaims(Scanner sc) {
    String sql = "SELECT c.id, c.student_name, c.claim_date, i.item_name "
               + "FROM claims c JOIN items i ON c.item_id = i.id "
               + "WHERE c.status = 'Pending'";
    try (Connection c = DBConnection.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ResultSet rs = ps.executeQuery();
        boolean any = false;
        while (rs.next()) {
            any = true;
            System.out.println("Claim ID: " + rs.getInt("id")
                + " | Item: " + rs.getString("item_name")
                + " | By: " + rs.getString("student_name")
                + " | Date: " + rs.getString("claim_date"));
        }
        if (!any) {
            System.out.println("No pending claims.");
            return;
        }
    } catch (SQLException e) {
        System.out.println("DB error: " + e.getMessage());
        return;
    }

    System.out.print("Enter Claim ID: ");
    int id = sc.nextInt();
    sc.nextLine();
    System.out.print("Type A to Approve or R to Reject: ");
    String d = sc.nextLine();
    String newStatus = d.equalsIgnoreCase("A") ? "Approved" : "Rejected";

    String up = "UPDATE claims SET status = ? WHERE id = ?";
    try (Connection c = DBConnection.get();
         PreparedStatement ps = c.prepareStatement(up)) {
        ps.setString(1, newStatus);
        ps.setInt(2, id);
        int rows = ps.executeUpdate();
        System.out.println(rows > 0 ? "Claim " + newStatus + "!" : "Claim ID not found.");
    } catch (SQLException e) {
        System.out.println("DB error: " + e.getMessage());
    }
}
     
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Student student = new Student("Adarsh", "adarsh@gmail.com", "6266930046");
        boolean running = true;

        while (running) {
            System.out.println("\n+--------------------------------------+");
            System.out.println("|     CAMPUS LOST & FOUND SYSTEM        |");
            System.out.println("+--------------------------------------+");
            System.out.println("|  [1] Report Lost Item                 |");
            System.out.println("|  [2] Report Found Item                |");
            System.out.println("|  [3] Search Item                      |");
            System.out.println("|  [4] View All Items                   |");
     
            System.out.println("|  [5] Claim Item                       |");
            System.out.println("|  [6] View Claims                      |");
            System.out.println("|  [7] Approve/Reject Claim             |");
            System.out.println("|  [8] Exit                             |");
            System.out.println("+--------------------------------------+");
            System.out.print(">> Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1 || choice == 2) {
                System.out.print("Enter your name: ");
                String name = sc.nextLine();
                System.out.print("Enter item name: ");
                String itemName = sc.nextLine();
                System.out.print("Enter location: ");
                String location = sc.nextLine();
                System.out.print("Enter date: ");
                String date = sc.nextLine();
                System.out.print("Enter contact: ");
                String contact = sc.nextLine();

                String status = (choice == 1) ? "Lost" : "Found";
                saveItem(new Item(itemName, location, date, status, name, contact));
                System.out.println("Item reported successfully!");
                showMatches(itemName, status);

            } else if (choice == 3) {
                System.out.print("Enter item name to search: ");
                String search = sc.nextLine();
                List<Item> result = loadItems(search);
                if (result.isEmpty()) {
                    System.out.println("No matching item found.");
                } else {
                    for (Item i : result) i.display();
                }

            } else if (choice == 4) {
                List<Item> all = loadItems(null);
                if (all.isEmpty()) {
                    System.out.println("No items reported yet.");
                } else {
                    for (Item i : all) i.display();
                }

            } else if (choice == 5) {
                System.out.print("Enter your name: ");
                String name = sc.nextLine();
                System.out.print("Which found item is yours? (item name): ");
                String itemName = sc.nextLine();
                int id = findFoundItemId(itemName);
                if (id == -1) {
                    System.out.println("No found item with that name.");
                } else {
                    saveClaim(new Claim(id, name, LocalDate.now().toString(), "Pending"));
                    System.out.println("Claim submitted!");
                }

            } else if (choice == 6) {
                showClaims();

            } else if (choice == 7) {
               manageClaims(sc);

            } else if (choice == 8) {
                    System.out.println("Thank you for using Campus Lost & Found System!");
                    running = false;
            } else {
                System.out.println("Invalid choice, try again.");
            }
        }
    }
}