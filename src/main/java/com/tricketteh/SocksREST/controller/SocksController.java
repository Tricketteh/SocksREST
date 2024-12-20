package com.tricketteh.SocksREST.controller;

import com.tricketteh.SocksREST.dto.SocksDTO;
import com.tricketteh.SocksREST.entity.Sock;
import com.tricketteh.SocksREST.service.SockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/socks")
@Tag(name = "Socks", description = "Controller provides interactions with socks in store e.g. income, outcome, update")
public class SocksController {

    private final SockService sockService;

    @Operation(
            summary = "Socks income",
            description = "Allows to register socks income"
    )
    @PostMapping("income")
    public ResponseEntity<Sock> socksIncome(@RequestBody @Valid SocksDTO socksDTO, UriComponentsBuilder uriBuilder) {
        Sock sock = sockService.socksIncome(socksDTO);
        return ResponseEntity
                .created(uriBuilder
                        .path("{sockId}")
                        .build(Map.of("sockId", sock.getId())))
                .body(sock);

    }

    @Operation(
            summary = "Socks outcome",
            description = "Allows to register socks outcome"
    )
    @PostMapping("outcome")
    public ResponseEntity<Sock> socksOutcome(SocksDTO socksDTO) {
        sockService.socksOutcome(socksDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get all socks",
            description = "Allows to get all socks filtered or sorted"
    )
    @GetMapping()
    public ResponseEntity<List<Sock>> getSocks(@RequestParam(required = false) String color,
                                               @RequestParam(required = false) String operation,
                                               @RequestParam(required = false) Double cottonPart,
                                               @RequestParam(required = false) Double cottonPartMin,
                                               @RequestParam(required = false) Double cottonPartMax,
                                               @RequestParam(required = false) String sortBy) {
        List<Sock> result = sockService.getFilteredAndSortedSocks(
                color, operation, cottonPart, cottonPartMin, cottonPartMax, sortBy
        );
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Update sock",
            description = "Allows to update socks values"
    )
    @PutMapping("{id}")
    public ResponseEntity<Sock> updateSocks(@PathVariable Long id, @RequestBody @Valid SocksDTO socksDTO) {
        this.sockService.update(id, socksDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Upload socks batch from CSV file",
            description = "Allows to upload socks from CSV file"
    )
    @PostMapping("/batch")
    public ResponseEntity<String> uploadSockBatch(@RequestParam("file") MultipartFile file) {
        try {
            sockService.loadCsvFile(file);
            return ResponseEntity.ok("File successfully loaded.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error happened in file loading: " + ex.getMessage());
        }
    }
}
