package bikram.model;

public class Business {
    private String business_name;
    private String owner;
    private String business_address;
    private double balance;
    private double loss;
    private double profit;
    private double budgect;
    public Business(){}
    public Business(String business_name,String owner,String business_address,double balance){
        this.business_name=business_name;
        this.owner=owner;
        this.business_address=business_address;
        this.balance=balance;
    }
    public Business(String business_name,String owner,String business_address){
        this.business_name=business_name;
        this.owner=owner;
        this.business_address=business_address;
        this.balance=0;
        this.loss=0;
        this.profit=0;
        this.budgect=0;
    }

    public double getBalance() {
        return balance;
    }

    public double getBudgect() {
        return budgect;
    }

    public double getLoss() {
        return loss;
    }

    public String getBusiness_address() {
        return business_address;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public double getProfit() {
        return profit;
    }

    public String getOwner() {
        return owner;
    }

    public void setBalance(double balance) {
        this.balance += balance;
    }

    public void setBusiness_address(String business_address) {
        this.business_address = business_address;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public void setBudgect(String title,double budgect) {
        title=title;
        this.budgect = budgect;
    }

    public void setLoss(double loss) {
        this.loss -= loss;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setProfit(double profit) {
        this.profit += profit;
    }
}
