import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.je.forohub.api.domain.foros.ForoRepository;
import com.je.forohub.api.domain.respuestas.DatosActualizarRespuesta;
import com.je.forohub.api.domain.respuestas.DatosCrearRespuesta;
import com.je.forohub.api.domain.respuestas.DatosRespuesta;
import com.je.forohub.api.domain.respuestas.Respuesta;
import com.je.forohub.api.domain.respuestas.RespuestaRepository;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;

@RestController
@RequestMapping("/respuestas")
public class RespuestasController {
	private final RespuestaRepository respuestaRepository;
	private final ForoRepository foroRepository;

	public RespuestasController(RespuestaRepository respuestaRepository, ForoRepository foroRepository) {
		this.respuestaRepository = respuestaRepository;
		this.foroRepository = foroRepository;
	}

	@GetMapping("/{id}")
	@Operation(summary = "Lista de respuestas")
	public ResponseEntity<DatosRespuesta> listarRespuestas(@PathVariable Long id) {
		if (id == null || !respuestaRepository.existsById(id)) {
			throw new ValidationException("Sin resultados");
		}
		Respuesta respuesta = respuestaRepository.getReferenceById(id);
		return ResponseEntity.ok(new DatosRespuesta(respuesta));
	}

	@PostMapping
	@Transactional
	@Operation(summary = "Crear respuesta")
	public ResponseEntity<DatosRespuesta> crearRespuesta(@RequestBody @Valid DatosCrearRespuesta datos,
			UriComponentsBuilder uriComponentsBuilder) {
		if(datos.idForo() == null || !foroRepository.existsById(datos.idForo())) {
			throw new ValidationException("Sin resultados");
		}
		
		var foro = foroRepository.getReferenceById(datos.idForo());
		
		if(!foro.getEstado()) {
			throw new ValidationException("El foro ha sido Eliminado");
		}
		
		var respuesta = new Respuesta();
		respuesta.setContenido(datos.contenido());
		respuesta.setForo(foro);
		
		respuesta = respuestaRepository.save(respuesta);
		
		DatosRespuesta res = new DatosRespuesta(respuesta);
		URI url = uriComponentsBuilder.path("/respuestas/{id}").buildAndExpand(respuesta.getId()).toUri();
		
		return ResponseEntity.created(url).body(res);
	}
	
	@PutMapping
    @Transactional
    @Operation(summary = "Actualiza respuesta existente")
	public ResponseEntity actualizarRespuesta(@RequestBody @Valid DatosActualizarRespuesta datos,
			UriComponentsBuilder uriComponentsBuilder) {
		if(datos.id() == null || !respuestaRepository.existsById(datos.id())) {
			throw new ValidationException("Sin resultados");
		}
		
		var respuesta = respuestaRepository.getReferenceById(datos.id());
		
		respuesta.setContenido(datos.contenido());
				
		return ResponseEntity.ok(new DatosRespuesta(respuesta));
	}

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Eliminar respuesta")
    public ResponseEntity<Void> eliminarRespuesta(@PathVariable Long id) {
        Optional<Respuesta> optionalRespuesta = respuestaRepository.findById(id);
        if (optionalRespuesta.isEmpty()) {
            throw new EntityNotFoundException("sin resultado para el ID: " + id);
        }

        respuestaRepository.delete(optionalRespuesta.get());

        return ResponseEntity.noContent().build();
    }
}
