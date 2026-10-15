package ro.tuc.ds2020.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.tuc.ds2020.Ds2020TestConfig;
import ro.tuc.ds2020.dtos.PersonDetailsDTO;
import ro.tuc.ds2020.services.PersonService;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PersonControllerUnitTest extends Ds2020TestConfig {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService service;

    @Test
    public void insertPersonTest() throws Exception {
        PersonDetailsDTO personDTO = new PersonDetailsDTO("John", "Somewhere Else street", 22);

        mockMvc.perform(post("/person")
                .content(jsonMapper.writeValueAsString(personDTO))
                .contentType("application/json"))
                .andExpect(status().isCreated());
    }

    @Test
    public void insertPersonTestFailsDueToAge() throws Exception {
        PersonDetailsDTO personDTO = new PersonDetailsDTO("John", "Somewhere Else street", 17);

        mockMvc.perform(post("/person")
                .content(jsonMapper.writeValueAsString(personDTO))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void insertPersonTestFailsDueToNull() throws Exception {
        PersonDetailsDTO personDTO = new PersonDetailsDTO("John", null, 17);

        mockMvc.perform(post("/person")
                .content(jsonMapper.writeValueAsString(personDTO))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }
}
