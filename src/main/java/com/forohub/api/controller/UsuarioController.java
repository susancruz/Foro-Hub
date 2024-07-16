package com.je.forohub.api.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.je.forohub.api.domain.usuarios.DatosActualizarUsuario;
import com.je.forohub.api.domain.usuarios.DatosListadoUsuario;
import com.je.forohub.api.domain.usuarios.DatosUsuario;
import com.je.forohub.api.domain.usuarios.Usuario;
import com.je.forohub.api.domain.usuarios.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping
	@Transactional
	@Operation(summary = "Registra usuario")
	public ResponseEntity<DatosUsuario> registrarUsuario(@RequestBody @Valid DatosUsuario datos,
			UriComponentsBuilder uriComponentsBuilder) {

		
		String claveEncriptada = passwordEncoder.encode(datos.clave());

		Usuario usuario = new Usuario(null, datos.usuario(), claveEncriptada);

		usuario = usuarioRepository.save(usuario);

		DatosUsuario res = new DatosUsuario(usuario.getId(), usuario.getUsuario(), usuario.getClave());

		URI url = uriComponentsBuilder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();

		return ResponseEntity.created(url).body(res);
	}

	@GetMapping
	@Operation(summary = "Lista de usuarios")
	public ResponseEntity<Page<DatosListadoUsuario>> listadoMedicos(@PageableDefault(size = 2) Pageable paginacion) {
//      return medicoRepository.findAll(paginacion).map(DatosListadoMedico::new);
		return ResponseEntity.ok(usuarioRepository.findAll(paginacion).map(DatosListadoUsuario::new));
	}
	
	@PutMapping
	@Transactional
	@Operation(summary = "Actualizar usuario")
	public ResponseEntity actualizarUsuario(@RequestBody @Valid DatosActualizarUsuario datos) {
		if(datos.id() == null || !usuarioRepository.existsById(datos.id())) {
			throw new ValidationException("Sin resultados");
		}
		
		var usuario = usuarioRepository.getReferenceById(datos.id());
		
		String claveEncriptada =  datos.clave() != null ? passwordEncoder.encode(datos.clave()) : usuario.getClave();
		
		usuario.setUsuario(datos.usuario());
		
		usuario.setClave(claveEncriptada);
		
		DatosUsuario res = new DatosUsuario(usuario.getId(), usuario.getUsuario(), usuario.getClave());
		return ResponseEntity.ok(res);
	}
}
