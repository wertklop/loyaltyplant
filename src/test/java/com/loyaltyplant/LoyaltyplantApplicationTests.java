package com.loyaltyplant;

import com.loyaltyplant.config.LoyaltyplantApplicationConfiguration;
import com.loyaltyplant.entity.BankAccount;
import com.loyaltyplant.repositories.BankAccountRepository;
import com.loyaltyplant.utils.StringUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LoyaltyplantApplicationConfiguration.class)
public class LoyaltyplantApplicationTests {

    public static final String USER_1 = "Иванов Петр";
    public static final String USER_2 = "Петров Иван";
    public static final String BANK_ACCOUNT_NUMBER_1 = "43-DRV-99";
    public static final String BANK_ACCOUNT_NUMBER_2 = "1W-555-V7";

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));


    @Autowired
    private DataSource dataSource;

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private BankAccount account1;
    private BankAccount account2;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void mainPageTest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void bankAccountsCreateTest() throws Exception {
        //Для чистоты тестов
        JdbcTestUtils.deleteFromTableWhere(new JdbcTemplate(dataSource), "bankaccount", "username in(?, ?)", USER_1, USER_2);
        assertEquals(0, JdbcTestUtils.countRowsInTableWhere(new JdbcTemplate(dataSource),
                "bankAccount", "username in('" + USER_1 + "', '" + USER_2 + "')"));

        account1 = new BankAccount(USER_1, BANK_ACCOUNT_NUMBER_1);
        account2 = new BankAccount(USER_2, BANK_ACCOUNT_NUMBER_2);

        //Создание 1-го счета
        MvcResult result = mockMvc.perform(post("/bankAccount/save")
                .contentType(APPLICATION_JSON_UTF8)
                .content(StringUtils.toJSON(account1)))
                .andExpect(status().isOk())
                .andReturn();
        account1.setId(Long.valueOf(result.getResponse().getContentAsString()));
        assertEquals(1, JdbcTestUtils.countRowsInTableWhere(new JdbcTemplate(dataSource),
                "bankAccount", "userName = '" + USER_1 + "'"));

        //Создание 2-го счета
        result = mockMvc.perform(post("/bankAccount/save")
                .contentType(APPLICATION_JSON_UTF8)
                .content(StringUtils.toJSON(account2)))
                .andExpect(status().isOk())
                .andReturn();
        account2.setId(Long.valueOf(result.getResponse().getContentAsString()));
        assertEquals(1, JdbcTestUtils.countRowsInTableWhere(new JdbcTemplate(dataSource),
                "bankAccount", "userName = '" + USER_2 + "'"));
    }

    @Test
    public void bankAccountsPaymentTest() throws Exception {
        account1 = accountRepository.findByUserName(USER_1);
        assertNotNull(account1);

        account2 = accountRepository.findByUserName(USER_2);
        assertNotNull(account1);

        JdbcTestUtils.deleteFromTableWhere(new JdbcTemplate(dataSource),
                "payment", "toaccount in(?, ?)", account1.getId(), account2.getId());
        assertEquals(0, JdbcTestUtils.countRowsInTableWhere(new JdbcTemplate(dataSource),
                "payment", "toaccount in('" + account1.getId() + "', '" + account2.getId() + "')"));

        //Проверяем, что на счетах нет средств
        assertEquals(0, getBalanceWithTest(account1), 0);
        assertEquals(0, getBalanceWithTest(account2), 0);

        //Перечисляем на один из счетов средства
        mockMvc.perform(post("/payment/transfer")
                .param("to", account1.getId().toString())
                .param("amount", "5"))
                .andDo(print())
                .andExpect(status().isOk());
        assertEquals(5, getBalanceWithTest(account1), 0);

        //Снимаем с счета с ненулевым балансом средств больше, чем на балансе и убеждаемся,
        //что возвращается ошибка
        mockMvc.perform(post("/payment/transfer")
                .param("to", account1.getId().toString())
                .param("amount", "-10"))
                .andExpect(status().isBadRequest());
        assertEquals(5, getBalanceWithTest(account1), 0);

        //Снимаем с счета с нулевым балансом и убеждаемся,
        //что возвращается ошибка
        mockMvc.perform(post("/payment/transfer")
                .param("to", account2.getId().toString())
                .param("amount", "-10"))
                .andExpect(status().isBadRequest());
        assertEquals(0, getBalanceWithTest(account2), 0);


        //Перечисляем некоторую сумму с счета с ненулевым балансом на другой счет.
        //Сумма перечисляемых средств присутствует на балансе счета, с которого производится
        // перечисление.
        mockMvc.perform(post("/payment/transfer")
                .param("from", account1.getId().toString())
                .param("to", account2.getId().toString())
                .param("amount", "3"))
                .andExpect(status().isOk());
        assertEquals(2, getBalanceWithTest(account1), 0);
        assertEquals(3, getBalanceWithTest(account2), 0);

        //Перечисляем некоторую сумму с счета с ненулевым балансом на другой счет.
        //Сумма перечисляемых средств недостаточна на балансе счета, с которого производится
        // перечисление.
        mockMvc.perform(post("/payment/transfer")
                .param("from", account1.getId().toString())
                .param("to", account2.getId().toString())
                .param("amount", "10"))
                .andExpect(status().isBadRequest());
        assertEquals(2, getBalanceWithTest(account1), 0);
        assertEquals(3, getBalanceWithTest(account2), 0);

    }

    @Test
    public void bankAccountsRemoveTest() throws Exception {
        account1 = accountRepository.findByUserName(USER_1);
        mockMvc.perform(delete("/bankAccount/delete/{id}", account1.getId())).andExpect(status().isOk());
        assertNull(accountRepository.findByUserName(USER_1));

        account2 = accountRepository.findByUserName(USER_2);
        mockMvc.perform(delete("/bankAccount/delete/{id}", account2.getId())).andExpect(status().isOk());
        assertNull(accountRepository.findByUserName(USER_2));
    }

    private Double getBalanceWithTest(BankAccount account) throws Exception {
        MvcResult result = mockMvc.perform(get("/payment/balance/{id}", account.getId()))
                .andExpect(status().isOk())
                .andReturn();
        return Double.valueOf(result.getResponse().getContentAsString());
    }
}
