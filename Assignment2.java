package assignments;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;
class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
interface Service {
    void request(String username, String password) throws AuthenticationException, IOException;
}
class RealService implements Service {
    @Override
    public void request(String username, String password) {
        System.out.println("Access granted! Performing the service for user: " + username);
    }
}
class ProxyAuthenticator implements Service {
    private RealService realService;
    private String validUser = "admin";
    private String validPassword = "4321";

    @Override
    
    
    
    
    
    
    
    public void request(String username, String password) throws AuthenticationException, IOException {
        if (authenticate(username, password)) {
            if (realService == null) {
                realService = new RealService();
            }
            realService.request(username, password);
            try (FileOutputStream fos = new FileOutputStream("login_log.txt", true);
                 PrintWriter pw = new PrintWriter(fos)) {
                pw.println("User: " + username + " successfully logged in.");
            }
        } else {
            throw new AuthenticationException("Access denied! Invalid username or password.");
        }
    }

    private boolean authenticate(String username, String password) {
        return username.equals(validUser) && password.equals(validPassword);
    }
}
public class Assignment2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Service proxy = new ProxyAuthenticator();
        System.out.println("Janise Deepthi YP - 2117240070124");
        System.out.print("Enter username: ");
        String user = sc.nextLine();

        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        try {
            proxy.request(user, pass);
        } catch (AuthenticationException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("File Error: " + e.getMessage());
        } finally {
            System.out.println("Authentication process completed.");
        }

        sc.close();
    }
}

