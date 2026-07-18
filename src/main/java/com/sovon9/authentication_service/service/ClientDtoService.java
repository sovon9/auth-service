package com.sovon9.authentication_service.service;

import com.sovon9.authentication_service.dto.ClientDto;
import com.sovon9.authentication_service.mapper.ClientDtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientDtoService {

    private final RegisteredClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public ClientDtoService(RegisteredClientRepository clientRepository,
                            PasswordEncoder passwordEncoder,
                            JdbcTemplate jdbcTemplate) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── GET all ───────────────────────────────────────────────────────────────
    // RegisteredClientRepository interface has no findAll() — intentional by Spring.
    // Standard workaround: query client_id list via JdbcTemplate, then load each
    // through the repository so Spring handles deserialization correctly.

    public List<ClientDto> getAllClients() {
        List<String> clientIds = jdbcTemplate.queryForList(
                "SELECT client_id FROM oauth2_registered_client", String.class);

        return clientIds.stream()
                .map(clientRepository::findByClientId)
                .map(ClientDtoMapper::registeredClientToClientDto)
                .collect(Collectors.toList());
    }

    // ── GET by clientId ───────────────────────────────────────────────────────

    public ClientDto getClientData(String clientId) {
        RegisteredClient registeredClient = clientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found: " + clientId);
        }
        return ClientDtoMapper.registeredClientToClientDto(registeredClient);
    }

    // ── POST — create new client ──────────────────────────────────────────────

    public ClientDto saveClientData(ClientDto dto) {
        // Guard: don't create if clientId already exists
        if (clientRepository.findByClientId(dto.getClientId()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Client already exists: " + dto.getClientId());
        }

        // BCrypt-encode the secret before passing to mapper
        if (dto.getClientSecret() == null || dto.getClientSecret().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "clientSecret is required when creating a new client.");
        }
        dto.setClientSecret(passwordEncoder.encode(dto.getClientSecret()));

        RegisteredClient newClient = ClientDtoMapper.clientDtoToRegisteredClient(dto, null);
        clientRepository.save(newClient);

        // Return the saved state (mask the secret in response)
        return ClientDtoMapper.registeredClientToClientDto(newClient);
    }

    // ── PUT — update existing client ──────────────────────────────────────────

    public ClientDto updateClientData(ClientDto dto, String clientId) {
        RegisteredClient existing = clientRepository.findByClientId(clientId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found: " + clientId);
        }

        // If a new secret was provided, BCrypt-encode it; otherwise keep the existing hash
        if (dto.getClientSecret() != null && !dto.getClientSecret().isBlank()) {
            dto.setClientSecret(passwordEncoder.encode(dto.getClientSecret()));
        } else {
            // null/blank → keep existing BCrypt hash (mapper skips it when null)
            dto.setClientSecret(null);
        }

        RegisteredClient updated = ClientDtoMapper.clientDtoToRegisteredClient(dto, existing);
        clientRepository.save(updated);

        return ClientDtoMapper.registeredClientToClientDto(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    // RegisteredClientRepository has no delete method — intentional by Spring.
    // Direct SQL DELETE via JdbcTemplate is the standard approach.

    public void deleteClientData(String clientId) {
        // Guard: verify client exists before attempting delete
        if (clientRepository.findByClientId(clientId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found: " + clientId);
        }
        jdbcTemplate.update(
                "DELETE FROM oauth2_registered_client WHERE client_id = ?", clientId);
    }
}
