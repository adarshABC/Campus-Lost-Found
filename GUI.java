import java.awt.*;
import java.sql.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class GUI {

    static JPanel reportPanel() {
        JPanel p = new JPanel(new GridLayout(7, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField name = new JTextField();
        JTextField item = new JTextField();
        JTextField loc = new JTextField();
        JTextField date = new JTextField();
        JTextField contact = new JTextField();
        JComboBox<String> type = new JComboBox<>(new String[]{"Lost", "Found"});
        JButton save = new JButton("Submit");

        p.add(new JLabel("Your name:"));   p.add(name);
        p.add(new JLabel("Item name:"));   p.add(item);
        p.add(new JLabel("Location:"));    p.add(loc);
        p.add(new JLabel("Date:"));        p.add(date);
        p.add(new JLabel("Contact:"));     p.add(contact);
        p.add(new JLabel("Lost or Found:")); p.add(type);
        p.add(new JLabel(""));             p.add(save);

        save.addActionListener(e -> {
            if (item.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(p, "Item name likho pehle.");
                return;
            }
            String status = (String) type.getSelectedItem();
            Main.saveItem(new Item(item.getText(), loc.getText(),
                    date.getText(), status, name.getText(), contact.getText()));

            // match check
            String opposite = status.equals("Lost") ? "Found" : "Lost";
            StringBuilder sb = new StringBuilder();
            for (Item i : Main.loadItems(item.getText())) {
                if (i.status.equals(opposite)) {
                    sb.append(i.itemName).append(" - ").append(i.location)
                      .append(" - ").append(i.studentName)
                      .append(" (").append(i.contact).append(")\n");
                }
            }
            String msg = "Item reported successfully!";
            if (sb.length() > 0) {
                msg += "\n\nMATCH FOUND! Ye " + opposite + " items milte-julte hain:\n" + sb;
            }
            JOptionPane.showMessageDialog(p, msg);

            name.setText(""); item.setText(""); loc.setText("");
            date.setText(""); contact.setText("");
        });
        return p;
    }

    static JPanel viewPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchBox = new JTextField(15);
        JButton searchBtn = new JButton("Search");
        JButton allBtn = new JButton("Show All");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Item name:"));
        top.add(searchBox);
        top.add(searchBtn);
        top.add(allBtn);

        String[] cols = {"Item", "Status", "Location", "Date", "Reported By", "Contact"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        Runnable showAll = () -> fill(model, Main.loadItems(null));
        searchBtn.addActionListener(e -> {
            String s = searchBox.getText().trim();
            fill(model, s.isEmpty() ? Main.loadItems(null) : Main.loadItems(s));
        });
        allBtn.addActionListener(e -> showAll.run());
        showAll.run();

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    static void fill(DefaultTableModel model, List<Item> items) {
        model.setRowCount(0);
        for (Item i : items) {
            model.addRow(new Object[]{i.itemName, i.status, i.location,
                    i.date, i.studentName, i.contact});
        }
    }

    static void loadClaims(DefaultTableModel model) {
    model.setRowCount(0);
    String sql = "SELECT c.id, i.item_name, c.student_name, c.claim_date, c.status "
               + "FROM claims c JOIN items i ON c.item_id = i.id";
    try (Connection c = DBConnection.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            model.addRow(new Object[]{rs.getInt("id"), rs.getString("item_name"),
                    rs.getString("student_name"), rs.getString("claim_date"),
                    rs.getString("status")});
        }
    } catch (SQLException e) {
        System.out.println("DB error: " + e.getMessage());
    }
}

static void setStatus(JTable table, String status, JPanel p) {
    int row = table.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(p, "Pehle table mein ek claim pe click karo.");
        return;
    }
    int id = (int) table.getValueAt(row, 0);
    try (Connection c = DBConnection.get();
         PreparedStatement ps = c.prepareStatement("UPDATE claims SET status = ? WHERE id = ?")) {
        ps.setString(1, status);
        ps.setInt(2, id);
        ps.executeUpdate();
    } catch (SQLException e) {
        System.out.println("DB error: " + e.getMessage());
    }
    loadClaims((DefaultTableModel) table.getModel());
}

static JPanel claimPanel() {
    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JTextField name = new JTextField(10);
    JTextField item = new JTextField(10);
    JButton claimBtn = new JButton("Claim");
    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(new JLabel("Your name:"));
    top.add(name);
    top.add(new JLabel("Found item:"));
    top.add(item);
    top.add(claimBtn);

    String[] cols = {"ID", "Item", "Claimed By", "Date", "Status"};
    DefaultTableModel model = new DefaultTableModel(cols, 0);
    JTable table = new JTable(model);

    JButton approve = new JButton("Approve");
    JButton reject = new JButton("Reject");
    JButton refresh = new JButton("Refresh");
    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
    bottom.add(approve);
    bottom.add(reject);
    bottom.add(refresh);

    claimBtn.addActionListener(e -> {
        int id = Main.findFoundItemId(item.getText().trim());
        if (id == -1) {
            JOptionPane.showMessageDialog(p, "Is naam ka koi Found item nahi mila.");
            return;
        }
        Main.saveClaim(new Claim(id, name.getText(),
                java.time.LocalDate.now().toString(), "Pending"));
        JOptionPane.showMessageDialog(p, "Claim submitted!");
        name.setText("");
        item.setText("");
        loadClaims(model);
    });
    approve.addActionListener(e -> setStatus(table, "Approved", p));
    reject.addActionListener(e -> setStatus(table, "Rejected", p));
    refresh.addActionListener(e -> loadClaims(model));
    loadClaims(model);

    p.add(top, BorderLayout.NORTH);
    p.add(new JScrollPane(table), BorderLayout.CENTER);
    p.add(bottom, BorderLayout.SOUTH);
    return p;
}

    public static void main(String[] args) {
        JFrame f = new JFrame("Campus Lost & Found");
        f.setSize(750, 450);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Report Item", reportPanel());
        tabs.add("Search / View", viewPanel());
        tabs.add("Claims", claimPanel());
        f.add(tabs);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
