import java.time.LocalDateTime;
import java.util.UUID;


class Money {
    private final long amountInPaise;
    private final String currency;

    public Money(long a, String c) {
        if (a < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (c == null || c.isBlank()) {
            throw new IllegalArgumentException("Invalid currency");
        }

        this.amountInPaise = a;
        this.currency = c;
    }

    public Money add(Money other) {
        checkCurrency(other);
        return new Money(
            Math.addExact(amountInPaise, other.amountInPaise),
            currency
        );
    }

    public Money subtract(Money other) {
        checkCurrency(other);
        return new Money(
            Math.subtractExact(amountInPaise, other.amountInPaise),
            currency
        );
    }

    public boolean isPositive() {
        return amountInPaise > 0;
    }

    private void checkCurrency(Money other) {
        if (other == null || !currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }

    public long getAmountInPaise() {
        return amountInPaise;
    }

    public String getCurrency() {
        return currency;
    }

    public String toString() {
        return currency + " " + (amountInPaise / 100.0);
    }
}



enum AccountStatus {
    ACTIVE, FROZEN, CLOSED
}



class BankAccount {
    private final String accountNumber;
    private Money balance;
    private final Money overdraftLimit;
    private AccountStatus status;

    public BankAccount(String accountNumber, Money overdraftLimit) {
        this.accountNumber = accountNumber;
        this.overdraftLimit = overdraftLimit;
        this.balance = new Money(0, overdraftLimit.getCurrency());
        this.status = AccountStatus.ACTIVE;
    }

    public void deposit(Money amount) {
        if (status != AccountStatus.ACTIVE)
            throw new IllegalStateException("Account is not active");

        if (!amount.isPositive())
            throw new IllegalArgumentException("Invalid deposit");

        balance = balance.add(amount);
        System.out.println("Deposited: " + amount);
    }

    public void withdraw(Money amount) {
        if (status != AccountStatus.ACTIVE)
            throw new IllegalStateException("Account is not active");

        if (!amount.isPositive())
            throw new IllegalArgumentException("Invalid withdrawal");

        Money newBalance = balance.subtract(amount);

        if (newBalance.getAmountInPaise() < -overdraftLimit.getAmountInPaise())
            throw new IllegalArgumentException("Overdraft limit exceeded");

        balance = newBalance;
        System.out.println("Withdrawn: " + amount);
    }

    public void freeze() {
        if (status != AccountStatus.ACTIVE)
            throw new IllegalStateException("Account is not active");

        status = AccountStatus.FROZEN;
    }

    public void unfreeze() {
        if (status != AccountStatus.FROZEN)
            throw new IllegalStateException("Account is not frozen");

        status = AccountStatus.ACTIVE;
    }

    public void close() {
        if (status != AccountStatus.ACTIVE || balance.getAmountInPaise() != 0)
            throw new IllegalStateException("Cannot close account");

        status = AccountStatus.CLOSED;
    }

    public Money getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountStatus getStatus() {
        return status;
    }
}



class LedgerEntry {
    private final String transactionId;
    private final String transactionType;
    private final Money amount;
    private final String timestamp;

    public LedgerEntry(String type, Money amount) {
        this.transactionId = UUID.randomUUID().toString();
        this.transactionType = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Money getAmount() {
        return amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void display() {
        System.out.println("Transaction ID: " + transactionId);
        System.out.println("Type: " + transactionType);
        System.out.println("Amount: " + amount);
        System.out.println("Timestamp: " + timestamp);
    }
}



class FundTransfer {
    private final String transferId;
    private final BankAccount source;
    private final BankAccount destination;
    private final Money amount;

    public FundTransfer(BankAccount source, BankAccount destination, Money amount) {
        this.transferId = UUID.randomUUID().toString();
        this.source = source;
        this.destination = destination;
        this.amount = amount;
    }

    public void transfer() {
        if (source == null || destination == null || amount == null)
            throw new IllegalArgumentException("Invalid transfer details");

        if (source == destination)
            throw new IllegalArgumentException("Cannot transfer to same account");

        if (!amount.isPositive())
            throw new IllegalArgumentException("Invalid transfer amount");

        if (source.getStatus() != AccountStatus.ACTIVE ||
            destination.getStatus() != AccountStatus.ACTIVE)
            throw new IllegalStateException("Both accounts must be active");

        if (!source.getBalance().getCurrency().equals(amount.getCurrency()) ||
            !destination.getBalance().getCurrency().equals(amount.getCurrency()))
            throw new IllegalArgumentException("Currency mismatch");

        source.withdraw(amount);
        destination.deposit(amount);

        System.out.println("Transfer successful!");
        System.out.println("Transfer ID: " + transferId);
    }
}



public class Bank {
    public static void main(String[] args) {

        Money overdraft = new Money(500000, "INR");

        BankAccount account1 = new BankAccount("1001", overdraft);
        BankAccount account2 = new BankAccount("1002", overdraft);

        account1.deposit(new Money(1000000, "INR"));
        account2.deposit(new Money(500000, "INR"));

        account1.withdraw(new Money(200000, "INR"));

        FundTransfer transfer = new FundTransfer(
            account1,
            account2,
            new Money(300000, "INR")
        );

        transfer.transfer();

        System.out.println("\n----- ACCOUNT DETAILS -----");
        System.out.println("Account 1: " + account1.getBalance());
        System.out.println("Account 2: " + account2.getBalance());

        account1.freeze();
        account1.unfreeze();

        System.out.println("Account Status: " + account1.getStatus());

        LedgerEntry entry = new LedgerEntry(
            "DEPOSIT",
            new Money(1000000, "INR")
        );

        System.out.println("\n----- LEDGER ENTRY -----");
        entry.display();
    }
}