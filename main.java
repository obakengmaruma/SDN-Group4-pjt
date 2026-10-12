import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n=== Console Menu ===");
            System.out.println("1. Encrypt a file");
            System.out.println("2. Decrypt a file");
            System.out.println("3. Generate new key pair");
            
            System.out.println("4. View activity log");
            
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
                System.out.print("Enter your choice: ");
            }

            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Encrypting file...");
                    break;
                case 2:
                    System.out.println("Decrypting file...");
                    break;
                case 3:
                    System.out.println("Generating new key pair...");
                    break;
                case 4:
                    System.out.println("Viewing activity log...");
                    break;
                case 5:
                    System.out.println("Exiting the program...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        } while (choice != 5);

        scanner.close();
    }
}
