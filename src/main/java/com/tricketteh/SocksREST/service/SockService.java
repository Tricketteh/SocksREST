package com.tricketteh.SocksREST.service;

import com.tricketteh.SocksREST.dto.SocksDTO;
import com.tricketteh.SocksREST.entity.Sock;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SockService {

    Sock socksIncome(SocksDTO socksDTO);

    void socksOutcome(SocksDTO socksDTO);

    void update(Long id, SocksDTO socksDTO);

    int getSocksCount(String color, String operation, Double cottonPart);

    List<Sock> getFilteredAndSortedSocks(
            String color,
            String operation,
            Double cottonPart,
            Double cottonPartMin,
            Double cottonPartMax,
            String sortBy
    );

    void loadCsvFile(MultipartFile file) throws Exception;
}
