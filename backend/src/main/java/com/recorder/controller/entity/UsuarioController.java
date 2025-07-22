package com.recorder.controller.entity;

import com.recorder.controller.entity.enuns.Roles;
import com.recorder.repository.UsuarioRepository;
import com.recorder.dto.LoginDTO;
import com.recorder.dto.UsuarioDTO;
import com.recorder.dto.UsuarioResponse;
import com.recorder.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {
	private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
	@Autowired
	private PasswordEncoder passwordEncoder;

	private final UsuarioService usuarioService;
	private final UsuarioRepository usuarioRepository; // Adicionado

	public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) { // Modificado
		this.usuarioService = usuarioService;
		this.usuarioRepository = usuarioRepository;
	}

	@PostMapping("/registrar")
	public ResponseEntity<?> registrar(@Valid @RequestBody UsuarioDTO usuarioDTO, BindingResult result) {
		try {
			if (result.hasErrors()) {
				return ResponseEntity.badRequest().body(
						result.getFieldErrors().stream()
								.map(error -> error.getDefaultMessage())
								.collect(Collectors.toList()));
			}

			// Usar o service em vez de lógica duplicada
			Usuario usuarioSalvo = usuarioService.registrar(usuarioDTO);

			return ResponseEntity.ok(new UsuarioResponse(
					usuarioSalvo.getIdUsuario(),
					usuarioSalvo.getNome(),
					usuarioSalvo.getEmail(),
					usuarioSalvo.getTelefone(),
					usuarioSalvo.getCpf(),
					usuarioSalvo.getRoles().stream()
							.map(Roles::name)
							.collect(Collectors.toList())));

		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			logger.error("Erro no registro", e);
			return ResponseEntity.internalServerError().body("Erro interno no servidor");
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
		try {
			Usuario usuario = usuarioService.autenticar(loginDTO.getEmail(), loginDTO.getSenha());
			return ResponseEntity.ok(usuario);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(401).body("Credenciais inválidas");
		}
	}

	// ======== CRUD AQUI ========

	@GetMapping
	public ResponseEntity<List<Usuario>> listarTodos() {
		return ResponseEntity.ok(usuarioService.listarTodos());
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
		try {
			Usuario usuario = usuarioService.buscarPorId(id);
			return ResponseEntity.ok(usuario);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
		try {
			Usuario atualizado = usuarioService.atualizar(id, usuarioDTO);
			return ResponseEntity.ok(atualizado);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletar(@PathVariable Long id) {
		try {
			usuarioService.deletar(id);
			return ResponseEntity.ok().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}
}
