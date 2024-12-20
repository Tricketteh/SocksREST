package com.tricketteh.SocksREST.service;

import com.opencsv.CSVReader;
import com.tricketteh.SocksREST.dto.SocksDTO;
import com.tricketteh.SocksREST.entity.Sock;
import com.tricketteh.SocksREST.repository.SockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultSockService implements SockService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSockService.class);
    private final SockRepository sockRepository;

    @Override
    @Transactional
    public Sock socksIncome(SocksDTO socksDTO) {
        logger.info("Processing socks income: {}", socksDTO);
        Sock sock = sockRepository.save(Sock.builder()
                .id(null)
                .color(socksDTO.getColor())
                .cottonPart(socksDTO.getCottonPart())
                .quantity(socksDTO.getQuantity())
                .build());
        logger.info("Socks income processed successfully: {}", sock);
        return sock;
    }

    @Override
    @Transactional
    public void socksOutcome(SocksDTO socksDTO) {
        logger.info("Processing socks outcome: {}", socksDTO);
        try {
            Optional<Long> sockId = sockRepository.findIdByFields(socksDTO.getColor(), socksDTO.getCottonPart(), socksDTO.getQuantity());
            Sock sock = sockId.map(id -> sockRepository.findById(id).orElseThrow(NoSuchElementException::new))
                    .orElseThrow(NoSuchElementException::new);

            if (sock.getQuantity() > socksDTO.getQuantity()) {
                sock.setQuantity(sock.getQuantity() - socksDTO.getQuantity());
                logger.info("Socks outcome processed successfully. Remaining quantity: {}", sock.getQuantity());
            } else {
                throw new IllegalArgumentException("Socks outcome quantity exceeds the available stock");
            }
        } catch (Exception ex) {
            logger.error("Error processing socks outcome", ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void update(Long id, SocksDTO socksDTO) {
        logger.info("Updating sock with ID: {}", id);
        sockRepository.findById(id)
                .ifPresentOrElse(sock -> {
                    sock.setColor(socksDTO.getColor());
                    sock.setQuantity(socksDTO.getQuantity());
                    logger.info("Sock updated successfully: {}", sock);
                }, () -> {
                    logger.error("Sock with ID {} not found for update", id);
                    throw new NoSuchElementException();
                });
    }

    @Override
    public int getSocksCount(String color, String operation, Double cottonPart) {
        logger.info("Getting socks count with filters - Color: {}, Operation: {}, CottonPart: {}", color, operation, cottonPart);
        List<Sock> socks = sockRepository.findAll();
        int count = socks.stream()
                .filter(sock -> color == null || sock.getColor().equalsIgnoreCase(color))
                .filter(sock -> {
                    if (operation == null || cottonPart == null) return true;
                    return switch (operation) {
                        case "moreThan" -> sock.getCottonPart() > cottonPart;
                        case "lessThan" -> sock.getCottonPart() < cottonPart;
                        case "equal" -> sock.getCottonPart().equals(cottonPart);
                        default -> throw new IllegalArgumentException("Incorrect operation: " + operation);
                    };
                })
                .mapToInt(Sock::getQuantity)
                .sum();
        logger.info("Total socks count: {}", count);
        return count;
    }

    @Override
    public List<Sock> getFilteredAndSortedSocks(String color, String operation, Double cottonPart, Double cottonPartMin, Double cottonPartMax, String sortBy) {
        logger.info("Getting filtered and sorted socks - Filters: Color={}, Operation={}, CottonPart={}, CottonPartMin={}, CottonPartMax={}, SortBy={}",
                color, operation, cottonPart, cottonPartMin, cottonPartMax, sortBy);

        if (sortBy == null || (!sortBy.equalsIgnoreCase("color") && !sortBy.equalsIgnoreCase("cottonPart"))) {
            sortBy = "color";
        }

        List<Sock> result = sockRepository.findFilteredAndSorted(color, operation, cottonPart, cottonPartMin, cottonPartMax, sortBy);
        logger.info("Filtered and sorted socks retrieved: {}", result);
        return result;
    }

    @Override
    public void loadCsvFile(MultipartFile file) {
        logger.info("Processing CSV file upload");
        if (file.isEmpty()) {
            logger.error("Uploaded file is empty");
            throw new IllegalArgumentException("File is empty. Please load a correct file");
        }

        List<String> errors = new ArrayList<>();
        List<Sock> socks = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVReader csvReader = new CSVReader(reader);
            String[] line;
            int lineNumber = 0;

            while ((line = csvReader.readNext()) != null) {
                lineNumber++;

                if (lineNumber == 1 && isHeaderRow(line)) {
                    logger.info("Skipping header row in CSV file");
                    continue;
                }

                try {
                    String color = validateColor(line[0], lineNumber);
                    double cottonPart = validateCottonPart(line[1], lineNumber);
                    int quantity = validateQuantity(line[2], lineNumber);

                    socks.add(new Sock(null, color, cottonPart, quantity));
                } catch (IllegalArgumentException ex) {
                    logger.warn("Error processing line {}: {}", lineNumber, ex.getMessage());
                    errors.add(ex.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                logger.error("Errors occurred while reading CSV file:\n{}", String.join("\n", errors));
                throw new IllegalArgumentException("Errors in file's reading:\n" + String.join("\n", errors));
            }

            sockRepository.saveAll(socks);
            logger.info("CSV file processed successfully. Saved socks: {}", socks);
        } catch (Exception ex) {
            logger.error("Error processing CSV file", ex);
            throw new RuntimeException("Unexpected error.", ex);
        }
    }

    private boolean isHeaderRow(String[] line) {
        return line[0].equalsIgnoreCase("color") &&
                line[1].equalsIgnoreCase("cottonPart") &&
                line[2].equalsIgnoreCase("quantity");
    }

    private String validateColor(String color, int lineNumber) {
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("String " + lineNumber + ": Color cannot be empty.");
        }
        return color.trim();
    }

    private double validateCottonPart(String cottonPartStr, int lineNumber) {
        try {
            double cottonPart = Double.parseDouble(cottonPartStr.trim());
            if (cottonPart < 0 || cottonPart > 100) {
                throw new IllegalArgumentException("String " + lineNumber + ": Cotton part must be between 0 and 100.");
            }
            return cottonPart;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("String " + lineNumber + ": Incorrect cotton part.");
        }
    }

    private int validateQuantity(String quantityStr, int lineNumber) {
        try {
            int quantity = Integer.parseInt(quantityStr.trim());
            if (quantity <= 0) {
                throw new IllegalArgumentException("String " + lineNumber + ": quantity must be greater than 0.");
            }
            return quantity;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("String " + lineNumber + ": incorrect quantity.");
        }
    }

}

