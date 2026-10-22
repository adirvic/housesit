package br.com.rodrigues.housesit;

import br.com.rodrigues.housesit.domain.model.ExpenseCategory;
import br.com.rodrigues.housesit.domain.model.SplitType;
import br.com.rodrigues.housesit.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("dev")
class HousesitApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testAuthAndHouseholdExpenseFlow() throws Exception {
        // 1. Dev login as Junior
        DevLoginRequest loginReq = new DevLoginRequest("junior@example.com", "Junior", "https://avatar.url");
        MvcResult authResult = mockMvc.perform(post("/api/auth/dev-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.name").value("Junior"))
                .andReturn();

        AuthResponse auth = objectMapper.readValue(authResult.getResponse().getContentAsString(), AuthResponse.class);
        String token = "Bearer " + auth.token();

        // 2. Fetch categories (public)
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 3. Fetch Dashboard summary
        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHouseholdSpending").isNumber());

        // 4. Create a new Expense
        ExpenseRequest expenseReq = new ExpenseRequest(
                "Weekend Weekend Coffee",
                new BigDecimal("18.50"),
                ExpenseCategory.DINING,
                LocalDate.now(),
                SplitType.EQUAL,
                null,
                null,
                "Latte for both"
        );

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Weekend Weekend Coffee"))
                .andExpect(jsonPath("$.payerShare").value(9.25))
                .andExpect(jsonPath("$.partnerShare").value(9.25));

        // 5. Query expenses list
        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
