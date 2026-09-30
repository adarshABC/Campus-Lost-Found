public class Claim {
    int itemId;
    String studentName;
    String claimDate;
    String status;

    Claim(int itemId, String studentName, String claimDate, String status) {
        this.itemId = itemId;
        this.studentName = studentName;
        this.claimDate = claimDate;
        this.status = status;
    }
}