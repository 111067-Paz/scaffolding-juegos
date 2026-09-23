package ar.edu.utn.frc.tup.p4.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * THE single error contract of the whole API.
 * Every exception handler returns this type — never Map<String, Object>.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorApi {

    private String timestamp;

    private Integer status;

    private String error;

    private String message;
}
