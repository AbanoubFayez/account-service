package quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import quarkus.models.Account;
import quarkus.models.AccountStatus;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountResourceTest {

    @Test
    @Order(1)
    void testRetrieveAllAccounts() {
        Response response = given()
                .when().get("/accounts")
                .then()
                .statusCode(200)
                .body(
                        containsString("George Baird"),
                        containsString("Mary Taylor"),
                        containsString("Diana Rigg")
                )
                .extract()
                .response();

        List<Object> accounts = response.jsonPath().getList("$");
        assertThat(accounts, not(empty()));
        assertThat(accounts, hasSize(3));
    }

    @Test
    @Order(2)
    void testRetrieveAccountById() {
        Account account = given()
                .when().get("/accounts/{accountNumber}", 123456789)
                .then()
                .statusCode(200)
                .extract()
                .as(Account.class);

        assertThat(account.getAccountNumber(), is(123456789L));
        assertThat(account.getAccountStatus(), is(AccountStatus.OPEN));
        assertThat(account.getCustomerName(), is("George Baird"));
        assertThat(account.getBalance(), is(BigDecimal.valueOf(354.23)));
    }


    @Test
    @Order(3)
    void testCreateAccount() {
        Account newAccount = new Account(1236789L, 9876321L, "George Baird",
                new BigDecimal("354.23"));

        Account returnedAccount = given()
                .contentType(ContentType.JSON)
                .body(newAccount)
                .when().post("/accounts")
                .then()
                .statusCode(201)
                .extract()
                .as(Account.class);

        assertThat(returnedAccount, notNullValue());
        assertThat(returnedAccount, is(newAccount));

        Response response = given()
                .when().get("/accounts")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Account> result = response.jsonPath().getList("$");

        assertThat(result, not(empty()));
        assertThat(result.size(), is(4));
    }

}