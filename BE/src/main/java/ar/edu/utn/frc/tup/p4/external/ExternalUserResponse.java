package ar.edu.utn.frc.tup.p4.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO owned by the external module — the external API never
 * dictates your domain model; a mapper converts this to your entities/DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalUserResponse {

    private Long id;

    private String name;

    private String email;
}
