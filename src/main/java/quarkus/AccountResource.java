package quarkus;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import quarkus.models.Account;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/accounts")
public class AccountResource {
    Set<Account> accounts = new HashSet<>();

    @Inject
    EntityManager entityManager;

    @PostConstruct
    public void setup() {
        accounts.add(new Account(123456789L, 987654321L, "George Baird",
                new BigDecimal("354.23")));
        accounts.add(new Account(121212121L, 888777666L, "Mary Taylor",
                new BigDecimal("560.03")));
        accounts.add(new Account(545454545L, 222444999L, "Diana Rigg",
                new BigDecimal("422.00")));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> allAccounts() {
        return entityManager
                .createNamedQuery("Accounts.findAll", Account.class)
                .getResultList();
    }

    @GET
    @Path("/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("accountNumber") Long accountNumber) {
        try {
            return
                    entityManager
                            .createNamedQuery("Accounts.findByAccountNumber", Account.class)
                            .setParameter("accountNumber", accountNumber)
                            .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("Account with id of " + accountNumber + " does not exist!");
        }
    }

    @PUT
    @Path("/{accountNumber}/markOverdrawn")
    public void markOverdrawn(@PathParam("accountNumber") Long accountNumber) {
        Account account = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Account with id of " + accountNumber + " does not exist!"));

        account.markOverdrawn();
    }

    @PUT
    @Path("/{accountNumber}/removeOverdrawn")
    public void removeOverdrawnStatus(@PathParam("accountNumber") Long accountNumber) {
        Account account = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Account with id of " + accountNumber + " does not exist!"));

        account.removeOverdrawnStatus();
    }

    @PUT
    @Path("/{accountNumber}/close")
    public void close(@PathParam("accountNumber") Long accountNumber) {
        Account account = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Account with id of " + accountNumber + " does not exist!"));

        account.close();
    }

    @POST
    @Path("/{accountNumber}/withdraw/{amount}")
    public Account withdrawl(@PathParam("accountNumber") Long accountNumber, @PathParam("amount") BigDecimal amount) {
        Account account = getAccount(accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new WebApplicationException("Insufficient balance!");
        }
        account.withdrawFunds(amount);
        return account;
    }

    @POST
    @Path("/{accountNumber}/deposit/{amount}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deposit(@PathParam("accountNumber") Long accountNumber, @PathParam("amount") BigDecimal amount) {
        Account account = accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Account with id of " + accountNumber + " does not exist!"));

        account.addFunds(amount);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response create(Account account) {
        entityManager.persist(account);
        return Response.status(201).entity(account).build();
    }
}
