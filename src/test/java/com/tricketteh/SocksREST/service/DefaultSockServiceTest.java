package com.tricketteh.SocksREST.service;

import com.tricketteh.SocksREST.dto.SocksDTO;
import com.tricketteh.SocksREST.entity.Sock;
import com.tricketteh.SocksREST.repository.SockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@SpringBootTest
public class DefaultSockServiceTest {

    @Mock
    private SockRepository sockRepository;

    @InjectMocks
    private DefaultSockService sockService;

    private SocksDTO socksDTO;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        socksDTO = new SocksDTO("red", 50.0, 100);
    }

    @Test
    public void testSocksIncome() {
        Sock sock = new Sock(null, socksDTO.getColor(), socksDTO.getCottonPart(), socksDTO.getQuantity());
        when(sockRepository.save(any(Sock.class))).thenReturn(sock);

        Sock result = sockService.socksIncome(socksDTO);

        assertNotNull(result);
        assertEquals(socksDTO.getColor(), result.getColor());
        assertEquals(socksDTO.getCottonPart(), result.getCottonPart());
        assertEquals(socksDTO.getQuantity(), result.getQuantity());
        verify(sockRepository, times(1)).save(any(Sock.class));
    }

    @Test
    public void testSocksOutcome_SufficientQuantity() {
        Sock existingSock = new Sock(1L, "red", 50.0, 200);
        when(sockRepository.findIdByFields(anyString(), anyDouble(), anyInt())).thenReturn(Optional.of(1L));
        when(sockRepository.findById(anyLong())).thenReturn(Optional.of(existingSock));

        sockService.socksOutcome(socksDTO);

        assertEquals(100, existingSock.getQuantity());
        verify(sockRepository, times(1)).save(existingSock);
    }

    @Test
    public void testSocksOutcome_InsufficientQuantity() {
        Sock existingSock = new Sock(1L, "red", 50.0, 50);
        when(sockRepository.findIdByFields(anyString(), anyDouble(), anyInt())).thenReturn(Optional.of(1L));
        when(sockRepository.findById(anyLong())).thenReturn(Optional.of(existingSock));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sockService.socksOutcome(socksDTO);
        });
        assertEquals("Socks outcome quantity exceeds the available stock", exception.getMessage());
    }

    @Test
    public void testUpdateSock_Success() {
        Sock existingSock = new Sock(1L, "red", 50.0, 100);
        when(sockRepository.findById(anyLong())).thenReturn(Optional.of(existingSock));

        sockService.update(1L, socksDTO);

        assertEquals(socksDTO.getColor(), existingSock.getColor());
        assertEquals(socksDTO.getCottonPart(), existingSock.getCottonPart());
        assertEquals(socksDTO.getQuantity(), existingSock.getQuantity());
        verify(sockRepository, times(1)).save(existingSock);
    }

    @Test
    public void testUpdateSock_NotFound() {
        when(sockRepository.findById(anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            sockService.update(1L, socksDTO);
        });
        assertEquals("Sock with ID 1 not found for update", exception.getMessage());
    }

    @Test
    public void testLoadCsvFile_Success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        sockService.loadCsvFile(file);

        verify(sockRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testLoadCsvFile_EmptyFile() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sockService.loadCsvFile(file);
        });
        assertEquals("File is empty. Please load a correct file", exception.getMessage());
    }

    @Test
    public void testGetSocksCount() {
        Sock sock1 = new Sock(1L, "red", 50.0, 100);
        Sock sock2 = new Sock(2L, "blue", 60.0, 150);
        when(sockRepository.findAll()).thenReturn(List.of(sock1, sock2));

        int count = sockService.getSocksCount("red", "moreThan", 40.0);

        assertEquals(100, count);
    }
}

