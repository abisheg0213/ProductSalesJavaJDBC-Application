import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Scanner;
import java.util.*;
class QuantitymismatchException extends Exception{
    public String toString(){
        return "The required quantity of product is not available in the Store \n We sincerely regret for the same";
    }
}
class Productmanagement {
    Connection con;

    Productmanagement() {
        try {
            this.con = DriverManager.getConnection("jdbc:mysql://localhost:3306/salesdb", "root", "");
            System.out.println(this.con);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void check_availablity(int y){
        try{
            String select_query="SELECT * FROM product WHERE prod_id="+y+";";
            Statement st1=this.con.createStatement();
            ResultSet rs=st1.executeQuery(select_query);
            while(rs.next()){
                System.out.println(rs.getInt("prod_id")+"  -  "+rs.getString("pname")+"  -  "+rs.getInt("quantity"));
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public void get_totalday_income(){
        try{
            String select_query="SELECT SUM(AMOUNT) FROM sales_data WHERE sales_date='"+new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"';";
            Statement st1=this.con.createStatement();
            ResultSet rs=st1.executeQuery(select_query);
            while(rs.next()){
                System.out.println("Total Sales amount for the day till now is "+rs.getDouble(1));
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public void add_new_stock(int prod_id,int quan){
        try{
            String update_query="UPDATE product set quantity=quantity+"+quan+" WHERE prod_id="+prod_id+";";
            System.out.println(update_query);
            Statement st1=this.con.createStatement();
            st1.execute(update_query);

        }catch(Exception e){
            System.out.println(e);
        }
    }
    public void show_customer_sales(int cus_id){
        try{
            String select_query="SELECT * FROM sales_data WHERE cus_id="+cus_id+";";
            Statement st1=this.con.createStatement();
            ResultSet rs=st1.executeQuery(select_query);
            System.out.println("Sales ID | Sales Date | Amount | Payment Status");
            while(rs.next()){
                System.out.println(rs.getInt("sales_id")+"|"+rs.getString("sales_date")+"|"+rs.getDouble("amount")+"|"+rs.getString("payment_status"));
            }}catch(Exception e){
            System.out.println(e);
        }
    }
    public void add_new_product(int pid, String name, int quan, String manf, double cost) {
        try {
            String insert_query = "INSERT INTO product VALUES(" + pid + ",'" + name + "'," + quan + ",'" + manf + "'," + cost + ")";
            System.out.println(insert_query);
            Statement st2 = this.con.createStatement();
            st2.execute(insert_query);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void add_new_customer(int cusid, String name, String address, int phone_no) {
        try {
            String insert_query = "INSERT INTO customer_data VALUES(" + cusid + ",'" + name + "','" + address + "'," +phone_no+")";
            System.out.println(insert_query);
            Statement st2 = this.con.createStatement();
            st2.execute(insert_query);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void new_sales(int sid,HashMap<Integer,Integer> prod_list,int cus_id){
        HashMap<Double,Integer> amount_cal=new HashMap<>();
        final double[] total_amount = {0};
        prod_list.forEach((key,value)->{
            try{
                String select_query="SELECT cost,quantity FROM product WHERE prod_id="+key+";";
                Statement st1=this.con.createStatement();
                ResultSet rs=st1.executeQuery(select_query);
                while(rs.next()){
                    amount_cal.put(rs.getDouble(1),value);
                    total_amount[0] = total_amount[0] +(rs.getDouble(1)*value);
                    String upadte_query="UPDATE PRODUCT SET QUANTITY="+(rs.getInt(2)-value)+" WHERE PROD_ID="+ key;
                    System.out.println(upadte_query);
                    Statement stw=this.con.createStatement();
                    stw.execute(upadte_query);
                }
            }catch(Exception e){
                System.out.println(e);
            }
        });
        System.out.println(total_amount[0]);
        try {
            String insert_query = "INSERT INTO sales_data VALUES(" + sid + ",'"+new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"'," +total_amount[0]+",'Not completed'," + cus_id + ")";
            System.out.println(insert_query);
            Statement st2 = this.con.createStatement();
            st2.execute(insert_query);
        } catch (Exception e) {
            System.out.println(e);
        }
        prod_list.forEach((key,value)->{
            try {
                String insert_query = "INSERT INTO sales_inventory VALUES(" + sid+","+key+","+value+ ")";
                System.out.println(insert_query);
                Statement st2 = this.con.createStatement();
                st2.execute(insert_query);
            } catch (Exception e) {
                System.out.println(e);
            }
        });
    }
    public void update_payment_status(int sid){
        try {
            String update_str = "UPDATE SALES_DATA SET PAYMENT_STATUS='Completed' WHERE SALES_ID=" + sid;
            Statement sdf = this.con.createStatement();
            sdf.execute(update_str);
        }catch (Exception e){
            System.out.println(e);
        }
    }
    public void generate_invoice(int sid){
        try {
            String sales_q="SELECT * FROM SALES_DATA WHERE SALES_ID="+sid;
            Statement stm_1=this.con.createStatement();
            ResultSet rs1=stm_1.executeQuery(sales_q);
            FileWriter fw=new FileWriter("Sales_Invoice_"+sid+".txt");
            while(rs1.next()){
                System.out.printf("Sales Invoice Number : %d\n Sales Date : %s\n Amount: %f \n Payment Status: %s \n Customer ID: %d\n",rs1.getInt("sales_id"),rs1.getString("sales_date"),rs1.getDouble("amount"),rs1.getString("payment_status"),rs1.getInt("cus_id"));
                fw.write(String.format("Sales Invoice Number : %d\n Sales Date : %s\n Amount: %f \n Payment Status: %s \n Customer ID: %d\n",rs1.getInt("sales_id"),rs1.getString("sales_date"),rs1.getDouble("amount"),rs1.getString("payment_status"),rs1.getInt("cus_id")));
            }
            System.out.println("******************Products*******************");
            fw.write("******************Products*******************");
            System.out.println("Product id | Quantity");
            fw.write("\nProduct id | Quantity");
            String sales_prod="SELECT * FROM SALES_INVENTORY WHERE SALES_ID="+sid;
            Statement stm_2=this.con.createStatement();
            ResultSet rs2=stm_2.executeQuery(sales_prod);
            while(rs2.next()){
                System.out.println(rs2.getInt("pid")+"|"+rs2.getInt("quantity"));
                fw.write("\n"+rs2.getInt("pid")+"|"+rs2.getInt("quantity"));
            }
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void show_invoice(int sid){
        try {
            String sales_q="SELECT * FROM SALES_DATA WHERE SALES_ID="+sid;
            Statement stm_1=this.con.createStatement();
            ResultSet rs1=stm_1.executeQuery(sales_q);
            while(rs1.next()){
                System.out.printf("Sales Invoice Number : %d\n Sales Date : %s\n Amount: %f \n Payment Status: %s \n Customer ID: %d\n",rs1.getInt("sales_id"),rs1.getString("sales_date"),rs1.getDouble("amount"),rs1.getString("payment_status"),rs1.getInt("cus_id"));
            }
            System.out.println("******************Products*******************");
            System.out.println("Product id | Quantity");
            String sales_prod="SELECT * FROM SALES_INVENTORY WHERE SALES_ID="+sid;
            Statement stm_2=this.con.createStatement();
            ResultSet rs2=stm_2.executeQuery(sales_prod);
            while(rs2.next()){
                System.out.println(rs2.getInt("pid")+"|"+rs2.getInt("quantity"));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
public class SalesManagment {
    public static void main(String[] args) throws Exception {
        Productmanagement pmg = new Productmanagement();
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/salesdb", "root", "");
        while(true){
            System.out.println("Enter 1 to add New product \n 2 to add new customer \n 3 to add new sales data \n 4 to display sales Invoice \n 5 to view customer sales details \n 6 to update payment status for sales \n 7 to check product availability \n 8 to add new quantity of the stock \n 9 to view today's sales revenue");
            Scanner scan = new Scanner(System.in);
            int choice = Integer.parseInt(scan.nextLine());
            if (choice == 1) {
                System.out.println("data added");
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter the product id");
                int pid = Integer.parseInt(sc.nextLine());
                System.out.println("Enter the product name");
                String pname = sc.nextLine();
                System.out.println("Enter the product quantity");
                int quan = Integer.parseInt(sc.nextLine());
                System.out.println("Enter the product manufacture");
                String pmanf = sc.nextLine();
                System.out.println("Enter the product price");
                double price = Double.parseDouble(sc.nextLine());
                pmg.add_new_product(pid, pname, quan, pmanf, price);
            } else if (choice == 2) {
                System.out.println("customer data added");
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter the Customer id");
                int cid = Integer.parseInt(sc.nextLine());
                System.out.println("Enter the Customer name");
                String cusname = sc.nextLine();
                System.out.println("Enter the Customer address");
                String address = sc.nextLine();
                System.out.println("Enter the Customer phone");
                int phone = Integer.parseInt(sc.nextLine());
                pmg.add_new_customer(cid, cusname, address, phone);
            } else if (choice == 3) {
                int y = 0;
                HashMap<Integer, Integer> product_quantity = new HashMap<>();
                while (y != -1) {
                    System.out.println("Enter the Product id");
                    y = Integer.parseInt(scan.nextLine());
                    System.out.println("Enter the product quantity");
                    int quan = Integer.parseInt(scan.nextLine());
                    String sales_q = "SELECT QUANTITY FROM PRODUCT WHERE PROD_ID=" + y;
                    Statement stm_1 = con.createStatement();
                    ResultSet rs1 = stm_1.executeQuery(sales_q);// exception for edge case to be added
                    int ty = 0;
                    while (rs1.next()) {
                        ty = rs1.getInt(1);
                    }
                    if (quan > ty) {
                        try {
                            throw new QuantitymismatchException();
                        } catch (QuantitymismatchException e) {
                            System.out.println(e);
                        }
                    }
                    if (y != -1) {
                        product_quantity.put(y, quan);
                    }
                }
                Random r = new Random();
                int sales_id = r.nextInt(10000);
                System.out.println("Enter Customer ID");
                int hy=Integer.parseInt(scan.nextLine());
                pmg.new_sales(sales_id, product_quantity, hy);
                pmg.generate_invoice(sales_id);
            } else if (choice == 4) {
                System.out.println("Enter the Sales ID");
                int sid = Integer.parseInt(scan.nextLine());
                pmg.show_invoice(sid);
            }
            else if(choice==5){
                System.out.println("Enter the Customer ID");
                int jus = Integer.parseInt(scan.nextLine());
                pmg.show_customer_sales(jus);
            }
            else if(choice==6){
                System.out.println("Enter the Sales ID");
                int jus = Integer.parseInt(scan.nextLine());
                pmg.update_payment_status(jus);
            }
            else if(choice==7){
                System.out.println("Enter the product id for availability");
                int jus = Integer.parseInt(scan.nextLine());
                pmg.check_availablity(jus);
            }
            else if(choice==8){
                System.out.println("Enter the product id");
                int jus = Integer.parseInt(scan.nextLine());
                System.out.println("Enter the new added quantity");
                int jus1 = Integer.parseInt(scan.nextLine());
                pmg.add_new_stock(jus,jus1);
            }
            else if(choice==9){
               pmg.get_totalday_income();
            }
            else {
                break;
            }
        }
    }}
