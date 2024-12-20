package com.tricketteh.SocksREST.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocksDTO {

    @NotBlank(message = "Color cannot be empty")
    @Schema(description = "Socks color", example = "Red")
    private String color;

    @Min(value = 0, message = "Cotton part cannot be under 0%")
    @Max(value = 100, message = "Cotton part cannot be more than 100%")
    @NotNull(message = "Cotton part is mandatory")
    @Schema(description = "Socks cotton part", example = "55%")
    private Double cottonPart;

    @Min(value = 0, message = "Socks quantity cannot be under 0")
    @NotNull(message = "Socks quantity is mandatory")
    @Schema(description = "Socks quantity", example = "55")
    private Integer quantity;
}
