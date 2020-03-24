import java.io.*; 
import java.net.*; 
import java.util.Scanner; 
  
// Client class 
public class Client  
{ 
    public static void main(String[] args) throws IOException  
    { 
        try
        { 
            String id;

            Scanner scn = new Scanner(System.in); 
              
            // getting localhost ip 
            InetAddress ip = InetAddress.getByName("localhost"); 
      
            // establish the connection with server port 5056 
            Socket s = new Socket(ip, 5056);  
      
            // obtaining input and out streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
      
            // the following loop performs the exchange of 
            // information between client and client handler 
            while (true)  
            { 
                String str = dis.readUTF();
                System.out.print(str); 
                if(str.equals("Request rejected!\nYou can't sign in again today!") ||
                   str.equals("Everyone has been signed in.")){
                    s.close(); 
                    System.out.println("\n\nConnection closed"); 
                    break;
                }

                String input = scn.nextLine(); 
                dos.writeUTF(input); 
                         
                // printing the response from server
                String result = dis.readUTF();
                System.out.println(result + "\n"); 

                if(result.equals("Accept!") || result.equals("You can't sign in again today!")){
                    if(result.equals("Accept!")){
                        System.out.print(dis.readUTF()); // "Please typing your student ID : "
                        id = scn.nextLine(); 
                        dos.writeUTF(id);
                        System.out.println("\n" + dis.readUTF() + "\n");
                    }
                    s.close(); 
                    System.out.println("Connection closed"); 
                    break;      
                }    
            }          
            // closing resources 
            scn.close(); 
            dis.close(); 
            dos.close(); 
        }catch(Exception e){ 
            System.out.println("Teacher closed the server, or everyone has been signed in.");
        }
    } 
} 