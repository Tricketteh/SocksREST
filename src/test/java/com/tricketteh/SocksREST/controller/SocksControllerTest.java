package com.tricketteh.SocksREST.controller;

import com.tricketteh.SocksREST.dto.SocksDTO;
import com.tricketteh.SocksREST.entity.Sock;
import com.tricketteh.SocksREST.service.SockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SocksControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SockService sockService;

    @InjectMocks
    private SocksController socksController;

    private SocksDTO socksDTO;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(socksController).build();
        socksDTO = new SocksDTO("red", 50.0, 100);
    }

    @Test
    public void testSocksIncome() throws Exception {
        Sock sock = new Sock(null, "red", 50.0, 100);
        when(sockService.socksIncome(any(SocksDTO.class))).thenReturn(sock);

        mockMvc.perform(post("/api/socks/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"red\", \"cottonPart\":50.0, \"quantity\":100}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/socks/{sockId}"))
                .andExpect(jsonPath("$.color").value("red"))
                .andExpect(jsonPath("$.cottonPart").value(50.0))
                .andExpect(jsonPath("$.quantity").value(100));

        verify(sockService, times(1)).socksIncome(any(SocksDTO.class));
    }

    @Test
    public void testSocksOutcome() throws Exception {
        mockMvc.perform(post("/api/socks/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"red\", \"cottonPart\":50.0, \"quantity\":50}"))
                .andExpect(status().isNoContent());

        verify(sockService, times(1)).socksOutcome(any(SocksDTO.class));
    }

    @Test
    public void testGetSocks() throws Exception {
        Sock sock1 = new Sock(1L, "red", 50.0, 100);
        Sock sock2 = new Sock(2L, "blue", 60.0, 150);
        when(sockService.getFilteredAndSortedSocks(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(List.of(sock1, sock2));

        mockMvc.perform(get("/api/socks")
                        .param("color", "red")
                        .param("operation", "moreThan")
                        .param("cottonPart", "40.0")
                        .param("cottonPartMin", "30.0")
                        .param("cottonPartMax", "70.0")
                        .param("sortBy", "color"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].color").value("red"))
                .andExpect(jsonPath("$[1].color").value("blue"));

        verify(sockService, times(1)).getFilteredAndSortedSocks(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyString());
    }

    @Test
    public void testUpdateSocks() throws Exception {
        mockMvc.perform(put("/api/socks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"red\", \"cottonPart\":50.0, \"quantity\":200}"))
                .andExpect(status().isNoContent());

        verify(sockService, times(1)).update(eq(1L), any(SocksDTO.class));
    }

    @Test
    public void testUploadSockBatch_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "socks.csv", "text/csv", "color,cottonPart,quantity\nred,50,100".getBytes());
        doNothing().when(sockService).loadCsvFile(file);

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("File successfully loaded."));

        verify(sockService, times(1)).loadCsvFile(file);
    }

    @Test
    public void testUploadSockBatch_Error() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "socks.csv", "text/csv", "color,cottonPart,quantity\nred,50,100".getBytes());
        doThrow(new IllegalArgumentException("Error in file processing")).when(sockService).loadCsvFile(file);

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Error in file processing"));

        verify(sockService, times(1)).loadCsvFile(file);
    }
}
