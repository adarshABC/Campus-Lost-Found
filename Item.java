public class Item {

    String itemName;
    String category;
    String description;
    String location;
    String date;
    String status;
    String studentName;
    String contact;

    
    Item(String itemName, String location, String date, String status, String studentName, String contact) {
        this.itemName = itemName;
        this.location = location;
        this.date = date;
        this.status = status;
        this.studentName = studentName;
        this.contact = contact;
    }

    void display() {
       
   
   
    System.out.println("+---------------------------+");
    System.out.println("| Item     : " + itemName);
    System.out.println("| Status   : " + status);
    System.out.println("| Location : " + location);
    System.out.println("| Date     : " + date);
    System.out.println("| Reported : " + studentName + " (" + contact + ")");
    System.out.println("+---------------------------+");
}
}

    



    

