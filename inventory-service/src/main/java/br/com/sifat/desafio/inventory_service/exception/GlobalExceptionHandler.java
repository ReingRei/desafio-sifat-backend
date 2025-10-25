package br.com.sifat.desafio.inventory_service.exception;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * @ExceptionHandler(EntityNotFoundException.class)
     *                                                  Captura exceções do tipo
     *                                                  EntityNotFoundException e
     *                                                  retorna um
     *                                                  ResponseEntity com status
     *                                                  404 Not Found e uma mensagem
     *                                                  personalizada.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Não Encontrado");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * @ExceptionHandler(MethodArgumentNotValidException.class)
     *                                                          Captura os erros
     *                                                          do @Valid
     *                                                          (ex: @NotBlank)
     *                                                          e retornar um 400
     *                                                          Bad Request com os
     *                                                          detalhes dos campos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erro de Validação");
        body.put("messages", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * @ExceptionHandler(DataIntegrityViolationException.class)
     *                                                          Este método vai
     *                                                          "escutar" por erros
     *                                                          de integridade do
     *                                                          banco,
     *                                                          como a violação de
     *                                                          uma constraint
     *                                                          'UNIQUE'.
     *                                                          * Ele vai
     *                                                          transformar o erro
     *                                                          500 em um 400 Bad
     *                                                          Request.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erro de Integridade de Dados");
        body.put("messages", ex.getMessage().contains("UK_..."));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * @ExceptionHandler(PropertyReferenceException.class)
     *                                                     Este método vai "escutar"
     *                                                     por erros ao tentar usar
     *                                                     uma propriedade
     *                                                     (campo) que não existe
     *                                                     para ordenação ou filtro.
     *
     *                                                     * Ele vai transformar o
     *                                                     erro 500 em um 400 Bad
     *                                                     Request.
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyReference(PropertyReferenceException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Parâmetro de Requisição Inválido");

        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * @ExceptionHandler(IllegalArgumentException.class)
     *                                                   Captura erros de argumentos
     *                                                   inválidos ou violações de
     *                                                   regras de negócio
     *                                                   que não se encaixam em
     *                                                   outras categorias (ex:
     *                                                   tentativa de deixar estoque
     *                                                   negativo).
     *
     *                                                   * Transforma o erro em um
     *                                                   400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Requisição Inválida");
        body.put("message", ex.getMessage()); // Usa a mensagem da exceção (ex: "Estoque não pode ser negativo.")

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}