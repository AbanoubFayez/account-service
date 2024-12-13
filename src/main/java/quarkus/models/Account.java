package quarkus.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@NamedQuery(name = "Accounts.findAll",
        query = "SELECT a FROM Account a ORDER BY a.accountNumber")
@NamedQuery(name = "Accounts.findByAccountNumber",
        query = "SELECT a FROM Account a WHERE a.accountNumber=:accountNumber ORDER BY a.accountNumber")
public class Account {
    @Id
    @SequenceGenerator(name = "accountSequence", sequenceName = "accounts_id_seq", allocationSize = 1, initialValue = 10)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountSequence")
    private Long id;
    private Long accountNumber;
    private Long customerNumber;
    private String customerName;
    private BigDecimal balance;
    private AccountStatus accountStatus = AccountStatus.OPEN;

    public Account(Long accountNumber, Long customerNumber, String customerName, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.customerNumber = customerNumber;
        this.customerName = customerName;
        this.balance = balance;
    }

    public void markOverdrawn() {
        accountStatus = AccountStatus.OVERDRAWN;
    }

    public void removeOverdrawnStatus() {
        accountStatus = AccountStatus.OPEN;
    }

    public void close() {
        accountStatus = AccountStatus.CLOSED;
        balance = BigDecimal.ZERO;
    }

    public void withdrawFunds(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void addFunds(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
