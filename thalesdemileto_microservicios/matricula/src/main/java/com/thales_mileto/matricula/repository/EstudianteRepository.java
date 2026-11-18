package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, String> {

    Optional<Estudiante> findByDni(String dni);

    boolean existsByDni(String dni);

    Optional<Estudiante> findByCodigoEstudiante(String codigoEstudiante);

    boolean existsByCodigoEstudiante(String codigoEstudiante);

    List<Estudiante> findByEstado(String estado);

    List<Estudiante> findByApellidoPaternoContainingIgnoreCaseOrApellidoMaternoContainingIgnoreCaseOrNombresContainingIgnoreCase(
            String apellidoPaterno,
            String apellidoMaterno,
            String nombres
    );

    /**
     * Busca por nombre completo con LIKE (parcial, sin distinguir mayúsculas ni tildes), tanto en el
     * orden "nombres apellidoPaterno apellidoMaterno" como "apellidoPaterno apellidoMaterno nombres".
     * El parámetro debe llegar ya en minúsculas y sin tildes (ver EstudianteService#buscarPorNombre).
     * Usa TRANSLATE (nativo de PostgreSQL) para no depender de la extensión unaccent.
     */
    @Query(value = """
            SELECT * FROM estudiantes e
            WHERE TRANSLATE(LOWER(CONCAT_WS(' ', e.nombres, e.apellido_paterno, e.apellido_materno)),
                            'áéíóúàèìòùäëïöüâêîôûñ', 'aeiouaeiouaeiouaeiouun')
                    LIKE CONCAT('%', :nombre, '%')
               OR TRANSLATE(LOWER(CONCAT_WS(' ', e.apellido_paterno, e.apellido_materno, e.nombres)),
                            'áéíóúàèìòùäëïöüâêîôûñ', 'aeiouaeiouaeiouaeiouun')
                    LIKE CONCAT('%', :nombre, '%')
            ORDER BY e.apellido_paterno, e.apellido_materno, e.nombres
            """, nativeQuery = true)
    List<Estudiante> buscarPorNombre(@Param("nombre") String nombre);
}