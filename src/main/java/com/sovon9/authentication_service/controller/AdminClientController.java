package com.sovon9.authentication_service.controller;

import com.sovon9.authentication_service.dto.ClientDto;
import com.sovon9.authentication_service.service.ClientDtoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin/clients")
public class AdminClientController {

    private final ClientDtoService clientDtoService;

    public AdminClientController(ClientDtoService clientDtoService) {
        this.clientDtoService = clientDtoService;
    }

    // ── GET all clients ───────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        return ResponseEntity.ok(clientDtoService.getAllClients());
    }

    // ── GET single client ─────────────────────────────────────────────────────
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDto> getClient(@PathVariable String clientId) {
        return ResponseEntity.ok(clientDtoService.getClientData(clientId));
    }

    // ── POST — create new client ──────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto clientDto) {
        ClientDto saved = clientDtoService.saveClientData(clientDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── PUT — update existing client ──────────────────────────────────────────
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientDto> updateClient(@RequestBody ClientDto clientDto,
                                                  @PathVariable String clientId) {
        ClientDto updated = clientDtoService.updateClientData(clientDto, clientId);
        return ResponseEntity.ok(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable String clientId) {
        clientDtoService.deleteClientData(clientId);
        return ResponseEntity.noContent().build();    // 204 No Content — standard for DELETE
    }
}
