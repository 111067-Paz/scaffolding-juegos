package ar.edu.utn.frc.tup.p4.dtos;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CellDTO {

    private Integer position;

    private CellType type;
}
