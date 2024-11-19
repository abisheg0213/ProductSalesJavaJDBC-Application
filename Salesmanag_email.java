
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

class Utlities{
    public void generate_invoice_file(int id,String type,String invoice_content){

        String name;
        if(type=="Invoice"){
            name="inv_"+id+".txt";
        }else {
            name="payinv_"+id+".txt";
        }
        try{
        FileOutputStream fout=new FileOutputStream(name);
        fout.write(invoice_content.getBytes());
        }catch (Exception e){}
    }
    public void sendEmail(String recipient, String subject, String messageText) {
        final String username = "jayasankarastores@gmail.com";
        final String password = "kefp xwbe hnmq zisk";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);

            System.out.println("Invoice Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
class SalesManagement{
    static Connection con;
    static int customers_count=0;
    static int product_count=5000;
    static int sales_id=10000;
    static int payid=15400;
    static Utlities util;
    SalesManagement(){
        util=new Utlities();
        try{
           con=DriverManager.getConnection("jdbc:mysql://localhost:3306/sales_manag", "root", "");
        }catch (Exception e){
            System.out.println(e);
        }
    }
    public void add_customer(String cus_name,long phone_no,String add,String email){
        try{
       Statement s= con.createStatement();
       String insert_query="INSERT INTO CUSTOMER VALUES ("+(customers_count+1200)+",'"+cus_name+"',"+phone_no+",'"+add+"',0.000,'"+email+"');";
       customers_count+=1;
       System.out.println(insert_query);
       s.execute(insert_query);
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    public void add_product(String prod_name,int quantity,double cost,String manf){
        try{
            Statement s= con.createStatement();
            String insert_query="INSERT INTO products VALUES ("+product_count+","+quantity+",'"+manf+"',"+cost+",0,'"+prod_name+"');";
            product_count+=1;
            System.out.println(insert_query);
            s.execute(insert_query);
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    public void makes_sales(int cusid, HashMap<Integer,Integer> prod_quan){
        final double[] total_amount = {0};
        prod_quan.forEach((key,value)->{
            try{
                Statement s= con.createStatement();
                String details="Select quan,cost from products where pid="+key+";";
                System.out.println(details);
                ResultSet rs=s.executeQuery(details);
                while(rs.next()){
                    int i1=rs.getInt("quan");
                    double cs=rs.getDouble("cost");
                    total_amount[0]+=(cs*value);
                    String update_query="update products set quan="+(i1-value)+" where pid="+key+";";
                    s.execute(update_query);
                    System.out.println(update_query);
                }

            }
            catch(Exception e){
                System.out.println(e);
            }
        } );
        prod_quan.forEach((key,val)->{
            try{
                Statement s=con.createStatement();
                String insert_query="INSERT INTO sales_inventory VALUES ("+key+","+sales_id+","+val+")";
                System.out.println(insert_query);
                s.execute(insert_query);
            }catch (Exception e){}
        });
        try {
            String st1="INSERT INTO SALES (sales_id,sales_date,cusid,amount) VALUES ("+sales_id+",'"+new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"',"+cusid+","+total_amount[0]+")";
            Statement fd=con.createStatement();
            fd.execute(st1);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(total_amount[0]);

    try{
        String upadte_q="UPDATE customer set total_points = total_points+"+((1.2/100)*total_amount[0])+" where cusid= "+cusid;
        System.out.println(upadte_q);
        Statement upd=con.createStatement();
        upd.execute(upadte_q);
    }catch (Exception e){
        System.out.println(e);
    }
        generate_invoice(sales_id);
        sales_id+=1;
    }
    public void make_payment(int sales_id,int cusid,String mode){
        final int[] data={0};
        try{
            Statement s= con.createStatement();
            String details="Select cusid from sales where sales_id="+sales_id+";";
            System.out.println(details);
            ResultSet rs=s.executeQuery(details);
            while (rs.next()){
                data[0]=rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        try{
            Statement s= con.createStatement();
            String insert_query="INSERT INTO payment VALUES ("+payid+",'"+new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"','"+mode+"',"+data[0]+","+sales_id+");";

            System.out.println(insert_query);
            s.execute(insert_query);
            String update_q="UPDATE SALES SET PAY_ID="+payid+" WHERE SALES_ID="+sales_id;
            System.out.println(update_q);
            s.execute(update_q);
            payid+=1;
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    public void update_quantity(){}
    public void generate_invoice(int sales_id){
        final double[] total={0};
        final int[] jop={0};
        final int[] cus_id={0};
        String [] emails={""};
        try {
            Statement inv= con.createStatement();
            String inv_query="SELECT * FROM SALES where sales_id="+sales_id;

            ResultSet rs=inv.executeQuery(inv_query);
            String message="**********Sales Invoice - JayaSankara Stores**********\n";
            while (rs.next()){
                message+=("Sales id: "+rs.getInt("sales_id")+"\n");
                message+=("Customer id: "+rs.getInt("cusid")+"\n");
                cus_id[0]=rs.getInt("cusid");
                message+=("Sales Date: "+rs.getString("sales_date")+"\n");
                total[0]=rs.getDouble("amount");
                jop[0]=rs.getInt("pay_id");
            }
            String cus_query="SELECT * FROM CUSTOMER WHERE CUSID="+cus_id[0];
            ResultSet cus_dea=inv.executeQuery(cus_query);
            message+="************Customer Details***********"+"\n";
            while (cus_dea.next()){
                message+="Customer Name: "+cus_dea.getString("cusname")+"\n";
                message+="Phone Number: "+cus_dea.getLong("phone_no")+"\n";
                message+="Address: "+cus_dea.getString("address")+"\n";
                message+="Email Address: "+cus_dea.getString("email")+"\n";
                message+="Points Earned in this sales: "+((1.2/100)*total[0])+"\n";
                message+="Total Points Earned: "+cus_dea.getDouble("total_points")+"\n";
                emails[0]=cus_dea.getString("email");
            }
            message+="*********Products Catlog********\nProduct ID    Quantity\n";

            String inven="SELECT * FROM sales_inventory where sales_id="+sales_id;
            ResultSet rs1=inv.executeQuery(inven);
            while (rs1.next()){
                message+=rs1.getInt("pid")+"  "+rs1.getInt("quan")+"\n";
            }
            message+="*************************************************************\nThe Total amount of the invoice is "+total[0]+"\n";
            if(jop[0]==0){
                message+="********** Payment Yet to be Done **********\n \n Please make the Payment at the earliest";
            }
            message+="\n\n Contact us at jayasankarastores@gmail.com for any clarification regarding the Invoice";
            util.sendEmail(emails[0],"Sales Invoice for Sales ID "+sales_id+" - JayaSankara Stores",message );
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public void generate_payment_invoice(int sales_id,int payment_id){
        final double[] total={0};
        final int[] jop={0};
        final int[] cus_id={0};
        String [] emails={""};
        try {
            Statement inv= con.createStatement();
            String inv_query="SELECT * FROM SALES where sales_id="+sales_id;

            ResultSet rs=inv.executeQuery(inv_query);
            String message="**********Sales Invoice - JayaSankara Stores**********\n";
            while (rs.next()){
                message+=("Sales id: "+rs.getInt("sales_id")+"\n");
                message+=("Customer id: "+rs.getInt("cusid")+"\n");
                cus_id[0]=rs.getInt("cusid");
                message+=("Sales Date: "+rs.getString("sales_date")+"\n");
                total[0]=rs.getDouble("amount");
                jop[0]=rs.getInt("pay_id");
            }
            String cus_query="SELECT * FROM CUSTOMER WHERE CUSID="+cus_id[0];
            ResultSet cus_dea=inv.executeQuery(cus_query);
            message+="************Customer Details***********"+"\n";
            while (cus_dea.next()){
                message+="Customer Name: "+cus_dea.getString("cusname")+"\n";
                message+="Phone Number: "+cus_dea.getLong("phone_no")+"\n";
                message+="Address: "+cus_dea.getString("address")+"\n";
                message+="Email Address: "+cus_dea.getString("email")+"\n";
                message+="Points Earned in this sales: "+((1.2/100)*total[0])+"\n";
                message+="Total Points Earned: "+cus_dea.getDouble("total_points")+"\n";
                emails[0]=cus_dea.getString("email");
            }
            message+="*********Products Catlog********\nProduct ID    Quantity\n";

            String inven="SELECT * FROM sales_inventory where sales_id="+sales_id;
            ResultSet rs1=inv.executeQuery(inven);
            while (rs1.next()){
                message+=rs1.getInt("pid")+"  "+rs1.getInt("quan")+"\n";
            }
            message+="*************************************************************\nThe Total amount of the invoice is "+total[0]+"\n";
            message+="*******Payment Details*******"+"\n";
            String pay_q="select * from payment where payment_id= "+payment_id;
            try{
                Statement pay_st=con.createStatement();
                ResultSet rsd=pay_st.executeQuery(pay_q);
                while (rsd.next()){
                    message+="Payment ID: "+rsd.getInt("payment_id")+"\n";
                    message+="Payment Date: "+rsd.getString("date_of_payment")+"\n";
                    message+="Payment Mode: "+rsd.getString(  "mode")+"\n";
                }
            }catch(Exception e){}
            message+="\n\n Contact us at jayasankarastores@gmail.com for any clarification regarding the Payment Invoice";
            util.sendEmail(emails[0],"Payment Invoice for Sales ID "+sales_id+" - JayaSankara Stores",message );
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
public class Main {
    public static void main(String[] args) {
    SalesManagement s=new SalesManagement();
    s.generate_payment_invoice(10000,15400);
    }
}
