import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;

  
// Server class 
public class Server  
{ 
    public static void main(String[] args) throws IOException  
    { 
        // server is listening on port 5056 
        ServerSocket ss = new ServerSocket(5056); 

        Vector<String> list = new Vector<String>(); 
        Vector<Integer> password = new Vector<Integer>();
        Vector<InetAddress> address = new Vector<InetAddress>(); // address that can't connect again
        Random ran = new Random();
        

        // read list.txt(student list) and write into list
        FileReader fr = new FileReader("list.txt");
        BufferedReader br = new BufferedReader(fr);
        while (br.ready()) {
            list.add(br.readLine());
        }
        fr.close();

        //list.remove(list.size()-1);

        int num = list.size(); // get the student number

        // produce the password depending on student number and store into password
        FileWriter fw = new FileWriter("password.txt");
        for(int i=0 ; i<num ; i++){
            password.addElement(new Integer(ran.nextInt(10000)+1));
            String stringValue = Integer.toString(password.get(i));
            fw.write(stringValue+"\n");
            fw.flush();
        }
        fw.close();

        Object obj = password.get(0);
        Object ob1 = password.get(1);
        System.out.println("Two of the password : " + obj + " " + ob1);

        System.out.println("\nPlease waiting for connecting form srudents...\n");
          
        // running infinite loop for getting request 
        while (true)  
        { 
            Socket s = null; 
              
            try 
            { 
                // socket object to receive incoming student requests 
                s = ss.accept();   
                //if(list.size() != 0)           
                System.out.println("A student is connected : " + s + "\n");      

                // obtaining input and out streams 
                DataInputStream dis = new DataInputStream(s.getInputStream()); 
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());  

                if(address.contains(s.getInetAddress())){
                    dos.writeUTF("Request rejected!\nYou can't sign in again today!");
                    System.out.println("The student typed wrong password for three times!\n");
                    System.out.println("Connection closed\n");   
                    continue;
                }

                if(list.size() == 0){
                    dos.writeUTF("Everyone has been signed in.");
                    System.out.println("Close this server."); 
                    break; 
                }
  
                // create a new thread object 
                Thread t = new ClientHandler(s, dis, dos, password, address, list); 
  
                // Invoking the start() method 
                t.start(); 
            } 
            catch (Exception e){ 
                s.close(); 
                e.printStackTrace(); 
            } 
        } 
    } 
} 
  
// ClientHandler class 
class ClientHandler extends Thread  
{  
    final DataInputStream dis; 
    final DataOutputStream dos; 
    final Vector password;
    final Vector address;
    final Vector list;
    final Socket s; 
    Boolean allin;
      
  
    // Constructor 
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos,
                         Vector<Integer> password, Vector<InetAddress> address, Vector<String> list)  
    { 
        this.s = s; 
        this.dis = dis; 
        this.dos = dos; 
        this.password = password;
        this.address = address;
        this.list = list;
    } 
  
    @Override
    public void run()  
    { 
        String received; 
        String toreturn; 
        String id;
        int count=3;
        Boolean correct = false;

        while (true)  
        { 
            try { 
                // Ask student for the password
                dos.writeUTF("Please typing the password : "); 
                  
                // receive the answer from client, judge it and giving the response 
                received = dis.readUTF(); 

                int intValue = Integer.valueOf(received);

                if(password.contains(new Integer(intValue))){
                    correct = true;
                    dos.writeUTF("Accept!"); 
                    dos.writeUTF("Please typing your student ID : ");
                    id = dis.readUTF(); // get student's id
                    Boolean bool = list.remove((String)id); // delete the student's id on the list
                    if(bool){
                        Boolean boo = password.remove((Integer)intValue); // delete the used password  
                        dos.writeUTF("Successfully signed in!");
                        System.out.println("Student " + id + " is here.\n");
                    }
                    else{
                        // enter wrong Student ID
                        dos.writeUTF("Failed to signed in!\nPlease check your Student ID and enter the password again.");
                        System.out.println("Student isn't on the list.");
                    }

                    this.s.close();                     
                    System.out.println("Connection closed\n"); 

                    if(list.size() == 0)
                        System.out.println("Everyone has been signed in.\n"); 

                    FileWriter fw = new FileWriter("absentee.txt");                 
                    if(list.size() == 0){
                        fw.write("Everyone is present today!!!\n");
                        fw.flush();
                        fw.close();
                        break;
                    }
                    fw.write("Absentee:\n");
                    for(int j=0 ; j<list.size() ; j++){
                        String str = list.get(j).toString();
                        fw.write(str+"\n");
                        fw.flush();
                    }
                    fw.close();   
                    break;
                }
                else{
                    if(count > 1)
                        dos.writeUTF("You have " + --count + " chances left."); 
                    else if(--count == 0) {
                        dos.writeUTF("You can't sign in again today!"); 
                        System.out.println("The student typing wrong password for three times!\n"); 
                        InetAddress a = s.getInetAddress();
                        address.addElement(a);
                    }
                }   

                // close the connection and write the absentee.txt
                if(correct || (count == 0 && !correct)){ 
                    this.s.close();                     
                    System.out.println("Connection closed\n"); 
                    FileWriter fw = new FileWriter("absentee.txt");                 
                    if(list.size() == 0){
                        fw.write("Everyone is present today!!!\n");
                        fw.flush();
                        fw.close();
                        break;
                    }
                    fw.write("Absentee:\n");
                    for(int j=0 ; j<list.size() ; j++){
                        String str = list.get(j).toString();
                        fw.write(str+"\n");
                        fw.flush();
                    }
                    fw.close();   
                    break;
                }

            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
        } 
       
        try
        { 
            // closing resources 
            this.dis.close(); 
            this.dos.close(); 
              
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
} 